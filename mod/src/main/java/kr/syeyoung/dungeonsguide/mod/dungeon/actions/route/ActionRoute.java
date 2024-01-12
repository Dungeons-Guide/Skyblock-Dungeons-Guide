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
    private final List<AbstractAction> actions;

    private final DungeonRoom dungeonRoom;

    @Getter
    private final ActionRouteProperties actionRouteProperties;

    public ActionRoute(DungeonRoom dungeonRoom, String mechanic, String state, ActionRouteProperties actionRouteProperties)throws PathfindImpossibleException  {

        this.name = mechanic+" -> "+state;
        this.actionRouteProperties = actionRouteProperties;
        this.checkCanCancel = (dg) -> dg.getMechanics().get(mechanic).getCurrentState(dungeonRoom).equalsIgnoreCase(state);

        ActionDAGBuilder actionDAGBuilder = new ActionDAGBuilder(dungeonRoom);
        actionDAGBuilder.requires(new ActionChangeState(mechanic, state));
        ActionDAG dag = actionDAGBuilder.build();
        ChatTransmitter.sendDebugChat("ActionDAG has "+dag.getCount()+" Possible action set");
        int cnt = 0;
        List<ActionDAGNode> node = null;
        for (List<ActionDAGNode> actionDAGNodes : dag.topologicalSort(dag.getCount() - 1)) {
            cnt ++;
            node = actionDAGNodes;
        }
        ChatTransmitter.sendDebugChat("ActionRoute has "+cnt+" Possible subroutes");

        actions = node.stream().map(ActionDAGNode::getAction).collect(Collectors.toList());
        actions.add(new ActionComplete());


        current = 0;
        this.dungeonRoom = dungeonRoom;
    }

    public ActionRoute(DungeonRoom dungeonRoom, ActionDAG dag, ActionRouteProperties actionRouteProperties) throws PathfindImpossibleException  {
        this.name = "DAG";
        this.actionRouteProperties = actionRouteProperties;
        this.checkCanCancel = (dg) -> false;

        ChatTransmitter.sendDebugChat("ActionDAG has "+dag.getCount()+" Possible action set");
        int cnt = 0;
        List<ActionDAGNode> node = null;
        for (List<ActionDAGNode> actionDAGNodes : dag.topologicalSort(dag.getCount() - 1)) {
            cnt ++;
            node = actionDAGNodes;
            if (cnt > 100000) break;
        }
        if (node == null) {
            System.out.println(node);
        }
        ChatTransmitter.sendDebugChat("ActionRoute has "+cnt+" Possible subroutes");

        actions = node.stream().map(ActionDAGNode::getAction).collect(Collectors.toList());
        actions.add(new ActionComplete());


        current = 0;
        this.dungeonRoom = dungeonRoom;
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

        if (currentAction.isComplete(dungeonRoom)) {
            next();
        }
    }

    public void onLivingInteract(PlayerInteractEntityEvent event) {
        getCurrentAction().onLivingInteract(dungeonRoom, event, actionRouteProperties );
    }

}
