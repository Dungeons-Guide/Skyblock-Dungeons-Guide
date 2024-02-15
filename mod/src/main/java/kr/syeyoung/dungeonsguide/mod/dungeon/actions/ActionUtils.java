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
import net.minecraft.util.Vec3;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class ActionUtils {

    public interface ActionDAGAccepter {
        ActionDAGBuilder build(ActionDAGBuilder builder) throws PathfindImpossibleException;
    }
    public static ActionDAGBuilder buildActionMoveAndClick(ActionDAGBuilder builder, DungeonRoom dungeonRoom, OffsetPoint target, ActionDAGAccepter eachBuild) throws PathfindImpossibleException {

        List<RaytraceHelper.PossibleClickingSpot> spots = RaytraceHelper.raycast(dungeonRoom.getCachedWorld(), target.getBlockPos(dungeonRoom), dungeonRoom::getBlock);
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
        ActionDAGBuilder last = builder;
        for (Map.Entry<Integer, List<RaytraceHelper.PossibleClickingSpot>> integerListEntry : spots.stream().collect(Collectors.groupingBy(a -> a.getClusterId())).entrySet()) {
            List<OffsetVec3> vec3s = new ArrayList<>();
            for (RaytraceHelper.PossibleClickingSpot possibleClickingSpot : integerListEntry.getValue()) {
                for (Vec3 vec3 : possibleClickingSpot.getOffsetPointSet()) {
                    vec3s.add(new OffsetVec3(dungeonRoom, vec3));
                }
            }
            ActionDAGBuilder builder1 = builder.requires(new AtomicAction.Builder()
                            .requires(new ActionClick(target))
                            .requires(new ActionMove(vec3s))
                            .build("MoveAndClick"));
            last = eachBuild.build(builder1);
        }
        return last;
    }
}
