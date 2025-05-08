/*
 * Dungeons Guide - The most intelligent Hypixel Skyblock Dungeons Mod
 * Copyright (C) 2021  cyoung06
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

package kr.syeyoung.dungeonsguide.mod.dungeon.actions.route;

import kr.syeyoung.dungeonsguide.mod.DungeonsGuide;
import kr.syeyoung.dungeonsguide.mod.chat.ChatTransmitter;
import kr.syeyoung.dungeonsguide.mod.dungeon.actions.*;
import kr.syeyoung.dungeonsguide.mod.dungeon.actions.tree.ActionDAG;
import kr.syeyoung.dungeonsguide.mod.dungeon.actions.tree.ActionDAGBuilder;
import kr.syeyoung.dungeonsguide.mod.dungeon.actions.tree.ActionDAGNode;
import kr.syeyoung.dungeonsguide.mod.pathfinding.TSPCache;
import kr.syeyoung.dungeonsguide.mod.pathfinding.abilitysetting.AlgorithmSetting;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomfinder.DungeonRoom;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomprocessor.GeneralRoomProcessor;
import kr.syeyoung.dungeonsguide.mod.events.impl.PlayerInteractEntityEvent;
import kr.syeyoung.dungeonsguide.mod.pathfinding.preset.RoomPresetPathPlanner;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Vec3;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ActionRoute {

    private String name;
    @Override
    public String toString() {
        return name;
    }

    @Getter
    private transient int current;
    @Getter
    private transient List<AbstractAction> actions;

    @Getter
    private final ActionDAG dag;
    @Getter
    private transient int dagId;
    @Getter
    private transient List<ActionDAGNode> order;

    @Getter
    private final DungeonRoom dungeonRoom;

    public ActionRoute(DungeonRoom dungeonRoom, String mechanic, String state, AlgorithmSetting algorithmSetting)throws PathfindImpossibleException  {
        this(mechanic +" -> "+state, dungeonRoom, new ActionDAGBuilder(dungeonRoom)
                .requires(new ActionChangeState(mechanic, state), algorithmSetting).build());
    }

    public ActionRoute(String name, DungeonRoom dungeonRoom, ActionDAG dag) throws PathfindImpossibleException  {
        this.name = name;
        this.checkCanCancel = (dg) -> false;
        this.dag = dag;
        this.dungeonRoom = dungeonRoom;

        recalculatePath();
    }

    @Getter
    private boolean calculating = false;

    private static final ExecutorService pathCalculator = DungeonsGuide.getDungeonsGuide().registerExecutorService(Executors.newWorkStealingPool(10));


    private void recalculatePath() {
        calculating = true;
        current = 0;
        actions = new ArrayList<>();
        actions.add(new ActionRoot());
        Vec3 start = Minecraft.getMinecraft().thePlayer.getPositionVector();
        pathCalculator.submit(() -> {
            ChatTransmitter.sendDebugChat("ActionDAG has "+dag.getCount()+" Possible action set");

            long startttt = System.currentTimeMillis();


            int minCount = 0;
            boolean annealing = false;
            for (int i = 0; i < dag.getCount(); i++) {
                for (List<ActionDAGNode> actionDAGNodes : dag.topologicalSort(i)) {
                    minCount++;
                    if (minCount > 100000) {
                        annealing = true;
                        break;
                    }
                }
            }
            ChatTransmitter.sendDebugChat("With "+minCount+" Sorts :: Annealing? "+annealing);

            TSPCache tspCache = new TSPCache((GeneralRoomProcessor) dungeonRoom.getRoomProcessor(), dungeonRoom, Collections.EMPTY_LIST, Collections.singletonList(start));

            RoomPresetPathPlanner pathPlanner = new RoomPresetPathPlanner(dungeonRoom.getContext().getPreset().getRoomPreset(dungeonRoom.getDungeonRoomInfo().getUuid()));

            boolean finalAnnealing = annealing;
            try {
//                List<TravelingSalesman.PartialCalculationResult> results = IntStream.range(0, dag.getCount())
//                        .parallel()
//                        .mapToObj((dagId) -> {
//                            if (finalAnnealing)
//                                return TravelingSalesman.annealing(dagId, dag, start, dungeonRoom, tspCache, pathPlanner);
//                            else return TravelingSalesman.bruteforce(dagId, dag, start, dungeonRoom, tspCache, pathPlanner);
//                        })
//                        .collect(Collectors.toList());
//                TravelingSalesman.PartialCalculationResult minCostRoute = results.stream()
//                        .min(Comparator.comparingDouble(a -> a.getCost())).orElse(null);
//
//                int cnt = results.stream().mapToInt(a -> a.getSearchSpace()).sum();
//
//                if (minCostRoute == null) {
//                    try {
//                        Thread.sleep(30000);
//                    } catch (InterruptedException e) {
//                        throw new RuntimeException(e);
//                    }
//                }
//
//                this.dagId = minCostRoute == null ? 0 : minCostRoute.getDagId();
//                order = minCostRoute == null ? new ArrayList<>() : minCostRoute.getRoute();
//                ChatTransmitter.sendDebugChat("ActionRoute has " + cnt + " Possible subroutes :: Chosen route with " + (minCostRoute == null ? Double.POSITIVE_INFINITY : minCostRoute.getCost()) + " cost with Id " + dagId);

                DPTSP dptsp = new DPTSP(dag, start, dungeonRoom);
                order = dptsp.reconstructPath();
                List<AbstractAction> nodes = order.stream().map(ActionDAGNode::getAction).collect(Collectors.toList());
                nodes.add(new ActionComplete());
                actions = nodes;
                current = 0;
                ChatTransmitter.sendDebugChat("Pathfinding took " + (System.currentTimeMillis() - startttt) + "ms");


                calculating = false;
            } catch (OutOfMemoryError e) {
                e.printStackTrace();
                ChatTransmitter.sendDebugChat("OOM While calc");

            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
    public AbstractAction next() {
        current ++;
        if (current >= actions.size()) {
            current = actions.size() - 1;
        }
        return getCurrentAction();
    }

    public AbstractAction prev() {
        current --;
        if (current < 0) {
            current = 0;
        }
        return getCurrentAction();
    }

    public AbstractAction getCurrentAction() {
        return actions.get(current);
    }



    public void onPlayerInteract(PlayerInteractEvent event) {
        if (calculating) return;
        getCurrentAction().onPlayerInteract(dungeonRoom, event );
    }
    public void onLivingDeath(LivingDeathEvent event) {
        if (calculating) return;
        getCurrentAction().onLivingDeath(dungeonRoom, event );
    }

    private final Function<DungeonRoom, Boolean> checkCanCancel;

    public void onTick() {
        if (calculating) return;
        AbstractAction currentAction = getCurrentAction();

        currentAction.onTick(dungeonRoom);

        if (checkCanCancel != null && checkCanCancel.apply(dungeonRoom)) { // action change state
            this.current = actions.size() - 1;
        }


        while (currentAction.isComplete(dungeonRoom)) {
            next();
            currentAction = getCurrentAction();
        }

        boolean recalc = false;
        for (int i = current; i < actions.size(); i++) {
            if (actions.get(i).shouldRecalculatePath(dungeonRoom)) {
                recalc = true;
            }
        }
        if (recalc) {
            recalculatePath();
            while (currentAction.isComplete(dungeonRoom)) {
                next();
                currentAction = getCurrentAction();
            }
        }
    }

    public void onLivingInteract(PlayerInteractEntityEvent event) {
        if (calculating) return;
        getCurrentAction().onLivingInteract(dungeonRoom, event );
    }

}
