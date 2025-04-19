package kr.syeyoung.dungeonsguide.mod.events.impl;

import kr.syeyoung.dungeonsguide.mod.dungeon.roomfinder.DungeonRoom;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraftforge.fml.common.eventhandler.Event;

@AllArgsConstructor @Getter
public class DungeonRoomEnterEvent extends Event {
    private DungeonRoom dungeonRoom;
}
