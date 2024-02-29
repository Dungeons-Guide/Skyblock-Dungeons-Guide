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
import kr.syeyoung.dungeonsguide.dungeon.data.OffsetVec3;
import kr.syeyoung.dungeonsguide.dungeon.mechanics.DungeonBreakableWall;
import kr.syeyoung.dungeonsguide.dungeon.mechanics.DungeonTomb;
import kr.syeyoung.dungeonsguide.dungeon.mechanics.dunegonmechanic.DungeonMechanic;
import kr.syeyoung.dungeonsguide.dungeon.mechanics.dunegonmechanic.RouteBlocker;
import kr.syeyoung.dungeonsguide.mod.dungeon.pathfinding.algorithms.IPathfindWorld;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomfinder.BitStorage;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomfinder.DungeonRoom;
import kr.syeyoung.dungeonsguide.mod.features.FeatureRegistry;
import kr.syeyoung.dungeonsguide.mod.features.impl.secret.FeaturePathfindSettings;
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

public class DRIWorld extends World implements IPathfindWorld {

    @Getter
    private DungeonRoomInfo dungeonRoomInfo;
    private List<String> openMechanics;
    private int shape;

    private HashSet<BlockPos> poses = new HashSet<>();
    private HashSet<BlockPos> open = new HashSet<>();
    private FeaturePathfindSettings.AlgorithmSettings algorithmSettings;

    private BitStorage enderpearl, whole;
    public DRIWorld(DungeonRoomInfo dungeonRoomInfo) {
        this(dungeonRoomInfo, Collections.emptyList());
    }

