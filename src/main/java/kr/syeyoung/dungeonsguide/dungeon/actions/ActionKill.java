package kr.syeyoung.dungeonsguide.dungeon.actions;

import com.google.common.base.Predicate;
import kr.syeyoung.dungeonsguide.dungeon.data.OffsetPoint;
import kr.syeyoung.dungeonsguide.dungeon.roomfinder.DungeonRoom;
import lombok.Data;
import net.minecraft.entity.Entity;

import java.util.HashSet;
import java.util.Set;

@Data
public class ActionKill implements Action {
    private Set<Action> preRequisite = new HashSet<Action>();
    private OffsetPoint target;
    private Predicate<Entity> predicate;
    private int radius;

    public ActionKill(OffsetPoint target) {
        this.target = target;
    }

    @Override
    public Set<Action> getPreRequisites(DungeonRoom dungeonRoom) {
        return preRequisite;
    }

    @Override
    public String toString() {
        return "KillEntity\n\ttarget: "+target.toString()+"\n\tradius: "+radius+"\n\tpredicate: "+(predicate == null ? "null" : predicate.getClass().getSimpleName());
    }
}
