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
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.List;

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

    // 1.8.9's Block.addCollisionBoxesToList: the block's collision boxes in world space that touch mask
    default void addCollisionBoxes(BlockPos pos, BlockState state, AABB mask, List<AABB> list) {
        VoxelShape shape = state.getCollisionShape(asVanilla(), pos);
        for (AABB box : shape.toAabbs()) {
            AABB moved = box.move(pos);
            if (moved.intersects(mask)) list.add(moved);
        }
    }

    // for vanilla's collision / hardness / raytrace / block model calls
    default BlockAndTintGetter asVanilla() {
        return new DGWorldVanillaAdapter(this);
    }
}
