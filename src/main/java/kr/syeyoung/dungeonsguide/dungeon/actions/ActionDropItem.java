package kr.syeyoung.dungeonsguide.dungeon.actions;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import kr.syeyoung.dungeonsguide.dungeon.data.OffsetPoint;
import kr.syeyoung.dungeonsguide.dungeon.roomfinder.DungeonRoom;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Data
public class ActionDropItem extends AbstractAction {
    private Set<Action> preRequisite = new HashSet<Action>();
    private OffsetPoint target;
    private Predicate<EntityItem> predicate = Predicates.alwaysTrue();

    public ActionDropItem(OffsetPoint target) {
        this.target = target;
    }

    @Override
    public Set<Action> getPreRequisites(DungeonRoom dungeonRoom) {
        return preRequisite;
    }

    @Override
    public boolean isComplete(DungeonRoom dungeonRoom) {
        BlockPos pos = target.getBlockPos(dungeonRoom);
        List<EntityItem> item = dungeonRoom.getContext().getWorld().getEntitiesWithinAABB(EntityItem.class,
                AxisAlignedBB.fromBounds(pos.getX(), pos.getY(), pos.getZ(), pos.getX()+1, pos.getY() + 1, pos.getZ() + 1));
        if (item.size() == 0) return false;
        return (predicate == null || predicate.apply(item.get(0)));
    }

    @Override
    public String toString() {
        return "DropItem\n- target: "+target.toString()+"\n- predicate: "+predicate.getClass().getSimpleName();
    }
}
