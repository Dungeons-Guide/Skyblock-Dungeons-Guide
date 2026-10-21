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

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.*;
import net.minecraft.world.chunk.IChunkProvider;

public class CachedWorld extends World {
    private ChunkCache chunkCache;

    public CachedWorld(ChunkCache chunkCache) {
        super(null, null, new WorldProviderSurface(), null, true);
        this.chunkCache = chunkCache;
    }


    public CachedWorld(ChunkCache chunkCache, WorldProvider provider) {
        super(null, null, provider, null, true);
        this.chunkCache = chunkCache;
    }

    @Override
    protected IChunkProvider createChunkProvider() {
        return null;
    }

    @Override
    protected int getRenderDistanceChunks() {
        return 999;
    }

    @Override
    public boolean extendedLevelsInChunkCache() {
        return chunkCache.extendedLevelsInChunkCache();
    }

    @Override
    public BlockEntity getTileEntity(BlockPos pos) {
        return chunkCache.getTileEntity(pos);
    }

    @Override
    public int getCombinedLight(BlockPos pos, int lightValue) {
        return chunkCache.getCombinedLight(pos, lightValue);
    }

    @Override
    public BlockState getBlockState(BlockPos pos) {
        return chunkCache.getBlockState(pos);
    }

    @Override
    public int getLightFor(EnumSkyBlock type, BlockPos pos) {
        return chunkCache.getLightFor(type, pos);
    }

    @Override
    public boolean isAirBlock(BlockPos pos) {
        return chunkCache.isAirBlock(pos);
    }

    @Override
    public int getStrongPower(BlockPos pos, Direction direction) {
        return chunkCache.getStrongPower(pos, direction);
    }

    @Override
    public boolean isSideSolid(BlockPos pos, Direction side, boolean _default) {
        return chunkCache.isSideSolid(pos, side, _default);
    }
}
