package kr.syeyoung.dungeonsguide.roomprocessor;

import kr.syeyoung.dungeonsguide.dungeon.roomfinder.DungeonRoom;

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
        registerRoomProcessor("default", new DefaultRoomProcessor.Generator());
    }
}
