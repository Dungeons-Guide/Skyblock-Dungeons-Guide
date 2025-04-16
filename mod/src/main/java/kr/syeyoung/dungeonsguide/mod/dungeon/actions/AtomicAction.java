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

import kr.syeyoung.dungeonsguide.mod.dungeon.actions.route.RoomState;
import kr.syeyoung.dungeonsguide.mod.pathfinding.TSPCache;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomfinder.DungeonRoom;
import kr.syeyoung.dungeonsguide.mod.events.impl.PlayerInteractEntityEvent;
import lombok.Getter;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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

    public AbstractAction next() {
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
    public void onPlayerInteract(DungeonRoom dungeonRoom, PlayerInteractEvent event) {
        getCurrentAction().onPlayerInteract(dungeonRoom, event);
    }

    @Override
    public void onLivingDeath(DungeonRoom dungeonRoom, LivingDeathEvent event) {
        getCurrentAction().onLivingDeath(dungeonRoom, event);
    }

    @Override
    public void onLivingInteract(DungeonRoom dungeonRoom, PlayerInteractEntityEvent event) {
        getCurrentAction().onLivingInteract(dungeonRoom, event);
    }

    @Override
    public void onTick(DungeonRoom dungeonRoom) {
        AbstractAction currentAction = getCurrentAction();

        currentAction.onTick(dungeonRoom);
        if (this.actions.get(this.actions.size() - 1) instanceof ActionChangeState && this.actions.get(this.actions.size() - 1).isComplete(dungeonRoom)) {
            this.current = this.actions.size() - 1;
        }

        if (currentAction.isComplete(dungeonRoom)) {
            next();
        }
    }

    @Override
    public boolean isComplete(DungeonRoom dungeonRoom) {
        return getCurrentAction() == null;
    }

    @Override
    public double evalulateCost(RoomState state, DungeonRoom room, Map<String, Object> memoization, TSPCache tspCache) {
        double cost = 0;
        for (int i = 0; i < getActions().size(); i++) {
            cost += getActions().get(i).evalulateCost(state, room, memoization, tspCache);
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