    public DRIWorld(DungeonRoomInfo dungeonRoomInfo, List<String> openMechanics) {
        super(null, null, new WorldProviderSurface(), null, true);
        this.dungeonRoomInfo = dungeonRoomInfo;
        this.openMechanics = openMechanics;
        this.shape = dungeonRoomInfo.getShape();

        for (DungeonMechanic value : dungeonRoomInfo.getMechanics().values()) {
            if (value instanceof DungeonTomb) {
                for (OffsetPoint offsetPoint : ((DungeonTomb) value).blockedPoints()) {
                    poses.add(new BlockPos(offsetPoint.getX(), offsetPoint.getY() + 70, offsetPoint.getZ()));
                }
            } else if (value instanceof DungeonBreakableWall) {
                for (OffsetPoint offsetPoint : ((DungeonBreakableWall) value).blockedPoints()) {
                    poses.add(new BlockPos(offsetPoint.getX(), offsetPoint.getY() + 70, offsetPoint.getZ()));
                }
            }
        }

        for (String openMechanic : openMechanics) {
            RouteBlocker routeBlocker = (RouteBlocker) dungeonRoomInfo.getMechanics().get(openMechanic);
            for (OffsetPoint offsetPoint : routeBlocker.blockedPoints()) {
                open.add(new BlockPos(offsetPoint.getX(), offsetPoint.getY() +70, offsetPoint.getZ()));
            }

        }


        whole = new BitStorage(getXwidth(), getYwidth(), getZwidth(), DungeonRoom.CollisionState.BITS); // plus 1 , because I don't wanna do floating point op for dividing and ceiling
        enderpearl = new BitStorage(getXwidth(), getYwidth(), getZwidth(), DungeonRoom.PearlLandType.BITS);

        this.algorithmSettings = FeatureRegistry.SECRET_PATHFIND_SETTINGS.getAlgorithmSettings();
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

    public boolean canAccessRelative(int x, int z) {
        boolean firstCond =  x> 0 && z > 0 && (shape >>((z/32) *4 +(x/32)) & 0x1) > 0;
        boolean zCond = (shape >> ((z / 32) * 4 + (x / 32) - 1) & 0x1) > 0;
        boolean xCond = (shape >> ((z / 32) * 4 + (x / 32) - 4) & 0x1) > 0;
        if (x % 32 == 0 && z % 32 == 0) {
            return firstCond && (shape >>((z/32) *4 +(x/32) - 5) & 0x1) > 0
                    && xCond
                    && zCond;
        } else if (x % 32 == 0) {
            return firstCond && zCond;
        } else if (z % 32 == 0) {
            return firstCond && xCond;
        }

        return firstCond;
    }
    private int isNoInstaBreak(IBlockState iBlockState, BlockPos pos) {
        Block b = iBlockState.getBlock();
        if (b == Blocks.air) return 0;
        if (b.getBlockHardness(this, pos) < 0) {
            return 99;
        } else if (algorithmSettings.getPickaxeSpeed() > 0 &&
                (((algorithmSettings.getPickaxe().canHarvestBlock(b)) &&
                        b.getBlockHardness(this, pos) <= algorithmSettings.getPickaxeSpeed() / 30.0) ||
                        (b.getBlockHardness(this, pos) <= algorithmSettings.getPickaxeSpeed() / 100.0))
        ) {
        } else if (algorithmSettings.getShovelSpeed() > 0
                && b.isToolEffective("shovel", iBlockState)
                && b.getBlockHardness(this, pos) <= algorithmSettings.getShovelSpeed()) {
        } else if (algorithmSettings.getAxeSpeed() > 0 && b.isToolEffective("axe", iBlockState) && b.getBlockHardness(this, pos) <= algorithmSettings.getAxeSpeed()) {
        } else {
            return algorithmSettings.getPickaxe().canHarvestBlock(b) ? 1 : 1;
        }
        return 0;
    }

    private DungeonRoom.CollisionState calculateIsBlocked(int x, int y, int z) {
//        if (x < minx || z < minz || x >= maxx || z >= maxz || y < miny || y+4 >= maxy) return CollisionState.BLOCKED;
        if (!canAccessRelative( (x ) / 2, (z ) / 2)) return DungeonRoom.CollisionState.BLOCKED;

        float wX = x / 2.0f, wY = y / 2.0f, wZ = z / 2.0f;
        float playerWidth = 0.25f;

        AxisAlignedBB bb = new AxisAlignedBB(wX - playerWidth, wY+0.06251, wZ - playerWidth,
                        wX + playerWidth, wY +0.06251 + 1.8, wZ + playerWidth);
        AxisAlignedBB pearlTest = new AxisAlignedBB(
                wX - 0.5, wY - 0.5, wZ - 0.5, wX + 0.5, wY + 0.5, wZ+0.5
        );

        int minX = MathHelper.floor_double(bb.minX);
        int maxX = MathHelper.floor_double(bb.maxX + 1.0D);
        int minY = MathHelper.floor_double(bb.minY);
        int maxY = MathHelper.floor_double(bb.maxY + 1.0D);
        int minZ = MathHelper.floor_double(bb.minZ);
        int maxZ = MathHelper.floor_double(bb.maxZ + 1.0D);

        AxisAlignedBB testBox = bb.offset(0, -0.5, 0);
        BlockPos.MutableBlockPos blockPos = new BlockPos.MutableBlockPos();
        List<AxisAlignedBB> list = new ArrayList<>();
        List<AxisAlignedBB> list2 = new ArrayList<>();
        int size = 0;

//        boolean
        boolean stairs = false;
        boolean superboom = false;
        boolean foundstairat = false;
        boolean slabTop = false;
        int notstonkable = 0;
        for (int k1 = minX; k1 < maxX; ++k1) {
            for (int l1 = minZ; l1 < maxZ; ++l1) {
                label: for (int i2 = minY-1; i2 < maxY; ++i2) {
                    blockPos.set(k1, i2, l1);


                    IBlockState state = getBlockState(blockPos);
                    Block block = state.getBlock();
                    block.addCollisionBoxesToList(
                            this, blockPos, state, testBox, list, null
                    );
                    block.addCollisionBoxesToList(
                            this, blockPos, state, bb, list2, null
                    );


                    if (list2.size() != size) {
                        // collision!!

                        if (poses.contains(blockPos)) {
                            for (int i = 0; i < Math.max(0, list2.size() - size); i++)
                                list2.remove(size);
                            superboom = true;
                            continue label;
                        }

                        int breakFactor = isNoInstaBreak(state, blockPos);
                        if (breakFactor > 0) {
                            if (i2 == maxY - 1 && (state.getBlock() != Blocks.iron_bars && !(state.getBlock() instanceof BlockFence)) && !(state.getBlock() instanceof BlockSkull)) {
                                // head level no break
                                notstonkable = 99;
                            } else {
                                notstonkable+= breakFactor;
                            }
                            if (state.getBlock() == Blocks.bedrock) {
                                notstonkable = 99;
                            }
                        }

                    }
                    size = list2.size();
                    if (block instanceof BlockStairs && i2 != minY - 1) {
                        stairs = true;
                    }
                    if (block instanceof BlockStairs && i2 == minY) {
                        foundstairat = true;
                        slabTop = state.getValue(BlockStairs.HALF) == BlockStairs.EnumHalf.TOP;
                    }
                }
            }
        }

        boolean isOnGround = false;
        for (AxisAlignedBB axisAlignedBB : list) {
            if (axisAlignedBB.maxY <= bb.minY) {
                isOnGround = true;
                break;
            }
        }
        boolean blocked = !list2.isEmpty();
        int headcut = 0, bodycut = 0;
        for (AxisAlignedBB axisAlignedBB : list2) {
            if (axisAlignedBB.minY >= wY + 0.9f && axisAlignedBB.minY <= wY + 1.4f) headcut++;
            if (axisAlignedBB.minY >= wY) bodycut++;
        }

        // weirdest thing ever check.
        list2.clear();
        size = 0;

        if (!blocked && (x%2 == 0) != (z%2 == 0) && y %2 == 0 && isOnGround) {
            boolean stairFloor = false;
            boolean elligible = false;
            label: for (int k1 = minX; k1 < maxX; ++k1) {
                for (int l1 = minZ; l1 < maxZ; ++l1) {
                    blockPos.set(k1, minY - 1, l1);

                    IBlockState state = this.getBlockState(blockPos);
                    Block block = state.getBlock();

                    block.addCollisionBoxesToList(
                            this, blockPos, state, testBox, list2, null
                    );
                    if (size != list2.size()) {
                        elligible = true;
                    } else if (block instanceof BlockStairs) {
                        stairFloor = true;
                    }
                    size = list2.size();


                    blockPos.set(k1, minY, l1);

                    state = this.getBlockState(blockPos);
                    block = state.getBlock();

                    if (block.canCollideCheck(state, true)) {
                        elligible = false;
                        break label;
                    }
                }
            }
            if (elligible && stairFloor) {
                return DungeonRoom.CollisionState.ENDERCHEST;
            }
        }

        if (!blocked) { // I'm on ground
            if (superboom) {
                if (isOnGround) {
                    return DungeonRoom.CollisionState.SUPERBOOMABLE_GROUND;
                } else {
                    return DungeonRoom.CollisionState.SUPERBOOMABLE_AIR;
                }
            }
            if (stairs && isOnGround) {
                return DungeonRoom.CollisionState.STAIR;
            }

            if (isOnGround) {
                return DungeonRoom.CollisionState.ONGROUND;
            } else {
                return DungeonRoom.CollisionState.ONAIR;
            }
        } else {


            // from here, blocked = true.
            if (notstonkable > 2) {
                if (!isOnGround) {
                    return DungeonRoom.CollisionState.BLOCKED;
                } else {
                    return DungeonRoom.CollisionState.BLOCKED_GROUND;
                }
            }

            if (!isOnGround) {
                return DungeonRoom.CollisionState.STONKING_AIR;
            } else {
                return DungeonRoom.CollisionState.STONKING;
            }
        }
    }

    private DungeonRoom.PearlLandType calculateCanPearl(int x, int y, int z) {
        if (!canAccessRelative( (x ) / 2, (z ) / 2)) return DungeonRoom.PearlLandType.BLOCKED;

        float wX = x / 2.0f, wY = y / 2.0f, wZ = z / 2.0f;

        AxisAlignedBB pearlTest = new AxisAlignedBB(
                wX-0.3, wY-0.3, wZ-0.3, wX+ 0.3, wY+ 0.3, wZ + 0.3
        );

        int minX = MathHelper.floor_double(pearlTest.minX);
        int maxX = MathHelper.floor_double(pearlTest.maxX + 1.0D);
        int minY = MathHelper.floor_double(pearlTest.minY);
        int maxY = MathHelper.floor_double(pearlTest.maxY + 1.0D);
        int minZ = MathHelper.floor_double(pearlTest.minZ);
        int maxZ = MathHelper.floor_double(pearlTest.maxZ + 1.0D);

        BlockPos.MutableBlockPos blockPos = new BlockPos.MutableBlockPos();
        List<AxisAlignedBB> pearlList = new ArrayList<>();
        for (int k1 = minX; k1 < maxX; ++k1) {
            for (int l1 = minZ; l1 < maxZ; ++l1) {
                label: for (int i2 = minY-1; i2 < maxY; ++i2) {
                    blockPos.set(k1, i2, l1);


                    IBlockState state = this.getBlockState(blockPos);
                    Block block = state.getBlock();
                    block.addCollisionBoxesToList(
                            this, blockPos, state, pearlTest, pearlList, null
                    );
                }
            }
        }
        if (pearlList.isEmpty()) return DungeonRoom.PearlLandType.OPEN;
        double wholeVolume = 0;
        double topVolume = 0;
        for (AxisAlignedBB a : pearlList) {
            double miX = Math.max(a.minX, pearlTest.minX);
            double miY = Math.max(a.minY, pearlTest.minY);
            double miZ = Math.max(a.minZ, pearlTest.minZ);
            double maX = Math.min(a.maxX, pearlTest.maxX);
            double maY = Math.min(a.maxY, pearlTest.maxY);
            double maZ = Math.min(a.maxZ, pearlTest.maxZ);
            wholeVolume += (maX - miX) * (maY - miY) * (maZ - miZ);
            miY = Math.max(a.minY, pearlTest.minY+0.3);
            if (miY > maY) continue;
            topVolume += (maX - miX) * (maY - miY) * (maZ - miZ);
        }
        // total is 0.216
        if (wholeVolume > 0.215) return DungeonRoom.PearlLandType.BLOCKED;
        if (wholeVolume > 0.027 && 0 == topVolume) return DungeonRoom.PearlLandType.FLOOR;
        if (wholeVolume  == topVolume && wholeVolume > 0.027) return DungeonRoom.PearlLandType.CEILING;
        // floor wall and ceiling wall.
        if (wholeVolume - topVolume > 0.027 && topVolume > 0 && wholeVolume != topVolume * 2) return DungeonRoom.PearlLandType.FLOOR_WALL;
        if (wholeVolume > 0) return DungeonRoom.PearlLandType.WALL;
        return DungeonRoom.PearlLandType.OPEN;
    }

    @Override
    public IBlockState getActualBlock(int x, int y, int z) {
        return getBlockState(new BlockPos(x,y,z));
    }


    public DungeonRoom.CollisionState getBlock(int x, int y, int z) {

        if (x < 2 || z < 2 || x >= 2 + getXwidth()|| z >= 2 + getZwidth() || y < 0 || y >= 512) return DungeonRoom.CollisionState.BLOCKED;

        if (!canAccessRelative( (x ) / 2, (z ) / 2)) return DungeonRoom.CollisionState.BLOCKED;
        int dx = x - 2, dy = y, dz = z - 2;
        int data = whole.read(dx, dy, dz);
        if (data != 0) return DungeonRoom.CollisionState.VALUES[data];
        DungeonRoom.CollisionState val = calculateIsBlocked(x, y, z);
        whole.store(dx,dy,dz, val.ordinal());
        return val;
    }
    public DungeonRoom.PearlLandType getPearl(int x, int y, int z) {
        if (x < 2 || z < 2 || x >= 2 + getXwidth()|| z >= 2 + getZwidth() || y < 0 || y >= 512) return DungeonRoom.PearlLandType.BLOCKED;
        int dx = x - 2, dy = y, dz = z - 2;
        int data = enderpearl.read(dx, dy, dz);
        if (data != 0) return DungeonRoom.PearlLandType.VALUES[data];
        DungeonRoom.PearlLandType val = calculateCanPearl(x, y, z);
        enderpearl.store(dx,dy,dz, val.ordinal());
        return val;
    }

    @Override
    public boolean isInstabreak(int x, int y, int z) {
        if (!canAccessRelative( (x ) / 2, (z ) / 2)) return false;
        if (x%2 != 0 && z%2 != 0) return false;

        BlockPos pos = new BlockPos(x/2,y/2,z/2);
        IBlockState blockState = this.getBlockState(pos);
        return isNoInstaBreak(blockState, pos) == 0;
    }

    @Override
    public int getXwidth() {
        return dungeonRoomInfo.getWidth() * 2;
    }

    @Override
    public int getYwidth() {
        return 512;
    }

    @Override
    public int getZwidth() {
        return dungeonRoomInfo.getLength() * 2;
    }

    @Override
    public int getMinX() {
        return 2;
    }

    @Override
    public int getMinY() {
        return 2;
    }

    @Override
    public int getMinZ() {
        return 2;
    }
}
