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

import kr.syeyoung.dungeonsguide.mod.dungeon.data.LegacyBlock;
import kr.syeyoung.dungeonsguide.mod.dungeon.data.OffsetVec3;
import kr.syeyoung.dungeonsguide.mod.dungeon.data.PossibleClickingSpot;
import kr.syeyoung.dungeonsguide.mod.dungeon.data.PossibleMoveSpot;
import kr.syeyoung.dungeonsguide.mod.dungeon.data.RequiredTool;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.util.*;
import kr.syeyoung.dungeonsguide.mod.dungeon.world.DGWorld;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.ImmutableTriple;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.StairBlock;

public class RaytraceHelper {

    public static Vec3 interpolate(AABB axisAlignedBB, double x, double y, double z) {
        return new Vec3(
                x * (axisAlignedBB.maxX - axisAlignedBB.minX) + axisAlignedBB.minX,
                y * (axisAlignedBB.maxY - axisAlignedBB.minY) + axisAlignedBB.minY,
                z * (axisAlignedBB.maxZ - axisAlignedBB.minZ) + axisAlignedBB.minZ
        );
    }

    public static interface CalculateIsBlocked {
        boolean canStand(int x, int y, int z);
    }
    public static List<PossibleClickingSpot> raycast(DGWorld w, BlockPos target) {
        return raycast(w, target, (a,b,c) -> RaytraceHelper.canStand(w,a,b,c));
    }

    public static List<PossibleClickingSpot> combine(List<List<PossibleClickingSpot>> possibleClickingSpotList) {
        Map<OffsetVec3, List<PossibleClickingSpot>> clickingSpot = new HashMap<>();
        for (List<PossibleClickingSpot> possibleClickingSpots : possibleClickingSpotList) {
            for (PossibleClickingSpot possibleClickingSpot : possibleClickingSpots) {
                for (OffsetVec3 offsetVec3 : possibleClickingSpot.getOffsetPointSet()) {
                    clickingSpot.computeIfAbsent(offsetVec3, (a) -> new ArrayList<>())
                            .add(possibleClickingSpot);
                }
            }
        }
        clickingSpot.entrySet().removeIf(elem -> elem.getValue().size() != possibleClickingSpotList.size());


        Map<OffsetVec3, RequiredTool[]> actualReq = new HashMap<>();
        Map<OffsetVec3, Boolean> stonk = new HashMap<>();

        for (Map.Entry<OffsetVec3, List<PossibleClickingSpot>> offsetVec3ListEntry : clickingSpot.entrySet()) {

            RequiredTool[] tools = new RequiredTool[3];
            boolean stonkingReq = offsetVec3ListEntry.getValue().get(0).isStonkingReq();
            for (PossibleClickingSpot possibleClickingSpot : offsetVec3ListEntry.getValue()) {
                stonkingReq |= possibleClickingSpot.isStonkingReq();
                for (int i = 0; i < tools.length; i++) {
                    if (tools[i] == null) {
                        tools[i] = possibleClickingSpot.getTools()[i];
                        continue;
                    }
                    if (possibleClickingSpot.getTools()[i] == null) continue;

                    tools[i].setBreakingPower(Math.max(tools[i].getBreakingPower(), possibleClickingSpot.getTools()[i].getBreakingPower()));
                    tools[i].setHarvestLv(Math.max(tools[i].getHarvestLv(), possibleClickingSpot.getTools()[i].getHarvestLv()));
                }
            }
            actualReq.put(offsetVec3ListEntry.getKey(), tools);
            stonk.put(offsetVec3ListEntry.getKey(), stonkingReq);
        }


        List<PossibleClickingSpot> spots = actualReq.entrySet().stream()
                .collect(Collectors.<Map.Entry<OffsetVec3, RequiredTool[]>, String>groupingBy(a -> {
                    return Arrays.stream(a.getValue())
                            .map(b -> b == null ? "n" : b.getBreakingPower() + ":" + b.getHarvestLv()).collect(Collectors.joining(";"))+";"+stonk.get(a.getKey());
                })).values().stream()
                .map(entries -> {
                    return new PossibleClickingSpot(
                            entries.get(0).getValue(),
                            entries.stream().map(Map.Entry::getKey)
                                    .collect(Collectors.toList()),
                            stonk.get(entries.get(0).getKey()), 0
                    );
                }).collect(Collectors.toList());
        return doClustering(spots);
    }

