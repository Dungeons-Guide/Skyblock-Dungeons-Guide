/*
 * Dungeons Guide - The most intelligent Hypixel Skyblock Dungeons Mod
 * Copyright (C) 2024  cyoung06 (syeyoung)
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

package kr.syeyoung.dungeonsguide.mod.dungeon.pathfinding;

import kr.syeyoung.dungeonsguide.dungeon.data.OffsetPoint;
import kr.syeyoung.dungeonsguide.dungeon.mechanics.dunegonmechanic.DungeonMechanic;
import kr.syeyoung.dungeonsguide.dungeon.mechanics.dunegonmechanic.RouteBlocker;
import kr.syeyoung.dungeonsguide.mod.dungeon.pathfinding.algorithms.IPathfindWorld;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomfinder.DungeonRoom;
import net.minecraft.util.BlockPos;

import java.util.HashSet;
import java.util.Set;

public class DungeonRoomButOpen implements IPathfindWorld {
    private DungeonRoom dungeonRoom;
    private Set<String> mechanics = new HashSet<>();

    private Set<BlockPos> freeeeePoints = new HashSet<>();


    public DungeonRoomButOpen(DungeonRoom dungeonRoom, Set<String> mechanics) {
        this.dungeonRoom = dungeonRoom;
        this.mechanics = mechanics;

        for (String mechanic : mechanics) {
            DungeonMechanic mechanic1 = dungeonRoom.getMechanics().get(mechanic);
            if (!(mechanic1 instanceof RouteBlocker)) continue;
            for (OffsetPoint offsetPoint : ((RouteBlocker) mechanic1).blockedPoints()) {
                BlockPos b = offsetPoint.getBlockPos(dungeonRoom);
                freeeeePoints.add(new BlockPos(b.getX() * 2+1, b.getY() * 2+1, b.getZ() * 2+1));
            }

        }
    }

    @Override
    public DungeonRoom.LayerNodeState getLayer(int x, int y, int z) {
        for (BlockPos freeeeePoint : freeeeePoints) {
            if (Math.abs(freeeeePoint.getX() - x) <= 3&& Math.abs(freeeeePoint.getZ() - z) <= 3 && Math.abs(freeeeePoint.getY() - y) <= 1) {
                return DungeonRoom.LayerNodeState.OPEN;
            }
        }
        return dungeonRoom.getLayer(x,y,z);
    }

    @Override
    public DungeonRoom.NodeState getBlock(int x, int y, int z) {
        for (BlockPos freeeeePoint : freeeeePoints) {
            if (Math.abs(freeeeePoint.getX() - x) <= 3 && Math.abs(freeeeePoint.getZ() - z) <= 3 && Math.abs(freeeeePoint.getY() - y) <= 1) {
                return DungeonRoom.NodeState.OPEN;
            }
        }
        return dungeonRoom.getBlock(x,y,z);
    }

    @Override
    public int getXwidth() {
        return dungeonRoom.getXwidth();
    }

    @Override
    public int getYwidth() {
        return dungeonRoom.getYwidth();
    }

    @Override
    public int getZwidth() {
        return dungeonRoom.getZwidth();
    }

    @Override
    public int getMinX() {
        return dungeonRoom.getMinX();
    }

    @Override
    public int getMinY() {
        return dungeonRoom.getMinY();
    }

    @Override
    public int getMinZ() {
        return dungeonRoom.getMinZ();
    }

}
