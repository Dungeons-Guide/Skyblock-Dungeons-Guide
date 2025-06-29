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

import kr.syeyoung.dungeonsguide.mod.dungeon.actions.tree.ActionDAGBuilder;
import kr.syeyoung.dungeonsguide.mod.dungeon.data.*;
import kr.syeyoung.dungeonsguide.mod.dungeon.data.mechanics.DungeonBreakableWallState;
import kr.syeyoung.dungeonsguide.mod.dungeon.data.mechanics.DungeonTombState;
import kr.syeyoung.dungeonsguide.mod.dungeon.data.mechanics.dunegonmechanic.WorldMutatingMechanicState;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomfinder.DungeonRoom;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomprocessor.GeneralRoomProcessor;
import kr.syeyoung.dungeonsguide.mod.dungeon.world.DRIWorld;
import kr.syeyoung.dungeonsguide.mod.pathfinding.abilitysetting.AlgorithmSetting;
import kr.syeyoung.modapi.data.VectorI3D;
import org.apache.commons.lang3.tuple.ImmutablePair;

import java.util.*;
import java.util.stream.Collectors;

public class ActionUtils {

    public interface ActionDAGAccepter {
        ActionDAGBuilder build(ActionDAGBuilder builder) throws PathfindImpossibleException;
    }
    public interface AtomicActionAccepter {
        AtomicAction.Builder build(AtomicAction.Builder builder) throws PathfindImpossibleException;
    }

