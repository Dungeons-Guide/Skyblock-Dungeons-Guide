/*
 *     Dungeons Guide - The most intelligent Hypixel Skyblock Dungeons Mod
 *     Copyright (C) 2021  cyoung06
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Affero General Public License as published
 *     by the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Affero General Public License for more details.
 *
 *     You should have received a copy of the GNU Affero General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package kr.syeyoung.dungeonsguide.mod.dungeon.data.mechanics;

import com.google.common.collect.Sets;
import kr.syeyoung.dungeonsguide.mod.dungeon.DungeonActionContext;
import kr.syeyoung.dungeonsguide.mod.dungeon.actions.*;
import kr.syeyoung.dungeonsguide.mod.dungeon.actions.tree.ActionDAGBuilder;
import kr.syeyoung.dungeonsguide.mod.dungeon.data.OffsetPoint;
import kr.syeyoung.dungeonsguide.mod.dungeon.data.PrecalculatedMoveNearest;
import kr.syeyoung.dungeonsguide.mod.dungeon.data.mechanics.dunegonmechanic.DungeonMechanicData;
import kr.syeyoung.dungeonsguide.mod.dungeon.data.mechanics.dunegonmechanic.DungeonMechanicState;
import kr.syeyoung.dungeonsguide.mod.dungeon.data.mechanics.dunegonmechanic.ISecret;
import kr.syeyoung.dungeonsguide.mod.dungeon.data.mechanics.predicates.PredicateBat;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomfinder.DungeonRoom;
import kr.syeyoung.dungeonsguide.mod.features.FeatureRegistry;
import kr.syeyoung.dungeonsguide.mod.pathfinding.abilitysetting.AlgorithmSetting;
import kr.syeyoung.dungeonsguide.mod.utils.RenderUtils;
import kr.syeyoung.modapi.ModAPI;
import kr.syeyoung.modapi.data.Vector3D;
import kr.syeyoung.modapi.data.VectorI3D;
import kr.syeyoung.modapi.entity.EntityType;
import kr.syeyoung.modapi.entity.UEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

import java.awt.*;
import java.util.List;
import java.util.*;

@Data
public class DungeonSecretBatState implements DungeonMechanicState, ISecret {
    private final DungeonSecretBatData data;
    private final DungeonRoom room;

    public DungeonSecretBatState(DungeonSecretBatData data, DungeonRoom room) {
        this.data = data;
        this.room = room;
    }

    private boolean didKillBat;

    public SecretStatus getSecretStatus(DungeonRoom dungeonRoom) {
        VectorI3D bpos = data.secretPoint.getBlockPos(dungeonRoom);
        if (didKillBat) {
            return SecretStatus.FOUND;
        }
        Vector3D spawn = new Vector3D(bpos);
        for (Integer killed : DungeonActionContext.getKilleds()) {
            if (DungeonActionContext.getSpawnLocation().get(killed) == null) continue;
            if (DungeonActionContext.getSpawnLocation().get(killed).distanceSq(spawn) < 100) {
                didKillBat = true;
                return SecretStatus.FOUND;
            }
        }
        return SecretStatus.NOT_SURE;
    }

    @Override
    public boolean isFound(DungeonRoom dungeonRoom) {
        return getSecretStatus(dungeonRoom) == SecretStatus.FOUND;
    }

    private int nearbyTicks;
    @Override
    public void tick(DungeonRoom dungeonRoom) {
        if (didKillBat) return;

        VectorI3D bpos = data.secretPoint.getBlockPos(dungeonRoom);
        Vector3D pos = new Vector3D(bpos);
        for (Map.Entry<Integer, Vector3D> integerVec3Entry : DungeonActionContext.getSpawnLocation().entrySet()) {
            if (integerVec3Entry.getValue().distanceSq(pos) < 100) {
                UEntity e = dungeonRoom.getContext().getUworld().getEntityById(integerVec3Entry.getKey());
                if (e == null) continue;
                if (e.getEntityType() != EntityType.BAT) continue;
                if (e.isDead()) continue;
                return; // bat is not dead.
            }
        }

        // can't find the bat!!!
        if (bpos.distanceSq(ModAPI.getAPI().getPlayer().getPositionVector()) < 49) {
            nearbyTicks++;
        }
        if (nearbyTicks > 100) {
            didKillBat = true;
        }
    }

    @Override
    public void buildAction(String action, ActionDAGBuilder builder, AlgorithmSetting algorithmSetting) throws PathfindImpossibleException {
        if (action.equalsIgnoreCase("navigate")) {
            builder = builder
                    .requires(new ActionMoveNearestAir(getRepresentingPoint()), algorithmSetting);
            for (String str : data.preRequisite) {
                if (str.isEmpty()) continue;
                builder.optional(new ActionChangeState(str.split(":")[0], str.split(":")[1]), algorithmSetting);
            }
            return;
        }
        if (!"found".equalsIgnoreCase(action))
            throw new PathfindImpossibleException(action + " is not valid state for secret");
        if (action.equals("found") && getSecretStatus(room) == SecretStatus.FOUND) return;
        if (data.moveNearest != null) {
            ActionUtils.buildActionMoveAnd(builder, room, data.moveNearest, Collections.emptyList(), data.preRequisite, um -> um.requires(() -> {
                ActionKill actionKill = new ActionKill(data.secretPoint);
                actionKill.setRadius(10);
                actionKill.setPredicate(PredicateBat.INSTANCE);
                return actionKill;
            }), "MoveAndKill", algorithmSetting);
        } else {
            builder = builder.requires(new AtomicAction.Builder()
                    .requires(() -> {
                        ActionKill actionKill = new ActionKill(data.secretPoint);
                        actionKill.setRadius(10);
                        actionKill.setPredicate(PredicateBat.INSTANCE);
                        return actionKill;
                    })
                    .requires(new ActionMoveNearestAir(data.secretPoint))
                    .build("MoveAndKill"), algorithmSetting);

            for (String str : data.preRequisite) {
                if (str.isEmpty()) continue;
                builder.optional(new ActionChangeState(str.split(":")[0], str.split(":")[1]), algorithmSetting);
            }
        }
    }

    @Override
    public void highlight(Color color, String name, float partialTicks) {
        VectorI3D pos = getSecretPoint().getBlockPos(room);
        RenderUtils.highlightBlock(pos, color, partialTicks);
        RenderUtils.drawTextAtWorld(name, pos.getX() + 0.5f, pos.getY() + 0.375f, pos.getZ() + 0.5f, 0xFFFFFFFF, 0.03f, false, true, partialTicks);
        RenderUtils.drawTextAtWorld(getCurrentState(), pos.getX() + 0.5f, pos.getY() + 0f, pos.getZ() + 0.5f, 0xFFFFFFFF, 0.03f, false, true, partialTicks);


        if (data.moveNearest != null && FeatureRegistry.DEBUG_AIR.isEnabled())
            data.moveNearest.render(partialTicks, room);
    }

    public enum SecretType {
        BAT, CHEST, ITEM_DROP, ESSENCE
    }

    @AllArgsConstructor
    @Getter
    public enum SecretStatus {
        DEFINITELY_NOT("definitely_not"), NOT_SURE("not_sure"), CREATED("created"), FOUND("found"), ERROR("error");

        private final String stateName;
    }


    @Override
    public String getCurrentState() {
        return getSecretStatus(room).getStateName();
    }

    @Override
    public Set<String> getAvailableActions() {
        SecretStatus status = getSecretStatus(room);
        if (status == SecretStatus.FOUND) return Sets.newHashSet("navigate");
        else return Sets.newHashSet("found", "navigate");
    }

    @Override
    public Set<String> getTotalPossibleStates() {
        return Sets.newHashSet("found"/*, "definitely_not", "not_sure", "created", "error"*/);
    }

    @Override
    public OffsetPoint getRepresentingPoint() {
        return data.secretPoint;
    }

    @Override
    public OffsetPoint getSecretPoint() {
        return data.secretPoint;
    }
    @Override
    public List<String> getPreRequisite() {
        return data.preRequisite;
    }

    @Data
    public static class DungeonSecretBatData implements DungeonMechanicData {
        private OffsetPoint secretPoint = new OffsetPoint(0, 0, 0);
        private PrecalculatedMoveNearest moveNearest;
        private List<String> preRequisite = new ArrayList<String>();


        public DungeonSecretBatData() {
        }

        @Override
        public DungeonSecretBatState createState(DungeonRoom room) {
            return new DungeonSecretBatState(this, room);
        }

        public DungeonSecretBatData clone() throws CloneNotSupportedException {
            DungeonSecretBatData data = new DungeonSecretBatData();
            data.secretPoint = (OffsetPoint) secretPoint.clone();
            data.moveNearest = moveNearest;
            data.preRequisite = new ArrayList<String>(preRequisite);
            ;
            return data;
        }
    }
}
