package kr.syeyoung.dungeonsguide.dungeon.actions;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import kr.syeyoung.dungeonsguide.dungeon.data.OffsetPoint;
import kr.syeyoung.dungeonsguide.dungeon.data.OffsetPointSet;
import kr.syeyoung.dungeonsguide.dungeon.roomfinder.DungeonRoom;
import kr.syeyoung.dungeonsguide.utils.RenderUtils;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;

import java.awt.*;
import java.util.HashSet;
import java.util.Set;

@Data
public class ActionClickSet extends AbstractAction {
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

    private boolean clicked = false;
    @Override
    public void onPlayerInteract(DungeonRoom dungeonRoom, PlayerInteractEvent event) {
        if (clicked) return;
        for (OffsetPoint pt2: target.getOffsetPointList()) {
            if (pt2.getBlockPos(dungeonRoom).equals(event.pos) &&
                    (predicate == null || predicate.apply(event.entityLiving.getHeldItem()))) {
                clicked = true;
            }
        }

    }

    @Override
    public void onRenderWorld(DungeonRoom dungeonRoom, float partialTicks) {
        for (OffsetPoint offsetPoint : target.getOffsetPointList()) {
            RenderUtils.highlightBlock(offsetPoint.getBlockPos(dungeonRoom), new Color(0, 255,0,50),partialTicks, true);
        }
    }

    @Override
    public boolean isComplete(DungeonRoom dungeonRoom) {
        return clicked;
    }
}
