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

package kr.syeyoung.dungeonsguide.mod.dungeon.roomprocessor.bombdefuse;


import kr.syeyoung.dungeonsguide.mod.dungeon.roomfinder.DungeonRoom;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomprocessor.GeneralRoomProcessor;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomprocessor.RoomProcessorGenerator;

public class RoomProcessorBombDefuseSolver extends GeneralRoomProcessor {

    public RoomProcessorBombDefuseSolver(DungeonRoom dungeonRoom) {
        super(dungeonRoom);
    }



    public static class Generator implements RoomProcessorGenerator<RoomProcessorBombDefuseSolver> {
        @Override
        public RoomProcessorBombDefuseSolver createNew(DungeonRoom dungeonRoom) {
            RoomProcessorBombDefuseSolver defaultRoomProcessor = new RoomProcessorBombDefuseSolver(dungeonRoom);
            return defaultRoomProcessor;
        }
    }
}
