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

import kr.syeyoung.dungeonsguide.mod.dungeon.data.OffsetVec3;
import kr.syeyoung.dungeonsguide.mod.dungeon.data.PossibleClickingSpot;
import kr.syeyoung.dungeonsguide.mod.dungeon.data.PossibleMoveSpot;
import kr.syeyoung.dungeonsguide.mod.dungeon.data.RequiredTool;
import kr.syeyoung.modapi.data.*;
import kr.syeyoung.modapi.util.RaycastResult;
import kr.syeyoung.modapi.world.BlockType;
import kr.syeyoung.modapi.world.IBlockAccessible;
import kr.syeyoung.modapi.world.UBlock;
import kr.syeyoung.modapi.world.UBlockState;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class RaytraceHelper {

    @Getter @AllArgsConstructor @EqualsAndHashCode
    public static final class Triple<L,R,T> {
        private final L left;
        private final R middle;
        private final T right;
    }

    public static Vector3D interpolate(AABB axisAlignedBB, double x, double y, double z) {
        return new Vector3D(
                x * (axisAlignedBB.maxX - axisAlignedBB.minX) + axisAlignedBB.minX,
                y * (axisAlignedBB.maxY - axisAlignedBB.minY) + axisAlignedBB.minY,
                z * (axisAlignedBB.maxZ - axisAlignedBB.minZ) + axisAlignedBB.minZ
        );
    }

    public static interface CalculateIsBlocked {
        boolean canStand(int x, int y, int z);
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


    public static List<PossibleClickingSpot> raycast(IBlockAccessible w, VectorI3D target, CalculateIsBlocked calculateIsBlocked) {
        UBlockState targetBlockState = w.getBlockStateAt(target);
        AABB bb = targetBlockState.getSelectedBoundingBox(w, target);

        Map<Vector3D, RequiredTool[]> actualReq = new HashMap<>();
        Map<Vector3D, Boolean> stonk = new HashMap<>();

        for (double x = target.getX() - 4.5; x <= target.getX() + 5.5; x += 0.5) {
            for (double y = target.getY() - 6; y <= target.getY() + 4.5; y += 0.5) {
                for (double z = target.getZ() - 4.5; z <= target.getZ() + 5.5; z += 0.5) {
                    // if can't stand on, we don't.
                    if (!calculateIsBlocked.canStand((int) Math.round(x * 2), (int) Math.round(y * 2), (int) Math.round(z * 2))) continue;
//                    if (!calculateIsBlocked.calculateIsBlocked((int) Math.round(x * 2), (int) Math.round(y * 2)-1, (int) Math.round(z * 2)).isBlockedNonStonk()
//                            && y > target.getY() - 5.9) continue;

                    boolean isAir = calculateIsBlocked.canStand((int) Math.round(x * 2), (int) Math.round(y * 2)-1, (int) Math.round(z * 2));

                    Vector3D playerFoot = new Vector3D(x, y, z);
                    for (int shift = 0; shift <= 1; shift++) {
                        Vector3D eye = playerFoot.add(0, 1.62F  - shift * 0.08F, 0); // assume sneaking lol
                        for (int ix = 0; ix <= 2; ix++) {
                            for (int iy = 0; iy <= 2; iy++) {
                                for (int iz = 0; iz <= 2; iz++) {
                                    Vector3D to = interpolate(bb, ix * 0.45 + 0.05 + 0.001, iy * 0.45 + 0.05 + 0.002, iz * 0.45 + 0.05 + 0.003);

                                    if (to.distanceSq(eye) > 4.5 * 4.5) {
                                        to = to.subtract(eye).normalize();
                                        to = eye.add(to.x * 4.5, to.y * 4.5, to.z * 4.5);
                                    }
                                    // 2 * 2 * 2 * 20 * 20 * 20 = 64k collision checks.
                                    List<VectorI3D> blocks = rayTraceBlocks(w, eye, to);

                                    if (blocks.size() == 0) continue;
                                    if (!blocks.get(blocks.size() - 1).equals(target)) continue;

                                    RequiredTool[] requiredTools = new RequiredTool[3];
                                    // 0 pick 1 shovel 2 axe
                                    boolean imposs = false;
                                    boolean notstonk = blocks.lastIndexOf(target) == 0;
                                    int until = notstonk ? 0 : blocks.lastIndexOf(blocks.get(blocks.lastIndexOf(target) - 1));

                                    if (!notstonk && !isAir) {
                                        VectorI3D pos = blocks.get(until);
                                        UBlockState from_state = w.getBlockStateAt(pos);
                                        UBlock from_block = from_state.getBlock();

                                        BlockBreakData breakData = new BlockBreakData(
                                                (isAir ? 5 : 1) * from_block.getBlockHardness(w, pos),
                                                from_state.getHarvestLevel(),
                                                from_state.getHarvestTool()
                                        );

                                        if (breakData.hardness < 0) {
                                            breakData.hardness = 9999;
                                            imposs = true;
                                            continue;
                                        }

                                        int idx = 0;
                                        if ("pickaxe".equals(breakData.toolClass)|| from_block.isHarvestPickaxe()) {
                                            idx = 0;
                                        } else if ("axe".equals(breakData.toolClass)|| from_block.isHarvestAxe()) {
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
                                        VectorI3D from_bpos = blocks.get(i);
                                        if (from_bpos.equals(target)) continue;
                                        if (imposs) break;
                                        UBlockState from_state = w.getBlockStateAt(from_bpos);
                                        UBlock from_block = from_state.getBlock();

                                        BlockBreakData breakData = new BlockBreakData(
                                                (isAir ? 5 : 1) * from_block.getBlockHardness(w, from_bpos),
                                                from_state.getHarvestLevel(),
                                                from_state.getHarvestTool()
                                        );
                                        if (breakData.hardness < 0) {
                                            imposs = true;
                                            break;
                                        }

                                        int idx = 0;
                                        if ("pickaxe".equals(breakData.toolClass) || from_block.isHarvestPickaxe()) {
                                            idx = 0;
                                        } else if ("axe".equals(breakData.toolClass) || from_block.isHarvestAxe()
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
                .collect(Collectors.<Map.Entry<Vector3D, RequiredTool[]>, String>groupingBy(a -> {
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
                .collect(Collectors.groupingBy(a -> new Triple<>(a.getKey().xCoord, a.getKey().zCoord, a.getValue())))
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
                for (EnumFacing face : EnumFacing.VALUES) {
                    OffsetVec3 newVec3 = new OffsetVec3(
                            vec3.xCoord + face.getFrontOffsetX() * 0.5,
                            vec3.yCoord + face.getFrontOffsetY() * 0.5,
                            vec3.zCoord + face.getFrontOffsetZ() * 0.5
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
                    return new Pair<>(clusterId.get(a), clusterMap.get(a));
                })).entrySet().stream().map(
                        a -> new PossibleClickingSpot(
                                a.getKey().getSecond().getTools(),
                                a.getValue(),
                                a.getKey().getSecond().isStonkingReq(),
                                a.getKey().getFirst()
                        )
                ).collect(Collectors.toList());
    }
    public static List<PossibleMoveSpot> findMovespots(IBlockAccessible w, VectorI3D target, Predicate<Vector3D> included, double manhattenDist, CalculateIsBlocked calculateIsBlocked) {

        Map<OffsetVec3, Boolean> lol = new HashMap<>();
        Map<OffsetVec3, Boolean> air = new HashMap<>();
        for (double x = target.getX() - manhattenDist; x <= target.getX() + manhattenDist + 1; x += 0.5) {
            for (double y = target.getY() - manhattenDist; y <= target.getY() + manhattenDist + 1; y += 0.5) {
                for (double z = target.getZ() - manhattenDist; z <= target.getZ() + manhattenDist + 1; z += 0.5) {
                    Vector3D posToCheck = new Vector3D(x,y,z);
                    if (!included.test(posToCheck)) continue;

                    boolean canStand = calculateIsBlocked.canStand((int) (x*2),(int) (y*2),(int) (z*2));

                    boolean isAir = false;
                    for (int xp = (int) Math.floor(x - 0.3); xp < Math.floor(x + 0.3) + 1; xp++) {
                        for (int zp = (int) Math.floor(z - 0.3); zp < Math.floor(z + 0.3) + 1; zp++) {
                            isAir |= w.getBlockStateAt(new VectorI3D(xp, posToCheck.y, zp)).isOf(BlockType.AIR);
                            isAir |= w.getBlockStateAt(new VectorI3D(xp, posToCheck.y-1, zp)).isOf(BlockType.AIR);
                        }
                    }
                    if ((int)(y*2) % 2 == 1) {
                        for (int xp = (int) Math.floor(x - 0.3); xp < Math.floor(x + 0.3) + 1; xp++) {
                            for (int zp = (int) Math.floor(z - 0.3); zp < Math.floor(z + 0.3) + 1; zp++) {
                                isAir |= w.getBlockStateAt(new VectorI3D(xp, posToCheck.y, zp)).isOf(BlockType.TAG_SLAB);
                                isAir |= w.getBlockStateAt(new VectorI3D(xp, posToCheck.y, zp)).isOf(BlockType.TAG_STAIR);
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
                for (EnumFacing face : EnumFacing.VALUES) {
                    OffsetVec3 newVec3 = new OffsetVec3(
                            vec3.xCoord + face.getFrontOffsetX() * 0.5,
                            vec3.yCoord + face.getFrontOffsetY() * 0.5,
                            vec3.zCoord + face.getFrontOffsetZ() * 0.5
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
                    return new Pair<>(clusterId.get(a), clusterMap.get(a));
                })).entrySet().stream().map(
                        a -> new PossibleMoveSpot(
                                a.getValue(),
                                a.getKey().second.isBlocked(),
                                a.getKey().first
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
                .collect(Collectors.groupingBy(a -> new Triple<>(a.getKey().xCoord, a.getKey().zCoord, a.getValue())))
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


    private static List<VectorI3D> rayTraceBlocks(IBlockAccessible driWorld, Vector3D from, Vector3D to) {
        List<VectorI3D> blocks = new ArrayList<>();
        if (!Double.isNaN(from.x) && !Double.isNaN(from.y) && !Double.isNaN(from.z)) {
            if (!Double.isNaN(to.x) && !Double.isNaN(to.y) && !Double.isNaN(to.z)) {
                int to_x_floor = (int) Math.floor(to.x);
                int to_y_floor = (int) Math.floor(to.y);
                int to_z_floor = (int) Math.floor(to.z);
                int from_x_floor = (int) Math.floor(from.x);
                int from_y_floor = (int) Math.floor(from.y);
                int from_z_floor = (int) Math.floor(from.z);


                VectorI3D from_bpos = new VectorI3D(from_x_floor, from_y_floor, from_z_floor);
                {
                    UBlockState from_state = driWorld.getBlockStateAt(from_bpos);
//                    UBlock from_block = from_state.getBlock();
                    if (from_state.canCollideCheck(false)) {
                        RaycastResult movingobjectposition2 = from_state.getBlock().collisionRaytrace(driWorld, from_bpos, from, to);
                        if (movingobjectposition2 != null) {
                            blocks.add(from_bpos);
                        }
                    }
                }
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

                    EnumFacing overwhat;
                    if (perc_x < perc_y && perc_x < perc_z) {
                        overwhat = to_x_floor > from_x_floor ? EnumFacing.WEST : EnumFacing.EAST;
                        from = new Vector3D(curr_x, from.y + diff_y * perc_x, from.z + diff_z * perc_x);
                    } else if (perc_y < perc_z) {
                        overwhat = to_y_floor > from_y_floor ? EnumFacing.DOWN : EnumFacing.UP;
                        from = new Vector3D(from.x + diff_x * perc_y, curr_y, from.z + diff_z * perc_y);
                    } else {
                        overwhat = to_z_floor > from_z_floor ? EnumFacing.NORTH : EnumFacing.SOUTH;
                        from = new Vector3D(from.x + diff_x * perc_z, from.y + diff_y * perc_z, curr_z);
                    }

                    from_x_floor = (int) Math.floor(from.x) - (overwhat == EnumFacing.EAST ? 1 : 0);
                    from_y_floor = (int) Math.floor(from.y) - (overwhat == EnumFacing.UP ? 1 : 0);
                    from_z_floor = (int) Math.floor(from.z) - (overwhat == EnumFacing.SOUTH ? 1 : 0);

                    from_bpos = new VectorI3D(from_x_floor, from_y_floor, from_z_floor);
                    UBlockState iblockstate1 = driWorld.getBlockStateAt(from_bpos);
                    UBlock block1 = iblockstate1.getBlock();
                        if (iblockstate1.canCollideCheck(false)) {
                            RaycastResult movingobjectposition1 = block1.collisionRaytrace(driWorld, from_bpos, from, to);
                            if (movingobjectposition1 != null) {
                                blocks.add(from_bpos);
                            }
                        }
                }
            }
        }
        return blocks;

    }
}
