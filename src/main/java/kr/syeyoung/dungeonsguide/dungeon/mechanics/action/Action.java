package kr.syeyoung.dungeonsguide.dungeon.mechanics.action;

import kr.syeyoung.dungeonsguide.dungeon.roomfinder.DungeonRoom;

import java.util.Set;

public interface Action {
    Set<Action> getPreRequisites(DungeonRoom dungeonRoom);
}
