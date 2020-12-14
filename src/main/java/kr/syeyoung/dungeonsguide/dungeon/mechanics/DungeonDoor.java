package kr.syeyoung.dungeonsguide.dungeon.mechanics;

import kr.syeyoung.dungeonsguide.dungeon.data.OffsetPoint;
import kr.syeyoung.dungeonsguide.dungeon.data.OffsetPointSet;
import kr.syeyoung.dungeonsguide.dungeon.actions.Action;
import kr.syeyoung.dungeonsguide.dungeon.actions.ActionChangeState;
import kr.syeyoung.dungeonsguide.dungeon.actions.ActionClickSet;
import kr.syeyoung.dungeonsguide.dungeon.actions.ActionMoveNearestAir;
import kr.syeyoung.dungeonsguide.dungeon.mechanics.predicates.PredicateSuperBoom;
import kr.syeyoung.dungeonsguide.dungeon.roomfinder.DungeonRoom;
import kr.syeyoung.dungeonsguide.utils.RenderUtils;
import lombok.Data;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;

import java.awt.*;
import java.util.List;
import java.util.*;

@Data
public class DungeonDoor implements DungeonMechanic, RouteBlocker {
    private OffsetPointSet secretPoint = new OffsetPointSet();
    private List<String> openPreRequisite = new ArrayList<String>();
    private List<String> closePreRequisite = new ArrayList<String>();


    @Override
    public Set<Action> getAction(String state, DungeonRoom dungeonRoom) {
        if (!("open".equalsIgnoreCase(state) || "closed".equalsIgnoreCase(state))) throw new IllegalArgumentException(state+" is not valid state for door");
        if (!isBlocking(dungeonRoom)) {
            return Collections.emptySet();
        }
        Set<Action> base;
        Set<Action> preRequisites = base = new HashSet<Action>();
        if (!state.equalsIgnoreCase(getCurrentState(dungeonRoom))) {
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
            if (state.equalsIgnoreCase("open")) {
                for (String str : openPreRequisite) {
                    if (str.isEmpty()) continue;
                    ActionChangeState actionChangeState = new ActionChangeState(str.split(":")[0], str.split(":")[1]);
                    preRequisites.add(actionChangeState);
                }
            } else {
                for (String str : closePreRequisite) {
                    if (str.isEmpty()) continue;
                    ActionChangeState actionChangeState = new ActionChangeState(str.split(":")[0], str.split(":")[1]);
                    preRequisites.add(actionChangeState);
                }
            }
        }
        return base;
    }

    @Override
    public void highlight(Color color, String name, DungeonRoom dungeonRoom, float partialTicks) {
        if (secretPoint.getOffsetPointList().isEmpty()) return;
        OffsetPoint firstpt = secretPoint.getOffsetPointList().get(0);
        BlockPos pos = firstpt.getBlockPos(dungeonRoom);
        RenderUtils.drawTextAtWorld(name, pos.getX() +0.5f, pos.getY()+0.75f, pos.getZ()+0.5f, 0xFFFFFFFF, 0.03f, false, true, partialTicks);
        RenderUtils.drawTextAtWorld(getCurrentState(dungeonRoom), pos.getX() +0.5f, pos.getY()+0.25f, pos.getZ()+0.5f, 0xFFFFFFFF, 0.03f, false, true, partialTicks);

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

    public DungeonDoor clone() throws CloneNotSupportedException {
        DungeonDoor dungeonSecret = new DungeonDoor();
        dungeonSecret.secretPoint = (OffsetPointSet) secretPoint.clone();
        dungeonSecret.openPreRequisite = new ArrayList<String>(openPreRequisite);
        dungeonSecret.closePreRequisite = new ArrayList<String>(closePreRequisite);
        return dungeonSecret;
    }

    @Override
    public String getCurrentState(DungeonRoom dungeonRoom) {
        return isBlocking(dungeonRoom) ?"closed":"open";
    }


    @Override
    public Set<String> getPossibleStates(DungeonRoom dungeonRoom) {
        String currentStatus = getCurrentState(dungeonRoom);
        if (currentStatus.equalsIgnoreCase("closed"))
            return Collections.singleton("open");
        else if (currentStatus.equalsIgnoreCase("open"))
            return Collections.singleton("closed");
        return Collections.emptySet();
    }
}
