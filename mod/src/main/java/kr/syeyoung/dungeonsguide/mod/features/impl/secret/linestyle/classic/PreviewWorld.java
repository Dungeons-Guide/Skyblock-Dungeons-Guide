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
import net.minecraft.core.BlockPos;
import kr.syeyoung.dungeonsguide.mod.dungeon.world.DGWorld;

public class PreviewWorld implements DGWorld {
    private BlockState iBlockState;
    private BlockPos pos;

    public PreviewWorld(BlockState blockState, BlockPos pos) {
        this.iBlockState = blockState;
        this.pos = pos;
    }

    @Override
    public BlockState getBlockState(BlockPos pos) {
        if (pos.equals(this.pos)) {return iBlockState;} return Blocks.AIR.defaultBlockState();
    }


    @Override
    public BlockState getBlock(int x, int y, int z) {
        return getBlockState(new BlockPos(x, y, z));
    }

    @Override
    public boolean isAir(BlockPos pos) {
        return !pos.equals(this.pos);
    }

    @Override
    public boolean isInScope(int x, int y, int z) {
        return true;
    }

    @Override
    public int getMinX() {
        return Integer.MIN_VALUE;
    }

    // 1.8.9 worlds were 256 high
    @Override
    public int getMinY() {
        return 0;
    }

    @Override
    public int getMinZ() {
        return Integer.MIN_VALUE;
    }

    @Override
    public int getMaxX() {
        return Integer.MAX_VALUE;
    }

    @Override
    public int getMaxY() {
        return 256;
    }

    @Override
    public int getMaxZ() {
        return Integer.MAX_VALUE;
    }
}

