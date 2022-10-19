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

package kr.syeyoung.dungeonsguide.dungeon.actions;

import kr.syeyoung.dungeonsguide.dungeon.actions.tree.ActionRouteProperties;
import kr.syeyoung.dungeonsguide.dungeon.roomfinder.DungeonRoom;
import kr.syeyoung.dungeonsguide.events.impl.PlayerInteractEntityEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;

import java.util.Set;

public abstract class AbstractAction {
    public void onPlayerInteract(DungeonRoom dungeonRoom, PlayerInteractEvent event, ActionRouteProperties actionRouteProperties){

    }

    public void onRenderWorld(DungeonRoom dungeonRoom, float partialTicks, ActionRouteProperties actionRouteProperties, boolean flag) {

    }

    public void onLivingDeath(DungeonRoom dungeonRoom, LivingDeathEvent event, ActionRouteProperties actionRouteProperties) {

    }

    public void onRenderScreen(DungeonRoom dungeonRoom, float partialTicks, ActionRouteProperties actionRouteProperties) {

    }

    public void onLivingInteract(DungeonRoom dungeonRoom, PlayerInteractEntityEvent event, ActionRouteProperties actionRouteProperties) {

    }

    public void onTick(DungeonRoom dungeonRoom, ActionRouteProperties actionRouteProperties) {

    }

    public Set<AbstractAction> getPreRequisites(DungeonRoom dungeonRoom) {
        return null;
    }

    public boolean isComplete(DungeonRoom dungeonRoom) {
        return false;
    }
}
