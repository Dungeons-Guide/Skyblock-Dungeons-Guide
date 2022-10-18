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

package kr.syeyoung.dungeonsguide.dungeon.mechanics;

import com.google.common.collect.Sets;
import kr.syeyoung.dungeonsguide.dungeon.actions.*;
import kr.syeyoung.dungeonsguide.dungeon.data.OffsetPoint;
import kr.syeyoung.dungeonsguide.dungeon.data.OffsetPointSet;
import kr.syeyoung.dungeonsguide.dungeon.roomfinder.DungeonRoom;
import kr.syeyoung.dungeonsguide.utils.RenderUtils;
import lombok.Data;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;

import java.awt.*;
import java.util.*;
import java.util.List;

@Data
public class DungeonBreakableWall implements DungeonMechanic, RouteBlocker {
    private static final long serialVersionUID = 1161593374765852217L;
    private OffsetPointSet secretPoint = new OffsetPointSet();
    private List<String> preRequisite = new ArrayList<String>();


    @Override
    public Set<AbstractAction> getAction(String state, DungeonRoom dungeonRoom) {
        if (state.equalsIgnoreCase("navigate")) {
            Set<AbstractAction> base;
            Set<AbstractAction> preRequisites = base = new HashSet<AbstractAction>();

            int leastY = Integer.MAX_VALUE;
            OffsetPoint thatPt = null;
            for (OffsetPoint offsetPoint : secretPoint.getOffsetPointList()) {
                if (offsetPoint.getY() < leastY) {
                    thatPt = offsetPoint;
                    leastY = offsetPoint.getY();
                }
            }
            ActionMoveNearestAir actionMove = new ActionMoveNearestAir(thatPt);
            preRequisites.add(actionMove);
            preRequisites = actionMove.getPreRequisite();
            for (String str : preRequisite) {
                if (str.isEmpty()) continue;
                ActionChangeState actionChangeState = new ActionChangeState(str.split(":")[0], str.split(":")[1]);
                preRequisites.add(actionChangeState);
            }
            return base;
        }

        if (!"open".equalsIgnoreCase(state)) throw new IllegalArgumentException(state+" is not valid state for breakable wall");
        if (!isBlocking(dungeonRoom)) {
            return Collections.emptySet();
        }
        Set<AbstractAction> base;
        Set<AbstractAction> preRequisites = base = new HashSet<AbstractAction>();
        {
            ActionBreakWithSuperBoom actionClick;
            preRequisites.add(actionClick = new ActionBreakWithSuperBoom(getRepresentingPoint(dungeonRoom)));
            preRequisites = actionClick.getPreRequisite();
        }
        {

            int leastY = Integer.MAX_VALUE;
            OffsetPoint thatPt = null;
            for (OffsetPoint offsetPoint : secretPoint.getOffsetPointList()) {
                if (offsetPoint.getY() < leastY) {
                    thatPt = offsetPoint;
                    leastY = offsetPoint.getY();
                }
            }
            ActionMoveNearestAir actionMove = new ActionMoveNearestAir(thatPt);
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

    public DungeonBreakableWall clone() throws CloneNotSupportedException {
        DungeonBreakableWall dungeonSecret = new DungeonBreakableWall();
        dungeonSecret.secretPoint = (OffsetPointSet) secretPoint.clone();
        dungeonSecret.preRequisite = new ArrayList<String>(preRequisite);
        return dungeonSecret;
    }

    @Override
    public String getCurrentState(DungeonRoom dungeonRoom) {
        Block b = Blocks.air;
        if (!secretPoint.getOffsetPointList().isEmpty())
            b = secretPoint.getOffsetPointList().get(0).getBlock(dungeonRoom);

        return b == Blocks.air ?"open" :"closed";
    }

    @Override
    public Set<String> getPossibleStates(DungeonRoom dungeonRoom) {
        return isBlocking(dungeonRoom) ? Sets.newHashSet("navigate", "open") : Sets.newHashSet("navigate");
    }

    @Override
    public Set<String> getTotalPossibleStates(DungeonRoom dungeonRoom) {
        return Sets.newHashSet("open", "closed");
    }

    @Override
    public OffsetPoint getRepresentingPoint(DungeonRoom dungeonRoom) {
        return secretPoint.getOffsetPointList().size() == 0 ? null : secretPoint.getOffsetPointList().get(secretPoint.getOffsetPointList().size() / 2);
    }
}
