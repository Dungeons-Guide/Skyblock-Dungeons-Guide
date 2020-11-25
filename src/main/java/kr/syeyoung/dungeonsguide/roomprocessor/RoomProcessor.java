package kr.syeyoung.dungeonsguide.roomprocessor;

import kr.syeyoung.dungeonsguide.dungeon.roomfinder.DungeonRoom;

public interface RoomProcessor {
    void tick();
    void drawScreen();
}