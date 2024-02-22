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

import com.google.common.collect.Sets;
import kr.syeyoung.dungeonsguide.dungeon.data.*;
import kr.syeyoung.dungeonsguide.dungeon.mechanics.dunegonmechanic.RouteBlocker;
import kr.syeyoung.dungeonsguide.mod.dungeon.pathfinding.NodeProcessorDungeonRoom;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomfinder.DungeonRoom;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;
import net.minecraft.world.WorldProviderSurface;
import net.minecraft.world.chunk.IChunkProvider;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.ImmutableTriple;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class RaytraceHelper {

    public static Vec3 interpolate(AxisAlignedBB axisAlignedBB, double x, double y, double z) {
        return new Vec3(
                x * (axisAlignedBB.maxX - axisAlignedBB.minX) + axisAlignedBB.minX,
                y * (axisAlignedBB.maxY - axisAlignedBB.minY) + axisAlignedBB.minY,
                z * (axisAlignedBB.maxZ - axisAlignedBB.minZ) + axisAlignedBB.minZ
        );
    }

    public static interface CalculateIsBlocked {
        boolean canStand(int x, int y, int z);
    }
    public static List<PossibleClickingSpot> raycast(World w, BlockPos target) {
        return raycast(w, target, (a,b,c) -> RaytraceHelper.canStand(w,a,b,c));
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
                    if (!calculateIsBlocked.canStand((int) Math.round(x * 2), (int) Math.round(y * 2), (int) Math.round(z * 2))) continue;
//                    if (!calculateIsBlocked.calculateIsBlocked((int) Math.round(x * 2), (int) Math.round(y * 2)-1, (int) Math.round(z * 2)).isBlockedNonStonk()
//                            && y > target.getY() - 5.9) continue;

                    boolean isAir = calculateIsBlocked.canStand((int) Math.round(x * 2), (int) Math.round(y * 2)-1, (int) Math.round(z * 2));

                    Vec3 playerFoot = new Vec3(x, y, z);
                    for (int shift = 0; shift <= 1; shift++) {
                        Vec3 eye = playerFoot.addVector(0, 1.62F  - shift * 0.08F, 0); // assume sneaking lol
                        for (int ix = 0; ix <= 2; ix++) {
                            for (int iy = 0; iy <= 2; iy++) {
                                for (int iz = 0; iz <= 2; iz++) {
                                    Vec3 to = interpolate(bb, ix * 0.45 + 0.05, iy * 0.45 + 0.05, iz * 0.45 + 0.05);

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
                                    boolean notstonk = blocks.lastIndexOf(target) == 0;
                                    int until = notstonk ? 0 : blocks.lastIndexOf(blocks.get(blocks.lastIndexOf(target) - 1));

                                    if (!notstonk && !isAir) {
                                        BlockPos pos = blocks.get(until);
                                        IBlockState from_state = w.getBlockState(pos);
                                        Block from_block = from_state.getBlock();

                                        BlockBreakData breakData = new BlockBreakData(
                                                (isAir ? 5 : 1) * from_block.getBlockHardness(w, pos),
                                                from_block.getHarvestLevel(from_state),
                                                from_block.getHarvestTool(from_state)
                                        );

                                        if (breakData.hardness < 0) {
                                            breakData.hardness = 9999;
                                            imposs = true;
                                            continue;
                                        }

                                        int idx = 0;
                                        if ("pickaxe".equals(breakData.toolClass)|| from_block.getMaterial() == Material.rock || from_block.getMaterial() == Material.anvil || from_block.getMaterial() == Material.iron) {
                                            idx = 0;
                                        } else if ("axe".equals(breakData.toolClass)||
                                                from_block.getMaterial() == Material.wood ||
                                                from_block.getMaterial() == Material.plants ||
                                                from_block.getMaterial() == Material.vine) {
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
                                        if ("pickaxe".equals(breakData.toolClass) || from_block.getMaterial() == Material.rock || from_block.getMaterial() == Material.anvil || from_block.getMaterial() == Material.iron) {
                                            idx = 0;
                                        } else if ("axe".equals(breakData.toolClass) ||
                                                from_block.getMaterial() == Material.wood ||
                                                from_block.getMaterial() == Material.plants ||
                                                from_block.getMaterial() == Material.vine
                                        ) {
                                            idx = 2;
                                        } else if ("shovel".equals(breakData.toolClass) ) {
                                            idx = 1;
                                        } else {
                                            breakData.harvestLv = 10;
                                        }

                                        if (requiredTools[idx] == null) requiredTools[idx] = new RequiredTool();
                                        if (requiredTools[idx].getBreakingPower() < breakData.hardness)
                                            requiredTools[idx].setBreakingPower(breakData.hardness);
                                        if (requiredTools[idx].getHarvestLv() < breakData.harvestLv)
                                            requiredTools[idx].setHarvestLv(breakData.harvestLv);
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

                                        if (newTool.getHarvestLv() < prevTool.getHarvestLv()) {
                                            swap = true;
                                            break;
                                        }
                                        if (newTool.getBreakingPower() < prevTool.getBreakingPower()) {
                                            swap = true;
                                            break;
                                        }
                                    }
                                    if (swap) {
                                        actualReq.put(
                                                playerFoot, requiredTools
                                        );
                                        stonk.put(
                                                playerFoot, !notstonk
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
                            .map(b -> b == null ? "n" : b.getBreakingPower() + ":" + b.getHarvestLv()).collect(Collectors.joining(";"))+";"+stonk.get(a.getKey());
                })).values().stream()
                .map(entries -> {
                    return new PossibleClickingSpot(
                            entries.get(0).getValue(),
                            entries.stream().map(Map.Entry::getKey)
                                    .map(b -> new OffsetVec3(b.xCoord, b.yCoord - 70, b.zCoord))
                                    .collect(Collectors.toList()),
                            stonk.get(entries.get(0).getKey()), 0
                    );
                }).collect(Collectors.toList());
        return doClustering(spots);
    }

    public static List<PossibleClickingSpot> chooseMinimalY(List<PossibleClickingSpot> spots) {
        Map<OffsetVec3, PossibleClickingSpot> clusterMap = new HashMap<>();

        for (PossibleClickingSpot spot : spots) {
            for (OffsetVec3 Vec3 : spot.getOffsetPointSet()) {
                clusterMap.put(Vec3, spot);
            }
        }

        return clusterMap.entrySet().stream()
                .collect(Collectors.groupingBy(a -> new ImmutableTriple<>(a.getKey().xCoord, a.getKey().zCoord, a.getValue())))
                .values().stream()
                    .map(a -> a.stream().min(Comparator.comparingDouble(b -> b.getKey().yCoord)).orElse(null))
                .collect(Collectors.groupingBy(a -> clusterMap.get(a.getKey())))
                .entrySet().stream().map(
                        a -> new PossibleClickingSpot(
                                a.getKey().getTools(),
                                a.getValue().stream().map(b -> b.getKey()).collect(Collectors.toList()),
                                a.getKey().isStonkingReq(),
                                a.getKey().getClusterId()
                        )
                ).collect(Collectors.toList());
    }
    public static List<PossibleClickingSpot> doClustering(List<PossibleClickingSpot> spots) {
        Map<OffsetVec3, PossibleClickingSpot> clusterMap = new HashMap<>();
        Map<OffsetVec3, Integer> clusterId = new HashMap<>();

        for (PossibleClickingSpot spot : spots) {
            for (OffsetVec3 Vec3 : spot.getOffsetPointSet()) {
                clusterId.put(Vec3, -1);
                clusterMap.put(Vec3, spot);
            }
        }

        for (OffsetVec3 vec3 : clusterId.keySet()) {
            for (EnumFacing face : EnumFacing.VALUES) {
                OffsetVec3 newVec3 = new OffsetVec3(
                        vec3.xCoord + face.getFrontOffsetX() * 0.5,
                        vec3.yCoord + face.getFrontOffsetY() * 0.5,
                        vec3.zCoord + face.getFrontOffsetZ() * 0.5
                );
                if (clusterId.containsKey(newVec3)) continue;
                clusterId.put(vec3, -2);
                break;
            }
        }
        int lastId = 0;

        for (OffsetVec3 Vec3 : clusterId.keySet()) {
            int id = clusterId.get(Vec3);
            if (id != -1) continue;
            id = ++lastId;

            Queue<OffsetVec3> toVisit = new LinkedList<>();
            toVisit.add(Vec3);
            while (!toVisit.isEmpty()) {
                OffsetVec3 vec3 = toVisit.poll();
                if (clusterId.get(vec3) != -1) continue;
                clusterId.put(vec3, id);

                for (int x = -1; x <= 1; x++) {
                    for (int y = -1;y  <= 1; y++) {
                        for (int z = -1; z <= 1; z++) {
                            OffsetVec3 newVec3 = new OffsetVec3(
                                    vec3.xCoord + x * 0.5,
                                    vec3.yCoord + y * 0.5,
                                    vec3.zCoord + z * 0.5
                            );
                            if (!clusterId.containsKey(newVec3)) continue;
                            toVisit.add(newVec3);
                        }
                    }
                }
            }
        }

        Queue<OffsetVec3> chk = new LinkedList<>();
        for (OffsetVec3 vec3 : clusterId.keySet()) {
            if (clusterId.get(vec3) == -2) {
                chk.add(vec3);
            }
        }

        while (!chk.isEmpty()) {
            OffsetVec3 vec3 = chk.poll();
            if (clusterId.get(vec3) != -2) continue;
            boolean found = false;
            for (EnumFacing face : EnumFacing.VALUES) {
                OffsetVec3 newVec3 = new OffsetVec3(
                        vec3.xCoord + face.getFrontOffsetX() * 0.5,
                        vec3.yCoord + face.getFrontOffsetY() * 0.5,
                        vec3.zCoord + face.getFrontOffsetZ() * 0.5
                );
                if (!clusterId.containsKey(newVec3) || clusterId.get(newVec3) == -2) continue;
                clusterId.put(vec3, clusterId.get(newVec3));
                found = true;
                break;
            }
            if (found) {
                for (EnumFacing face : EnumFacing.VALUES) {
                    OffsetVec3 newVec3 = new OffsetVec3(
                            vec3.xCoord + face.getFrontOffsetX() * 0.5,
                            vec3.yCoord + face.getFrontOffsetY() * 0.5,
                            vec3.zCoord + face.getFrontOffsetZ() * 0.5
                    );
                    if (!clusterId.containsKey(newVec3) || clusterId.get(newVec3) != -2) continue;
                    chk.add(newVec3);
                }
            }
        }
        for (OffsetVec3 Vec3 : clusterId.keySet()) {
            int id = clusterId.get(Vec3);
            if (id != -2) continue;
            id = ++lastId;

            Queue<OffsetVec3> toVisit = new LinkedList<>();
            toVisit.add(Vec3);
            while (!toVisit.isEmpty()) {
                OffsetVec3 vec3 = toVisit.poll();
                if (clusterId.get(vec3) != -2) continue;
                clusterId.put(vec3, id);

                for (int x = -1; x <= 1; x++) {
                    for (int y = -1;y  <= 1; y++) {
                        for (int z = -1; z <= 1; z++) {
                            OffsetVec3 newVec3 = new OffsetVec3(
                                    vec3.xCoord + x * 0.5,
                                    vec3.yCoord + y * 0.5,
                                    vec3.zCoord + z * 0.5
                            );
                            if (!clusterId.containsKey(newVec3)) continue;
                            toVisit.add(newVec3);
                        }
                    }
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
                                a.getKey().getRight().isStonkingReq(),
                                a.getKey().getLeft()
                        )
                ).collect(Collectors.toList());
    }
    public static List<PossibleMoveSpot> findMovespots(World w, BlockPos target, Predicate<Vec3> included, double manhattenDist) {
        return findMovespots(w, target, included, manhattenDist, (x,y,z) -> RaytraceHelper.canStand(w, x,y,z));
    }

    public static List<PossibleMoveSpot> findMovespots(World w, BlockPos target, Predicate<Vec3> included, double manhattenDist, CalculateIsBlocked calculateIsBlocked) {

        Map<OffsetVec3, Boolean> lol = new HashMap<>();
        Map<OffsetVec3, Boolean> air = new HashMap<>();
        for (double x = target.getX() - manhattenDist; x <= target.getX() + manhattenDist + 1; x += 0.5) {
            for (double y = target.getY() - manhattenDist; y <= target.getY() + manhattenDist + 1; y += 0.5) {
                for (double z = target.getZ() - manhattenDist; z <= target.getZ() + manhattenDist + 1; z += 0.5) {
                    Vec3 posToCheck = new Vec3(x,y,z);
                    if (!included.test(posToCheck)) continue;

                    boolean canStand = calculateIsBlocked.canStand((int) (x*2),(int) (y*2),(int) (z*2));

                    boolean isAir = false;
                    for (int xp = MathHelper.floor_double(x - 0.3); xp < MathHelper.floor_double(x + 0.3) + 1; xp++) {
                        for (int zp = MathHelper.floor_double(z - 0.3); zp < MathHelper.floor_double(z + 0.3) + 1; zp++) {
                            isAir |= w.getBlockState(new BlockPos(xp, posToCheck.yCoord, zp)).getBlock() == Blocks.air;
                            isAir |= w.getBlockState(new BlockPos(xp, posToCheck.yCoord-1, zp)).getBlock() == Blocks.air;
                        }
                    }
                    if ((int)(y*2) % 2 == 1) {
                        for (int xp = MathHelper.floor_double(x - 0.3); xp < MathHelper.floor_double(x + 0.3) + 1; xp++) {
                            for (int zp = MathHelper.floor_double(z - 0.3); zp < MathHelper.floor_double(z + 0.3) + 1; zp++) {
                                isAir |= w.getBlockState(new BlockPos(xp, posToCheck.yCoord, zp)).getBlock() instanceof BlockSlab;
                                isAir |= w.getBlockState(new BlockPos(xp, posToCheck.yCoord, zp)).getBlock() instanceof BlockStairs;
                            }
                        }
                    }


                    if (isAir || canStand) {
                        OffsetVec3 vec3 = new OffsetVec3(posToCheck.xCoord, posToCheck.yCoord -70, posToCheck.zCoord);
                        lol.put(vec3, !canStand);
                    }
                }
            }
        }

        List<PossibleMoveSpot> moveSpots = lol.keySet().stream().collect(Collectors.groupingBy(
                                a -> lol.get(a)
                        ))
                        .entrySet().stream().map(
                                a->
                                    new PossibleMoveSpot(a.getValue(), a.getKey(), 0)
                        ).collect(Collectors.toList());

        return doClustering2(moveSpots);
    }
    public static List<PossibleMoveSpot> doClustering2(List<PossibleMoveSpot> spots) {
        Map<OffsetVec3, PossibleMoveSpot> clusterMap = new HashMap<>();
        Map<OffsetVec3, Integer> clusterId = new HashMap<>();

        for (PossibleMoveSpot spot : spots) {
            for (OffsetVec3 Vec3 : spot.getOffsetPointSet()) {
                clusterId.put(Vec3, spot.isBlocked() ? -2 :  -1);
                clusterMap.put(Vec3, spot);
            }
        }

        for (OffsetVec3 vec3 : clusterId.keySet()) {
            for (EnumFacing face : EnumFacing.VALUES) {
                OffsetVec3 newVec3 = new OffsetVec3(
                        vec3.xCoord + face.getFrontOffsetX() * 0.5,
                        vec3.yCoord + face.getFrontOffsetY() * 0.5,
                        vec3.zCoord + face.getFrontOffsetZ() * 0.5
                );
                if (clusterId.containsKey(newVec3)) continue;
                clusterId.put(vec3, -2);
                break;
            }
        }
        int lastId = 0;

        for (OffsetVec3 Vec3 : clusterId.keySet()) {
            int id = clusterId.get(Vec3);
            if (id != -1) continue;
            id = ++lastId;

            Queue<OffsetVec3> toVisit = new LinkedList<>();
            toVisit.add(Vec3);
            while (!toVisit.isEmpty()) {
                OffsetVec3 vec3 = toVisit.poll();
                if (clusterId.get(vec3) != -1) continue;
                clusterId.put(vec3, id);

                for (int x = -1; x <= 1; x++) {
                    for (int y = -1;y  <= 1; y++) {
                        for (int z = -1; z <= 1; z++) {
                            OffsetVec3 newVec3 = new OffsetVec3(
                                    vec3.xCoord + x * 0.5,
                                    vec3.yCoord + y * 0.5,
                                    vec3.zCoord + z * 0.5
                            );
                            if (!clusterId.containsKey(newVec3)) continue;
                            toVisit.add(newVec3);
                        }
                    }
                }
            }
        }

        Queue<OffsetVec3> chk = new LinkedList<>();
        for (OffsetVec3 vec3 : clusterId.keySet()) {
            if (clusterId.get(vec3) == -2) {
                chk.add(vec3);
            }
        }

        while (!chk.isEmpty()) {
            OffsetVec3 vec3 = chk.poll();
            if (clusterId.get(vec3) != -2) continue;
            boolean found = false;
            for (EnumFacing face : EnumFacing.VALUES) {
                OffsetVec3 newVec3 = new OffsetVec3(
                        vec3.xCoord + face.getFrontOffsetX() * 0.5,
                        vec3.yCoord + face.getFrontOffsetY() * 0.5,
                        vec3.zCoord + face.getFrontOffsetZ() * 0.5
                );
                if (!clusterId.containsKey(newVec3) || clusterId.get(newVec3) == -2) continue;
                clusterId.put(vec3, clusterId.get(newVec3));
                found = true;
                break;
            }
            if (found) {
                for (EnumFacing face : EnumFacing.VALUES) {
                    OffsetVec3 newVec3 = new OffsetVec3(
                            vec3.xCoord + face.getFrontOffsetX() * 0.5,
                            vec3.yCoord + face.getFrontOffsetY() * 0.5,
                            vec3.zCoord + face.getFrontOffsetZ() * 0.5
                    );
                    if (!clusterId.containsKey(newVec3) || clusterId.get(newVec3) != -2) continue;
                    chk.add(newVec3);
                }
            }
        }
        for (OffsetVec3 Vec3 : clusterId.keySet()) {
            int id = clusterId.get(Vec3);
            if (id != -2) continue;
            id = ++lastId;

            Queue<OffsetVec3> toVisit = new LinkedList<>();
            toVisit.add(Vec3);
            while (!toVisit.isEmpty()) {
                OffsetVec3 vec3 = toVisit.poll();
                if (clusterId.get(vec3) != -2) continue;
                clusterId.put(vec3, id);

                for (int x = -1; x <= 1; x++) {
                    for (int y = -1;y  <= 1; y++) {
                        for (int z = -1; z <= 1; z++) {
                            OffsetVec3 newVec3 = new OffsetVec3(
                                    vec3.xCoord + x * 0.5,
                                    vec3.yCoord + y * 0.5,
                                    vec3.zCoord + z * 0.5
                            );
                            if (!clusterId.containsKey(newVec3)) continue;
                            toVisit.add(newVec3);
                        }
                    }
                }
            }
        }

        return clusterMap.keySet().stream()
                .collect(Collectors.groupingBy(a -> {
                    return new ImmutablePair<>(clusterId.get(a), clusterMap.get(a));
                })).entrySet().stream().map(
                        a -> new PossibleMoveSpot(
                                a.getValue(),
                                a.getKey().right.isBlocked(),
                                a.getKey().left
                        )
                ).collect(Collectors.toList());
    }
    public static List<PossibleMoveSpot> chooseMinimalY2(List<PossibleMoveSpot> spots) {
        Map<OffsetVec3, PossibleMoveSpot> clusterMap = new HashMap<>();

        for (PossibleMoveSpot spot : spots) {
            for (OffsetVec3 Vec3 : spot.getOffsetPointSet()) {
                clusterMap.put(Vec3, spot);
            }
        }

        return clusterMap.entrySet().stream()
                .collect(Collectors.groupingBy(a -> new ImmutableTriple<>(a.getKey().xCoord, a.getKey().zCoord, a.getValue())))
                .values().stream()
                .map(a -> a.stream().min(Comparator.comparingDouble(b -> b.getKey().yCoord)).orElse(null))
                .collect(Collectors.groupingBy(a -> clusterMap.get(a.getKey())))
                .entrySet().stream().map(
                        a -> new PossibleMoveSpot(
                                a.getValue().stream().map(b -> b.getKey()).collect(Collectors.toList()),
                                a.getKey().isBlocked(),
                                a.getKey().getClusterId()
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


    private static  boolean canStand(World w, int x, int y, int z) {
       
        float wX = x / 2.0f, wY = y / 2.0f, wZ = z / 2.0f;

        float playerWidth = 0.3f;
        AxisAlignedBB bb = AxisAlignedBB
                .fromBounds(wX - playerWidth, wY+0.06251, wZ - playerWidth,
                        wX + playerWidth, wY +0.06251 + 1.8, wZ + playerWidth);

        int minX = MathHelper.floor_double(bb.minX);
        int maxX = MathHelper.floor_double(bb.maxX + 1.0D);
        int minY = MathHelper.floor_double(bb.minY);
        int maxY = MathHelper.floor_double(bb.maxY + 1.0D);
        int minZ = MathHelper.floor_double(bb.minZ);
        int maxZ = MathHelper.floor_double(bb.maxZ + 1.0D);

        BlockPos.MutableBlockPos blockPos = new BlockPos.MutableBlockPos();
        List<AxisAlignedBB> list2 = new ArrayList<>();
        int size = 0;

        int notstonkable = 0;
        for (int k1 = minX; k1 < maxX; ++k1) {
            for (int l1 = minZ; l1 < maxZ; ++l1) {
                for (int i2 = minY-1; i2 < maxY; ++i2) {
                    blockPos.set(k1, i2, l1);
                    IBlockState state = w.getBlockState(blockPos);
                    Block block = state.getBlock();
                    block.addCollisionBoxesToList(
                            w, blockPos, state, bb, list2, null
                    );
                }
            }
        }
        boolean blocked = !list2.isEmpty();
        return !blocked;
    }

}
