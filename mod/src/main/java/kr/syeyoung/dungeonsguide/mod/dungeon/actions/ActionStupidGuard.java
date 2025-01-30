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

package kr.syeyoung.dungeonsguide.mod.dungeon.actions;

import kr.syeyoung.dungeonsguide.mod.dungeon.actions.route.ActionRouteProperties;
import kr.syeyoung.dungeonsguide.mod.dungeon.actions.route.RoomState;
import kr.syeyoung.dungeonsguide.mod.dungeon.pathfinding.TSPCache;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomfinder.DungeonRoom;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;

import java.util.Map;

@Data
@EqualsAndHashCode(callSuper=false)
public class ActionStupidGuard extends AbstractAction {


    public ActionStupidGuard() {

    }

    @Override
    public boolean isComplete(DungeonRoom dungeonRoom) {
        return true;
    }

    @Override
    public void onPlayerInteract(DungeonRoom dungeonRoom, PlayerInteractEvent event, ActionRouteProperties actionRouteProperties) {

    }
    @Override
    public void onRenderWorld(DungeonRoom dungeonRoom, float partialTicks, ActionRouteProperties actionRouteProperties, boolean flag) {
    }

    @Override
    public boolean isSanityCheck() {
        return true;
    }

    @Override
    public boolean childComplete() {
        return true;
    }
    @Override
    public String toString() {
        return "Stupid Guard";
    }

    @Override
    public double evalulateCost(RoomState state, DungeonRoom room, Map<String, Object> memoization, TSPCache tspCache) {
        return state.isStupidHeuristic() ? Double.POSITIVE_INFINITY : 0;
    }
}
