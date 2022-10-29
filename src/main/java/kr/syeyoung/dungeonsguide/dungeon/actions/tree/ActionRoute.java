/*
 *     Dungeons Guide - The most intelligent Hypixel Skyblock Dungeons Mod
 *     Copyright (C) 2021  cyoung06
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Affero General Public License as published
 *     by the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Affero General Public License for more details.
 *
 *     You should have received a copy of the GNU Affero General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package kr.syeyoung.dungeonsguide.dungeon.actions.tree;

import kr.syeyoung.dungeonsguide.chat.ChatTransmitter;
import kr.syeyoung.dungeonsguide.dungeon.actions.*;
import kr.syeyoung.dungeonsguide.dungeon.roomfinder.DungeonRoom;
import kr.syeyoung.dungeonsguide.events.impl.PlayerInteractEntityEvent;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;

import java.util.List;

public class ActionRoute {
    @Getter
    private final String mechanic;
    @Getter
    private final String state;

    @Getter
    private int current;
    @Getter
    private final List<AbstractAction> actions;

    private final DungeonRoom dungeonRoom;

    @Getter
    private final ActionRouteProperties actionRouteProperties;

    public ActionRoute(DungeonRoom dungeonRoom, String mechanic, String state, ActionRouteProperties actionRouteProperties) {
        this.mechanic = mechanic;
        this.state = state;
        this.actionRouteProperties = actionRouteProperties;

        System.out.println("Creating Action Route with mechanic:" + mechanic + " State:" + state);
        ActionChangeState actionChangeState = new ActionChangeState(mechanic, state);
        ActionTree tree= ActionTree.buildActionTree(actionChangeState, dungeonRoom);
        actions = ActionTreeUtil.linearifyActionTree(tree);
        actions.add(new ActionComplete());
        ChatTransmitter.sendDebugChat("Created ActionRoute with " + actions.size() + " steps");
        ChatTransmitter.sendDebugChat("========== STEPS ==========");
        for (AbstractAction action : actions) {
            ChatTransmitter.sendDebugChat(action.toString());
        }
        ChatTransmitter.sendDebugChat("=========== END ===========");


        current = 0;
        this.dungeonRoom = dungeonRoom;
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

    public void onRenderScreen(float partialTicks) {
        getCurrentAction().onRenderScreen(dungeonRoom, partialTicks, actionRouteProperties);
    }

    public void onTick() {
        AbstractAction currentAction = getCurrentAction();

        currentAction.onTick(dungeonRoom, actionRouteProperties);
        if (this.current -1 >= 0 && (actions.get(this.current-1) instanceof ActionMove || actions.get(this.current-1) instanceof ActionMoveNearestAir)) actions.get(this.current-1).onTick(dungeonRoom, actionRouteProperties );

        if (dungeonRoom.getMechanics().get(mechanic).getCurrentState(dungeonRoom).equals(state)) {
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
