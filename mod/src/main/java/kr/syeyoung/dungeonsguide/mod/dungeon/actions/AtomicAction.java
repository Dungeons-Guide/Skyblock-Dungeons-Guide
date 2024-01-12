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

import kr.syeyoung.dungeonsguide.mod.dungeon.actions.route.ActionRouteProperties;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomfinder.DungeonRoom;
import kr.syeyoung.dungeonsguide.mod.events.impl.PlayerInteractEntityEvent;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class AtomicAction extends AbstractAction {


    @Getter
    private int current;
    @Getter
    private final List<AbstractAction> actions;


    public AtomicAction(List<AbstractAction> orderedActions, String name) {
        current = 0;
        this.actions = orderedActions;
        this.name = name;
    }

    public AbstractAction next(DungeonRoom dungeonRoom, ActionRouteProperties actionRouteProperties) {
        if (!(getCurrentAction() instanceof  ActionMove || getCurrentAction() instanceof  ActionMoveNearestAir))
            getCurrentAction().cleanup(dungeonRoom, actionRouteProperties);
        if (this.current -1 >= 0 && (actions.get(this.current-1) instanceof ActionMove || actions.get(this.current-1) instanceof ActionMoveNearestAir))
            actions.get(this.current-1).cleanup(dungeonRoom, actionRouteProperties);
        current ++;
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
        return current >= actions.size() ? null : actions.get(current);
    }


    @Override
    public void onPlayerInteract(DungeonRoom dungeonRoom, PlayerInteractEvent event, ActionRouteProperties actionRouteProperties) {
        getCurrentAction().onPlayerInteract(dungeonRoom, event, actionRouteProperties );
    }

    @Override
    public void onRenderWorld(DungeonRoom dungeonRoom, float partialTicks, ActionRouteProperties actionRouteProperties, boolean flag) {
        if (current -1 >= 0) {
            AbstractAction abstractAction = actions.get(current - 1);
            if(((abstractAction instanceof ActionMove && ((ActionMove) abstractAction).getTarget().getBlockPos(dungeonRoom).distanceSq(Minecraft.getMinecraft().thePlayer.getPosition()) >= 25)
                    || (abstractAction instanceof ActionMoveNearestAir  && ((ActionMoveNearestAir) abstractAction).getTarget().getBlockPos(dungeonRoom).distanceSq(Minecraft.getMinecraft().thePlayer.getPosition()) >= 25))){
                abstractAction.onRenderWorld(dungeonRoom, partialTicks, actionRouteProperties, flag );
            }
        }
        getCurrentAction().onRenderWorld(dungeonRoom, partialTicks, actionRouteProperties, flag);
    }

    @Override
    public void onLivingDeath(DungeonRoom dungeonRoom, LivingDeathEvent event, ActionRouteProperties actionRouteProperties) {
        getCurrentAction().onLivingDeath(dungeonRoom, event, actionRouteProperties );
    }

    @Override
    public void onRenderScreen(DungeonRoom dungeonRoom, float partialTicks, ActionRouteProperties actionRouteProperties) {
        getCurrentAction().onRenderScreen(dungeonRoom, partialTicks, actionRouteProperties);
    }

    @Override
    public void onLivingInteract(DungeonRoom dungeonRoom, PlayerInteractEntityEvent event, ActionRouteProperties actionRouteProperties) {
        getCurrentAction().onLivingInteract(dungeonRoom, event, actionRouteProperties );
    }

    @Override
    public void onTick(DungeonRoom dungeonRoom, ActionRouteProperties actionRouteProperties) {
        AbstractAction currentAction = getCurrentAction();

        currentAction.onTick(dungeonRoom, actionRouteProperties);
        if (this.current -1 >= 0 && (actions.get(this.current-1) instanceof ActionMove || actions.get(this.current-1) instanceof ActionMoveNearestAir)) actions.get(this.current-1).onTick(dungeonRoom, actionRouteProperties );
        if (this.actions.get(this.actions.size() - 1) instanceof ActionChangeState && this.actions.get(this.actions.size() - 1).isComplete(dungeonRoom)) {
            this.current = this.actions.size() - 1;
        }

        if (currentAction.isComplete(dungeonRoom)) {
            next(dungeonRoom, actionRouteProperties);
        }
    }

    @Override
    public void cleanup(DungeonRoom dungeonRoom, ActionRouteProperties actionRouteProperties) {
        super.cleanup(dungeonRoom, actionRouteProperties);
    }

    @Override
    public boolean isComplete(DungeonRoom dungeonRoom) {
        return getCurrentAction() == null;
    }

    @Override
    public double evalulateCost() {
        double cost = 0;
        for (AbstractAction action : getActions()) {
            cost += action.evalulateCost();
        }
        return cost;
    }

    @Getter
    private final String name;
    @Override
    public String toString() {
        return name+"\n"+ getActions().stream().map(AbstractAction::toString).collect(Collectors.joining("\n"));
    }

    public static class Builder {
        private List<AbstractAction> actions = new ArrayList<>();
        public Builder requires(AbstractAction abstractAction) {
            actions.add(0, abstractAction);
            return this;
        }

        public Builder requires(Supplier<AbstractAction> abstractActionSupplier) {
            return requires(abstractActionSupplier.get());
        }

        public AtomicAction build(String name) {
            return new AtomicAction(actions, name);
        }
    }
}
