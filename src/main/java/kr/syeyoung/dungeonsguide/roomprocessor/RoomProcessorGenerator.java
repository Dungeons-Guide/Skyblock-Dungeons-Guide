package kr.syeyoung.dungeonsguide.roomprocessor;

import kr.syeyoung.dungeonsguide.dungeon.roomfinder.DungeonRoom;

public interface RoomProcessorGenerator<T extends RoomProcessor> {
    T createNew(DungeonRoom dungeonRoom);
}
