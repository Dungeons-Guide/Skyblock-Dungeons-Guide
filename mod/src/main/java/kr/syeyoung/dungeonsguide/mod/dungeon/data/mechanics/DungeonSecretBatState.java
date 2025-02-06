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
import kr.syeyoung.dungeonsguide.mod.dungeon.data.OffsetPoint;
import kr.syeyoung.dungeonsguide.mod.dungeon.data.PrecalculatedMoveNearest;
import kr.syeyoung.dungeonsguide.mod.dungeon.data.mechanics.dunegonmechanic.DungeonMechanicData;
import kr.syeyoung.dungeonsguide.mod.dungeon.data.mechanics.dunegonmechanic.DungeonMechanicState;
import kr.syeyoung.dungeonsguide.mod.dungeon.data.mechanics.dunegonmechanic.ISecret;
import kr.syeyoung.dungeonsguide.mod.dungeon.data.mechanics.predicates.PredicateBat;
import kr.syeyoung.dungeonsguide.mod.dungeon.DungeonActionContext;
import kr.syeyoung.dungeonsguide.mod.dungeon.actions.*;
import kr.syeyoung.dungeonsguide.mod.dungeon.actions.tree.ActionDAGBuilder;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomfinder.DungeonRoom;
import kr.syeyoung.dungeonsguide.mod.features.FeatureRegistry;
import kr.syeyoung.dungeonsguide.mod.utils.RenderUtils;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import net.minecraft.util.BlockPos;
import net.minecraft.util.Vec3;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

@Data
public class DungeonSecretBatState implements DungeonMechanicState, ISecret {
    private final DungeonSecretBatData data;
    private final DungeonRoom room;

    public DungeonSecretBatState(DungeonSecretBatData data, DungeonRoom room) {
        this.data = data;
        this.room = room;
    }

    public SecretStatus getSecretStatus(DungeonRoom dungeonRoom) {
        BlockPos bpos = data.secretPoint.getBlockPos(dungeonRoom);
        if (dungeonRoom.getRoomContext().containsKey("b-" + ISecret.toString(bpos))) {
            return SecretStatus.FOUND;
        }
        Vec3 spawn = new Vec3(bpos);
        for (Integer killed : DungeonActionContext.getKilleds()) {
            if (DungeonActionContext.getSpawnLocation().get(killed) == null) continue;
            if (DungeonActionContext.getSpawnLocation().get(killed).squareDistanceTo(spawn) < 100) {
                dungeonRoom.getRoomContext().put("b-" + ISecret.toString(bpos), true);
                return SecretStatus.FOUND;
            }
        }
        return SecretStatus.NOT_SURE;
    }

    @Override
    public boolean isFound(DungeonRoom dungeonRoom) {
        return getSecretStatus(dungeonRoom) == SecretStatus.FOUND;
    }

    @Override
    public void tick(DungeonRoom dungeonRoom) {
        // nothin
    }

    @Override
    public void buildAction(String state, ActionDAGBuilder builder) throws PathfindImpossibleException {
        if (state.equalsIgnoreCase("navigate")) {
            builder = builder
                    .requires(new ActionMoveNearestAir(getRepresentingPoint()));
            for (String str : data.preRequisite) {
                if (str.isEmpty()) continue;
                builder.optional(new ActionChangeState(str.split(":")[0], str.split(":")[1]));
            }
            return;
        }
        if (!"found".equalsIgnoreCase(state))
            throw new PathfindImpossibleException(state + " is not valid state for secret");
        if (state.equals("found") && getSecretStatus(room) == SecretStatus.FOUND) return;
        if (data.moveNearest != null) {
            ActionUtils.buildActionMoveAnd(builder, room, data.moveNearest, Collections.emptyList(), data.preRequisite, um -> um.requires(() -> {
                ActionKill actionKill = new ActionKill(data.secretPoint);
                actionKill.setRadius(10);
                actionKill.setPredicate(PredicateBat.INSTANCE);
                return actionKill;
            }), "MoveAndKill");
        } else {
            builder = builder.requires(new AtomicAction.Builder()
                    .requires(() -> {
                        ActionKill actionKill = new ActionKill(data.secretPoint);
                        actionKill.setRadius(10);
                        actionKill.setPredicate(PredicateBat.INSTANCE);
                        return actionKill;
                    })
                    .requires(new ActionMoveNearestAir(data.secretPoint))
                    .build("MoveAndKill"));

            for (String str : data.preRequisite) {
                if (str.isEmpty()) continue;
                builder.optional(new ActionChangeState(str.split(":")[0], str.split(":")[1]));
            }
        }
    }

    @Override
    public void highlight(Color color, String name, float partialTicks) {
        BlockPos pos = getSecretPoint().getBlockPos(room);
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
    public Set<String> getPossibleStates() {
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
