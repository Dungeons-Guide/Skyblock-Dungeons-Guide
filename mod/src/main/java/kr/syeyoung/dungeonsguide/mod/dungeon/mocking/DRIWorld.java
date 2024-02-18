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

package kr.syeyoung.dungeonsguide.mod.dungeon.mocking;

import kr.syeyoung.dungeonsguide.dungeon.data.DungeonRoomInfo;
import kr.syeyoung.dungeonsguide.dungeon.data.OffsetPoint;
import kr.syeyoung.dungeonsguide.dungeon.mechanics.dunegonmechanic.RouteBlocker;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;
import net.minecraft.world.WorldProviderSurface;
import net.minecraft.world.chunk.IChunkProvider;

import java.util.Collections;
import java.util.List;

public class DRIWorld extends World {

    private DungeonRoomInfo dungeonRoomInfo;
    private List<String> openMechanics;

    public DRIWorld(DungeonRoomInfo dungeonRoomInfo) {
        this(dungeonRoomInfo, Collections.emptyList());
    }

    public DRIWorld(DungeonRoomInfo dungeonRoomInfo, List<String> openMechanics) {
        super(null, null, new WorldProviderSurface(), null, true);
        this.dungeonRoomInfo = dungeonRoomInfo;
        this.openMechanics = openMechanics;
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
    public TileEntity getTileEntity(BlockPos pos) {
        return null;
    }

    @Override
    public int getCombinedLight(BlockPos pos, int lightValue) {
        return 0;
    }

    @Override
    public IBlockState getBlockState(BlockPos pos) {
        for (String openMechanic : openMechanics) {
            RouteBlocker routeBlocker = (RouteBlocker) dungeonRoomInfo.getMechanics().get(openMechanic);
            for (OffsetPoint offsetPoint : routeBlocker.blockedPoints()) {
                if (offsetPoint.getX() == pos.getX() && offsetPoint.getY() == pos.getY() && offsetPoint.getZ() == pos.getZ()) {
                    return Blocks.air.getDefaultState();
                }
            }

        }
        return dungeonRoomInfo.getBlock(pos.getX(), pos.getY(), pos.getZ(), 0);
    }

    @Override
    public int getLightFor(EnumSkyBlock type, BlockPos pos) {
        return 0;
    }

    @Override
    public boolean isAirBlock(BlockPos pos) {
        return getBlockState(pos) == null || getBlockState(pos).getBlock() == Blocks.air;
    }

    @Override
    public int getStrongPower(BlockPos pos, EnumFacing direction) {
        return 0;
    }

    @Override
    public boolean isSideSolid(BlockPos pos, EnumFacing side, boolean _default) {
        return this.getBlockState(pos).getBlock().isSideSolid(this, pos, side);
    }
}
