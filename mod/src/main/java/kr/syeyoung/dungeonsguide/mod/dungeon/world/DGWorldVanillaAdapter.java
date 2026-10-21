/*
 * Dungeons Guide - The most intelligent Hypixel Skyblock Dungeons Mod
 * Copyright (C) 2026  cyoung06 (syeyoung)
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

import lombok.AllArgsConstructor;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.world.level.CardinalLighting;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ColorResolver;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraft.world.level.material.FluidState;
import org.jetbrains.annotations.Nullable;

/**
 * A DGWorld as vanilla's BlockAndTintGetter. No light (1.8.9's fake worlds returned 0) and no tint.
 */
@AllArgsConstructor
public class DGWorldVanillaAdapter implements BlockAndTintGetter {
    private final DGWorld world;

    @Override
    public CardinalLighting cardinalLighting() {
        return CardinalLighting.DEFAULT;
    }

    @Override
    public LevelLightEngine getLightEngine() {
        return LevelLightEngine.EMPTY;
    }

    @Override
    public int getBlockTint(BlockPos pos, ColorResolver color) {
        return -1;
    }

    @Override
    public @Nullable BlockEntity getBlockEntity(BlockPos pos) {
        return world.getBlockEntity(pos);
    }

    @Override
    public BlockState getBlockState(BlockPos pos) {
        BlockState blockState = world.getBlockState(pos);
        return blockState == null ? Blocks.AIR.defaultBlockState() : blockState;
    }

    @Override
    public FluidState getFluidState(BlockPos pos) {
        return getBlockState(pos).getFluidState();
    }

    @Override
    public int getHeight() {
        return world.getLenY();
    }

    @Override
    public int getMinY() {
        return world.getMinY();
    }
}
