package kr.syeyoung.dungeonsguide.dungeon.actions;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import kr.syeyoung.dungeonsguide.dungeon.data.OffsetPoint;
import kr.syeyoung.dungeonsguide.dungeon.roomfinder.DungeonRoom;
import kr.syeyoung.dungeonsguide.utils.RenderUtils;
import lombok.Data;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;

import java.awt.*;
import java.util.HashSet;
import java.util.Set;

@Data
public class ActionBreakWithSuperBoom extends AbstractAction {
    private Set<Action> preRequisite = new HashSet<Action>();
    private OffsetPoint target;

    public ActionBreakWithSuperBoom(OffsetPoint target) {
        this.target = target;
    }

    @Override
    public Set<Action> getPreRequisites(DungeonRoom dungeonRoom) {
        return preRequisite;
    }

    @Override
    public boolean isComplete(DungeonRoom dungeonRoom) {
        return target.getBlock(dungeonRoom) == Blocks.air;
    }

    @Override
    public void onRenderWorld(DungeonRoom dungeonRoom, float partialTicks) {
        RenderUtils.highlightBlock(target.getBlockPos(dungeonRoom), new Color(255, 0,0,255),partialTicks, false);
    }

    @Override
    public String toString() {
        return "Click\n- target: "+target.toString();
    }
}