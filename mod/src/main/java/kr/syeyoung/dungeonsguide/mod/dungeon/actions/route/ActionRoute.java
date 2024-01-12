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

import kr.syeyoung.dungeonsguide.mod.chat.ChatTransmitter;
import kr.syeyoung.dungeonsguide.mod.dungeon.actions.*;
import kr.syeyoung.dungeonsguide.mod.dungeon.actions.tree.ActionDAG;
import kr.syeyoung.dungeonsguide.mod.dungeon.actions.tree.ActionDAGBuilder;
import kr.syeyoung.dungeonsguide.mod.dungeon.actions.tree.ActionDAGNode;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomfinder.DungeonRoom;
import kr.syeyoung.dungeonsguide.mod.events.impl.PlayerInteractEntityEvent;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ActionRoute {

    private String name;
    @Override
    public String toString() {
        return name;
    }

    @Getter
    private int current;
    @Getter
    private List<AbstractAction> actions;

    @Getter
    private final ActionDAG dag;
    @Getter
    private int dagId;
    @Getter
    private List<ActionDAGNode> order;

    @Getter
    private int[] nodeStatus;
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

    private void recalculatePath() {
        int cnt = 0;
        ChatTransmitter.sendDebugChat("ActionDAG has "+dag.getCount()+" Possible action set");
        List<ActionDAGNode> minCostRoute = null;
        double minCost = Double.POSITIVE_INFINITY;

        this.nodeStatus = dag.getNodeStatus(dag.getCount() - 1);
        for (List<ActionDAGNode> actionDAGNodes : dag.topologicalSort(dag.getCount() - 1)) {
            cnt ++;

            RoomState roomState = new RoomState();
            roomState.setPlayerPos(Minecraft.getMinecraft().thePlayer.getPositionVector());
            double cost = 0;
            for (ActionDAGNode actionDAGNode : actionDAGNodes) {
                cost += actionDAGNode.getAction().evalulateCost(roomState, dungeonRoom);
            }
            if (cost < minCost) {
                minCost = cost;
                minCostRoute = actionDAGNodes;
            }

            if (cnt > 100000) break;
        }
        if (minCostRoute == null) {
            System.out.println(minCostRoute);
            minCostRoute = new ArrayList<>();
        }
        this.dagId = dag.getCount() - 1;
        order = minCostRoute;
        ChatTransmitter.sendDebugChat("ActionRoute has "+cnt+" Possible subroutes :: Chosen route with "+minCost+" cost");

        actions = minCostRoute.stream().map(ActionDAGNode::getAction).collect(Collectors.toList());
        actions.add(new ActionComplete());
        current = 0;
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
        getCurrentAction().onPlayerInteract(dungeonRoom, event, actionRouteProperties );
    }
    public void onLivingDeath(LivingDeathEvent event) {
        getCurrentAction().onLivingDeath(dungeonRoom, event, actionRouteProperties );
    }
    public void onRenderWorld(float partialTicks, boolean flag) {


        if (current -1 >= 0) {
            AbstractAction abstractAction = actions.get(current - 1);
            if(((abstractAction instanceof ActionMove && ((ActionMove) abstractAction).getTarget().getBlockPos(dungeonRoom).distanceSq(Minecraft.getMinecraft().thePlayer.getPosition()) >= 25)
                            || (abstractAction instanceof ActionMoveNearestAir  && ((ActionMoveNearestAir) abstractAction).getTarget().getBlockPos(dungeonRoom).distanceSq(Minecraft.getMinecraft().thePlayer.getPosition()) >= 25))){
                abstractAction.onRenderWorld(dungeonRoom, partialTicks, actionRouteProperties, flag );
            }
        }
        getCurrentAction().onRenderWorld(dungeonRoom, partialTicks, actionRouteProperties, flag);



        getCurrentAction().onRenderWorld(dungeonRoom, partialTicks, actionRouteProperties, flag);
    }

    private final Function<DungeonRoom, Boolean> checkCanCancel;
    public void onRenderScreen(float partialTicks) {
        getCurrentAction().onRenderScreen(dungeonRoom, partialTicks, actionRouteProperties);
    }

    public void onTick() {
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
        for (int i = current; i < actions.size(); i++) {
            if (actions.get(i).isIdempotent() && actions.get(i).isComplete(dungeonRoom)) {
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
        getCurrentAction().onLivingInteract(dungeonRoom, event, actionRouteProperties );
    }

}
