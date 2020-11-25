package kr.syeyoung.dungeonsguide.roomprocessor;

import kr.syeyoung.dungeonsguide.dungeon.roomfinder.DungeonRoom;

public class DefaultRoomProcessor implements RoomProcessor {

    public DefaultRoomProcessor(DungeonRoom dungeonRoom) {

    }

    @Override
    public void tick() {

    }

    @Override
    public void drawScreen() {

    }

    public static class Generator implements RoomProcessorGenerator<DefaultRoomProcessor> {
        @Override
        public DefaultRoomProcessor createNew(DungeonRoom dungeonRoom) {

            DefaultRoomProcessor defaultRoomProcessor = new DefaultRoomProcessor(dungeonRoom);
            return defaultRoomProcessor;
        }
    }
}
