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
import kr.syeyoung.dungeonsguide.mod.dungeon.actions.tree.ActionBuilder;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomfinder.DungeonRoom;
import kr.syeyoung.dungeonsguide.mod.utils.RenderUtils;
import lombok.Data;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;

import java.awt.*;
import java.util.*;
import java.util.List;

@Data
public class DungeonRedstoneKeySlot implements DungeonMechanic {

    private static final long serialVersionUID = -3203171200265940652L;
    private OffsetPoint slotPoint = new OffsetPoint(0,0,0);
    private OffsetPointSet headPoint = new OffsetPointSet();
    private List<String> preRequisite = new ArrayList<String>();

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
        if (!("triggered".equalsIgnoreCase(state))) throw new PathfindImpossibleException(state+" is not valid state for secret");
        ActionBuilder actionBuilder = new ActionBuilder(dungeonRoom)
                .requiresDo(new ActionBuilder(dungeonRoom)
                        .requiresDo(new ActionClick(slotPoint))
                        .requiresDo(new ActionMove(slotPoint))
                        .toAtomicAction("MoveAndClick"));
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
        BlockPos pos = getSlotPoint().getBlockPos(dungeonRoom);
        RenderUtils.highlightBlock(pos, color,partialTicks);
        RenderUtils.drawTextAtWorld(name, pos.getX() +0.5f, pos.getY()+0.75f, pos.getZ()+0.5f, 0xFFFFFFFF, 0.03f, false, true, partialTicks);
        RenderUtils.drawTextAtWorld(getCurrentState(dungeonRoom), pos.getX() +0.5f, pos.getY()+0.25f, pos.getZ()+0.5f, 0xFFFFFFFF, 0.03f, false, true, partialTicks);
    }

    public DungeonRedstoneKeySlot clone() throws CloneNotSupportedException {
        DungeonRedstoneKeySlot dungeonSecret = new DungeonRedstoneKeySlot();
        dungeonSecret.slotPoint = (OffsetPoint) slotPoint.clone();
        dungeonSecret.headPoint = (OffsetPointSet) headPoint.clone();
        dungeonSecret.preRequisite = new ArrayList<String>(preRequisite);
        return dungeonSecret;
    }


    @Override
    public String getCurrentState(DungeonRoom dungeonRoom) {
        for (OffsetPoint offsetPoint : headPoint.getOffsetPointList()) {
            if (offsetPoint.getBlock(dungeonRoom) == Blocks.skull) {
                return "triggered";
            }
        }
        return "untriggered";
    }

    @Override
    public Set<String> getPossibleStates(DungeonRoom dungeonRoom) {
        String currentStatus = getCurrentState(dungeonRoom);
        if (currentStatus.equalsIgnoreCase("untriggered"))
            return Sets.newHashSet("navigate", "triggered");
        return Sets.newHashSet("navigate");
    }
    @Override
    public Set<String> getTotalPossibleStates(DungeonRoom dungeonRoom) {
        return Sets.newHashSet("triggered", "untriggered");
    }

    @Override
    public OffsetPoint getRepresentingPoint(DungeonRoom dungeonRoom) {
        return slotPoint;
    }
}
