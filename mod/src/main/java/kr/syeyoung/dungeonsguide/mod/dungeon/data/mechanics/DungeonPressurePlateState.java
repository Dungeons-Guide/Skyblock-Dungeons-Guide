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

package kr.syeyoung.dungeonsguide.mod.dungeon.data.mechanics;

import com.google.common.collect.Sets;
import kr.syeyoung.dungeonsguide.mod.dungeon.actions.*;
import kr.syeyoung.dungeonsguide.mod.dungeon.actions.tree.ActionDAGBuilder;
import kr.syeyoung.dungeonsguide.mod.dungeon.data.OffsetPoint;
import kr.syeyoung.dungeonsguide.mod.dungeon.data.mechanics.dunegonmechanic.DungeonMechanicData;
import kr.syeyoung.dungeonsguide.mod.dungeon.data.mechanics.dunegonmechanic.DungeonMechanicState;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomfinder.DungeonRoom;
import kr.syeyoung.dungeonsguide.mod.pathfinding.abilitysetting.AlgorithmSetting;
import kr.syeyoung.dungeonsguide.mod.utils.RenderUtils;
import kr.syeyoung.modapi.data.VectorI3D;
import lombok.Data;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Data
public class DungeonPressurePlateState implements DungeonMechanicState {
    private final DungeonPressurePlateData data;
    private final DungeonRoom room;

    public DungeonPressurePlateState(DungeonPressurePlateData data, DungeonRoom room) {
        this.data = data;
        this.room = room;
    }

    @Override
    public void buildAction(String action, ActionDAGBuilder builder, AlgorithmSetting algorithmSetting) throws PathfindImpossibleException {
        if (action.equals(getCurrentState())) return;
        if (action.equalsIgnoreCase("navigate")) {
            builder = builder
                    .requires(new ActionMoveNearestAir(getRepresentingPoint()), algorithmSetting);
            for (String str : data.preRequisite) {
                if (str.isEmpty()) continue;
                builder.optional(new ActionChangeState(str.split(":")[0], str.split(":")[1]), algorithmSetting);
            }
            return;
        }
        if (!("triggered".equalsIgnoreCase(action) || "untriggered".equalsIgnoreCase(action)))
            throw new PathfindImpossibleException(action + " is not valid state for secret");


        if ("triggered".equalsIgnoreCase(action)) {
            builder = builder
                    .requires(new AtomicAction.Builder()
                            .requires(new ActionDropItem(data.platePoint))
                            .requires(new ActionMoveNearestAir(data.platePoint))
                            .build("MoveAndDropItem"), algorithmSetting);
        } else {
            builder = builder
                    .requires(new ActionMoveNearestAir(data.platePoint), algorithmSetting);
        }
        for (String str : data.preRequisite) {
            if (str.isEmpty()) continue;
            builder.optional(new ActionChangeState(str.split(":")[0], str.split(":")[1]), algorithmSetting);
        }

        return;
    }

    @Override
    public void highlight(Color color, String name, float partialTicks) {
        VectorI3D pos = data.platePoint.getBlockPos(room);
        RenderUtils.highlightBlockStencil(pos, partialTicks, color, false);
        RenderUtils.drawTextAtWorld(name, pos.getX() + 0.5f, pos.getY() + 0.75f, pos.getZ() + 0.5f, 0xFFFFFFFF, 0.03f, false, true, partialTicks);
        RenderUtils.drawTextAtWorld(getCurrentState(), pos.getX() + 0.5f, pos.getY() + 0.25f, pos.getZ() + 0.5f, 0xFFFFFFFF, 0.03f, false, true, partialTicks);
    }


    @Override
    public String getCurrentState() {
        if (data.triggering == null) data.triggering = "null";
        DungeonMechanicState mechanic = room.getMechanics().get(data.triggering);
        if (mechanic == null) {
            return "undeterminable";
        } else {
            String state = mechanic.getCurrentState();
            if ("open".equalsIgnoreCase(state)) {
                return "triggered";
            } else {
                return "untriggered";
            }
        }
    }

    @Override
    public Set<String> getAvailableActions() {
        String currentStatus = getCurrentState();
        if (currentStatus.equalsIgnoreCase("triggered"))
            return Sets.newHashSet("navigate", "untriggered");
        else if (currentStatus.equalsIgnoreCase("untriggered"))
            return Sets.newHashSet("navigate", "triggered");
        return Sets.newHashSet("navigate");
    }

    @Override
    public Set<String> getTotalPossibleStates() {
        return Sets.newHashSet("triggered", "untriggered");
    }

    @Override
    public OffsetPoint getRepresentingPoint() {
        return data.platePoint;
    }

    @Data
    public static class DungeonPressurePlateData implements DungeonMechanicData {
        private OffsetPoint platePoint = new OffsetPoint(0, 0, 0);
        private List<String> preRequisite = new ArrayList<String>();
        private String triggering = "";


        public DungeonPressurePlateData() {
        }

        @Override
        public DungeonPressurePlateState createState(DungeonRoom room) {
            return new DungeonPressurePlateState(this, room);
        }

        public DungeonPressurePlateData clone() throws CloneNotSupportedException {
            DungeonPressurePlateData data = new DungeonPressurePlateData();
            data.platePoint = (OffsetPoint) platePoint.clone();
            data.triggering = triggering;
            data.preRequisite = new ArrayList<String>(preRequisite);
            ;
            return data;
        }
    }
}
