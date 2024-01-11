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

import kr.syeyoung.dungeonsguide.dungeon.mechanics.DungeonDummy;
import kr.syeyoung.dungeonsguide.dungeon.mechanics.DungeonSecret;
import kr.syeyoung.dungeonsguide.dungeon.mechanics.dunegonmechanic.DungeonMechanic;
import kr.syeyoung.dungeonsguide.mod.dungeon.actions.tree.ActionDAGBuilder;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomfinder.DungeonRoom;
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
        DungeonMechanic mechanic = dungeonRoom.getMechanics().get(mechanicName);
        if (state.equalsIgnoreCase("navigate")) {
            return true;
        }
        if (state.equalsIgnoreCase("click")) {
            return true;
        }
        if (mechanic == null) {
            return false;
        }
        if (mechanic instanceof DungeonSecret && ((DungeonSecret) mechanic).getSecretType() == DungeonSecret.SecretType.BAT) {
            return true;
        }
        if (mechanic instanceof DungeonDummy) {
            return true;
        }
        return mechanic.getCurrentState(dungeonRoom).equalsIgnoreCase(state);
    }

    @Override
    public boolean isIdempotent() {
        return true;
    }

    @Override
    public ActionDAGBuilder buildActionDAG(ActionDAGBuilder builder, DungeonRoom dungeonRoom) throws PathfindImpossibleException {
        DungeonMechanic mechanic = dungeonRoom.getMechanics().get(mechanicName);
        if (mechanic!= null)
            mechanic.buildAction(state, dungeonRoom, builder);
        return new ActionDAGBuilder.ActionDAGBuilderNoMore(builder);
    }
}
