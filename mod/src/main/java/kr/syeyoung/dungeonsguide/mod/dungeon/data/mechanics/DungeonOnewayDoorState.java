/*
 * Dungeons Guide - The most intelligent Hypixel Skyblock Dungeons Mod
 * Copyright (C) 2021  cyoung06
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package kr.syeyoung.dungeonsguide.mod.dungeon.data.mechanics;

import com.google.common.collect.Sets;
import kr.syeyoung.dungeonsguide.mod.dungeon.actions.ActionChangeState;
import kr.syeyoung.dungeonsguide.mod.dungeon.actions.ActionMoveNearestAir;
import kr.syeyoung.dungeonsguide.mod.dungeon.actions.PathfindImpossibleException;
import kr.syeyoung.dungeonsguide.mod.dungeon.actions.tree.ActionDAGBuilder;
import kr.syeyoung.dungeonsguide.mod.dungeon.data.OffsetPoint;
import kr.syeyoung.dungeonsguide.mod.dungeon.data.OffsetPointSet;
import kr.syeyoung.dungeonsguide.mod.dungeon.data.mechanics.dunegonmechanic.DungeonMechanicState;
import kr.syeyoung.dungeonsguide.mod.dungeon.data.mechanics.dunegonmechanic.WorldMutatingMechanicData;
import kr.syeyoung.dungeonsguide.mod.dungeon.data.mechanics.dunegonmechanic.WorldMutatingMechanicState;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomfinder.DungeonRoom;
import kr.syeyoung.dungeonsguide.mod.pathfinding.abilitysetting.AlgorithmSetting;
import kr.syeyoung.dungeonsguide.mod.utils.RenderUtils;
import kr.syeyoung.modapi.data.AABB;
import kr.syeyoung.modapi.data.VectorI3D;
import kr.syeyoung.modapi.entity.EntityType;
import lombok.Data;
import net.minecraft.init.Blocks;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Data
public class DungeonOnewayDoorState implements DungeonMechanicState, WorldMutatingMechanicState {
    private final DungeonOnewayDoorData data;
    private final DungeonRoom room;

    public DungeonOnewayDoorState(DungeonOnewayDoorData data, DungeonRoom room) {
        this.data = data;
        this.room = room;
    }


    @Override
    public void buildAction(String action, ActionDAGBuilder builder, AlgorithmSetting algorithmSetting) throws PathfindImpossibleException {
        if (action.equalsIgnoreCase("navigate")) {
            builder = builder
                    .requires(new ActionMoveNearestAir(getRepresentingPoint()), algorithmSetting);

            for (String str : data.movePreRequisite) {
                if (str.isEmpty()) continue;
                builder.optional(new ActionChangeState(str.split(":")[0], str.split(":")[1]), algorithmSetting);
            }
            return;
        }
        if (!("open".equalsIgnoreCase(action)))
            throw new PathfindImpossibleException(action + " is not a valid state for door");
        if (!isBlocking(room)) {
            return;
        }
        {
            for (String str : data.preRequisite) {
                if (str.isEmpty()) continue;
                builder.requires(new ActionChangeState(str.split(":")[0], str.split(":")[1]), algorithmSetting);
            }
        }
    }

    @Override
    public void highlight(Color color, String name, float partialTicks) {
        if (data.secretPoint.getOffsetPointList().isEmpty()) return;
        OffsetPoint firstpt = data.secretPoint.getOffsetPointList().get(0);
        VectorI3D pos = firstpt.getBlockPos(room);
        RenderUtils.drawTextAtWorld(name, pos.getX() + 0.5f, pos.getY() + 0.75f, pos.getZ() + 0.5f, 0xFFFFFFFF, 0.03f, false, true, partialTicks);
        RenderUtils.drawTextAtWorld(getCurrentState(), pos.getX() + 0.5f, pos.getY() + 0.25f, pos.getZ() + 0.5f, 0xFFFFFFFF, 0.03f, false, true, partialTicks);

        List<VectorI3D> list = new ArrayList<>();
        for (OffsetPoint offsetPoint : data.secretPoint.getOffsetPointList()) {
            list.add(offsetPoint.getBlockPos(room));
        }
        RenderUtils.highlightBlocksStencil(list, partialTicks, color, false);
    }

    @Override
    public boolean isBlocking(DungeonRoom dungeonRoom) {

        for (OffsetPoint offsetPoint : data.secretPoint.getOffsetPointList()) {
            if (offsetPoint.getBlock(dungeonRoom) != Blocks.air) {
                VectorI3D blockPos = offsetPoint.getBlockPos(dungeonRoom);
                if (dungeonRoom.getContext().getUworld().getEntitiesWithinAabb(EntityType.FALLING_BLOCK, new AABB(
                            blockPos.getX(), blockPos.getY() - 4, blockPos.getZ(),
                            blockPos.getX() + 1, blockPos.getY() + 1, blockPos.getZ() + 1
                )).isEmpty()) return false;
                return true;
            }
        }
        return false;
    }

    @Override
    public List<OffsetPoint> blockedPoints() {
        return data.secretPoint.getOffsetPointList();
    }

    @Override
    public String getCurrentState() {
        return isBlocking(room) ? "closed" : "open";
    }


    @Override
    public Set<String> getAvailableActions() {
        String currentStatus = getCurrentState();
        if (currentStatus.equalsIgnoreCase("closed"))
            return Sets.newHashSet("navigate", "open");
        return Sets.newHashSet("navigate");
    }

    @Override
    public Set<String> getTotalPossibleStates() {
        return Sets.newHashSet("open", "closed");
    }


    @Override
    public OffsetPoint getRepresentingPoint() {
        int leastY = Integer.MAX_VALUE;
        OffsetPoint thatPt = null;
        for (OffsetPoint offsetPoint : data.secretPoint.getOffsetPointList()) {
            if (offsetPoint.getY() < leastY) {
                thatPt = offsetPoint;
                leastY = offsetPoint.getY();
            }
        }
        return thatPt;
    }

    @Data
    public static class DungeonOnewayDoorData implements WorldMutatingMechanicData {
        private OffsetPointSet secretPoint = new OffsetPointSet();
        private List<String> preRequisite = new ArrayList<String>();
        private List<String> movePreRequisite = new ArrayList<String>();


        public DungeonOnewayDoorData() {
        }

        @Override
        public DungeonOnewayDoorState createState(DungeonRoom room) {
            return new DungeonOnewayDoorState(this, room);
        }

        public DungeonOnewayDoorData clone() throws CloneNotSupportedException {
            DungeonOnewayDoorData data = new DungeonOnewayDoorData();
            data.secretPoint = (OffsetPointSet) secretPoint.clone();
            data.preRequisite = new ArrayList<String>(preRequisite);
            ;
            return data;
        }

        @Override
        public List<OffsetPoint> blockedPoints() {
            return secretPoint.getOffsetPointList();
        }
    }
}
