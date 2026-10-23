package kr.syeyoung.dungeonsguide.mod.events.impl;

import kr.syeyoung.dungeonsguide.mod.dungeon.roomfinder.DungeonRoom;
import lombok.AllArgsConstructor;
import lombok.Getter;
import kr.syeyoung.dungeonsguide.mod.events.DGEvent;

@AllArgsConstructor @Getter
public class DungeonRoomEnterEvent extends DGEvent {
    private DungeonRoom dungeonRoom;
}
