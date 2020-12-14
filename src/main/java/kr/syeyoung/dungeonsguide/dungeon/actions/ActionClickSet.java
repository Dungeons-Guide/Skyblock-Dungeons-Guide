package kr.syeyoung.dungeonsguide.dungeon.actions;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import kr.syeyoung.dungeonsguide.dungeon.data.OffsetPointSet;
import kr.syeyoung.dungeonsguide.dungeon.roomfinder.DungeonRoom;
import lombok.Data;
import net.minecraft.item.ItemStack;

import java.util.HashSet;
import java.util.Set;

@Data
public class ActionClickSet implements Action {
    private Set<Action> preRequisite = new HashSet<Action>();
    private OffsetPointSet target;
    private Predicate<ItemStack> predicate = Predicates.alwaysTrue();

    public ActionClickSet(OffsetPointSet target) {
        this.target = target;
    }

    @Override
    public Set<Action> getPreRequisites(DungeonRoom dungeonRoom) {
        return preRequisite;
    }

    @Override
    public String toString() {
        return "ClickSet\n- targets size: "+target.getOffsetPointList().size()+"\n- predicate: "+predicate.getClass().getSimpleName();
    }
}
