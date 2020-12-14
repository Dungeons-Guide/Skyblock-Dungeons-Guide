package kr.syeyoung.dungeonsguide.dungeon.actions;

import kr.syeyoung.dungeonsguide.dungeon.roomfinder.DungeonRoom;
import lombok.Data;

import java.util.HashSet;
import java.util.Set;

@Data
public class ActionRoot implements Action {
    private Set<Action> preRequisite = new HashSet<Action>();

    public ActionRoot() {
    }

    @Override
    public Set<Action> getPreRequisites(DungeonRoom dungeonRoom) {
        return preRequisite;
    }

    @Override
    public String toString() {
        return "Action Root";
    }
}
