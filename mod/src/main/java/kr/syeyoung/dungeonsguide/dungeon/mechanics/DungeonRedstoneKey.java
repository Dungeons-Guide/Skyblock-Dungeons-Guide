/*
 * Dungeons Guide - The most intelligent Hypixel Skyblock Dungeons Mod
 * Copyright (C) 2024  cyoung06 (syeyoung)
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
import kr.syeyoung.dungeonsguide.dungeon.data.OffsetPointSet;
import kr.syeyoung.dungeonsguide.dungeon.mechanics.dunegonmechanic.DungeonMechanic;
import kr.syeyoung.dungeonsguide.mod.dungeon.actions.*;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomfinder.DungeonRoom;
import kr.syeyoung.dungeonsguide.mod.utils.RenderUtils;
import lombok.Data;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;

import java.awt.*;
import java.util.*;
import java.util.List;

@Data
public class DungeonRedstoneKey implements DungeonMechanic {
    private OffsetPoint secretPoint = new OffsetPoint(0, 0, 0);
    private List<String> preRequisite = new ArrayList<String>();
    private String triggering = "";

    @Override
    public Set<AbstractAction> getAction(String state, DungeonRoom dungeonRoom) {
        if (state.equals(getCurrentState(dungeonRoom))) return Collections.emptySet();
        if (state.equalsIgnoreCase("navigate")) {
            Set<AbstractAction> base;
            Set<AbstractAction> preRequisites = base = new HashSet<AbstractAction>();
            ActionMoveNearestAir actionMove = new ActionMoveNearestAir(getRepresentingPoint(dungeonRoom));
            preRequisites.add(actionMove);
            preRequisites = actionMove.getPreRequisite();
            for (String str : preRequisite) {
                if (str.isEmpty()) continue;
                ActionChangeState actionChangeState = new ActionChangeState(str.split(":")[0], str.split(":")[1]);
                preRequisites.add(actionChangeState);
            }
            return base;
        }

        if (!("obtained-self".equalsIgnoreCase(state) || "placed".equalsIgnoreCase(state))) throw new IllegalArgumentException(state+" is not valid state for secret");

        if (state.equalsIgnoreCase("obtained-self")) {

            Set<AbstractAction> base;
            Set<AbstractAction> preRequisites = base = new HashSet<AbstractAction>();
            if (!state.equalsIgnoreCase(getCurrentState(dungeonRoom))){
                ActionClick actionClick;
                preRequisites.add(actionClick = new ActionClick(secretPoint));
                preRequisites = actionClick.getPreRequisite();
            }
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
        } else {
            DungeonMechanic mechanic = dungeonRoom.getMechanics().get(triggering);
            return mechanic.getAction("triggered", dungeonRoom);
        }
    }

    @Override
    public void highlight(Color color, String name, DungeonRoom dungeonRoom, float partialTicks) {
        BlockPos pos = secretPoint.getBlockPos(dungeonRoom);
        RenderUtils.highlightBlock(pos, color,partialTicks);
        RenderUtils.drawTextAtWorld(name, pos.getX() +0.5f, pos.getY()+0.75f, pos.getZ()+0.5f, 0xFFFFFFFF, 0.03f, false, true, partialTicks);
        RenderUtils.drawTextAtWorld(getCurrentState(dungeonRoom), pos.getX() +0.5f, pos.getY()+0.25f, pos.getZ()+0.5f, 0xFFFFFFFF, 0.03f, false, true, partialTicks);
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        DungeonRedstoneKey dungeonSecret = new DungeonRedstoneKey();
        dungeonSecret.secretPoint = (OffsetPoint) secretPoint.clone();
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
        }
        String state = mechanic.getCurrentState(dungeonRoom);
        if ("triggered".equalsIgnoreCase(state)) {
            return "placed";
        }
        if (dungeonRoom.getRoomContext().containsKey("redstonekey")) {
            return "obtained-self";
        }
        if (secretPoint.getBlock(dungeonRoom) == Blocks.skull) {
            return "unobtained";
        }
        return "obtained-other";
    }

    @Override
    public Set<String> getPossibleStates(DungeonRoom dungeonRoom) {
        String currentState = getCurrentState(dungeonRoom);
        if (currentState.equalsIgnoreCase("obtained-self")) {
            return Sets.newHashSet("placed", "navigate");
        }
        if (currentState.equalsIgnoreCase("unobtained")) {
            return Sets.newHashSet("obtained-self", "navigate");
        }
        return Sets.newHashSet("navigate");
    }

    @Override
    public Set<String> getTotalPossibleStates(DungeonRoom dungeonRoom) {
        return Sets.newHashSet("placed", "obtained-self", "unobtained", "obtained-other");
    }

    @Override
    public OffsetPoint getRepresentingPoint(DungeonRoom dungeonRoom) {
        return secretPoint;
    }
}
