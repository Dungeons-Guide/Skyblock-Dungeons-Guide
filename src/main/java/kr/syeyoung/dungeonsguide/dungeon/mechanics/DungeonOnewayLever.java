package kr.syeyoung.dungeonsguide.dungeon.mechanics;

import kr.syeyoung.dungeonsguide.dungeon.actions.Action;
import kr.syeyoung.dungeonsguide.dungeon.actions.ActionChangeState;
import kr.syeyoung.dungeonsguide.dungeon.actions.ActionClick;
import kr.syeyoung.dungeonsguide.dungeon.actions.ActionMove;
import kr.syeyoung.dungeonsguide.dungeon.data.OffsetPoint;
import kr.syeyoung.dungeonsguide.dungeon.roomfinder.DungeonRoom;
import kr.syeyoung.dungeonsguide.utils.RenderUtils;
import lombok.Data;
import net.minecraft.util.BlockPos;

import java.awt.*;
import java.util.*;
import java.util.List;

@Data
public class DungeonOnewayLever implements DungeonMechanic {
    private OffsetPoint leverPoint = new OffsetPoint(0,0,0);
    private List<String> preRequisite = new ArrayList<String>();
    private String triggering = "";

    @Override
    public Set<Action> getAction(String state, DungeonRoom dungeonRoom) {
        if (!("triggered".equalsIgnoreCase(state))) throw new IllegalArgumentException(state+" is not valid state for secret");
        Set<Action> base;
        Set<Action> preRequisites = base = new HashSet<Action>();
        {
            ActionClick actionClick;
            preRequisites.add(actionClick = new ActionClick(leverPoint));
            preRequisites = actionClick.getPreRequisite();
        }
        {
            ActionMove actionMove = new ActionMove(leverPoint);
            preRequisites.add(actionMove);
            preRequisites = actionMove.getPreRequisite();
        }
        {
            for (String str : preRequisite) {
                if (str.isEmpty()) continue;
                ActionChangeState actionChangeState = new ActionChangeState(str.split(":")[0], str.split(":")[1]);
                preRequisites.add(actionChangeState);
            }
        }
        return base;
    }

    @Override
    public void highlight(Color color, String name, DungeonRoom dungeonRoom, float partialTicks) {
        BlockPos pos = getLeverPoint().getBlockPos(dungeonRoom);
        RenderUtils.highlightBlock(pos, color,partialTicks);
        RenderUtils.drawTextAtWorld(name, pos.getX() +0.5f, pos.getY()+0.75f, pos.getZ()+0.5f, 0xFFFFFFFF, 0.03f, false, true, partialTicks);
        RenderUtils.drawTextAtWorld(getCurrentState(dungeonRoom), pos.getX() +0.5f, pos.getY()+0.25f, pos.getZ()+0.5f, 0xFFFFFFFF, 0.03f, false, true, partialTicks);
    }

    public DungeonOnewayLever clone() throws CloneNotSupportedException {
        DungeonOnewayLever dungeonSecret = new DungeonOnewayLever();
        dungeonSecret.leverPoint = (OffsetPoint) leverPoint.clone();
        dungeonSecret.triggering = triggering;
        dungeonSecret.preRequisite = new ArrayList<String>(preRequisite);
        return dungeonSecret;
    }


    @Override
    public String getCurrentState(DungeonRoom dungeonRoom) {
        if (triggering == null) triggering = "null";
        DungeonMechanic mechanic = dungeonRoom.getDungeonRoomInfo().getMechanics().get(triggering);
        if (mechanic == null)
        {
            return "undeterminable";
        } else {
            String state = mechanic.getCurrentState(dungeonRoom);
            if ("open".equalsIgnoreCase(state)) {
                return "triggered";
            } else {
                return "untriggered";
            }
        }
    }

    @Override
    public Set<String> getPossibleStates(DungeonRoom dungeonRoom) {
        String currentStatus = getCurrentState(dungeonRoom);
        if (currentStatus.equalsIgnoreCase("untriggered"))
            return Collections.singleton("triggered");
        return Collections.emptySet();
    }
}
