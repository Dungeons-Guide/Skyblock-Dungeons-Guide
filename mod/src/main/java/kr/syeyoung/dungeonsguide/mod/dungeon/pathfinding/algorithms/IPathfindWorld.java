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

package kr.syeyoung.dungeonsguide.mod.dungeon.pathfinding.algorithms;

import kr.syeyoung.dungeonsguide.mod.dungeon.roomfinder.DungeonRoom;
import net.minecraft.block.state.IBlockState;

public interface IPathfindWorld {
    public IBlockState getActualBlock(int x, int y, int z);
    public DungeonRoom.CollisionState getBlock(int x, int y, int z);
    public DungeonRoom.PearlLandType getPearl(int x, int y, int z);
    public boolean isInstabreak(int x, int y, int z);

    public int getXwidth();
    public int getYwidth();
    public int getZwidth();
    public int getMinX();
    public int getMinY();
    public int getMinZ();


}
