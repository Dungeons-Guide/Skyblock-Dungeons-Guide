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

import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/**
 * Every world DG reads blocks from: the client level or one of DG's own (D108).
 */
public interface DGWorld extends ICoordinateMap<BlockState> {
    default BlockState getBlockState(BlockPos pos) {
        return getBlock(pos.getX(), pos.getY(), pos.getZ());
    }

    default @Nullable BlockEntity getBlockEntity(BlockPos pos) {
        return null;
    }

    default boolean isAir(BlockPos pos) {
        BlockState blockState = getBlockState(pos);
        return blockState == null || blockState.isAir();
    }

    // for vanilla's collision / hardness / raytrace / block model calls
    default BlockAndTintGetter asVanilla() {
        return new DGWorldVanillaAdapter(this);
    }
}
