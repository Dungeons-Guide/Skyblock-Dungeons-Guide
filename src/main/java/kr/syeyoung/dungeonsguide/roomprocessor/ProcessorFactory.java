package kr.syeyoung.dungeonsguide.roomprocessor;

import kr.syeyoung.dungeonsguide.roomprocessor.boxpuzzle.RoomProcessorBoxSolver;
import kr.syeyoung.dungeonsguide.roomprocessor.waterpuzzle.RoomProcessorWaterPuzzle;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ProcessorFactory {
    private static Map<String, RoomProcessorGenerator> map = new HashMap<String, RoomProcessorGenerator>();

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
    }
}