    public static class StonkCalculationResult {
        private boolean possible;
        private RequiredTool[] lastStonk;
        private RequiredTool[] normalStonk;
        private int count;
    }

    private static StonkCalculationResult calculateStonk() {
        return null;
    }


    public static List<PossibleClickingSpot> raycast(DGWorld w, BlockPos target, CalculateIsBlocked calculateIsBlocked) {
        BlockState targetBlockState = w.getBlockState(target);
        // 1.8.9's selected bounding box is the outline shape, empty ones (air) were the full block
        VoxelShape targetShape = targetBlockState.getShape(w.asVanilla(), target);
        AABB bb = targetShape.isEmpty() ? new AABB(target) : targetShape.bounds().move(target);

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
                        Vec3 eye = playerFoot.add(0, 1.62F  - shift * 0.08F, 0); // assume sneaking lol
                        for (int ix = 0; ix <= 2; ix++) {
                            for (int iy = 0; iy <= 2; iy++) {
                                for (int iz = 0; iz <= 2; iz++) {
                                    Vec3 to = interpolate(bb, ix * 0.45 + 0.05 + 0.001, iy * 0.45 + 0.05 + 0.002, iz * 0.45 + 0.05 + 0.003);

                                    if (to.distanceToSqr(eye) > 4.5 * 4.5) {
                                        to = to.subtract(eye).normalize();
                                        to = eye.add(to.x * 4.5, to.y * 4.5, to.z * 4.5);
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
                                        BlockState from_state = w.getBlockState(pos);

                                        // stonks are instant on 26.1, harvest level / tool are kept for compatibility only (D117)
                                        BlockBreakData breakData = new BlockBreakData(
                                                (isAir ? 5 : 1) * from_state.getDestroySpeed(w.asVanilla(), pos),
                                                from_state.is(BlockTags.NEEDS_DIAMOND_TOOL) ? 3 : from_state.is(BlockTags.NEEDS_IRON_TOOL) ? 2 : from_state.is(BlockTags.NEEDS_STONE_TOOL) ? 1 : 0,
                                                from_state.is(BlockTags.MINEABLE_WITH_PICKAXE) ? "pickaxe" : from_state.is(BlockTags.MINEABLE_WITH_AXE) ? "axe" : from_state.is(BlockTags.MINEABLE_WITH_SHOVEL) ? "shovel" : null
                                        );

                                        if (breakData.hardness < 0) {
                                            breakData.hardness = 9999;
                                            imposs = true;
                                            continue;
                                        }

                                        int idx = 0;
                                        // 1.8.9 also sorted by Material (rock / anvil / iron, wood / plants / vine), 26.1's mineable tags cover it
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
                                        BlockState from_state = w.getBlockState(from_bpos);

                                        // stonks are instant on 26.1, harvest level / tool are kept for compatibility only (D117)
                                        BlockBreakData breakData = new BlockBreakData(
                                                (isAir ? 5 : 1) * from_state.getDestroySpeed(w.asVanilla(), from_bpos),
                                                from_state.is(BlockTags.NEEDS_DIAMOND_TOOL) ? 3 : from_state.is(BlockTags.NEEDS_IRON_TOOL) ? 2 : from_state.is(BlockTags.NEEDS_STONE_TOOL) ? 1 : 0,
                                                from_state.is(BlockTags.MINEABLE_WITH_PICKAXE) ? "pickaxe" : from_state.is(BlockTags.MINEABLE_WITH_AXE) ? "axe" : from_state.is(BlockTags.MINEABLE_WITH_SHOVEL) ? "shovel" : null
                                        );
                                        if (breakData.hardness < 0) {
                                            imposs = true;
                                            break;
                                        }

                                        int idx = 0;
                                        // 1.8.9 also sorted by Material (rock / anvil / iron, wood / plants / vine), 26.1's mineable tags cover it
                                        if ("pickaxe".equals(breakData.toolClass)) {
                                            idx = 0;
                                        } else if ("axe".equals(breakData.toolClass)) {
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
                                    .map(b -> new OffsetVec3(b.x, b.y - 70, b.z))
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
        List<OffsetVec3> sortedClusterId = clusterId.keySet().stream().sorted(
                Comparator.<OffsetVec3>comparingDouble(a -> a.xCoord).thenComparingDouble(a -> a.yCoord).thenComparingDouble(a -> a.zCoord)
        ).collect(Collectors.toList());

        for (OffsetVec3 vec3 : sortedClusterId) {
//            int cnt = 0;
            for (Direction face : Direction.values()) {
                OffsetVec3 newVec3 = new OffsetVec3(
                        vec3.xCoord + face.getStepX() * 0.5,
                        vec3.yCoord + face.getStepY() * 0.5,
                        vec3.zCoord + face.getStepZ() * 0.5
                );
                if (clusterId.containsKey(newVec3)) continue;
                clusterId.put(vec3, -2);
                break;
            }
//            if (cnt < 4)
//                clusterId.put(vec3, -2);
        }
        int lastId = 0;

        for (OffsetVec3 Vec3 : sortedClusterId) {
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
        for (OffsetVec3 vec3 : sortedClusterId) {
            if (clusterId.get(vec3) == -2) {
                for (Direction face : Direction.values()) {
                    OffsetVec3 newVec3 = new OffsetVec3(
                            vec3.xCoord + face.getStepX() * 0.5,
                            vec3.yCoord + face.getStepY() * 0.5,
                            vec3.zCoord + face.getStepZ() * 0.5
                    );
                    if (!clusterId.containsKey(newVec3) || clusterId.get(newVec3) == -2) continue;
                    chk.add(vec3);
                    break;
                }
            }
        }

        while (!chk.isEmpty()) {
            OffsetVec3 vec3 = chk.poll();
            if (clusterId.get(vec3) != -2) continue;
            boolean found = false;
            for (Direction face : Direction.values()) {
                OffsetVec3 newVec3 = new OffsetVec3(
                        vec3.xCoord + face.getStepX() * 0.5,
                        vec3.yCoord + face.getStepY() * 0.5,
                        vec3.zCoord + face.getStepZ() * 0.5
                );
                if (!clusterId.containsKey(newVec3) || clusterId.get(newVec3) == -2) continue;
                clusterId.put(vec3, clusterId.get(newVec3));
                found = true;
                break;
            }
            if (found) {
                for (Direction face : Direction.values()) {
                    OffsetVec3 newVec3 = new OffsetVec3(
                            vec3.xCoord + face.getStepX() * 0.5,
                            vec3.yCoord + face.getStepY() * 0.5,
                            vec3.zCoord + face.getStepZ() * 0.5
                    );
                    if (!clusterId.containsKey(newVec3) || clusterId.get(newVec3) != -2) continue;
                    chk.add(newVec3);
                }
            }
        }
        for (OffsetVec3 Vec3 : sortedClusterId) {
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
                .filter(a -> clusterId.get(a) != -2)
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
    public static List<PossibleMoveSpot> findMovespots(DGWorld w, BlockPos target, Predicate<Vec3> included, double manhattenDist) {
        return findMovespots(w, target, included, manhattenDist, (x,y,z) -> RaytraceHelper.canStand(w, x,y,z));
    }

    public static List<PossibleMoveSpot> findMovespots(DGWorld w, BlockPos target, Predicate<Vec3> included, double manhattenDist, CalculateIsBlocked calculateIsBlocked) {

        Map<OffsetVec3, Boolean> lol = new HashMap<>();
        Map<OffsetVec3, Boolean> air = new HashMap<>();
        for (double x = target.getX() - manhattenDist; x <= target.getX() + manhattenDist + 1; x += 0.5) {
            for (double y = target.getY() - manhattenDist; y <= target.getY() + manhattenDist + 1; y += 0.5) {
                for (double z = target.getZ() - manhattenDist; z <= target.getZ() + manhattenDist + 1; z += 0.5) {
                    Vec3 posToCheck = new Vec3(x,y,z);
                    if (!included.test(posToCheck)) continue;

                    boolean canStand = calculateIsBlocked.canStand((int) (x*2),(int) (y*2),(int) (z*2));

                    boolean isAir = false;
                    for (int xp = Mth.floor(x - 0.3); xp < Mth.floor(x + 0.3) + 1; xp++) {
                        for (int zp = Mth.floor(z - 0.3); zp < Mth.floor(z + 0.3) + 1; zp++) {
                            isAir |= LegacyBlock.AIR.is(w.getBlockState(BlockPos.containing(xp, posToCheck.y, zp)));
                            isAir |= LegacyBlock.AIR.is(w.getBlockState(BlockPos.containing(xp, posToCheck.y-1, zp)));
                        }
                    }
                    if ((int)(y*2) % 2 == 1) {
                        for (int xp = Mth.floor(x - 0.3); xp < Mth.floor(x + 0.3) + 1; xp++) {
                            for (int zp = Mth.floor(z - 0.3); zp < Mth.floor(z + 0.3) + 1; zp++) {
                                isAir |= w.getBlockState(BlockPos.containing(xp, posToCheck.y, zp)).getBlock() instanceof SlabBlock;
                                isAir |= w.getBlockState(BlockPos.containing(xp, posToCheck.y, zp)).getBlock() instanceof StairBlock;
                            }
                        }
                    }


                    if (isAir || canStand) {
                        OffsetVec3 vec3 = new OffsetVec3(posToCheck.x, posToCheck.y -70, posToCheck.z);
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

        List<OffsetVec3> sortedClusterId = clusterId.keySet().stream().sorted(
                Comparator.<OffsetVec3>comparingDouble(a -> a.xCoord).thenComparingDouble(a -> a.yCoord).thenComparingDouble(a -> a.zCoord)
        ).collect(Collectors.toList());

        for (OffsetVec3 vec3 : sortedClusterId) {
            for (Direction face : Direction.values()) {
                OffsetVec3 newVec3 = new OffsetVec3(
                        vec3.xCoord + face.getStepX() * 0.5,
                        vec3.yCoord + face.getStepY() * 0.5,
                        vec3.zCoord + face.getStepZ() * 0.5
                );
                if (clusterId.containsKey(newVec3)) continue;
                clusterId.put(vec3, -2);
                break;
            }
        }
        int lastId = 0;

        for (OffsetVec3 Vec3 : sortedClusterId) {
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
        for (OffsetVec3 vec3 : sortedClusterId) {
            if (clusterId.get(vec3) == -2) {
                for (Direction face : Direction.values()) {
                    OffsetVec3 newVec3 = new OffsetVec3(
                            vec3.xCoord + face.getStepX() * 0.5,
                            vec3.yCoord + face.getStepY() * 0.5,
                            vec3.zCoord + face.getStepZ() * 0.5
                    );
                    if (!clusterId.containsKey(newVec3) || clusterId.get(newVec3) == -2) continue;
                    chk.add(vec3);
                    break;
                }
            }
        }

        while (!chk.isEmpty()) {
            OffsetVec3 vec3 = chk.poll();
            if (clusterId.get(vec3) != -2) continue;
            boolean found = false;
            for (Direction face : Direction.values()) {
                OffsetVec3 newVec3 = new OffsetVec3(
                        vec3.xCoord + face.getStepX() * 0.5,
                        vec3.yCoord + face.getStepY() * 0.5,
                        vec3.zCoord + face.getStepZ() * 0.5
                );
                if (!clusterId.containsKey(newVec3) || clusterId.get(newVec3) == -2) continue;
                clusterId.put(vec3, clusterId.get(newVec3));
                found = true;
                break;
            }
            if (found) {
                for (Direction face : Direction.values()) {
                    OffsetVec3 newVec3 = new OffsetVec3(
                            vec3.xCoord + face.getStepX() * 0.5,
                            vec3.yCoord + face.getStepY() * 0.5,
                            vec3.zCoord + face.getStepZ() * 0.5
                    );
                    if (!clusterId.containsKey(newVec3) || clusterId.get(newVec3) != -2) continue;
                    chk.add(newVec3);
                }
            }
        }
        for (OffsetVec3 Vec3 : sortedClusterId) {
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


    private static List<BlockPos> rayTraceBlocks(DGWorld driWorld, Vec3 from, Vec3 to) {
        List<BlockPos> blocks = new ArrayList<>();
        if (!Double.isNaN(from.x) && !Double.isNaN(from.y) && !Double.isNaN(from.z)) {
            if (!Double.isNaN(to.x) && !Double.isNaN(to.y) && !Double.isNaN(to.z)) {
                int to_x_floor = Mth.floor(to.x);
                int to_y_floor = Mth.floor(to.y);
                int to_z_floor = Mth.floor(to.z);
                int from_x_floor = Mth.floor(from.x);
                int from_y_floor = Mth.floor(from.y);
                int from_z_floor = Mth.floor(from.z);


                BlockGetter getter = driWorld.asVanilla();
                BlockPos from_bpos = new BlockPos(from_x_floor, from_y_floor, from_z_floor);
                {
                    BlockState from_state = driWorld.getBlockState(from_bpos);
                    // 1.8.9's canCollideCheck + collisionRayTrace: a ray against the outline shape, which is empty for air / liquids
                    HitResult movingobjectposition2 = from_state.getShape(getter, from_bpos).clip(from, to, from_bpos);
                    if (movingobjectposition2 != null) {
                        blocks.add(from_bpos);
                    }
                }
                HitResult movingobjectposition2 = null;
                int k1 = 200;

                while(k1-- >= 0) {
                    if (Double.isNaN(from.x) || Double.isNaN(from.y) || Double.isNaN(from.z)) {
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
                    double diff_x = to.x - from.x;
                    double diff_y = to.y - from.y;
                    double diff_z = to.z - from.z;
                    if (x_equal) {
                        perc_x = (curr_x - from.x) / diff_x;
                    }

                    if (y_equal) {
                        perc_y = (curr_y - from.y) / diff_y;
                    }

                    if (z_equal) {
                        perc_z = (curr_z - from.z) / diff_z;
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

                    Direction overwhat;
                    if (perc_x < perc_y && perc_x < perc_z) {
                        overwhat = to_x_floor > from_x_floor ? Direction.WEST : Direction.EAST;
                        from = new Vec3(curr_x, from.y + diff_y * perc_x, from.z + diff_z * perc_x);
                    } else if (perc_y < perc_z) {
                        overwhat = to_y_floor > from_y_floor ? Direction.DOWN : Direction.UP;
                        from = new Vec3(from.x + diff_x * perc_y, curr_y, from.z + diff_z * perc_y);
                    } else {
                        overwhat = to_z_floor > from_z_floor ? Direction.NORTH : Direction.SOUTH;
                        from = new Vec3(from.x + diff_x * perc_z, from.y + diff_y * perc_z, curr_z);
                    }

                    from_x_floor = Mth.floor(from.x) - (overwhat == Direction.EAST ? 1 : 0);
                    from_y_floor = Mth.floor(from.y) - (overwhat == Direction.UP ? 1 : 0);
                    from_z_floor = Mth.floor(from.z) - (overwhat == Direction.SOUTH ? 1 : 0);

                    from_bpos = new BlockPos(from_x_floor, from_y_floor, from_z_floor);
                    BlockState iblockstate1 = driWorld.getBlockState(from_bpos);
                            HitResult movingobjectposition1 = iblockstate1.getShape(getter, from_bpos).clip(from, to, from_bpos);
                            if (movingobjectposition1 != null) {
                                blocks.add(from_bpos);
                            }
                }
            }
        }
        return blocks;

    }


    private static  boolean canStand(DGWorld w, int x, int y, int z) {
       
        float wX = x / 2.0f, wY = y / 2.0f, wZ = z / 2.0f;

        float playerWidth = 0.3f;
        AABB bb = new AABB(wX - playerWidth, wY+0.06251, wZ - playerWidth,
                        wX + playerWidth, wY +0.06251 + 1.8, wZ + playerWidth);

        int minX = Mth.floor(bb.minX);
        int maxX = Mth.floor(bb.maxX + 1.0D);
        int minY = Mth.floor(bb.minY);
        int maxY = Mth.floor(bb.maxY + 1.0D);
        int minZ = Mth.floor(bb.minZ);
        int maxZ = Mth.floor(bb.maxZ + 1.0D);

        BlockPos.MutableBlockPos blockPos = new BlockPos.MutableBlockPos();
        BlockGetter getter = w.asVanilla();
        VoxelShape playerShape = Shapes.create(bb);
        List<VoxelShape> list2 = new ArrayList<>();
        int size = 0;

        int notstonkable = 0;
        for (int k1 = minX; k1 < maxX; ++k1) {
            for (int l1 = minZ; l1 < maxZ; ++l1) {
                for (int i2 = minY-1; i2 < maxY; ++i2) {
                    blockPos.set(k1, i2, l1);
                    BlockState state = w.getBlockState(blockPos);
                    VoxelShape hit = Shapes.join(state.getCollisionShape(getter, blockPos).move(k1, i2, l1), playerShape, BooleanOp.AND);
                    if (!hit.isEmpty()) list2.add(hit);
                }
            }
        }
        boolean blocked = !list2.isEmpty();
        return !blocked;
    }

}
