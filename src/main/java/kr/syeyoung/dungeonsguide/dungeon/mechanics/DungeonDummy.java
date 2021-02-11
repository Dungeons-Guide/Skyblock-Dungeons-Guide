package kr.syeyoung.dungeonsguide.dungeon.mechanics;

import com.google.common.collect.Sets;
import kr.syeyoung.dungeonsguide.dungeon.actions.Action;
import kr.syeyoung.dungeonsguide.dungeon.actions.ActionChangeState;
import kr.syeyoung.dungeonsguide.dungeon.actions.ActionInteract;
import kr.syeyoung.dungeonsguide.dungeon.actions.ActionMove;
import kr.syeyoung.dungeonsguide.dungeon.data.OffsetPoint;
import kr.syeyoung.dungeonsguide.dungeon.mechanics.predicates.PredicateArmorStand;
import kr.syeyoung.dungeonsguide.dungeon.roomfinder.DungeonRoom;
import kr.syeyoung.dungeonsguide.utils.RenderUtils;
import lombok.Data;
import net.minecraft.util.BlockPos;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Data
public class DungeonDummy implements DungeonMechanic {
    private OffsetPoint secretPoint = new OffsetPoint(0,0,0);
    private List<String> preRequisite = new ArrayList<String>();


    @Override
    public Set<Action> getAction(String state, DungeonRoom dungeonRoom) {
        if (!"navigate".equalsIgnoreCase(state)) throw new IllegalArgumentException(state+" is not valid state for secret");
        Set<Action> base;
        Set<Action> preRequisites = base = new HashSet<Action>();
        {
            ActionMove actionMove = new ActionMove(secretPoint);
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
        BlockPos pos = getSecretPoint().getBlockPos(dungeonRoom);
        RenderUtils.highlightBlock(pos, color,partialTicks);
        RenderUtils.drawTextAtWorld("D-"+name, pos.getX() +0.5f, pos.getY()+0.375f, pos.getZ()+0.5f, 0xFFFFFFFF, 0.03f, false, true, partialTicks);
        RenderUtils.drawTextAtWorld(getCurrentState(dungeonRoom), pos.getX() +0.5f, pos.getY()+0f, pos.getZ()+0.5f, 0xFFFFFFFF, 0.03f, false, true, partialTicks);
    }


    public DungeonDummy clone() throws CloneNotSupportedException {
        DungeonDummy dungeonSecret = new DungeonDummy();
        dungeonSecret.secretPoint = (OffsetPoint) secretPoint.clone();
        dungeonSecret.preRequisite = new ArrayList<String>(preRequisite);
        return dungeonSecret;
    }


    @Override
    public String getCurrentState(DungeonRoom dungeonRoom) {
        return "no-state";
    }

    @Override
    public Set<String> getPossibleStates(DungeonRoom dungeonRoom) {
        return Sets.newHashSet("navigate");
    }
    @Override
    public Set<String> getTotalPossibleStates(DungeonRoom dungeonRoom) {
        return Sets.newHashSet("no-state","navigate");
    }
    @Override
    public OffsetPoint getRepresentingPoint() {
        return secretPoint;
    }
}