    public static ActionDAGBuilder buildActionMoveAndClick(ActionDAGBuilder builder, DungeonRoom dungeonRoom, List<PossibleClickingSpot> spots, OffsetPoint[] target, ActionDAGAccepter eachBuild, boolean guard, AlgorithmSetting settings) throws PathfindImpossibleException {
        spots = spots.stream().filter(a -> {
            {
                RequiredTool pickaxe = a.getTools()[0];
                if (pickaxe != null) {
                    if (settings.getPickaxeSpeed() < 0) return false;
                    int lv =  settings.getPickaxe().getTool().getToolMaterial().getHarvestLevel();
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
                    int lv = settings.getShovel().getTool().getToolMaterial().getHarvestLevel();
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
                    int lv = settings.getAxe().getTool().getToolMaterial().getHarvestLevel();
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
            ActionDAGBuilder builder1 = builder;
//            if (guard)
//                builder1 = builder.or(new ActionStupidGuard());


            AtomicAction.Builder builder2 = new AtomicAction.Builder();
            for (OffsetPoint offsetPoint : target) {
                builder2.requires(integerListEntry.getKey().right ? new ActionStonkClick(offsetPoint) : new ActionClick(offsetPoint));
            }
            if (integerListEntry.getKey().right) {
                builder1 = builder1.or(builder2
                        .requires(new ActionMove(integerListEntry.getValue(), dungeonRoom))
                        .build("MoveAndStonkClick"), settings);
                last = eachBuild.build(builder1);
            } else {
                builder1 = builder1.or(builder2
                        .requires(new ActionMove(integerListEntry.getValue(), dungeonRoom))
                        .build("MoveAndClick"), settings);
                last = eachBuild.build(builder1);
            }
        }
        return last;
    }
    public static ActionDAGBuilder buildActionMoveAndClick(ActionDAGBuilder builder,
                                                           DungeonRoom dungeonRoom,
                                                           PrecalculatedStonk precalculatedStonk,
                                                           List<String> optionalPrerequisite,
                                                           List<String> requiredPrerequisite, AlgorithmSetting algorithmSetting) throws PathfindImpossibleException {
        List<String> defaultOpenBlockers = dungeonRoom.getMechanics().entrySet().stream()
                .filter(a -> a.getValue() instanceof WorldMutatingMechanicState)
                .filter(a-> !((WorldMutatingMechanicState) a.getValue()).isBlocking(dungeonRoom))
                .filter(a -> precalculatedStonk.getDependentRouteBlocker().contains(a.getKey()))
                .map(a -> a.getKey())
                .collect(Collectors.toList());
        defaultOpenBlockers.addAll(requiredPrerequisite.stream().filter(a -> !a.isEmpty()).map(a -> a.split(":")[0]).collect(Collectors.toList()));
        List<String> optionalOpenBlockers = optionalPrerequisite.stream().filter(a -> !a.isEmpty()).map(a -> a.split(":")[0]).collect(Collectors.toList());

        List<String> optionalSubset = precalculatedStonk.getDependentRouteBlocker().stream()
                .filter(a -> optionalOpenBlockers.contains(a))
                .collect(Collectors.toList());
        ActionDAGBuilder last = null;
        for (int i = 0; i < (1 << optionalSubset.size()); i++) {
            Set<String> newBlockers = new HashSet<>(defaultOpenBlockers);
            Set<String> notBlockers = new HashSet<>();
            for (int i1 = 0; i1 < optionalSubset.size(); i1++) {
                if (((i >> i1) & 0x1) > 0) {
                    newBlockers.add(optionalSubset.get(i1));
                } else {
                    notBlockers.add(optionalSubset.get(i1));
                }
            }

            boolean flag = false;
            for (String newBlocker : newBlockers) {
                if (!dungeonRoom.getMechanics().get(newBlocker).getCurrentState().equals("open") &&
                        !dungeonRoom.getMechanics().get(newBlocker).getAvailableActions().contains("open"))
                    flag = true;
            }
            for (String notBlocker : notBlockers) {
                if (!dungeonRoom.getMechanics().get(notBlocker).getCurrentState().equals("closed") &&
                        !dungeonRoom.getMechanics().get(notBlocker).getAvailableActions().contains("closed"))
                    flag = true;
            }
            if (flag) continue;
            last = buildActionMoveAndClick(builder, dungeonRoom,
                    precalculatedStonk.getPrecalculatedStonk(newBlockers), precalculatedStonk.getTargets(),
                    builder1 -> {
                        for (String newBlocker : newBlockers) {
                            if (dungeonRoom.getMechanics().get(newBlocker) instanceof DungeonBreakableWallState) continue;
                            if (dungeonRoom.getMechanics().get(newBlocker) instanceof DungeonTombState) continue;
                            builder1.requires(new ActionChangeState(newBlocker, "open"), algorithmSetting);
                        }
                        for (String notBlocker : notBlockers) {
                            builder1.requires(new ActionChangeState(notBlocker, "closed"), algorithmSetting);
                        }
                        for (String s : requiredPrerequisite) {
                            if (s.isEmpty()) continue;
                            String mech = s.split(":")[0];
                            if (newBlockers.contains(mech)) continue;
                            if (dungeonRoom.getMechanics().get(mech) instanceof DungeonTombState) continue;
                            if (dungeonRoom.getMechanics().get(mech) instanceof DungeonBreakableWallState) continue;
                            String state = s.split(":")[1];
                            builder1.requires(new ActionChangeState(mech, state), algorithmSetting);
                        }
                        for (String s : optionalPrerequisite) {
                            if (s.isEmpty()) continue;
                            String mech = s.split(":")[0];
                            if (dungeonRoom.getMechanics().get(mech) instanceof DungeonTombState) continue;
                            if (dungeonRoom.getMechanics().get(mech) instanceof DungeonBreakableWallState) continue;
                            String state = s.split(":")[1];
                            if (!optionalSubset.contains(mech)) {
                                builder1.optional(new ActionChangeState(mech, state), algorithmSetting);
                            }
                        }
                        return null;
                    }, i != (1 << optionalSubset.size()) - 1, algorithmSetting);
        }
        return last;
    }

    public static ActionDAGBuilder buildActionMoveAndClick(ActionDAGBuilder builder, DungeonRoom dungeonRoom, OffsetPoint target, ActionDAGAccepter eachBuild, AlgorithmSetting algorithmSetting) throws PathfindImpossibleException {
        List<String> openBlockers = dungeonRoom.getMechanics().entrySet().stream()
                .filter(a -> a.getValue() instanceof WorldMutatingMechanicState)
                .filter(a-> !((WorldMutatingMechanicState) a.getValue()).isBlocking(dungeonRoom))
                .map(a -> a.getKey())
                .collect(Collectors.toList());

        List<PossibleClickingSpot> spots = RaytraceHelper.chooseMinimalY(RaytraceHelper.raycast(
                dungeonRoom.getDungeonRoomInfo().getWorld() != null ?
                        new DRIWorld(dungeonRoom.getDungeonRoomInfo(), openBlockers) : dungeonRoom.getRoomWorld(),
                        new VectorI3D(target.getX(), target.getY(), target.getZ()),
                (x,y,z) -> ((GeneralRoomProcessor)dungeonRoom.getRoomProcessor()).getPathfinderWorld().getBlock(x,y,z).isBlocked()
        ));
        return buildActionMoveAndClick(builder, dungeonRoom, spots, new OffsetPoint[]{target}, eachBuild, false, algorithmSetting);
    }


    public static ActionDAGBuilder buildActionMoveAnd(ActionDAGBuilder builder, DungeonRoom dungeonRoom, List<PossibleMoveSpot> spots, String name, AtomicActionAccepter eachBuild, ActionDAGAccepter afterBuild, boolean guard, AlgorithmSetting algorithmSetting) throws PathfindImpossibleException {

        ActionDAGBuilder last = builder;
        for (Map.Entry<Integer, List<PossibleMoveSpot>> integerListEntry :
                spots.stream().collect(Collectors.groupingBy(a ->a.getClusterId())).entrySet()) {
            ActionDAGBuilder builder1 = builder;
//            if (guard)
//                builder1 = builder.or(new ActionStupidGuard());

            builder1 = builder1.or(
                    eachBuild.build(new AtomicAction.Builder())
                            .requires(new ActionMoveSpot(integerListEntry.getValue(), dungeonRoom))
                            .build(name), algorithmSetting);
            last = afterBuild.build(builder1);
        }
        return last;
    }
    public static ActionDAGBuilder buildActionMoveAnd(ActionDAGBuilder builder,
                                                      DungeonRoom dungeonRoom,
                                                      PrecalculatedMoveNearest precalculatedStonk,
                                                      List<String> optionalPrerequisite,
                                                      List<String> requiredPrerequisite,
                                                      AtomicActionAccepter eachBuild,
                                                      String name, AlgorithmSetting algorithmSetting) throws PathfindImpossibleException {

        List<String> defaultOpenBlockers = dungeonRoom.getMechanics().entrySet().stream()
                .filter(a -> a.getValue() instanceof WorldMutatingMechanicState)
                .filter(a-> !((WorldMutatingMechanicState) a.getValue()).isBlocking(dungeonRoom))
                .filter(a -> precalculatedStonk.getDependentRouteBlocker().contains(a.getKey()))
                .map(a -> a.getKey())
                .collect(Collectors.toList());
        defaultOpenBlockers.addAll(requiredPrerequisite.stream()
                .filter(a -> !a.isEmpty())
                .map(a -> a.split(":")[0])
                .collect(Collectors.toList()));
        List<String> optionalOpenBlockers = optionalPrerequisite.stream()
                .filter(a -> !a.isEmpty())
                .map(a -> a.split(":")[0]).collect(Collectors.toList());

        List<String> optionalSubset = precalculatedStonk.getDependentRouteBlocker().stream()
                .filter(a -> optionalOpenBlockers.contains(a))
                .collect(Collectors.toList());
        ActionDAGBuilder last = null;
        for (int i = 0; i < (1 << optionalSubset.size()); i++) {
            Set<String> newBlockers = new HashSet<>(defaultOpenBlockers);
            Set<String> notBlockers = new HashSet<>();
            for (int i1 = 0; i1 < optionalSubset.size(); i1++) {
                if (((i >> i1) & 0x1) > 0) {
                    newBlockers.add(optionalSubset.get(i1));
                } else {
                    notBlockers.add(optionalSubset.get(i1));
                }
            }
            boolean flag = false;
            for (String newBlocker : newBlockers) {
                if (!dungeonRoom.getMechanics().get(newBlocker).getCurrentState().equals("open") &&
                        !dungeonRoom.getMechanics().get(newBlocker).getAvailableActions().contains("open"))
                    flag = true;
            }
            for (String notBlocker : notBlockers) {
                if (!dungeonRoom.getMechanics().get(notBlocker).getCurrentState().equals("closed") &&
                        !dungeonRoom.getMechanics().get(notBlocker).getAvailableActions().contains("closed"))
                    flag = true;
            }
            if (flag) continue;

            last = buildActionMoveAnd(builder, dungeonRoom,
                    precalculatedStonk.getPrecalculatedStonk(newBlockers),
                    name,
                    eachBuild,
                    builder1 -> {
                        for (String newBlocker : newBlockers) {
                            if (dungeonRoom.getMechanics().get(newBlocker) instanceof DungeonBreakableWallState) continue;
                            if (dungeonRoom.getMechanics().get(newBlocker) instanceof DungeonTombState) continue;
                            builder1.requires(new ActionChangeState(newBlocker, "open"), algorithmSetting);
                        }
                        for (String notBlocker : notBlockers) {
                            builder1.requires(new ActionChangeState(notBlocker, "closed"), algorithmSetting);
                        }
                        for (String s : requiredPrerequisite) {
                            if (s.isEmpty()) continue;
                            String mech = s.split(":")[0];
                            if (newBlockers.contains(mech)) continue;
                            if (dungeonRoom.getMechanics().get(mech) instanceof DungeonBreakableWallState) continue;
                            if (dungeonRoom.getMechanics().get(mech) instanceof DungeonTombState) continue;
                            String state = s.split(":")[1];
                            builder1.requires(new ActionChangeState(mech, state), algorithmSetting);
                        }
                        for (String s : optionalPrerequisite) {
                            if (s.isEmpty()) continue;
                            String mech = s.split(":")[0];
                            if (dungeonRoom.getMechanics().get(mech) instanceof DungeonBreakableWallState) continue;
                            if (dungeonRoom.getMechanics().get(mech) instanceof DungeonTombState) continue;
                            String state = s.split(":")[1];
                            if (!optionalSubset.contains(mech)) {
                                builder1.optional(new ActionChangeState(mech, state), algorithmSetting);
                            }
                        }
                        return null;
                    }, i != (1 << optionalSubset.size()) - 1, algorithmSetting);
        }
        return last;
    }

}
