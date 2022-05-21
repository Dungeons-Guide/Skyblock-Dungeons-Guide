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

package kr.syeyoung.dungeonsguide.roomprocessor;

import kr.syeyoung.dungeonsguide.roomprocessor.bombdefuse.RoomProcessorBombDefuseSolver;
import kr.syeyoung.dungeonsguide.roomprocessor.boxpuzzle.RoomProcessorBoxSolver;
import kr.syeyoung.dungeonsguide.roomprocessor.icefill.RoomProcessorIcePath2;
import kr.syeyoung.dungeonsguide.roomprocessor.waterpuzzle.RoomProcessorWaterPuzzle;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ProcessorFactory {
    private static final Map<String, RoomProcessorGenerator> map = new HashMap<String, RoomProcessorGenerator>();

    public static RoomProcessorGenerator getRoomProcessorGenerator(String processorId) {
        return map.get(processorId);
    }

    public static void registerRoomProcessor(String processorId, RoomProcessorGenerator generator) {
        map.put(processorId, generator);
    }

    public static Set<String> getProcessors() {
        return map.keySet();
    }

    static {
        registerRoomProcessor("default", new GeneralRoomProcessor.Generator());
        registerRoomProcessor("button_5", new RoomProcessorButtonSolver.Generator());
        registerRoomProcessor("puzzle_water_solver", new RoomProcessorWaterPuzzle.Generator());
        registerRoomProcessor("puzzle_teleport_solver", new RoomProcessorTeleportMazeSolver.Generator());
        registerRoomProcessor("puzzle_riddle_solver", new RoomProcessorRiddle.Generator());
        registerRoomProcessor("puzzle_creeper_solver", new RoomProcessorCreeperSolver.Generator());
        registerRoomProcessor("puzzle_tictactoe_solver", new RoomProcessorTicTacToeSolver.Generator());

        registerRoomProcessor("puzzle_blaze_solver", new RoomProcessorBlazeSolver.Generator());


        registerRoomProcessor("puzzle_silverfish", new RoomProcessorIcePath.Generator()); // done
        registerRoomProcessor("puzzle_icefill", new RoomProcessorIcePath2.Generator());
        registerRoomProcessor("puzzle_box", new RoomProcessorBoxSolver.Generator());
        registerRoomProcessor("puzzle_trivia", new RoomProcessorTrivia.Generator());
        registerRoomProcessor("puzzle_bombdefuse", new RoomProcessorBombDefuseSolver.Generator());

        registerRoomProcessor("bossroom", new RoomProcessorRedRoom.Generator());
    }
}
