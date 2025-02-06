package kr.syeyoung.dungeonsguide.mod.dungeon.data.mechanics.dunegonmechanic;

import kr.syeyoung.dungeonsguide.mod.dungeon.roomfinder.DungeonRoom;

public interface DungeonMechanicData {
    public DungeonMechanicState createState(DungeonRoom room);
}
