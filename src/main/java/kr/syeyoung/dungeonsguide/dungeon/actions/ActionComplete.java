package kr.syeyoung.dungeonsguide.dungeon.actions;

import kr.syeyoung.dungeonsguide.dungeon.roomfinder.DungeonRoom;

import java.util.Collections;
import java.util.Set;

public class ActionComplete extends AbstractAction {
    @Override
    public Set<Action> getPreRequisites(DungeonRoom dungeonRoom) {
        return Collections.emptySet();
    }

    @Override
    public boolean isComplete(DungeonRoom dungeonRoom) {
        return false;
    }

    @Override
    public String toString() {
        return "Completed";
    }
}
