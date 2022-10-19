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

import kr.syeyoung.dungeonsguide.dungeon.actions.tree.ActionRoute;
import kr.syeyoung.dungeonsguide.dungeon.roomfinder.DungeonRoom;
import kr.syeyoung.dungeonsguide.events.impl.PlayerInteractEntityEvent;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;

import java.util.HashSet;
import java.util.Set;

@Data
@EqualsAndHashCode(callSuper=false)
public class ActionRoot implements AbstractAction {
    private Set<AbstractAction> preRequisite = new HashSet<>();

    @Override
    public void onPlayerInteract(DungeonRoom dungeonRoom, PlayerInteractEvent event, ActionRoute.ActionRouteProperties actionRouteProperties) {

    }

    @Override
    public void onRenderWorld(DungeonRoom dungeonRoom, float partialTicks, ActionRoute.ActionRouteProperties actionRouteProperties, boolean flag) {

    }

    @Override
    public void onLivingDeath(DungeonRoom dungeonRoom, LivingDeathEvent event, ActionRoute.ActionRouteProperties actionRouteProperties) {

    }

    @Override
    public void onRenderScreen(DungeonRoom dungeonRoom, float partialTicks, ActionRoute.ActionRouteProperties actionRouteProperties) {

    }

    @Override
    public void onLivingInteract(DungeonRoom dungeonRoom, PlayerInteractEntityEvent event, ActionRoute.ActionRouteProperties actionRouteProperties) {

    }

    @Override
    public void onTick(DungeonRoom dungeonRoom, ActionRoute.ActionRouteProperties actionRouteProperties) {

    }

    @Override
    public Set<AbstractAction> getPreRequisites(DungeonRoom dungeonRoom) {
        return preRequisite;
    }

    @Override
    public boolean isComplete(DungeonRoom dungeonRoom) {
        return true;
    }

    @Override
    public String toString() {
        return "Action Root";
    }
}
