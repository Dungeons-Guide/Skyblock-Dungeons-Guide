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

package kr.syeyoung.dungeonsguide.mod.dungeon.world;

import kr.syeyoung.dungeonsguide.mod.pathfinding.world.EditableChunkCache;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

// a room's view of the level through its chunk cache (D108)
public class CachedWorld implements DGWorld {
    private final EditableChunkCache chunkCache;
    private final ThreadLocal<BlockPos.MutableBlockPos> blockPosThreadLocal = ThreadLocal.withInitial(BlockPos.MutableBlockPos::new);

    public CachedWorld(EditableChunkCache chunkCache) {
        this.chunkCache = chunkCache;
    }

    @Override
    public BlockState getBlock(int x, int y, int z) {
        return chunkCache.getBlockState(blockPosThreadLocal.get().set(x, y, z));
    }

    @Override
    public BlockState getBlockState(BlockPos pos) {
        return chunkCache.getBlockState(pos);
    }

    @Override
    public BlockEntity getBlockEntity(BlockPos pos) {
        return chunkCache.getBlockEntity(pos);
    }

    @Override
    public boolean isInScope(int x, int y, int z) {
        return x >= getMinX() && x < getMaxX() && y >= getMinY() && y < getMaxY() && z >= getMinZ() && z < getMaxZ();
    }

    @Override
    public int getMinX() {
        return chunkCache.getMinX();
    }

    @Override
    public int getMinY() {
        return chunkCache.getLevel().getMinY();
    }

    @Override
    public int getMinZ() {
        return chunkCache.getMinZ();
    }

    @Override
    public int getMaxX() {
        return chunkCache.getMaxX();
    }

    @Override
    public int getMaxY() {
        return chunkCache.getLevel().getMaxY() + 1;
    }

    @Override
    public int getMaxZ() {
        return chunkCache.getMaxZ();
    }

    // 1.8.9 forwarded light and isSideSolid to the chunk cache, light is the adapter's (none) and solidity comes from block shapes on 26.1
}
