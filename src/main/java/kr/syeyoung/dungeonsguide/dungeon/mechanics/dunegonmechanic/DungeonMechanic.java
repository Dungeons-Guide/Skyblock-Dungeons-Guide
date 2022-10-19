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

package kr.syeyoung.dungeonsguide.dungeon.mechanics.dunegonmechanic;

import kr.syeyoung.dungeonsguide.dungeon.actions.AbstractAction;
import kr.syeyoung.dungeonsguide.dungeon.data.OffsetPoint;
import kr.syeyoung.dungeonsguide.dungeon.roomfinder.DungeonRoom;

import java.awt.*;
import java.io.Serializable;
import java.util.Set;

public interface DungeonMechanic extends Serializable {
    Set<AbstractAction> getAction(String state, DungeonRoom dungeonRoom);

    void highlight(Color color, String name, DungeonRoom dungeonRoom, float partialTicks);

    String getCurrentState(DungeonRoom dungeonRoom);

    Set<String> getPossibleStates(DungeonRoom dungeonRoom);
    Set<String> getTotalPossibleStates(DungeonRoom dungeonRoom);

    OffsetPoint getRepresentingPoint(DungeonRoom dungeonRoom);
}
