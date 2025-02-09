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

package kr.syeyoung.dungeonsguide.mod.dungeon.data.mechanics;

import com.google.common.collect.Sets;
import kr.syeyoung.dungeonsguide.mod.dungeon.data.OffsetPoint;
import kr.syeyoung.dungeonsguide.mod.dungeon.data.OffsetPointSet;
import kr.syeyoung.dungeonsguide.mod.dungeon.data.mechanics.dunegonmechanic.DungeonMechanicData;
import kr.syeyoung.dungeonsguide.mod.dungeon.data.mechanics.dunegonmechanic.DungeonMechanicState;
import kr.syeyoung.dungeonsguide.mod.dungeon.actions.*;
import kr.syeyoung.dungeonsguide.mod.dungeon.actions.tree.ActionDAGBuilder;
import kr.syeyoung.dungeonsguide.mod.dungeon.pathfinding.abilitysetting.AlgorithmSetting;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomfinder.DungeonRoom;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomprocessor.GeneralRoomProcessor;
import kr.syeyoung.dungeonsguide.mod.utils.RenderUtils;
import lombok.Data;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

@Data
public class DungeonRedstoneKeySlotState implements DungeonMechanicState {
    private final DungeonRedstoneKeySlotData data;
    private final DungeonRoom room;

    public DungeonRedstoneKeySlotState(DungeonRedstoneKeySlotData data, DungeonRoom room) {
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
        if (!("triggered".equalsIgnoreCase(action)))
            throw new PathfindImpossibleException(action + " is not valid state for secret");
        builder = builder
                .requires(new AtomicAction.Builder()
                        .requires(new ActionClick(data.slotPoint))
                        .requires(new ActionMoveNearestAir(data.slotPoint))
                        .build("MoveAndClick"), algorithmSetting);
        {
            for (String s : data.preRequisite.stream().filter(a -> a.contains(":obtained-self")).collect(Collectors.toList())) {
                builder.requires(new ActionChangeState(s.split(":")[0], s.split(":")[1]), algorithmSetting);
            }
            builder = builder.requires(new ActionRoot(), algorithmSetting);
            for (String str : data.preRequisite) {
                if (str.isEmpty()) continue;
                if (str.contains(":obtained-self")) continue;
                builder.optional(new ActionChangeState(str.split(":")[0], str.split(":")[1]), algorithmSetting);
            }
        }
    }

    @Override
    public void highlight(Color color, String name, float partialTicks) {
        BlockPos pos = data.slotPoint.getBlockPos(room);
        RenderUtils.highlightBlock(pos, color, partialTicks);
        RenderUtils.drawTextAtWorld(name, pos.getX() + 0.5f, pos.getY() + 0.75f, pos.getZ() + 0.5f, 0xFFFFFFFF, 0.03f, false, true, partialTicks);
        RenderUtils.drawTextAtWorld(getCurrentState(), pos.getX() + 0.5f, pos.getY() + 0.25f, pos.getZ() + 0.5f, 0xFFFFFFFF, 0.03f, false, true, partialTicks);
    }


    @Override
    public String getCurrentState() {
        for (OffsetPoint offsetPoint : data.headPoint.getOffsetPointList()) {
            if (offsetPoint.getBlock(room) == Blocks.skull) {
                return "triggered";
            }
        }
        return "untriggered";
    }

    @Override
    public Set<String> getAvailableActions() {
        String currentStatus = getCurrentState();
        if (currentStatus.equalsIgnoreCase("untriggered"))
            return Sets.newHashSet("navigate", "triggered");
        return Sets.newHashSet("navigate");
    }

    @Override
    public Set<String> getTotalPossibleStates() {
        return Sets.newHashSet("triggered", "untriggered");
    }

    @Override
    public OffsetPoint getRepresentingPoint() {
        return data.slotPoint;
    }

    @Data
    public static class DungeonRedstoneKeySlotData implements DungeonMechanicData {
        private OffsetPoint slotPoint = new OffsetPoint(0, 0, 0);
        private OffsetPointSet headPoint = new OffsetPointSet();
        private List<String> preRequisite = new ArrayList<String>();


        public DungeonRedstoneKeySlotData() {
        }

        @Override
        public DungeonRedstoneKeySlotState createState(DungeonRoom room) {
            return new DungeonRedstoneKeySlotState(this, room);
        }

        public DungeonRedstoneKeySlotData clone() throws CloneNotSupportedException {
            DungeonRedstoneKeySlotData data = new DungeonRedstoneKeySlotData();
            data.slotPoint = (OffsetPoint) slotPoint.clone();
            data.headPoint = (OffsetPointSet) headPoint.clone();
            data.preRequisite = new ArrayList<String>(preRequisite);
            ;
            return data;
        }
    }
}
