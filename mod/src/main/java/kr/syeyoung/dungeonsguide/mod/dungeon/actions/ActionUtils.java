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

import kr.syeyoung.dungeonsguide.dungeon.data.OffsetPoint;
import kr.syeyoung.dungeonsguide.mod.dungeon.actions.tree.ActionDAGBuilder;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomfinder.DungeonRoom;
import kr.syeyoung.dungeonsguide.mod.features.FeatureRegistry;
import kr.syeyoung.dungeonsguide.mod.features.impl.secret.FeaturePathfindSettings;
import net.minecraft.item.ItemPickaxe;
import net.minecraft.item.ItemTool;
import net.minecraft.util.BlockPos;
import net.minecraft.util.Vec3;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class ActionUtils {

    public interface ActionDAGAccepter {
        ActionDAGBuilder build(ActionDAGBuilder builder) throws PathfindImpossibleException;
    }
    public static ActionDAGBuilder buildActionMoveAndClick(ActionDAGBuilder builder, DungeonRoom dungeonRoom, OffsetPoint target, ActionDAGAccepter eachBuild) throws PathfindImpossibleException {
//dungeonRoom.getDungeonRoomInfo().getBlock(0,0,0 , )
        List<RaytraceHelper.PossibleClickingSpot> spots = RaytraceHelper.raycast(
                dungeonRoom.getDungeonRoomInfo().getBlocks() != null ?
                        new RaytraceHelper.DRIWorld(dungeonRoom.getDungeonRoomInfo(), 0) : dungeonRoom.getCachedWorld(), new BlockPos(target.getX(), target.getY(), target.getZ())
        );
        System.out.println(spots.size()+" wat " + dungeonRoom.getDungeonRoomInfo().getBlocks() );
        System.out.println(  new RaytraceHelper.DRIWorld(dungeonRoom.getDungeonRoomInfo(), 0) .getBlockState(new BlockPos(target.getX(), target.getY(), target.getZ())));
        spots = spots.stream().filter(a -> {
            FeaturePathfindSettings.AlgorithmSettings settings = FeatureRegistry.SECRET_PATHFIND_SETTINGS.getAlgorithmSettings();
            {
                RaytraceHelper.RequiredTool pickaxe = a.getTools()[0];
                if (pickaxe != null) {
                    if (settings.getPickaxeSpeed() < 0) return false;
                    int lv = ((ItemPickaxe) settings.getPickaxe()).getToolMaterial().getHarvestLevel();
                    if (lv >= pickaxe.getHarvestLv()) {
                        if (settings.getPickaxeSpeed() / 30 > pickaxe.getBreakingPower()) {
                            // good
                        } else {
                            return false;
                        }
                    } else {
                        if (settings.getPickaxeSpeed() / 100 > pickaxe.getBreakingPower()) {
                            // good
                        } else {
                            return false;
                        }
                    }
                }
            }
            {
                RaytraceHelper.RequiredTool shovel = a.getTools()[1];
                if (shovel != null) {
                    if (settings.getShovelSpeed() < 0) return false;
                    int lv = ((ItemTool) settings.getPickaxe()).getToolMaterial().getHarvestLevel();
                    if (lv >= shovel.getHarvestLv()) {
                        if (settings.getPickaxeSpeed() / 30 > shovel.getBreakingPower()) {
                            // good
                        } else {
                            return false;
                        }
                    } else {
                        if (settings.getPickaxeSpeed() / 100 > shovel.getBreakingPower()) {
                            // good
                        } else {
                            return false;
                        }
                    }
                }
            }
            {
                RaytraceHelper.RequiredTool axe = a.getTools()[2];
                if (axe != null) {
                    if (settings.getAxeSpeed() < 0) return false;
                    int lv = ((ItemTool) settings.getPickaxe()).getToolMaterial().getHarvestLevel();
                    if (lv >= axe.getHarvestLv()) {
                        if (settings.getPickaxeSpeed() / 30 > axe.getBreakingPower()) {
                            // good
                        } else {
                            return false;
                        }
                    } else {
                        if (settings.getPickaxeSpeed() / 100 > axe.getBreakingPower()) {
                            // good
                        } else {
                            return false;
                        }
                    }
                }
            }
            return true;
        }).collect(Collectors.toList());
        for (RaytraceHelper.PossibleClickingSpot spot : spots) {
            List<RaytraceHelper.TameVec3> tames = spot.getOffsetPointSet().stream()
                    .map(a -> {
                        return new OffsetVec3(a.xCoord, a.yCoord, a.zCoord).getPos(dungeonRoom);
                    })
                    .map(a -> {
                        return new RaytraceHelper.TameVec3(a.xCoord ,a.yCoord, a.zCoord);
                    }).collect(Collectors.toList());
            spot.getOffsetPointSet().removeIf(a -> true);
            spot.getOffsetPointSet().addAll(tames);
        }
        ActionDAGBuilder last = builder;
        Map<Integer, Boolean> stonkReq = new HashMap<>();
        for (RaytraceHelper.PossibleClickingSpot spot : spots) {
            if (!spot.isStonkingReq()) {
                stonkReq.put(spot.getClusterId(), false);
            }
        }
        for (Map.Entry<ImmutablePair<Integer, Boolean>, List<RaytraceHelper.PossibleClickingSpot>> integerListEntry :
                spots.stream()
                        .filter(a -> {
                            if (stonkReq.containsKey(a.getClusterId()) && !a.isStonkingReq()) return true;
                            return !stonkReq.containsKey(a.getClusterId());
                        })
                        .collect(Collectors.groupingBy(a -> new ImmutablePair<>(a.getClusterId(), a.isStonkingReq()))).entrySet()) {
            if (integerListEntry.getKey().right) {
                ActionDAGBuilder builder1 = builder.or(new AtomicAction.Builder()
                        .requires(new ActionStonkClick(target))
                        .requires(new ActionMove(integerListEntry.getValue(), dungeonRoom))
                        .build("MoveAndStonkClick"));
                last = eachBuild.build(builder1);
            } else {
                ActionDAGBuilder builder1 = builder.or(new AtomicAction.Builder()
                        .requires(new ActionClick(target))
                        .requires(new ActionMove(integerListEntry.getValue(), dungeonRoom))
                        .build("MoveAndClick"));
                last = eachBuild.build(builder1);
            }
        }
        return last;
    }
}
