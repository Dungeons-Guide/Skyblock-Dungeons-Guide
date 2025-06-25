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


import kr.syeyoung.dungeonsguide.mod.dungeon.data.OffsetPoint;
import kr.syeyoung.dungeonsguide.mod.dungeon.data.OffsetVec3;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomfinder.DungeonRoom;
import kr.syeyoung.modapi.ModAPI;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@EqualsAndHashCode(callSuper=false)
public class ActionMoveNearestAir extends AbstractActionMove {
    @Getter
    private OffsetVec3 target;

    public ActionMoveNearestAir(OffsetPoint target) {
        super(new OffsetVec3(target.getX()+0.5, target.getY(), target.getZ()+0.5));
        this.target = new OffsetVec3(target.getX()+0.5, target.getY(), target.getZ()+0.5);
    }
    public ActionMoveNearestAir(OffsetVec3 target) {
        super(target);
        this.target = target;
    }

    @Override
    public boolean isComplete(DungeonRoom dungeonRoom) {
        return target.getPos(dungeonRoom).distanceSq(ModAPI.getAPI().getPlayer().getPositionVector()) < 25;
    }

    @Override
    public String toString() {
        return "MoveNearestAir\n- target: "+target.toString();
    }

}
