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

import kr.syeyoung.dungeonsguide.mod.dungeon.data.DungeonRoomInfo;
import kr.syeyoung.dungeonsguide.mod.dungeon.data.OffsetPoint;
import kr.syeyoung.dungeonsguide.mod.dungeon.data.mechanics.DungeonBreakableWallState;
import kr.syeyoung.dungeonsguide.mod.dungeon.data.mechanics.DungeonTombState;
import kr.syeyoung.dungeonsguide.mod.dungeon.data.mechanics.dunegonmechanic.DungeonMechanicData;
import kr.syeyoung.dungeonsguide.mod.dungeon.data.mechanics.dunegonmechanic.WorldMutatingMechanicState;
import kr.syeyoung.dungeonsguide.mod.dungeon.pathfinding.algorithms.IPathfindWorld;
import kr.syeyoung.dungeonsguide.mod.dungeon.pathfinding.pathfindcache.PathfindPreset;
import kr.syeyoung.dungeonsguide.mod.dungeon.pathfinding.world.CoordinateMapBackedPathfindWorld;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomfinder.BitStorage;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomfinder.RoomBounds;
import kr.syeyoung.dungeonsguide.mod.dungeon.world.CollisionStateCalculatingCoordinateMap;
import kr.syeyoung.dungeonsguide.mod.dungeon.world.ICoordinateMap;
import kr.syeyoung.dungeonsguide.mod.dungeon.world.PearlCalculatingCoordinateMap;
import kr.syeyoung.dungeonsguide.mod.features.FeatureRegistry;
import kr.syeyoung.dungeonsguide.mod.dungeon.pathfinding.abilitysetting.AlgorithmSetting;
import lombok.Getter;
import net.minecraft.block.Block;
import net.minecraft.block.BlockFence;
import net.minecraft.block.BlockSkull;
import net.minecraft.block.BlockStairs;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MathHelper;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;
import net.minecraft.world.WorldProviderSurface;
import net.minecraft.world.chunk.IChunkProvider;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

// TODO: maybe touch some time later
public class DRIWorld extends World implements ICoordinateMap<IBlockState> {

    @Getter
    private DungeonRoomInfo dungeonRoomInfo;
    private List<String> openMechanics;
    private int shape;

    private HashSet<BlockPos> poses = new HashSet<>();
    private HashSet<BlockPos> open = new HashSet<>();
    private AlgorithmSetting algorithmSetting;

    @Getter
    private CoordinateMapBackedPathfindWorld pathfindWorld;

    public DRIWorld(DungeonRoomInfo dungeonRoomInfo) {
        this(dungeonRoomInfo, Collections.emptyList());
    }

    public DRIWorld(DungeonRoomInfo dungeonRoomInfo, List<String> openMechanics) {
        super(null, null, new WorldProviderSurface(), null, true);
        this.dungeonRoomInfo = dungeonRoomInfo;
        this.openMechanics = openMechanics;
        this.shape = dungeonRoomInfo.getShape();

        for (DungeonMechanicData value : dungeonRoomInfo.getMechanics().values()) {
            if (value instanceof DungeonTombState.DungeonTombData) {
                for (OffsetPoint offsetPoint : ((DungeonTombState.DungeonTombData) value).blockedPoints()) {
                    poses.add(new BlockPos(offsetPoint.getX(), offsetPoint.getY() + 70, offsetPoint.getZ()));
                }
            } else if (value instanceof DungeonBreakableWallState.DungeonBreakableWallData) {
                for (OffsetPoint offsetPoint : ((DungeonBreakableWallState.DungeonBreakableWallData) value).blockedPoints()) {
                    poses.add(new BlockPos(offsetPoint.getX(), offsetPoint.getY() + 70, offsetPoint.getZ()));
                }
            }
        } // TODO: construct actual mechanics.

        for (String openMechanic : openMechanics) {
            WorldMutatingMechanicState routeBlocker = (WorldMutatingMechanicState) dungeonRoomInfo.getMechanics().get(openMechanic);
            for (OffsetPoint offsetPoint : routeBlocker.blockedPoints()) {
                open.add(new BlockPos(offsetPoint.getX(), offsetPoint.getY() +70, offsetPoint.getZ()));
            }

        }


        PathfindPreset preset = FeatureRegistry.SECRET_PRECALC_LIST.getSelectedPreset();
        AlgorithmSetting algorithmSetting1 = preset.getRoomPreset(dungeonRoomInfo.getUuid()).getEffectiveAlgorithmSetting(dungeonRoomInfo);
        this.algorithmSetting = algorithmSetting1;

        pathfindWorld = new CoordinateMapBackedPathfindWorld(this, algorithmSetting, new RoomBounds(
                dungeonRoomInfo.getShape(),
                new BlockPos(0, 70, 0),
                new BlockPos(dungeonRoomInfo.getWidth() -1, 70, dungeonRoomInfo.getLength() - 1)
        ), poses);
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
        if (open.contains(pos)) {
            return Blocks.air.getDefaultState();
        }
        return dungeonRoomInfo.getBlock(pos.getX(), pos.getY()-70, pos.getZ(), 0);
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

    @Override
    public IBlockState getBlock(int x, int y, int z) {
        if (open.contains(new BlockPos(x, y, z))) {
            return Blocks.air.getDefaultState();
        }
        return dungeonRoomInfo.getBlock(x, y, z, 0);
    }

    @Override
    public boolean isInScope(int x, int y, int z) {
        return true;
    }

    @Override
    public int getMinX() {
        return 0;
    }

    @Override
    public int getMinY() {
        return 0;
    }

    @Override
    public int getMinZ() {
        return 0;
    }

    @Override
    public int getMaxX() {
        return dungeonRoomInfo.getWidth();
    }

    @Override
    public int getMaxY() {
        return 256;
    }

    @Override
    public int getMaxZ() {
        return dungeonRoomInfo.getLength();
    }

    @Override
    public int getLenX() {
        return ICoordinateMap.super.getLenX();
    }

    @Override
    public int getLenY() {
        return ICoordinateMap.super.getLenY();
    }

    @Override
    public int getLenZ() {
        return ICoordinateMap.super.getLenZ();
    }
}
