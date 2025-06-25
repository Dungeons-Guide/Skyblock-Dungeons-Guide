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
import kr.syeyoung.dungeonsguide.mod.dungeon.data.mechanics.DungeonBreakableWallState;
import kr.syeyoung.dungeonsguide.mod.dungeon.data.mechanics.DungeonDoorState;
import kr.syeyoung.dungeonsguide.mod.dungeon.data.mechanics.DungeonOnewayDoorState;
import kr.syeyoung.dungeonsguide.mod.dungeon.data.mechanics.DungeonTombState;
import kr.syeyoung.dungeonsguide.mod.dungeon.data.mechanics.dunegonmechanic.DungeonMechanicState;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomfinder.DungeonRoom;
import kr.syeyoung.dungeonsguide.mod.pathfinding.TSPCache;
import kr.syeyoung.dungeonsguide.mod.pathfinding.abilitysetting.AlgorithmSetting;
import kr.syeyoung.dungeonsguide.mod.pathfinding.preset.RoomPresetPathPlanner;
import kr.syeyoung.modapi.ModAPI;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=false)
public class ActionChangeState extends AbstractAction {

    private String mechanicName;
    private String state;

    public ActionChangeState(String mechanicName, String state) {
        this.mechanicName = mechanicName;
        this.state = state;
    }

    @Override
    public String toString() {
        return "ChangeState\n- target: "+mechanicName+"\n- state: "+state;
    }

    @Override
    public boolean isComplete(DungeonRoom dungeonRoom) {
        DungeonMechanicState mechanic = dungeonRoom.getMechanics().get(mechanicName);
        if (state.equalsIgnoreCase("navigate")) {
            return ModAPI.getAPI().getPlayer().getPositionVector().distanceSq(mechanic.getRepresentingPoint().getBlockPos(dungeonRoom)) < 36;
        }
        if (state.equalsIgnoreCase("click")) {
            return true;
        }
        if (mechanic == null) {
            return false;
        }
        return mechanic.getCurrentState().equalsIgnoreCase(state);
    }

    @Override
    public boolean shouldRecalculatePath(DungeonRoom dungeonRoom) {
        return dungeonRoom.getMechanics()
                .get(mechanicName)
                .getCurrentState()
                .equalsIgnoreCase(state);
    }

    @Override
    public double evalulateCost(RoomState state, DungeonRoom room, TSPCache tspCache, RoomPresetPathPlanner pathPlanner) {
        DungeonMechanicState mechanic = room.getMechanics().get(mechanicName);
        if (mechanic instanceof DungeonTombState || mechanic instanceof DungeonOnewayDoorState || mechanic instanceof DungeonDoorState || mechanic instanceof DungeonBreakableWallState) {
            if (this.state.equals("open")) {
                int index = state.getOpenMechanicsIndex().indexOf(mechanicName);
                if (index != -1) {
                    state.openMechanicsBitset  |= (1 << index);
                }
            }
        }
        return 0;
    }

    @Override
    public boolean isIdempotent() {
        return true;
    }

    @Override
    public boolean isSanityCheck() {
        return true;
    }

    @Override
    public boolean childComplete() {
        return mechanicName.contains("dummy") || state.equalsIgnoreCase("navigate") || state.equalsIgnoreCase("click");
    }

    @Override
    public ActionDAGBuilder buildActionDAG(ActionDAGBuilder builder, DungeonRoom dungeonRoom, AlgorithmSetting algorithmSetting) throws PathfindImpossibleException {
        DungeonMechanicState mechanic = dungeonRoom.getMechanics().get(mechanicName);
        if (mechanic!= null && !mechanic.getCurrentState().equalsIgnoreCase(state))
            mechanic.buildAction(state, builder, algorithmSetting);
        return new ActionDAGBuilder.ActionDAGBuilderNoMore(builder);
    }
}
