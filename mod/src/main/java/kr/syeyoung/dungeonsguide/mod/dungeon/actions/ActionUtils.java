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
import kr.syeyoung.dungeonsguide.dungeon.data.PossibleClickingSpot;
import kr.syeyoung.dungeonsguide.dungeon.data.PrecalculatedStonk;
import kr.syeyoung.dungeonsguide.dungeon.data.RequiredTool;
import kr.syeyoung.dungeonsguide.dungeon.mechanics.DungeonOnewayDoor;
import kr.syeyoung.dungeonsguide.dungeon.mechanics.dunegonmechanic.RouteBlocker;
import kr.syeyoung.dungeonsguide.mod.dungeon.actions.tree.ActionDAGBuilder;
import kr.syeyoung.dungeonsguide.mod.dungeon.actions.tree.ActionDAGNode;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomfinder.DungeonRoom;
import kr.syeyoung.dungeonsguide.mod.features.FeatureRegistry;
import kr.syeyoung.dungeonsguide.mod.features.impl.secret.FeaturePathfindSettings;
import net.minecraft.item.ItemPickaxe;
import net.minecraft.item.ItemTool;
import net.minecraft.util.BlockPos;
import org.apache.commons.lang3.tuple.ImmutablePair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ActionUtils {

    public interface ActionDAGAccepter {
        ActionDAGBuilder build(ActionDAGBuilder builder) throws PathfindImpossibleException;
    }

    public static ActionDAGBuilder buildActionMoveAndClick(ActionDAGBuilder builder, DungeonRoom dungeonRoom, List<PossibleClickingSpot> spots, OffsetPoint target, ActionDAGAccepter eachBuild) throws PathfindImpossibleException {
        spots = spots.stream().filter(a -> {
            FeaturePathfindSettings.AlgorithmSettings settings = FeatureRegistry.SECRET_PATHFIND_SETTINGS.getAlgorithmSettings();
            {
                RequiredTool pickaxe = a.getTools()[0];
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
                RequiredTool shovel = a.getTools()[1];
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
                RequiredTool axe = a.getTools()[2];
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

        ActionDAGBuilder last = builder;
        Map<Integer, Boolean> stonkReq = new HashMap<>();
        for (PossibleClickingSpot spot : spots) {
            if (!spot.isStonkingReq()) {
                stonkReq.put(spot.getClusterId(), false);
            }
        }
        for (Map.Entry<ImmutablePair<Integer, Boolean>, List<PossibleClickingSpot>> integerListEntry :
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
    public static ActionDAGBuilder buildActionMoveAndClick(ActionDAGBuilder builder,
                                                           DungeonRoom dungeonRoom,
                                                           PrecalculatedStonk precalculatedStonk,
                                                           List<String> optionalPrerequisite,
                                                           List<String> requiredPrerequisite) throws PathfindImpossibleException {
        List<String> defaultOpenBlockers = dungeonRoom.getMechanics().entrySet().stream()
                .filter(a -> a.getValue() instanceof RouteBlocker)
                .filter(a-> !((RouteBlocker) a.getValue()).isBlocking(dungeonRoom))
                .map(a -> a.getKey())
                .collect(Collectors.toList());
        defaultOpenBlockers.addAll(requiredPrerequisite.stream().map(a -> a.split(":")[0]).collect(Collectors.toList()));
        List<String> optionalOpenBlockers = optionalPrerequisite.stream().map(a -> a.split(":")[0]).collect(Collectors.toList());

        List<String> optionalSubset = precalculatedStonk.getDependentRouteBlocker().stream()
                .filter(a -> optionalOpenBlockers.contains(a))
                .collect(Collectors.toList());
        ActionDAGBuilder last = null;
        for (int i = 0; i < (2 << optionalSubset.size()); i++) {
            ArrayList<String> newBlockers = new ArrayList<>(defaultOpenBlockers);
            for (int i1 = 0; i1 < optionalSubset.size(); i1++) {
                if (((i1 >> i) & 0x1) > 0) {
                    newBlockers.add(optionalSubset.get(i1));
                }
            }
            last = buildActionMoveAndClick(builder, dungeonRoom,
                    RaytraceHelper.chooseMinimalY(precalculatedStonk.getPrecalculatedStonk(newBlockers)), precalculatedStonk.getTarget(),
                    builder1 -> {
                        for (String s : requiredPrerequisite) {
                            String mech = s.split(":")[0];
                            String state = s.split(":")[1];
                            builder1.requires(new ActionChangeState(mech, state));
                        }
                        for (String s : optionalPrerequisite) {
                            String mech = s.split(":")[0];
                            String state = s.split(":")[1];
                            if (optionalSubset.contains(mech)) {
                                if (newBlockers.contains(mech))
                                    builder1.requires(new ActionChangeState(mech, state));
                            } else {
                                builder1.optional(new ActionChangeState(mech, state));
                            }
                        }
                        return null;
                    });
        }
        return last;
    }

    public static ActionDAGBuilder buildActionMoveAndClick(ActionDAGBuilder builder,
                                                           DungeonRoom dungeonRoom,
                                                           PrecalculatedStonk precalculatedStonk,
                                                           ActionDAGAccepter eachBuild) throws PathfindImpossibleException {
        List<String> openBlockers = dungeonRoom.getMechanics().entrySet().stream()
                .filter(a -> a.getValue() instanceof RouteBlocker)
                .filter(a-> !((RouteBlocker) a.getValue()).isBlocking(dungeonRoom))
                .map(a -> a.getKey())
                .collect(Collectors.toList());

        return buildActionMoveAndClick(builder, dungeonRoom,
                RaytraceHelper.chooseMinimalY(precalculatedStonk.getPrecalculatedStonk(openBlockers)), precalculatedStonk.getTarget(), eachBuild);
    }
    public static ActionDAGBuilder buildActionMoveAndClick(ActionDAGBuilder builder, DungeonRoom dungeonRoom, OffsetPoint target, ActionDAGAccepter eachBuild) throws PathfindImpossibleException {
        List<String> openBlockers = dungeonRoom.getMechanics().entrySet().stream()
                .filter(a -> a.getValue() instanceof RouteBlocker)
                .filter(a-> !((RouteBlocker) a.getValue()).isBlocking(dungeonRoom))
                .map(a -> a.getKey())
                .collect(Collectors.toList());

        List<PossibleClickingSpot> spots = RaytraceHelper.chooseMinimalY(RaytraceHelper.raycast(
                dungeonRoom.getDungeonRoomInfo().getBlocks() != null ?
                        new RaytraceHelper.DRIWorld(dungeonRoom.getDungeonRoomInfo(), openBlockers) : dungeonRoom.getCachedWorld(), new BlockPos(target.getX(), target.getY(), target.getZ())
        ));
        return buildActionMoveAndClick(builder, dungeonRoom, spots, target, eachBuild);
    }
}
