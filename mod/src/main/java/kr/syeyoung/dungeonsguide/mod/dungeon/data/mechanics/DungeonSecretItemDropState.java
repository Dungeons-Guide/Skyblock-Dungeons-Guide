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
import kr.syeyoung.dungeonsguide.mod.chat.ChatTransmitter;
import kr.syeyoung.dungeonsguide.mod.dungeon.DungeonActionContext;
import kr.syeyoung.dungeonsguide.mod.dungeon.actions.ActionChangeState;
import kr.syeyoung.dungeonsguide.mod.dungeon.actions.ActionMoveNearestAir;
import kr.syeyoung.dungeonsguide.mod.dungeon.actions.ActionUtils;
import kr.syeyoung.dungeonsguide.mod.dungeon.actions.PathfindImpossibleException;
import kr.syeyoung.dungeonsguide.mod.dungeon.actions.tree.ActionDAGBuilder;
import kr.syeyoung.dungeonsguide.mod.dungeon.data.OffsetPoint;
import kr.syeyoung.dungeonsguide.mod.dungeon.data.PrecalculatedMoveNearest;
import kr.syeyoung.dungeonsguide.mod.dungeon.data.mechanics.dunegonmechanic.DungeonMechanicData;
import kr.syeyoung.dungeonsguide.mod.dungeon.data.mechanics.dunegonmechanic.DungeonMechanicState;
import kr.syeyoung.dungeonsguide.mod.dungeon.data.mechanics.dunegonmechanic.ISecret;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomfinder.DungeonRoom;
import kr.syeyoung.dungeonsguide.mod.features.FeatureRegistry;
import kr.syeyoung.dungeonsguide.mod.pathfinding.abilitysetting.AlgorithmSetting;
import kr.syeyoung.modapi.ModAPI;
import kr.syeyoung.modapi.data.AABB;
import kr.syeyoung.modapi.data.Vector3D;
import kr.syeyoung.modapi.data.VectorI3D;
import kr.syeyoung.modapi.entity.EntityType;
import kr.syeyoung.modapi.entity.UEntity;
import kr.syeyoung.modapi.entity.UEntityItem;
import kr.syeyoung.modapi.item.Item;
import kr.syeyoung.modapi.rendering.UWorldRenderContext;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

@Data
public class DungeonSecretItemDropState implements DungeonMechanicState, ISecret {
    private final DungeonSecretItemDropData data;
    private final DungeonRoom room;

    public DungeonSecretItemDropState(DungeonSecretItemDropData data, DungeonRoom room) {
        this.data = data;
        this.room = room;
    }

    @Override
    public boolean isFound(DungeonRoom dungeonRoom) {
        return getSecretStatus(dungeonRoom) == SecretStatus.FOUND;
    }

    private SecretStatus status = SecretStatus.NOT_SURE;
    private boolean absolutelyFound = false;
    private int nearbyTicks = 0;
    public void tick(DungeonRoom dungeonRoom) {
        if (absolutelyFound) return;

        VectorI3D pos = data.secretPoint.getBlockPos(dungeonRoom);
        boolean itemFound = false;
        for (UEntity entityItem : dungeonRoom.getContext().getWorld().getEntitiesWithinAabb(EntityType.ITEM, new AABB(-4, -4, -4, 4, 4, 4).addCoord(pos.getX(), pos.getY(), pos.getZ()))) {
            if (((UEntityItem)entityItem).getItem().getItem() == Item.DYE) continue;
            itemFound = true;
        }

        if (ModAPI.getAPI().getPlayer().getPositionVector().distanceSq(pos) < 40) {
            nearbyTicks++;
            if (itemFound) {
                status = SecretStatus.DEFINITELY_NOT;
            } else if (status != SecretStatus.FOUND && nearbyTicks > 40) {
                status = SecretStatus.FOUND;
                List<UEntity> items = dungeonRoom.getContext().getWorld().getEntitiesWithinAabb(EntityType.ITEM, new AABB(-4, -4, -4, 4, 4, 4).addCoord(pos.getX(), pos.getY(), pos.getZ()));
                ChatTransmitter.sendDebugChat("Assume at " + ISecret.toString(pos) + "found? " + items.size());
            }
        }

        if (!absolutelyFound) {
            Vector3D pos2 = new Vector3D(pos);
            for (Integer pickedup : DungeonActionContext.getPickedups()) {
                if (DungeonActionContext.getSpawnLocation().get(pickedup) == null) continue;
                if (DungeonActionContext.getSpawnLocation().get(pickedup).distanceSq(pos2) < 4) {
                    status = SecretStatus.FOUND;
                    absolutelyFound = true;
                }
            }
        }
    }

    public SecretStatus getSecretStatus(DungeonRoom dungeonRoom) {
        return status;
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
            ActionUtils.buildActionMoveAnd(builder, room, data.moveNearest, Collections.emptyList(), data.preRequisite, um -> um, "MoveNearest", algorithmSetting);
        } else {
            builder = builder.requires(new ActionMoveNearestAir(data.secretPoint), algorithmSetting);
            for (String str : data.preRequisite) {
                if (str.isEmpty()) continue;
                builder.optional(new ActionChangeState(str.split(":")[0], str.split(":")[1]), algorithmSetting);
            }
        }
    }

    @Override
    public void highlight(Color color, String name, UWorldRenderContext context, float partialTicks) {
        VectorI3D pos = getSecretPoint().getBlockPos(room);
        context.highlightBlock(pos, color, partialTicks, false);
        context.drawTextAtWorld(name, pos.getX() + 0.5f, pos.getY() + 0.375f, pos.getZ() + 0.5f, 0xFFFFFFFF, 0.03f, false, true, partialTicks);
        context.drawTextAtWorld(getCurrentState(), pos.getX() + 0.5f, pos.getY() + 0f, pos.getZ() + 0.5f, 0xFFFFFFFF, 0.03f, false, true, partialTicks);


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
    public static class DungeonSecretItemDropData implements DungeonMechanicData {
        private OffsetPoint secretPoint = new OffsetPoint(0, 0, 0);
        private PrecalculatedMoveNearest moveNearest;
        private List<String> preRequisite = new ArrayList<String>();


        public DungeonSecretItemDropData() {
        }

        @Override
        public DungeonSecretItemDropState createState(DungeonRoom room) {
            return new DungeonSecretItemDropState(this, room);
        }

        public DungeonSecretItemDropData clone() throws CloneNotSupportedException {
            DungeonSecretItemDropData data = new DungeonSecretItemDropData();
            data.moveNearest = moveNearest;
            data.secretPoint = (OffsetPoint) secretPoint.clone();
            data.preRequisite = new ArrayList<String>(preRequisite);
            ;
            return data;
        }
    }
}
