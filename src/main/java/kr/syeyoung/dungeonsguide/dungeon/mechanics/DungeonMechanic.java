package kr.syeyoung.dungeonsguide.dungeon.mechanics;

import kr.syeyoung.dungeonsguide.dungeon.actions.Action;
import kr.syeyoung.dungeonsguide.dungeon.data.OffsetPoint;
import kr.syeyoung.dungeonsguide.dungeon.roomfinder.DungeonRoom;

import java.awt.*;
import java.io.Serializable;
import java.util.Set;

public interface DungeonMechanic extends Serializable {
    Set<Action> getAction(String state, DungeonRoom dungeonRoom);

    void highlight(Color color, String name, DungeonRoom dungeonRoom, float partialTicks);

    String getCurrentState(DungeonRoom dungeonRoom);

    Set<String> getPossibleStates(DungeonRoom dungeonRoom);
    Set<String> getTotalPossibleStates(DungeonRoom dungeonRoom);

    OffsetPoint getRepresentingPoint(DungeonRoom dungeonRoom);
}
