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

package kr.syeyoung.dungeonsguide.dungeon.actions.tree;

import kr.syeyoung.dungeonsguide.config.types.AColor;
import kr.syeyoung.dungeonsguide.dungeon.actions.*;
import kr.syeyoung.dungeonsguide.dungeon.roomfinder.DungeonRoom;
import kr.syeyoung.dungeonsguide.events.PlayerInteractEntityEvent;
import lombok.Data;
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
    private final List<Action> actions;

    private final DungeonRoom dungeonRoom;

    @Getter
    private final ActionRouteProperties actionRouteProperties;

    public ActionRoute(DungeonRoom dungeonRoom, String mechanic, String state, ActionRouteProperties actionRouteProperties) {
        this.mechanic = mechanic;
        this.state = state;
        this.actionRouteProperties = actionRouteProperties;

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
        getCurrentAction().onPlayerInteract(dungeonRoom, event, actionRouteProperties );
    }
    public void onLivingDeath(LivingDeathEvent event) {
        getCurrentAction().onLivingDeath(dungeonRoom, event, actionRouteProperties );
    }
    public void onRenderWorld(float partialTicks, boolean flag) {
        if (current -1 >= 0 && (
                (actions.get(current-1) instanceof ActionMove && ((ActionMove) actions.get(current-1)).getTarget().getBlockPos(dungeonRoom).distanceSq(Minecraft.getMinecraft().thePlayer.getPosition()) >= 25)
                        || (actions.get(current-1) instanceof ActionMoveNearestAir && ((ActionMoveNearestAir) actions.get(current-1)).getTarget().getBlockPos(dungeonRoom).distanceSq(Minecraft.getMinecraft().thePlayer.getPosition()) >= 25))) actions.get(current-1).onRenderWorld(dungeonRoom, partialTicks, actionRouteProperties, flag );
        getCurrentAction().onRenderWorld(dungeonRoom, partialTicks, actionRouteProperties, flag);
    }

    public void onRenderScreen(float partialTicks) {
        getCurrentAction().onRenderScreen(dungeonRoom, partialTicks, actionRouteProperties);
    }

    public void onTick() {
        Action current = getCurrentAction();

        current.onTick(dungeonRoom, actionRouteProperties);
        if (this.current -1 >= 0 && (actions.get(this.current-1) instanceof ActionMove || actions.get(this.current-1) instanceof ActionMoveNearestAir)) actions.get(this.current-1).onTick(dungeonRoom, actionRouteProperties );

        if (dungeonRoom.getMechanics().get(mechanic).getCurrentState(dungeonRoom).equals(state)) {
            this.current = actions.size() - 1;
        }

        if (current.isComplete(dungeonRoom))
            next();
    }

    public void onLivingInteract(PlayerInteractEntityEvent event) { getCurrentAction().onLivingInteract(dungeonRoom, event, actionRouteProperties ); }

    @Data
    public static class ActionRouteProperties {
        private boolean pathfind;
        private int lineRefreshRate;
        private AColor lineColor;
        private float lineWidth;

        private boolean beacon;
        private AColor beaconColor;
        private AColor beaconBeamColor;
    }
}
