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

import lombok.Getter;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/**
 * The client level as a DGWorld (D108).
 */
public class MinecraftWorld implements DGWorld {
    @Getter
    private final ClientLevel level;
    private final ThreadLocal<BlockPos.MutableBlockPos> blockPosThreadLocal = ThreadLocal.withInitial(BlockPos.MutableBlockPos::new);

    public MinecraftWorld(ClientLevel level) {
        this.level = level;
    }

    @Override
    public BlockState getBlock(int x, int y, int z) {
        return level.getBlockState(blockPosThreadLocal.get().set(x, y, z));
    }

    @Override
    public BlockState getBlockState(BlockPos pos) {
        return level.getBlockState(pos);
    }

    @Override
    public @Nullable BlockEntity getBlockEntity(BlockPos pos) {
        return level.getBlockEntity(pos);
    }

    @Override
    public boolean isAir(BlockPos pos) {
        return level.isEmptyBlock(pos);
    }

    @Override
    public BlockAndTintGetter asVanilla() {
        return level;
    }

    @Override
    public boolean isInScope(int x, int y, int z) {
        return y >= getMinY() && y < getMaxY();
    }

    @Override
    public int getMinX() {
        return Integer.MIN_VALUE;
    }

    @Override
    public int getMinY() {
        return level.getMinY();
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
        return level.getMaxY() + 1;
    }

    @Override
    public int getMaxZ() {
        return Integer.MAX_VALUE;
    }
}
