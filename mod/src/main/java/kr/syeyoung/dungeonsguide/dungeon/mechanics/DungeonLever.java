/*
 * Dungeons Guide - The most intelligent Hypixel Skyblock Dungeons Mod
 * Copyright (C) 2021  cyoung06
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package kr.syeyoung.dungeonsguide.dungeon.mechanics;

import com.google.common.collect.Sets;
import kr.syeyoung.dungeonsguide.dungeon.data.OffsetPoint;
import kr.syeyoung.dungeonsguide.dungeon.mechanics.dunegonmechanic.DungeonMechanic;
import kr.syeyoung.dungeonsguide.mod.dungeon.actions.*;
import kr.syeyoung.dungeonsguide.mod.dungeon.actions.tree.ActionBuilder;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomfinder.DungeonRoom;
import kr.syeyoung.dungeonsguide.mod.utils.RenderUtils;
import lombok.Data;
import net.minecraft.util.BlockPos;

import java.awt.*;
import java.util.List;
import java.util.*;

@Data
public class DungeonLever implements DungeonMechanic {
    private OffsetPoint leverPoint = new OffsetPoint(0,0,0);
    private List<String> preRequisite = new ArrayList<String>();
    private String triggering = "";

    @Override
    public Set<AbstractAction> getAction(String state, DungeonRoom dungeonRoom) throws PathfindImpossibleException {
        if (state.equals(getCurrentState(dungeonRoom))) return Collections.emptySet();
        if (state.equalsIgnoreCase("navigate")) {
            ActionBuilder actionBuilder = new ActionBuilder(dungeonRoom)
                    .requiresDo(new ActionMoveNearestAir(getRepresentingPoint(dungeonRoom)));
            for (String str : preRequisite) {
                if (str.isEmpty()) continue;
                actionBuilder.and(new ActionChangeState(str.split(":")[0], str.split(":")[1]));
            }
            return actionBuilder.getPreRequisites();
        }

        if (!("triggered".equalsIgnoreCase(state) || "untriggered".equalsIgnoreCase(state))) throw new PathfindImpossibleException(state+" is not valid state for secret");

        if (state.equalsIgnoreCase(getCurrentState(dungeonRoom))) return Collections.emptySet();

        ActionBuilder actionBuilder = new ActionBuilder(dungeonRoom)
                .requiresDo(new ActionBuilder(dungeonRoom)
                    .requiresDo(new ActionClick(leverPoint))
                    .requiresDo(new ActionMoveNearestAir(leverPoint))
                    .toAtomicAction("MoveAndClick")
                );

        {
            for (String str : preRequisite) {
                if (str.isEmpty()) continue;
                actionBuilder.and(new ActionChangeState(str.split(":")[0], str.split(":")[1]));
            }
        }
        return actionBuilder.getPreRequisites();
    }

    @Override
    public void highlight(Color color, String name, DungeonRoom dungeonRoom, float partialTicks) {
        BlockPos pos = getLeverPoint().getBlockPos(dungeonRoom);
        RenderUtils.highlightBlock(pos, color,partialTicks);
        RenderUtils.drawTextAtWorld(name, pos.getX() +0.5f, pos.getY()+0.75f, pos.getZ()+0.5f, 0xFFFFFFFF, 0.03f, false, true, partialTicks);
        RenderUtils.drawTextAtWorld(getCurrentState(dungeonRoom), pos.getX() +0.5f, pos.getY()+0.25f, pos.getZ()+0.5f, 0xFFFFFFFF, 0.03f, false, true, partialTicks);
    }

    public DungeonLever clone() throws CloneNotSupportedException {
        DungeonLever dungeonSecret = new DungeonLever();
        dungeonSecret.leverPoint = (OffsetPoint) leverPoint.clone();
        dungeonSecret.triggering = triggering;
        dungeonSecret.preRequisite = new ArrayList<String>(preRequisite);
        return dungeonSecret;
    }


    @Override
    public String getCurrentState(DungeonRoom dungeonRoom) {
        if (triggering == null) triggering = "null";
        DungeonMechanic mechanic = dungeonRoom.getMechanics().get(triggering);
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
            return Sets.newHashSet("navigate", "triggered");
        else if (currentStatus.equalsIgnoreCase("triggered"))
            return Sets.newHashSet("navigate","untriggered");
        return Sets.newHashSet("navigate");
    }
    @Override
    public Set<String> getTotalPossibleStates(DungeonRoom dungeonRoom) {
        return Sets.newHashSet("triggered", "untriggered");
    }
    @Override
    public OffsetPoint getRepresentingPoint(DungeonRoom dungeonRoom) {
        return leverPoint;
    }
}
