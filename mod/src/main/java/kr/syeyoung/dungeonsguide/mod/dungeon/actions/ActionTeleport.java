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

package kr.syeyoung.dungeonsguide.mod.dungeon.actions;

import kr.syeyoung.dungeonsguide.dungeon.data.OffsetPoint;
import kr.syeyoung.dungeonsguide.mod.dungeon.actions.route.RoomState;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomfinder.DungeonRoom;
import net.minecraft.util.Vec3;

import java.util.Map;

public class ActionTeleport extends AbstractAction {
    private OffsetPoint target;
    public ActionTeleport(OffsetPoint target) {
        this.target = target;
    }

    @Override
    public boolean isComplete(DungeonRoom dungeonRoom) {
        return true;
    }

    @Override
    public String toString() {
        return "Teleport to ("+target+")";
    }

    @Override
    public boolean isSanityCheck() {
        return true;
    }

    @Override
    public double evalulateCost(RoomState state, DungeonRoom room, Map<String, Object> memoization) {
        state.setPlayerPos(new Vec3(target.getBlockPos(room)));
        return 0;
    }
}
