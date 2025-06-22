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
import kr.syeyoung.dungeonsguide.mod.dungeon.data.mechanics.predicates.PredicateNPC;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomfinder.DungeonRoom;
import kr.syeyoung.dungeonsguide.mod.pathfinding.abilitysetting.AlgorithmSetting;
import kr.syeyoung.dungeonsguide.mod.utils.RenderUtils;
import lombok.Data;
import net.minecraft.util.BlockPos;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Data
public class DungeonNPCState implements DungeonMechanicState {
    private final DungeonNPCData data;
    private final DungeonRoom room;

    public DungeonNPCState(DungeonNPCData data, DungeonRoom room) {
        this.data = data;
        this.room = room;
    }

    @Override
    public void buildAction(String action, ActionDAGBuilder builder, AlgorithmSetting algorithmSetting) throws PathfindImpossibleException {
        if (!"navigate".equalsIgnoreCase(action) && !"click".equalsIgnoreCase(action))
            throw new PathfindImpossibleException(action + " is not a valid state for secret");

        if ("click".equalsIgnoreCase(action)) {
            builder = builder.requires(new AtomicAction.Builder()
                    .requires(() -> {
                        ActionInteract actionClick = new ActionInteract(data.secretPoint);
                        actionClick.setPredicate(PredicateNPC.INSTANCE);
                        actionClick.setRadius(3);
                        return actionClick;
                    })
                    .requires(new ActionMoveNearestAir(data.secretPoint))
                    .build("MoveAndInteract"), algorithmSetting);
        } else {
            builder = builder.requires(new ActionMoveNearestAir(data.secretPoint), algorithmSetting);
        }

        for (String str : data.preRequisite) {
            if (!str.isEmpty()) {
                String[] split = str.split(":");
                builder.optional(new ActionChangeState(split[0], split[1]), algorithmSetting);
            }
        }
    }

    @Override
    public void highlight(Color color, String name, float partialTicks) {
        BlockPos pos = data.secretPoint.getBlockPos(room);
        RenderUtils.highlightBlock(pos, color, partialTicks);
        RenderUtils.drawTextAtWorld("F-" + name, pos.getX() + 0.5f, pos.getY() + 0.375f, pos.getZ() + 0.5f, 0xFFFFFFFF, 0.03f, false, true, partialTicks);
        RenderUtils.drawTextAtWorld(getCurrentState(), pos.getX() + 0.5f, pos.getY() + 0f, pos.getZ() + 0.5f, 0xFFFFFFFF, 0.03f, false, true, partialTicks);
    }


    @Override
    public String getCurrentState() {
        return "no-state";
    }

    @Override
    public Set<String> getAvailableActions() {
        return Sets.newHashSet("navigate", "click");
    }

    @Override
    public Set<String> getTotalPossibleStates() {
        return Sets.newHashSet("no-state", "navigate", "click");
    }

    @Override
    public OffsetPoint getRepresentingPoint() {
        return data.secretPoint;
    }

    @Data
    public static class DungeonNPCData implements DungeonMechanicData {
        private OffsetPoint secretPoint = new OffsetPoint(0, 0, 0);
        private List<String> preRequisite = new ArrayList<String>();


        public DungeonNPCData() {
        }

        @Override
        public DungeonNPCState createState(DungeonRoom room) {
            return new DungeonNPCState(this, room);
        }

        public DungeonNPCData clone() throws CloneNotSupportedException {
            DungeonNPCData data = new DungeonNPCData();
            data.secretPoint = (OffsetPoint) secretPoint.clone();
            data.preRequisite = new ArrayList<String>(preRequisite);
            ;
            return data;
        }
    }

}
