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

package kr.syeyoung.dungeonsguide.mod.dungeon.actions;

import kr.syeyoung.dungeonsguide.dungeon.data.DungeonRoomInfo;
import kr.syeyoung.dungeonsguide.dungeon.data.OffsetPoint;
import kr.syeyoung.dungeonsguide.mod.dungeon.pathfinding.NodeProcessorDungeonRoom;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomfinder.DungeonRoom;
import kr.syeyoung.dungeonsguide.mod.utils.VectorUtils;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.minecraft.block.*;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;
import net.minecraft.world.WorldProviderSurface;
import net.minecraft.world.chunk.IChunkProvider;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import javax.vecmath.Vector2d;
import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

public class RaytraceHelper {
    private static class DRIWorld extends World {

        private DungeonRoomInfo dungeonRoomInfo;
        protected DRIWorld(DungeonRoomInfo dungeonRoomInfo) {
            super(null, null, new WorldProviderSurface(), null, true);
            this.dungeonRoomInfo =  dungeonRoomInfo;
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
            return dungeonRoomInfo.getBlock(pos.getX(), pos.getY(), pos.getZ(),0);
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


    public static Vec3 interpolate(AxisAlignedBB axisAlignedBB, double x, double y, double z) {
        return new Vec3(
                x * (axisAlignedBB.maxX - axisAlignedBB.minX) + axisAlignedBB.minX,
                y * (axisAlignedBB.maxY - axisAlignedBB.minY) + axisAlignedBB.minY,
                z * (axisAlignedBB.maxZ - axisAlignedBB.minZ) + axisAlignedBB.minZ
        );
    }

    public static interface CalculateIsBlocked {
        DungeonRoom.NodeState calculateIsBlocked(int x, int y, int z);
    }
    public static List<PossibleClickingSpot> raycast(World w, BlockPos target) {
        return raycast(w, target, (a,b,c) -> RaytraceHelper.calculateIsBlocked(w,a,b,c));
    }
    public static List<PossibleClickingSpot> raycast(World w, BlockPos target, CalculateIsBlocked calculateIsBlocked) {
        IBlockState targetBlockState = w.getBlockState(target);
        AxisAlignedBB bb = targetBlockState.getBlock().getSelectedBoundingBox(w, target);

        Map<Vec3, RequiredTool[]> actualReq = new HashMap<>();
        Map<Vec3, Boolean> stonk = new HashMap<>();

        for (double x = target.getX() - 4.5; x <= target.getX() + 5.5; x += 0.5) {
            for (double y = target.getY() - 6; y <= target.getY() + 4.5; y += 0.5) {
                for (double z = target.getZ() - 4.5; z <= target.getZ() + 5.5; z += 0.5) {
                    // if can't stand on, we don't.
                    if (calculateIsBlocked.calculateIsBlocked((int) Math.round(x * 2), (int) Math.round(y * 2), (int) Math.round(z * 2)).isBlockedNonStonk()) continue;
//                    if (!calculateIsBlocked.calculateIsBlocked((int) Math.round(x * 2), (int) Math.round(y * 2)-1, (int) Math.round(z * 2)).isBlockedNonStonk()
//                            && y > target.getY() - 5.9) continue;

                    boolean isAir = !calculateIsBlocked.calculateIsBlocked((int) Math.round(x * 2), (int) Math.round(y * 2)-1, (int) Math.round(z * 2)).isBlockedNonStonk();

                    Vec3 playerFoot = new Vec3(x, y, z);
                    for (int shift = 0; shift <= 1; shift++) {
                        Vec3 eye = playerFoot.addVector(0, 1.62F  - shift * 0.08F, 0); // assume sneaking lol
                        for (int ix = 0; ix <= 1; ix++) {
                            for (int iy = 0; iy <= 1; iy++) {
                                for (int iz = 0; iz <= 1; iz++) {
                                    Vec3 to = interpolate(bb, ix * 0.9 + 0.05, iy * 0.9 + 0.05, iz * 0.9 + 0.05);

                                    if (to.squareDistanceTo(eye) > 4.5 * 4.5) {
                                        to = to.subtract(eye).normalize();
                                        to = eye.addVector(to.xCoord * 4.5, to.yCoord * 4.5, to.zCoord * 4.5);
                                    }
                                    // 2 * 2 * 2 * 20 * 20 * 20 = 64k collision checks.
                                    List<BlockPos> blocks = rayTraceBlocks(w, eye, to);

                                    if (blocks.size() == 0) continue;
                                    if (!blocks.get(blocks.size() - 1).equals(target)) continue;

                                    RequiredTool[] requiredTools = new RequiredTool[3];
                                    // 0 pick 1 shovel 2 axe
                                    boolean imposs = false;
                                    boolean stonka = blocks.lastIndexOf(target) == 0;
                                    int until = stonka ? 0 : blocks.lastIndexOf(blocks.get(blocks.lastIndexOf(target) - 1));

                                    if (!stonka && !isAir) {
                                        BlockPos pos = blocks.get(until);
                                        IBlockState from_state = w.getBlockState(pos);
                                        Block from_block = from_state.getBlock();

                                        BlockBreakData breakData = new BlockBreakData(
                                                (isAir ? 5 : 1) * from_block.getBlockHardness(w, pos),
                                                from_block.getHarvestLevel(from_state),
                                                from_block.getHarvestTool(from_state)
                                        );

                                        if (breakData.hardness < 0) {
                                            imposs = true;
                                            break;
                                        }

                                        int idx = 0;
                                        if ("pickaxe".equals(breakData.toolClass)) {
                                            idx = 0;
                                        } else if ("axe".equals(breakData.toolClass)) {
                                            idx = 2;
                                        } else if ("shovel".equals(breakData.toolClass)) {
                                            idx = 1;
                                        }

                                        requiredTools[idx] = new RequiredTool(
                                                0, 0
                                        );

                                    }
                                    if (isAir) {
                                        until = blocks.lastIndexOf(target);
                                    }

                                    for (int i = 0; i < until; i++) { // last two one doesn't matter!
                                        BlockPos from_bpos = blocks.get(i);
                                        if (from_bpos.equals(target)) continue;
                                        if (imposs) break;
                                        IBlockState from_state = w.getBlockState(from_bpos);
                                        Block from_block = from_state.getBlock();

                                        BlockBreakData breakData = new BlockBreakData(
                                                (isAir ? 5 : 1) * from_block.getBlockHardness(w, from_bpos),
                                                from_block.getHarvestLevel(from_state),
                                                from_block.getHarvestTool(from_state)
                                        );
                                        if (breakData.hardness < 0) {
                                            imposs = true;
                                            break;
                                        }

                                        int idx = 0;
                                        if ("pickaxe".equals(breakData.toolClass)) {
                                            idx = 0;
                                        } else if ("axe".equals(breakData.toolClass)) {
                                            idx = 2;
                                        } else if ("shovel".equals(breakData.toolClass)) {
                                            idx = 1;
                                        }

                                        if (requiredTools[idx] == null) requiredTools[idx] = new RequiredTool();
                                        if (requiredTools[idx].breakingPower < breakData.hardness)
                                            requiredTools[idx].breakingPower = breakData.hardness;
                                        if (requiredTools[idx].harvestLv < breakData.harvestLv)
                                            requiredTools[idx].harvestLv = breakData.harvestLv;
                                    }
                                    if (imposs) continue;

                                    if (actualReq.get(playerFoot) == null)
                                        actualReq.put(playerFoot, new RequiredTool[]{
                                                new RequiredTool(Float.MAX_VALUE, 99),
                                                new RequiredTool(Float.MAX_VALUE, 99),
                                                new RequiredTool(Float.MAX_VALUE, 99)
                                        });

                                    RequiredTool[] prev = actualReq.get(playerFoot);

                                    boolean swap = false;
                                    for (int i = 0; i < prev.length; i++) {
                                        RequiredTool prevTool = prev[i];
                                        RequiredTool newTool = requiredTools[i];
                                        if (prevTool == null) {
                                            continue;
                                        }
                                        if (newTool == null) {
                                            swap = true;
                                            break;
                                        }

                                        if (newTool.harvestLv < prevTool.harvestLv) {
                                            swap = true;
                                            break;
                                        }
                                        if (newTool.breakingPower < prevTool.breakingPower) {
                                            swap = true;
                                            break;
                                        }
                                    }
                                    if (swap) {
                                        actualReq.put(
                                                playerFoot, requiredTools
                                        );
                                        stonk.put(
                                                playerFoot, stonka
                                        );
                                    }
                                }

                            }
                        }
                    }
                }
            }
        }

        List<PossibleClickingSpot> spots = actualReq.entrySet().stream()
                .collect(Collectors.<Map.Entry<Vec3, RequiredTool[]>, String>groupingBy(a -> {
                    return Arrays.stream(a.getValue())
                            .map(b -> b == null ? "n" : b.breakingPower + ":" + b.harvestLv).collect(Collectors.joining(";"))+";"+stonk.get(a.getKey());
                })).values().stream()
                .map(entries -> {
                    return new PossibleClickingSpot(
                            entries.get(0).getValue(),
                            entries.stream().map(Map.Entry::getKey)
                                    .map(b -> new OffsetVec3(b.xCoord, b.yCoord, b.zCoord))
                                    .collect(Collectors.toList()),
                            stonk.get(entries.get(0).getKey()), 0
                    );
                }).collect(Collectors.toList());
        return chooseMinimalY(doClustering(spots));
    }

    public static List<PossibleClickingSpot> chooseMinimalY(List<PossibleClickingSpot> spots) {
        Map<OffsetVec3, PossibleClickingSpot> clusterMap = new HashMap<>();

        for (PossibleClickingSpot spot : spots) {
            for (OffsetVec3 offsetVec3 : spot.getOffsetPointSet()) {
                clusterMap.put(offsetVec3, spot);
            }
        }

        return clusterMap.keySet().stream()
                .collect(Collectors.groupingBy(a -> new ImmutablePair<>(a.getX(), a.getZ())))
                .values().stream()
                    .map(a -> a.stream().min(Comparator.comparingDouble(OffsetVec3::getY)).orElse(null))
                .collect(Collectors.groupingBy(clusterMap::get))
                .entrySet().stream().map(
                        a -> new PossibleClickingSpot(
                                a.getKey().getTools(),
                                a.getValue(),
                                a.getKey().stonkingReq,
                                a.getKey().clusterId
                        )
                ).collect(Collectors.toList());
    }
    public static List<PossibleClickingSpot> doClustering(List<PossibleClickingSpot> spots) {
        Map<OffsetVec3, PossibleClickingSpot> clusterMap = new HashMap<>();
        Map<OffsetVec3, Integer> clusterId = new HashMap<>();

        for (PossibleClickingSpot spot : spots) {
            for (OffsetVec3 offsetVec3 : spot.getOffsetPointSet()) {
                clusterId.put(offsetVec3, -1);
                clusterMap.put(offsetVec3, spot);
            }
        }
        int lastId = 0;

        for (OffsetVec3 offsetVec3 : clusterId.keySet()) {
            int id = clusterId.get(offsetVec3);
            if (id != -1) continue;
            id = ++lastId;

            Queue<OffsetVec3> toVisit = new LinkedList<>();
            toVisit.add(offsetVec3);
            while (!toVisit.isEmpty()) {
                OffsetVec3 vec3 = toVisit.poll();
                if (clusterId.get(vec3) != -1) continue;
                clusterId.put(vec3, id);

                for (EnumFacing value : EnumFacing.VALUES) {
                    OffsetVec3 newVec3 = new OffsetVec3(
                            vec3.x + value.getFrontOffsetX() * 0.5,
                            vec3.y + value.getFrontOffsetY() * 0.5,
                            vec3.z + value.getFrontOffsetZ() * 0.5
                    );
                    if (!clusterId.containsKey(newVec3)) continue;
                    toVisit.add(newVec3);
                }
            }
        }

        return clusterMap.keySet().stream()
                .collect(Collectors.groupingBy(a -> {
                    return new ImmutablePair<>(clusterId.get(a), clusterMap.get(a));
                })).entrySet().stream().map(
                        a -> new PossibleClickingSpot(
                                a.getKey().getRight().getTools(),
                                a.getValue(),
                                a.getKey().getRight().stonkingReq,
                                a.getKey().getLeft()
                        )
                ).collect(Collectors.toList());
    }

    @AllArgsConstructor @Getter
    private static class BlockBreakData {
        private float hardness;
        private int harvestLv;
        private String toolClass;
    }


    private static List<BlockPos> rayTraceBlocks(World driWorld, Vec3 from, Vec3 to) {
        List<BlockPos> blocks = new ArrayList<>();
        if (!Double.isNaN(from.xCoord) && !Double.isNaN(from.yCoord) && !Double.isNaN(from.zCoord)) {
            if (!Double.isNaN(to.xCoord) && !Double.isNaN(to.yCoord) && !Double.isNaN(to.zCoord)) {
                int to_x_floor = MathHelper.floor_double(to.xCoord);
                int to_y_floor = MathHelper.floor_double(to.yCoord);
                int to_z_floor = MathHelper.floor_double(to.zCoord);
                int from_x_floor = MathHelper.floor_double(from.xCoord);
                int from_y_floor = MathHelper.floor_double(from.yCoord);
                int from_z_floor = MathHelper.floor_double(from.zCoord);


                BlockPos from_bpos = new BlockPos(from_x_floor, from_y_floor, from_z_floor);
                {
                    IBlockState from_state = driWorld.getBlockState(from_bpos);
                    Block from_block = from_state.getBlock();
                    if (from_block.canCollideCheck(from_state, false)) {
                        MovingObjectPosition movingobjectposition2 = from_block.collisionRayTrace(driWorld, from_bpos, from, to);
                        if (movingobjectposition2 != null) {
                            blocks.add(from_bpos);
                        }
                    }
                }
                MovingObjectPosition movingobjectposition2 = null;
                int k1 = 200;

                while(k1-- >= 0) {
                    if (Double.isNaN(from.xCoord) || Double.isNaN(from.yCoord) || Double.isNaN(from.zCoord)) {
                        return blocks;
                    }

                    if (from_x_floor == to_x_floor && from_y_floor == to_y_floor && from_z_floor == to_z_floor) {
                        return blocks;
                    }

                    boolean x_equal = true;
                    boolean y_equal = true;
                    boolean z_equal = true;
                    double curr_x = 999.0;
                    double curr_y = 999.0;
                    double curr_z = 999.0;
                    if (to_x_floor > from_x_floor) {
                        curr_x = (double)from_x_floor + 1.0;
                    } else if (to_x_floor < from_x_floor) {
                        curr_x = (double)from_x_floor + 0.0;
                    } else {
                        x_equal = false;
                    }

                    if (to_y_floor > from_y_floor) {
                        curr_y = (double)from_y_floor + 1.0;
                    } else if (to_y_floor < from_y_floor) {
                        curr_y = (double)from_y_floor + 0.0;
                    } else {
                        y_equal = false;
                    }

                    if (to_z_floor > from_z_floor) {
                        curr_z = (double)from_z_floor + 1.0;
                    } else if (to_z_floor < from_z_floor) {
                        curr_z = (double)from_z_floor + 0.0;
                    } else {
                        z_equal = false;
                    }

                    double perc_x = 999.0;
                    double perc_y = 999.0;
                    double perc_z = 999.0;
                    double diff_x = to.xCoord - from.xCoord;
                    double diff_y = to.yCoord - from.yCoord;
                    double diff_z = to.zCoord - from.zCoord;
                    if (x_equal) {
                        perc_x = (curr_x - from.xCoord) / diff_x;
                    }

                    if (y_equal) {
                        perc_y = (curr_y - from.yCoord) / diff_y;
                    }

                    if (z_equal) {
                        perc_z = (curr_z - from.zCoord) / diff_z;
                    }

                    if (perc_x == -0.0) {
                        perc_x = -1.0E-4;
                    }

                    if (perc_y == -0.0) {
                        perc_y = -1.0E-4;
                    }

                    if (perc_z == -0.0) {
                        perc_z = -1.0E-4;
                    }

                    EnumFacing overwhat;
                    if (perc_x < perc_y && perc_x < perc_z) {
                        overwhat = to_x_floor > from_x_floor ? EnumFacing.WEST : EnumFacing.EAST;
                        from = new Vec3(curr_x, from.yCoord + diff_y * perc_x, from.zCoord + diff_z * perc_x);
                    } else if (perc_y < perc_z) {
                        overwhat = to_y_floor > from_y_floor ? EnumFacing.DOWN : EnumFacing.UP;
                        from = new Vec3(from.xCoord + diff_x * perc_y, curr_y, from.zCoord + diff_z * perc_y);
                    } else {
                        overwhat = to_z_floor > from_z_floor ? EnumFacing.NORTH : EnumFacing.SOUTH;
                        from = new Vec3(from.xCoord + diff_x * perc_z, from.yCoord + diff_y * perc_z, curr_z);
                    }

                    from_x_floor = MathHelper.floor_double(from.xCoord) - (overwhat == EnumFacing.EAST ? 1 : 0);
                    from_y_floor = MathHelper.floor_double(from.yCoord) - (overwhat == EnumFacing.UP ? 1 : 0);
                    from_z_floor = MathHelper.floor_double(from.zCoord) - (overwhat == EnumFacing.SOUTH ? 1 : 0);

                    from_bpos = new BlockPos(from_x_floor, from_y_floor, from_z_floor);
                    IBlockState iblockstate1 = driWorld.getBlockState(from_bpos);
                    Block block1 = iblockstate1.getBlock();
                        if (block1.canCollideCheck(iblockstate1, false)) {
                            MovingObjectPosition movingobjectposition1 = block1.collisionRayTrace(driWorld, from_bpos, from, to);
                            if (movingobjectposition1 != null) {
                                blocks.add(from_bpos);
                            }
                        }
                }
            }
        }
        return blocks;

    }

    @AllArgsConstructor @NoArgsConstructor @Getter
    public static class RequiredTool {
        private float breakingPower;
        private int harvestLv;
    }
    @AllArgsConstructor @Getter
    public static class PossibleClickingSpot {
        private RequiredTool[] tools;
        private List<OffsetVec3> offsetPointSet;
        private boolean stonkingReq;
        private int clusterId;
    }

    @Data
    @AllArgsConstructor
    public static class OffsetVec3 implements Cloneable, Serializable {
        private static final long serialVersionUID = 3102336358774967540L;

        private double x;
        private double y;
        private double z;

        public OffsetVec3(DungeonRoom dungeonRoom, Vec3 pos) {
            setPosInWorld(dungeonRoom, pos);
        }


        public void setPosInWorld(DungeonRoom dungeonRoom, Vec3 pos) {
            Vector2d vector2d = new Vector2d(pos.xCoord - dungeonRoom.getMin().getX(), pos.zCoord - dungeonRoom.getMin().getZ());
            for (int i = 0; i < dungeonRoom.getRoomMatcher().getRotation(); i++) {
                vector2d = VectorUtils.rotateClockwise(vector2d);
                if (i % 2 == 0) {
                    vector2d.x += dungeonRoom.getDungeonRoomInfo().getBlocks()[0].length - 1; // + Z
                } else {
                    vector2d.x += dungeonRoom.getDungeonRoomInfo().getBlocks().length - 1; // + X
                }
            }

            this.x =  vector2d.x;
            this.z =  vector2d.y;
            this.y = pos.yCoord-dungeonRoom.getMin().getY();
        }

        public BlockPos toRotatedRelBlockPos(DungeonRoom dungeonRoom) {
            Vector2d rot = new Vector2d(x,z);
            for (int i = 0; i < dungeonRoom.getRoomMatcher().getRotation(); i++) {
                rot = VectorUtils.rotateCounterClockwise(rot);
                if (i % 2 == 0) {
                    rot.y += dungeonRoom.getMax().getZ() - dungeonRoom.getMin().getZ() + 1; // + Z
                } else {
                    rot.y += dungeonRoom.getMax().getX() - dungeonRoom.getMin().getX() + 1; // + X
                }
            }

            return new BlockPos(rot.x, y, rot.y);
        }

        public Block getBlock(DungeonRoom dungeonRoom) {
            BlockPos relBp = toRotatedRelBlockPos(dungeonRoom);

            return dungeonRoom.getRelativeBlockAt(relBp.getX(), relBp.getY(), relBp.getZ());
        }
        public BlockPos getBlockPos(DungeonRoom dungeonRoom) {
            BlockPos relBp = toRotatedRelBlockPos(dungeonRoom);
            return dungeonRoom.getRelativeBlockPosAt(relBp.getX(), relBp.getY(), relBp.getZ());
        }

        public int getData(DungeonRoom dungeonRoom) {
            BlockPos relBp = toRotatedRelBlockPos(dungeonRoom);

            return dungeonRoom.getRelativeBlockDataAt(relBp.getX(), relBp.getY(), relBp.getZ());
        }

        @Override
        public Object clone() throws CloneNotSupportedException {
            return new OffsetVec3(x,y,z);
        }

        @Override
        public String toString() {
            return "OffsetPoint{x=" + x +
                    ", y=" + y +
                    ", z=" + z +
                    '}';
        }
    }


    private static DungeonRoom.LayerNodeState calculateOneLayerIsBlocked(World world,int x, int y, int z) {
//        if (x < minx || z < minz || x >= maxx || z >= maxz || y < miny || y >= maxy) return DungeonRoom.LayerNodeState.OUT_OF_DUNGEON;
        float wX = x / 2.0f, wY = y / 2.0f, wZ = z / 2.0f;
        double playerWidth = 0.3f;

        AxisAlignedBB bb = AxisAlignedBB.fromBounds(wX - playerWidth, wY+0.06251, wZ - playerWidth, wX + playerWidth, wY + .49f, wZ + playerWidth);

        int minX = MathHelper.floor_double(bb.minX);
        int maxX = MathHelper.floor_double(bb.maxX + 1.0D);
        int minY = MathHelper.floor_double(bb.minY);
        int maxY = MathHelper.floor_double(bb.maxY + 1.0D);
        int minZ = MathHelper.floor_double(bb.minZ);
        int maxZ = MathHelper.floor_double(bb.maxZ + 1.0D);
        BlockPos.MutableBlockPos blockPos = new BlockPos.MutableBlockPos();

        List<AxisAlignedBB> list = new ArrayList<>();
        int blocked = 0;

        int stairValid = 0;
        int fence = 0;
        boolean missedStair = false;
        for (int k1 = minX; k1 < maxX; ++k1) {
            for (int l1 = minZ; l1 < maxZ; ++l1) {
                for (int i2 = minY-1; i2 < maxY; ++i2) {
                    boolean blocked2 = false;
                    blockPos.set(k1, i2, l1);
                    IBlockState iBlockState1 = world.getBlockState(blockPos);
                    Block b = iBlockState1.getBlock();
                    if (!b.getMaterial().blocksMovement())continue;
                    if (!(b instanceof BlockWall || b instanceof BlockFence || b instanceof BlockFenceGate) && i2 == minY-1) continue;
                    if (iBlockState1.equals( NodeProcessorDungeonRoom.preBuilt)) continue;


                    if (b.isFullCube()) {
                        blocked2 = true;

                        if (b.getBlockHardness(world, blockPos) < 0) {
                            return DungeonRoom.LayerNodeState.FORBIDDEN;
                        }
                    }

                    if (b instanceof BlockStairs) {
                        if (iBlockState1.getValue(BlockStairs.HALF) == BlockStairs.EnumHalf.BOTTOM  && y % 2 == 0 && (stairValid >= 0)) {
                            stairValid = 1;
                        } else if (iBlockState1.getValue(BlockStairs.HALF) == BlockStairs.EnumHalf.TOP && y % 2 == 1 && (stairValid >= 0)) {
                            stairValid = 1;
                        } else {
                            stairValid = -1;
                        }
                    }

                    try {
                        int prev = list.size();
                        b.addCollisionBoxesToList(world, blockPos, iBlockState1, bb, list, null);
                        if (list.size() - prev > 0) {
                            blocked2 = true;
                        }

                        if (b instanceof BlockStairs && !blocked2) {
                            missedStair = true;
                        } else if (b instanceof BlockStairs) {
                            missedStair = false;
                        }
                    } catch (Exception e) {
                        blocked2 = true;
                    }


                    if (blocked2) {
                        blocked++;
                    }
                    if (blocked2 && i2 == minY - 1) {
                        fence++;
                    }
                }
            }
        }

        if (blocked > 0) {
            if (stairValid == 1 && fence == 0) {
                return DungeonRoom.LayerNodeState.ENTRANCE_STAIR_STONK;
            }

            if (missedStair)
                return DungeonRoom.LayerNodeState.STONKABLE_STAIR_MID;
            else
                return DungeonRoom.LayerNodeState.STONKABLE;
        }


        return DungeonRoom.LayerNodeState.OPEN;
    }

    private static DungeonRoom.NodeState calculateIsBlocked(World world, int x, int y, int z) {
//        if (x < minx || z < minz || x >= maxx || z >= maxz || y < miny || y+4 >= maxy) return DungeonRoom.NodeState.OUT_OF_DUNGEON;

        DungeonRoom.LayerNodeState bottom = calculateOneLayerIsBlocked(world, x,y,z);
        DungeonRoom.LayerNodeState bottomMid = calculateOneLayerIsBlocked(world, x,y+1,z);
        DungeonRoom.LayerNodeState top= calculateOneLayerIsBlocked(world, x,y+2,z);
        DungeonRoom.LayerNodeState topMid = calculateOneLayerIsBlocked(world, x,y+3,z);


        int openCount = 0;


        if (bottom == DungeonRoom.LayerNodeState.OPEN) openCount++;
        if (bottomMid == DungeonRoom.LayerNodeState.OPEN) openCount++;
        if (top == DungeonRoom.LayerNodeState.OPEN) openCount++;
        if (topMid == DungeonRoom.LayerNodeState.OPEN) openCount++;

        boolean falls = calculateOneLayerIsBlocked(world, x, y-1, z) == DungeonRoom.LayerNodeState.OPEN;
        boolean highCeiling = calculateOneLayerIsBlocked(world, x, y+4, z) == DungeonRoom.LayerNodeState.OPEN;


        if (openCount == 4) {
            return DungeonRoom.NodeState.OPEN;
        }
        if (bottom == DungeonRoom.LayerNodeState.FORBIDDEN || bottomMid == DungeonRoom.LayerNodeState.FORBIDDEN || top == DungeonRoom.LayerNodeState.FORBIDDEN || topMid == DungeonRoom.LayerNodeState.FORBIDDEN) {
            return DungeonRoom.NodeState.BLOCKED;
        }
        if (!topMid.isInstabreak()) {
            // if top mid is blocked, then player can't go anywhere, unless, falling...
            return DungeonRoom.NodeState.BLOCKED;

        }


        if (y % 2 == 0 && bottom.isStair() && openCount == 3 && highCeiling) {
            if (falls) return DungeonRoom.NodeState.ENTRANCE_STONK_DOWN_FALLING;
            return DungeonRoom.NodeState.ENTRANCE_STONK_DOWN;
        }
        if (((x % 2 == 0) != (z % 2 == 0)) && y % 2 == 1 && calculateOneLayerIsBlocked(world, x, y-1, z).isStair() &&
                (bottom == DungeonRoom.LayerNodeState.BLOCKED_ONE_STONK_STAIR_MID || bottom == DungeonRoom.LayerNodeState.STONKABLE_STAIR_MID) && openCount == 3 && highCeiling) {
            return DungeonRoom.NodeState.ENTRANCE_STONK_DOWN_ECHEST;
        }

        if (y % 2 != 0 && topMid.isStair() && openCount == 3 && falls) {
            return DungeonRoom.NodeState.ENTRANCE_STONK_UP;
        }

        // wall
        if (x % 2 != 0 && z % 2 != 0 && y % 2 == 0 && bottom.isBlocked() && openCount == 3 && highCeiling) {
            IBlockState iBlockState1 = world.getBlockState(new BlockPos(x/2.0, y/2-1, z/2.0));
            Block b = iBlockState1.getBlock();
            if (b instanceof BlockWall || b instanceof BlockFence || b instanceof BlockFenceGate) {
                iBlockState1 = world.getBlockState(new BlockPos(x/2.0, y/2, z/2.0));
                b = iBlockState1.getBlock();
                if (b == Blocks.air) {
                    return DungeonRoom.NodeState.ENTRANCE_TELEPORT_DOWN;
                }
            }
        }

        if (x % 2 != 0 && z % 2 != 0 && y % 2 == 0 && openCount == 1 && topMid == DungeonRoom.LayerNodeState.OPEN) {
            IBlockState iBlockState1 = world.getBlockState(new BlockPos(x/2.0, y/2, z/2.0));
            Block b = iBlockState1.getBlock();
            if (b instanceof BlockWall || b instanceof BlockFence || b instanceof BlockFenceGate) {
                iBlockState1 = world.getBlockState(new BlockPos(x/2.0, y/2+1, z/2.0));
                b = iBlockState1.getBlock();
                if (!b.getMaterial().blocksMovement()) {
                    iBlockState1 = world.getBlockState(new BlockPos(x/2.0, y/2+2, z/2.0));
                    b = iBlockState1.getBlock();
                    if (!b.getMaterial().blocksMovement()) {
                        if (falls) return DungeonRoom.NodeState.ENTRANCE_ETHERWARP_FALL;
                        return DungeonRoom.NodeState.ENTRANCE_ETHERWARP;
                    }
                }
            }
        }

        if (x%2 == 0 && z%2 == 0) {
            return DungeonRoom.NodeState.BLOCKED; // never go corners while stonking..
        }


        if (x % 2 != 0 && z % 2 != 0 && y % 2 == 0) {
            IBlockState iBlockState1 = world.getBlockState(new BlockPos(x/2.0, y/2-1, z/2.0));
            Block b = iBlockState1.getBlock();
            if (b instanceof BlockWall || b instanceof BlockFence || b instanceof BlockFenceGate) {
                falls = true;
            }
        }

        if (y % 2 == 0 && falls) return DungeonRoom.NodeState.BLOCKED_STONKABLE_FALLING;
        if (y % 2 == 1 && bottom != DungeonRoom.LayerNodeState.OPEN || falls) return DungeonRoom.NodeState.BLOCKED_STONKABLE_FALLING;
        return DungeonRoom.NodeState.BLOCKED_STONKABLE;
    }

}
