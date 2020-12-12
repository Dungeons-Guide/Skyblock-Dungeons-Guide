package kr.syeyoung.dungeonsguide.dungeon.mechanics;

import kr.syeyoung.dungeonsguide.dungeon.roomfinder.DungeonRoom;

public interface RouteBlocker {
    boolean isBlocking(DungeonRoom dungeonRoom);
}
