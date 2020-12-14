package kr.syeyoung.dungeonsguide.dungeon.actions;

import kr.syeyoung.dungeonsguide.dungeon.data.OffsetPoint;
import kr.syeyoung.dungeonsguide.dungeon.roomfinder.DungeonRoom;
import lombok.Data;

import java.util.HashSet;
import java.util.Set;

@Data
public class ActionMove implements Action {
    private Set<Action> preRequisite = new HashSet<Action>();
    private OffsetPoint target;

    public ActionMove(OffsetPoint target) {
        this.target = target;
    }

    @Override
    public Set<Action> getPreRequisites(DungeonRoom dungeonRoom) {
        return preRequisite;
    }


    @Override
    public String toString() {
        return "Move\n\ttarget: "+target.toString();
    }
}
