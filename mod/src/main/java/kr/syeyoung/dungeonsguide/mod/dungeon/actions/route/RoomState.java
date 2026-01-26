/*
 * Dungeons Guide - The most intelligent Hypixel Skyblock Dungeons Mod
 * Copyright (C) 2024  cyoung06 (syeyoung)
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

package kr.syeyoung.dungeonsguide.mod.dungeon.actions.route;

import kr.syeyoung.dungeonsguide.mod.dungeon.data.OffsetVec3;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomfinder.DungeonRoom;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import net.minecraft.util.Vec3;

import java.util.*;

@Getter @Setter @ToString
public class RoomState {
    private DungeonRoom dungeonRoom;
    private Vec3 playerPos;
    private OffsetVec3 playerPosOff = new OffsetVec3();

    public void setPlayerPos(Vec3 playerPos) {
        this.playerPos = playerPos;
        this.playerPosOff.setPosInWorld(dungeonRoom, playerPos);
    }

    public int openMechanicsBitset;
    private final List<String> openMechanicsIndex;
    public RoomState(List<String> openMechanicsIndex) {
        this.openMechanicsIndex = openMechanicsIndex;
    }
}