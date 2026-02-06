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

package kr.syeyoung.dungeonsguide.mod.dungeon.data.mechanics;

import com.google.common.collect.Sets;
import kr.syeyoung.dungeonsguide.mod.dungeon.actions.ActionChangeState;
import kr.syeyoung.dungeonsguide.mod.dungeon.actions.ActionMoveNearestAir;
import kr.syeyoung.dungeonsguide.mod.dungeon.actions.PathfindImpossibleException;
import kr.syeyoung.dungeonsguide.mod.dungeon.actions.tree.ActionDAGBuilder;
import kr.syeyoung.dungeonsguide.mod.dungeon.data.OffsetPoint;
import kr.syeyoung.dungeonsguide.mod.dungeon.data.OffsetPointSet;
import kr.syeyoung.dungeonsguide.mod.dungeon.data.mechanics.dunegonmechanic.DungeonMechanicData;
import kr.syeyoung.dungeonsguide.mod.dungeon.data.mechanics.dunegonmechanic.DungeonMechanicState;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomfinder.DungeonRoom;
import kr.syeyoung.dungeonsguide.mod.pathfinding.abilitysetting.AlgorithmSetting;
import kr.syeyoung.modapi.data.VectorI3D;
import kr.syeyoung.modapi.rendering.UWorldRenderContext;
import lombok.Data;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Data
public class DungeonTripwireTrapState implements DungeonMechanicState {
    private final DungeonTripwireTrapData data;
    private final DungeonRoom room;

    public DungeonTripwireTrapState(DungeonTripwireTrapData data, DungeonRoom room) {
        this.data = data;
        this.room = room;
    }


    @Override
    public void buildAction(String action, ActionDAGBuilder builder, AlgorithmSetting algorithmSetting) throws PathfindImpossibleException {
        if (action.equalsIgnoreCase("navigate")) {
            builder = builder
                    .requires(new ActionMoveNearestAir(getRepresentingPoint()), algorithmSetting);
            for (String str : data.preRequisite) {
                if (str.isEmpty()) continue;
                builder.requires(new ActionChangeState(str.split(":")[0], str.split(":")[1]), algorithmSetting);
            }
            return;
        }
        throw new PathfindImpossibleException(action + " is not valid state for tomb");
    }

    @Override
    public void highlight(Color color, String name, UWorldRenderContext context, float partialTicks) {
        if (data.dangerRegion.getOffsetPointList().isEmpty()) return;
        OffsetPoint firstPoint = data.dangerRegion.getOffsetPointList().get(0);
        VectorI3D pos = firstPoint.getBlockPos(room);
        context.drawTextAtWorld(name, pos.getX() + 0.5f, pos.getY() + 0.75f, pos.getZ() + 0.5f, 0xFFFFFFFF, 0.03f, false, true, partialTicks);
        context.drawTextAtWorld(getCurrentState(), pos.getX() + 0.5f, pos.getY() + 0.25f, pos.getZ() + 0.5f, 0xFFFFFFFF, 0.03f, false, true, partialTicks);

        List<VectorI3D> list = new ArrayList<>();
        for (OffsetPoint offsetPoint : data.dangerRegion.getOffsetPointList()) {
            list.add(offsetPoint.getBlockPos(room));
        }
        context.highlightBlocksStencil(list, partialTicks, color, false);
    }

    @Override
    public String getCurrentState() {
        return "no-state";
    }

    @Override
    public Set<String> getAvailableActions() {
        return Sets.newHashSet("navigate");
    }

    @Override
    public Set<String> getTotalPossibleStates() {
        return Sets.newHashSet("no-state");
    }


    @Override
    public OffsetPoint getRepresentingPoint() {
        return data.dangerRegion.getOffsetPointList().size() == 0 ? null : data.dangerRegion.getOffsetPointList().get(0);
    }

    @Data
    public static class DungeonTripwireTrapData implements DungeonMechanicData {
        private OffsetPointSet dangerRegion = new OffsetPointSet();
        private List<String> preRequisite = new ArrayList<>();


        public DungeonTripwireTrapData() {
        }

        @Override
        public DungeonTripwireTrapState createState(DungeonRoom room) {
            return new DungeonTripwireTrapState(this, room);
        }

        public DungeonTripwireTrapData clone() throws CloneNotSupportedException {
            DungeonTripwireTrapData data = new DungeonTripwireTrapData();
            data.dangerRegion = (OffsetPointSet) dangerRegion.clone();
            data.preRequisite = new ArrayList<>(preRequisite);
            ;
            return data;
        }
    }
}
