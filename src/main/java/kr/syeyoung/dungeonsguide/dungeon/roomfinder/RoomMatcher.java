package kr.syeyoung.dungeonsguide.dungeon.roomfinder;

import kr.syeyoung.dungeonsguide.dungeon.data.DungeonRoomInfo;

public class RoomMatcher {
    private DungeonRoom dungeonRoom;
    public RoomMatcher(DungeonRoom dungeonRoom) {
        this.dungeonRoom = dungeonRoom;
    }

    public DungeonRoomInfo match() {
        return null;
    }

    public DungeonRoomInfo createNew() {
        return new DungeonRoomInfo();
    }
}
