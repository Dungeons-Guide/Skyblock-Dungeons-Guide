package kr.syeyoung.dungeonsguide.dungeon.mechanics.action;

import kr.syeyoung.dungeonsguide.dungeon.roomfinder.DungeonRoom;
import lombok.Data;

import java.util.HashSet;
import java.util.Set;

@Data
public class ActionChangeState implements Action{
    private Set<Action> preRequisite2 = new HashSet<Action>();

    private String mechanicName;
    private String state;

    public ActionChangeState(String mechanicName, String state) {
        this.mechanicName = mechanicName;
        this.state = state;
    }

    @Override
    public Set<Action> getPreRequisites(DungeonRoom dungeonRoom) {
        Set<Action> set = new HashSet<Action>();
        set.addAll(preRequisite2);
        set.addAll(dungeonRoom.getDungeonRoomInfo().getMechanics().get(mechanicName).getAction(state, dungeonRoom));
        return set;
    }
}
