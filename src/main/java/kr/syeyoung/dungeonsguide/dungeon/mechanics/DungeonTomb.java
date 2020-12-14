package kr.syeyoung.dungeonsguide.dungeon.mechanics;

import com.google.common.base.Predicate;
import kr.syeyoung.dungeonsguide.dungeon.data.OffsetPoint;
import kr.syeyoung.dungeonsguide.dungeon.data.OffsetPointSet;
import kr.syeyoung.dungeonsguide.dungeon.mechanics.action.*;
import kr.syeyoung.dungeonsguide.dungeon.mechanics.predicates.PredicateSuperBoom;
import kr.syeyoung.dungeonsguide.dungeon.roomfinder.DungeonRoom;
import kr.syeyoung.dungeonsguide.roomedit.EditingContext;
import kr.syeyoung.dungeonsguide.utils.RenderUtils;
import lombok.Data;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.EntityBat;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.BlockPos;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.*;
import java.util.List;

@Data
public class DungeonTomb implements DungeonMechanic, RouteBlocker {
    private OffsetPointSet secretPoint = new OffsetPointSet();
    private List<String> preRequisite = new ArrayList<String>();


    @Override
    public Set<Action> getAction(String state, DungeonRoom dungeonRoom) {
        if (!"broken".equalsIgnoreCase(state)) throw new IllegalArgumentException(state+" is not valid state for tomb");
        if (!isBlocking(dungeonRoom)) {
            return Collections.emptySet();
        }
        Set<Action> base;
        Set<Action> preRequisites = base = new HashSet<Action>();
        {
            ActionClickSet actionClick;
            preRequisites.add(actionClick = new ActionClickSet(secretPoint));
            actionClick.setPredicate(PredicateSuperBoom.INSTANCE);
            preRequisites = actionClick.getPreRequisite();
        }
        {
            ActionMoveNearestAir actionMove = new ActionMoveNearestAir(secretPoint.getOffsetPointList().get(0));
            preRequisites.add(actionMove);
            preRequisites = actionMove.getPreRequisite();
        }
        {
            for (String str : preRequisite) {
                ActionChangeState actionChangeState = new ActionChangeState(str.split(":")[0], str.split(":")[1]);
                preRequisites.add(actionChangeState);
            }
        }
        return base;
    }

    @Override
    public void highlight(Color color, String name, DungeonRoom dungeonRoom, float partialTicks) {
        if (secretPoint.getOffsetPointList().isEmpty()) return;
        OffsetPoint firstpt = secretPoint.getOffsetPointList().get(0);
        BlockPos pos = firstpt.getBlockPos(dungeonRoom);
        RenderUtils.drawTextAtWorld(name, pos.getX() +0.5f, pos.getY()+0.5f, pos.getZ()+0.25f, 0xFFFFFFFF, 0.03f, false, true, partialTicks);
        RenderUtils.drawTextAtWorld(getCurrentState(dungeonRoom), pos.getX() +0.5f, pos.getY()+0.75f, pos.getZ()+0.5f, 0xFFFFFFFF, 0.03f, false, true, partialTicks);

        for (OffsetPoint offsetPoint : secretPoint.getOffsetPointList()) {
            RenderUtils.highlightBlock(offsetPoint.getBlockPos(dungeonRoom), color,partialTicks);
        }
    }

    @Override
    public boolean isBlocking(DungeonRoom dungeonRoom) {
        for (OffsetPoint offsetPoint : secretPoint.getOffsetPointList()) {
            if (offsetPoint.getBlock(dungeonRoom) != Blocks.air) return true;
        }
        return false;
    }

    public DungeonTomb clone() throws CloneNotSupportedException {
        DungeonTomb dungeonSecret = new DungeonTomb();
        dungeonSecret.secretPoint = (OffsetPointSet) secretPoint.clone();
        dungeonSecret.preRequisite = new ArrayList<String>(preRequisite);
        return dungeonSecret;
    }

    @Override
    public String getCurrentState(DungeonRoom dungeonRoom) {
        Block b = Blocks.air;
        if (!secretPoint.getOffsetPointList().isEmpty())
            b = secretPoint.getOffsetPointList().get(0).getBlock(dungeonRoom);
        return b == Blocks.air ?"broken" :"blocking";
    }
}
