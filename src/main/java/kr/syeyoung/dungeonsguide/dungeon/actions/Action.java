package kr.syeyoung.dungeonsguide.dungeon.actions;

import kr.syeyoung.dungeonsguide.dungeon.roomfinder.DungeonRoom;

import java.util.Set;

public interface Action {
    Set<Action> getPreRequisites(DungeonRoom dungeonRoom);
}
