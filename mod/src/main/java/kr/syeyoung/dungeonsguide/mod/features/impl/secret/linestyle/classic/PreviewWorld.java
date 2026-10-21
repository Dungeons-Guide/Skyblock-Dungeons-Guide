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

package kr.syeyoung.dungeonsguide.mod.features.impl.secret.linestyle.classic;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.*;
import net.minecraft.world.chunk.IChunkProvider;

public class PreviewWorld extends World {
    private BlockState iBlockState;
    private BlockPos pos;

    public PreviewWorld(BlockState blockState, BlockPos pos) {
        super(null, null, new WorldProviderSurface(), null, true);
        this.iBlockState = blockState;
        this.pos = pos;
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
        return false;
    }

    @Override
    public BlockEntity getTileEntity(BlockPos pos) {
        return null;
    }

    @Override
    public int getCombinedLight(BlockPos pos, int lightValue) {
        return 0;
    }

    @Override
    public BlockState getBlockState(BlockPos pos) {
        if (pos.equals(this.pos)) {return iBlockState;} return Blocks.air.getDefaultState();
    }

    @Override
    public int getLightFor(EnumSkyBlock type, BlockPos pos) {
        return 0;
    }

    @Override
    public boolean isAirBlock(BlockPos pos) {
        return !pos.equals(this.pos);
    }

    @Override
    public int getStrongPower(BlockPos pos, Direction direction) {
        return 0;
    }

    @Override
    public boolean isSideSolid(BlockPos pos, Direction side, boolean _default) {
        return false;
    }
}

