package kr.syeyoung.dungeonsguide.dungeon.actions;

import kr.syeyoung.dungeonsguide.dungeon.mechanics.DungeonDummy;
import kr.syeyoung.dungeonsguide.dungeon.mechanics.DungeonFairySoul;
import kr.syeyoung.dungeonsguide.dungeon.mechanics.DungeonMechanic;
import kr.syeyoung.dungeonsguide.dungeon.mechanics.DungeonSecret;
import kr.syeyoung.dungeonsguide.dungeon.roomfinder.DungeonRoom;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.HashSet;
import java.util.Set;

@Data
public class ActionChangeState extends AbstractAction{
    @EqualsAndHashCode.Exclude
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
        DungeonMechanic mechanic = dungeonRoom.getDungeonRoomInfo().getMechanics().get(mechanicName);
        if (mechanic!= null)
            set.addAll(mechanic.getAction(state, dungeonRoom));
        return set;
    }
    @Override
    public String toString() {
        return "ChangeState\n- target: "+mechanicName+"\n- state: "+state;
    }

    @Override
    public boolean isComplete(DungeonRoom dungeonRoom) {
        DungeonMechanic mechanic = dungeonRoom.getDungeonRoomInfo().getMechanics().get(mechanicName);
        if (state.equalsIgnoreCase("navigate"))
            return true;
        if (mechanic== null)
            return false;
        if (mechanic instanceof DungeonSecret && ((DungeonSecret) mechanic).getSecretType() != DungeonSecret.SecretType.CHEST)
            return true;
        if (mechanic instanceof DungeonDummy)
            return true;
        return mechanic.getCurrentState(dungeonRoom).equalsIgnoreCase(state);
    }
}
