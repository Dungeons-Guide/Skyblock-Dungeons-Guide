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

import kr.syeyoung.dungeonsguide.dungeon.actions.Action;
import kr.syeyoung.dungeonsguide.dungeon.actions.ActionChangeState;
import kr.syeyoung.dungeonsguide.dungeon.actions.ActionComplete;
import kr.syeyoung.dungeonsguide.dungeon.roomfinder.DungeonRoom;
import kr.syeyoung.dungeonsguide.events.PlayerInteractEntityEvent;
import lombok.Getter;
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
    private final List<Action> actions;

    private final DungeonRoom dungeonRoom;

    public ActionRoute(DungeonRoom dungeonRoom, String mechanic, String state) {
        this.mechanic = mechanic;
        this.state = state;

        ActionChangeState actionChangeState = new ActionChangeState(mechanic, state);
        ActionTree tree= ActionTree.buildActionTree(actionChangeState, dungeonRoom);
        actions = ActionTreeUtil.linearifyActionTree(tree);
        actions.add(new ActionComplete());
        current = 0;
        this.dungeonRoom = dungeonRoom;
    }

    public Action next() {
        current ++;
        if (current >= actions.size()) current = actions.size() - 1;
        return actions.get(current);
    }

    public Action prev() {
        current --;
        if (current < 0) current = 0;
        return actions.get(current);
    }

    public Action getCurrentAction() {
        return actions.get(current);
    }



    public void onPlayerInteract(PlayerInteractEvent event) {
        getCurrentAction().onPlayerInteract(dungeonRoom, event);
    }
    public void onLivingDeath(LivingDeathEvent event) {
        getCurrentAction().onLivingDeath(dungeonRoom, event);
    }
    public void onRenderWorld(float partialTicks) {
        getCurrentAction().onRenderWorld(dungeonRoom, partialTicks);
    }

    public void onRenderScreen(float partialTicks) {
        getCurrentAction().onRenderScreen(dungeonRoom, partialTicks);
    }

    public void onTick() {
        Action current = getCurrentAction();

        current.onTick(dungeonRoom);

        if (dungeonRoom.getMechanics().get(mechanic).getCurrentState(dungeonRoom).equals(state)) {
            this.current = actions.size() - 1;
        }

        if (current.isComplete(dungeonRoom))
            next();
    }

    public void onLivingInteract(PlayerInteractEntityEvent event) { getCurrentAction().onLivingInteract(dungeonRoom, event); }
}
