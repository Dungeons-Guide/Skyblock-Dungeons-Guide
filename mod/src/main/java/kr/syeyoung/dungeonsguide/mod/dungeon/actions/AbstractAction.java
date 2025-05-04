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

import kr.syeyoung.dungeonsguide.mod.dungeon.actions.route.RoomState;
import kr.syeyoung.dungeonsguide.mod.dungeon.actions.tree.ActionDAGBuilder;
import kr.syeyoung.dungeonsguide.mod.pathfinding.TSPCache;
import kr.syeyoung.dungeonsguide.mod.pathfinding.abilitysetting.AlgorithmSetting;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomfinder.DungeonRoom;
import kr.syeyoung.dungeonsguide.mod.events.impl.PlayerInteractEntityEvent;
import kr.syeyoung.dungeonsguide.mod.pathfinding.preset.RoomPresetPathPlanner;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;

public abstract class AbstractAction {
    public void onPlayerInteract(DungeonRoom dungeonRoom, PlayerInteractEvent event){

    }

    public void onLivingDeath(DungeonRoom dungeonRoom, LivingDeathEvent event) {

    }

    public void onLivingInteract(DungeonRoom dungeonRoom, PlayerInteractEntityEvent event) {

    }

    public void onTick(DungeonRoom dungeonRoom) {

    }


    public boolean isComplete(DungeonRoom dungeonRoom) {
        return false;
    }
    public boolean shouldRecalculatePath(DungeonRoom dungeonRoom) {
        return false;
    }

    public double evalulateCost(RoomState state, DungeonRoom room, TSPCache tspCache, RoomPresetPathPlanner pathPlanner) { return 0; }

    public boolean isIdempotent() { return false; }
    public boolean isSanityCheck() { return false; }
    public boolean childComplete() { return true; }

    public ActionDAGBuilder buildActionDAG(ActionDAGBuilder builder, DungeonRoom dungeonRoom, AlgorithmSetting algorithmSetting) throws PathfindImpossibleException { return builder; }
}
