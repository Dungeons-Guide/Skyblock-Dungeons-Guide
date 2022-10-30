/*
 *     Dungeons Guide - The most intelligent Hypixel Skyblock Dungeons Mod
 *     Copyright (C) 2021  cyoung06
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Affero General Public License as published
 *     by the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Affero General Public License for more details.
 *
 *     You should have received a copy of the GNU Affero General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package kr.syeyoung.dungeonsguide.mod.dungeon.mechanics;

import com.google.common.collect.Sets;
import kr.syeyoung.dungeonsguide.mod.dungeon.actions.AbstractAction;
import kr.syeyoung.dungeonsguide.mod.dungeon.data.OffsetPoint;
import kr.syeyoung.dungeonsguide.mod.dungeon.data.OffsetPointSet;
import kr.syeyoung.dungeonsguide.mod.dungeon.actions.ActionChangeState;
import kr.syeyoung.dungeonsguide.mod.dungeon.mechanics.dunegonmechanic.RouteBlocker;
import kr.syeyoung.dungeonsguide.mod.dungeon.mechanics.dunegonmechanic.DungeonMechanic;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomfinder.DungeonRoom;
import kr.syeyoung.dungeonsguide.mod.utils.RenderUtils;
import lombok.Data;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;

import java.awt.*;
import java.util.List;
import java.util.*;

@Data
public class DungeonDoor implements DungeonMechanic, RouteBlocker {
    private static final long serialVersionUID = -1011605722415475761L;
    private OffsetPointSet secretPoint = new OffsetPointSet();
    private List<String> openPreRequisite = new ArrayList<String>();
    private List<String> closePreRequisite = new ArrayList<String>();


    @Override
    public Set<AbstractAction> getAction(String state, DungeonRoom dungeonRoom) {
        if (!("open".equalsIgnoreCase(state) || "closed".equalsIgnoreCase(state))) throw new IllegalArgumentException(state+" is not valid state for door");
        if (state.equalsIgnoreCase(getCurrentState(dungeonRoom))) return Collections.emptySet();
        Set<AbstractAction> base;
        Set<AbstractAction> preRequisites = base = new HashSet<AbstractAction>();
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
    @Override
    public Set<String> getTotalPossibleStates(DungeonRoom dungeonRoom) {
        return Sets.newHashSet("open", "closed");
    }

    @Override
    public OffsetPoint getRepresentingPoint(DungeonRoom dungeonRoom) {
        int leastY = Integer.MAX_VALUE;
        OffsetPoint thatPt = null;
        for (OffsetPoint offsetPoint : secretPoint.getOffsetPointList()) {
            if (offsetPoint.getY() < leastY) {
                thatPt = offsetPoint;
                leastY = offsetPoint.getY();
            }
        }
        return thatPt;
    }
}
