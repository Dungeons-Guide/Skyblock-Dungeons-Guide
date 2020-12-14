package kr.syeyoung.dungeonsguide.dungeon.mechanics;

import kr.syeyoung.dungeonsguide.dungeon.mechanics.action.Action;
import kr.syeyoung.dungeonsguide.dungeon.roomfinder.DungeonRoom;

import java.awt.*;
import java.util.Set;

public interface DungeonMechanic {
    Set<Action> getAction(String state, DungeonRoom dungeonRoom);

    void highlight(Color color, String name, DungeonRoom dungeonRoom, float partialTicks);

    String getCurrentState(DungeonRoom dungeonRoom);
}
