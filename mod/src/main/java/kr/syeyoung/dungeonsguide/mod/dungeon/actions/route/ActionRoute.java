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
import kr.syeyoung.dungeonsguide.mod.dungeon.roomfinder.DungeonRoom;
import kr.syeyoung.dungeonsguide.mod.events.impl.PlayerInteractEntityEvent;
import lombok.AllArgsConstructor;
import lombok.Data;
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

    private final DungeonRoom dungeonRoom;

    @Getter
    private final ActionRouteProperties actionRouteProperties;

    public ActionRoute(DungeonRoom dungeonRoom, String mechanic, String state, ActionRouteProperties actionRouteProperties)throws PathfindImpossibleException  {
        this(mechanic +" -> "+state, dungeonRoom, new ActionDAGBuilder(dungeonRoom)
                .requires(new ActionChangeState(mechanic, state)).build(), actionRouteProperties);
    }

    public ActionRoute(String name, DungeonRoom dungeonRoom, ActionDAG dag, ActionRouteProperties actionRouteProperties) throws PathfindImpossibleException  {
        this.name = name;
        this.actionRouteProperties = actionRouteProperties;
        this.checkCanCancel = (dg) -> false;
        this.dag = dag;
        this.dungeonRoom = dungeonRoom;

        recalculatePath();
    }

    @Getter
    private boolean calculating = false;

    private static final ExecutorService pathCalculator = DungeonsGuide.getDungeonsGuide().registerExecutorService(Executors.newWorkStealingPool(10));


    @Data @AllArgsConstructor
    private static class PartialCalculationResult {
        private int dagId;
        private List<ActionDAGNode> route;
        private double cost;
        private int searchSpace;
    }
    private void recalculatePath() {
        calculating = true;
        current = 0;
        actions = new ArrayList<>();
        actions.add(new ActionRoot());
        pathCalculator.submit(() -> {
            Vec3 start = Minecraft.getMinecraft().thePlayer.getPositionVector();


            ChatTransmitter.sendDebugChat("ActionDAG has "+dag.getCount()+" Possible action set");

            long startttt = System.currentTimeMillis();

            boolean stupidheuristic = false;
            int minCount = 0;
            for (int i = 0; i < dag.getCount(); i++) {

                for (List<ActionDAGNode> actionDAGNodes : dag.topologicalSort(i)) {
                    minCount++;
                    if (minCount > 1000000) {
                        break;
                    }
                }
            }
            ChatTransmitter.sendDebugChat("With "+minCount+" Sorts");

            Map<String, Object> memoization = new ConcurrentHashMap<>();
            List<PartialCalculationResult> results = IntStream.range(0, dag.getCount())
                    .parallel()
                    .mapToObj((dagId) -> {
                        int[] nodeStatus = dag.getNodeStatus(dagId);
                        if (stupidheuristic) {
                            if (dag.getAllNodes().stream().flatMap(a -> a.getOptional().stream())
                                    .anyMatch(a -> nodeStatus[a.getId()] == 0)) return new PartialCalculationResult(dagId, null, Double.POSITIVE_INFINITY, 0);
                        }
                        int cnt = 0;
                        double localMinCost = Double.POSITIVE_INFINITY;
                        List<ActionDAGNode> localMinCostRoute = null;

                        for (List<ActionDAGNode> actionDAGNodes : dag.topologicalSort(dagId)) {
                            cnt++;

                            RoomState roomState = new RoomState();
                            roomState.setPlayerPos(start);
                            double cost = 0;
                            for (ActionDAGNode actionDAGNode : actionDAGNodes) {
                                cost += actionDAGNode.getAction().evalulateCost(roomState, dungeonRoom, memoization);
                                if (cost == Double.POSITIVE_INFINITY) break;
                            }
                            if (cost < localMinCost) {
                                localMinCost = cost;
                                localMinCostRoute = actionDAGNodes;
                            }

                            if (cnt > 100000)  {
                                ChatTransmitter.sendDebugChat("While traversing "+dagId+ " limit of 100000 was reached");
                                break;
                            }
                        }
                        return new PartialCalculationResult(dagId, localMinCostRoute, localMinCost, cnt);
                    })
                    .collect(Collectors.toList());
            PartialCalculationResult minCostRoute = results.stream()
                    .min(Comparator.comparingDouble(a -> a.cost)).orElse(null);

            int cnt = results.stream().mapToInt(a -> a.searchSpace).sum();

            if (minCostRoute == null) {
                try {
                    Thread.sleep(30000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }

            this.dagId = minCostRoute == null ? 0 : minCostRoute.dagId;
            order = minCostRoute == null ? new ArrayList<>() : minCostRoute.route;
            ChatTransmitter.sendDebugChat("ActionRoute has "+cnt+" Possible subroutes :: Chosen route with "+(minCostRoute == null ? Double.POSITIVE_INFINITY : minCostRoute.cost)+" cost with Id "+dagId);
            ChatTransmitter.sendDebugChat("Pathfinding took "+ (System.currentTimeMillis() - startttt)+"ms");
            List<AbstractAction> nodes = minCostRoute != null ? minCostRoute.route.stream().map(ActionDAGNode::getAction).collect(Collectors.toList()) : new ArrayList<>();
            nodes.add(new ActionComplete());
            actions = nodes;
            current = 0;


            calculating = false;

        });
    }
    public AbstractAction next() {
        if (!(getCurrentAction() instanceof  ActionMove || getCurrentAction() instanceof  ActionMoveNearestAir))
            getCurrentAction().cleanup(dungeonRoom, actionRouteProperties);
        if (this.current -1 >= 0 && (actions.get(this.current-1) instanceof ActionMove || actions.get(this.current-1) instanceof ActionMoveNearestAir))
            actions.get(this.current-1).cleanup(dungeonRoom, actionRouteProperties);
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
        getCurrentAction().onPlayerInteract(dungeonRoom, event, actionRouteProperties );
    }
    public void onLivingDeath(LivingDeathEvent event) {
        if (calculating) return;
        getCurrentAction().onLivingDeath(dungeonRoom, event, actionRouteProperties );
    }
    public void onRenderWorld(float partialTicks, boolean flag) {
        if (calculating) return;


        if (current -1 >= 0) {
            AbstractAction abstractAction = actions.get(current - 1);
            if(((abstractAction instanceof ActionMove && ((ActionMove) abstractAction).getTargetVec3().getPos(dungeonRoom).squareDistanceTo(Minecraft.getMinecraft().thePlayer.getPositionVector()) >= 25)
                            || (abstractAction instanceof ActionMoveNearestAir  && ((ActionMoveNearestAir) abstractAction).getTarget().getBlockPos(dungeonRoom).distanceSq(Minecraft.getMinecraft().thePlayer.getPosition()) >= 25))){
                abstractAction.onRenderWorld(dungeonRoom, partialTicks, actionRouteProperties, flag );
            }
        }
        getCurrentAction().onRenderWorld(dungeonRoom, partialTicks, actionRouteProperties, flag);



        getCurrentAction().onRenderWorld(dungeonRoom, partialTicks, actionRouteProperties, flag);
    }

    private final Function<DungeonRoom, Boolean> checkCanCancel;
    public void onRenderScreen(float partialTicks) {
        if (calculating) return;
        getCurrentAction().onRenderScreen(dungeonRoom, partialTicks, actionRouteProperties);
    }

    public void onTick() {
        if (calculating) return;
        AbstractAction currentAction = getCurrentAction();

        currentAction.onTick(dungeonRoom, actionRouteProperties);
        if (this.current -1 >= 0 && (actions.get(this.current-1) instanceof ActionMove || actions.get(this.current-1) instanceof ActionMoveNearestAir)) actions.get(this.current-1).onTick(dungeonRoom, actionRouteProperties );

        if (checkCanCancel != null && checkCanCancel.apply(dungeonRoom)) { // action change state
            this.current = actions.size() - 1;
        }


        while (currentAction.isComplete(dungeonRoom)) {
            next();
            currentAction = getCurrentAction();
        }

        boolean recalc = false;
//        for (int i = current; i < actions.size(); i++) {
//            if (actions.get(i).shouldRecalculatePath(dungeonRoom)) {
//                recalc = true;
//            }
//        }
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
        getCurrentAction().onLivingInteract(dungeonRoom, event, actionRouteProperties );
    }

}
