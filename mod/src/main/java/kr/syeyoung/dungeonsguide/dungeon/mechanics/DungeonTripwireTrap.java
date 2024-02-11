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
import kr.syeyoung.dungeonsguide.dungeon.data.OffsetPoint;
import kr.syeyoung.dungeonsguide.dungeon.data.OffsetPointSet;
import kr.syeyoung.dungeonsguide.dungeon.mechanics.dunegonmechanic.DungeonMechanic;
import kr.syeyoung.dungeonsguide.mod.dungeon.actions.ActionChangeState;
import kr.syeyoung.dungeonsguide.mod.dungeon.actions.ActionMoveNearestAir;
import kr.syeyoung.dungeonsguide.mod.dungeon.actions.PathfindImpossibleException;
import kr.syeyoung.dungeonsguide.mod.dungeon.actions.tree.ActionDAGBuilder;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomfinder.DungeonRoom;
import kr.syeyoung.dungeonsguide.mod.utils.RenderUtils;
import lombok.Data;
import net.minecraft.util.BlockPos;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Data
public class DungeonTripwireTrap implements DungeonMechanic {
    private static final long serialVersionUID = -7347076019472222115L;
    private OffsetPointSet dangerRegion = new OffsetPointSet();
    private List<String> preRequisite = new ArrayList<>();


    @Override
    public void buildAction(String state, DungeonRoom dungeonRoom, ActionDAGBuilder builder) throws PathfindImpossibleException {
        if (state.equalsIgnoreCase("navigate")) {
            builder = builder
                    .requires(new ActionMoveNearestAir(getRepresentingPoint(dungeonRoom)));
            for (String str : preRequisite) {
                if (str.isEmpty()) continue;
                builder.requires(new ActionChangeState(str.split(":")[0], str.split(":")[1]));
            }
            return;
        }
        throw new PathfindImpossibleException(state + " is not valid state for tomb");
    }

    @Override
    public void highlight(Color color, String name, DungeonRoom dungeonRoom, float partialTicks) {
        if (dangerRegion.getOffsetPointList().isEmpty()) return;
        OffsetPoint firstPoint = dangerRegion.getOffsetPointList().get(0);
        BlockPos pos = firstPoint.getBlockPos(dungeonRoom);
        RenderUtils.drawTextAtWorld(name, pos.getX() + 0.5f, pos.getY() + 0.75f, pos.getZ() + 0.5f, 0xFFFFFFFF, 0.03f, false, true, partialTicks);
        RenderUtils.drawTextAtWorld(getCurrentState(dungeonRoom), pos.getX() + 0.5f, pos.getY() + 0.25f, pos.getZ() + 0.5f, 0xFFFFFFFF, 0.03f, false, true, partialTicks);

        for (OffsetPoint offsetPoint : dangerRegion.getOffsetPointList()) {
            RenderUtils.highlightBlock(offsetPoint.getBlockPos(dungeonRoom), color, partialTicks);
        }
    }


    public DungeonTripwireTrap clone() throws CloneNotSupportedException {
        DungeonTripwireTrap dungeonSecret = new DungeonTripwireTrap();
        dungeonSecret.dangerRegion = (OffsetPointSet) dangerRegion.clone();
        dungeonSecret.preRequisite = new ArrayList<>(preRequisite);
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
        return Sets.newHashSet("no-state");
    }


    @Override
    public OffsetPoint getRepresentingPoint(DungeonRoom dungeonRoom) {
        return dangerRegion.getOffsetPointList().size() == 0 ? null : dangerRegion.getOffsetPointList().get(0);
    }
}
