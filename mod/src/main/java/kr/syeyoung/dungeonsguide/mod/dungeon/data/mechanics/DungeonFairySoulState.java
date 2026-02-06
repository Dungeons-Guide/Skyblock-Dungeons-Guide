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
import kr.syeyoung.dungeonsguide.mod.dungeon.actions.*;
import kr.syeyoung.dungeonsguide.mod.dungeon.actions.tree.ActionDAGBuilder;
import kr.syeyoung.dungeonsguide.mod.dungeon.data.OffsetPoint;
import kr.syeyoung.dungeonsguide.mod.dungeon.data.mechanics.dunegonmechanic.DungeonMechanicData;
import kr.syeyoung.dungeonsguide.mod.dungeon.data.mechanics.dunegonmechanic.DungeonMechanicState;
import kr.syeyoung.dungeonsguide.mod.dungeon.data.mechanics.predicates.PredicateArmorStand;
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
public class DungeonFairySoulState implements DungeonMechanicState {
    private final DungeonFairySoulData data;
    private final DungeonRoom room;

    public DungeonFairySoulState(DungeonFairySoulData data, DungeonRoom room) {
        this.data = data;
        this.room = room;
    }


    @Override
    public void buildAction(String action, ActionDAGBuilder builder, AlgorithmSetting algorithmSetting) throws PathfindImpossibleException {
        if (!"navigate".equalsIgnoreCase(action))
            throw new PathfindImpossibleException(action + " is not valid state for secret");

        builder = builder.requires(new AtomicAction.Builder()
                .requires(() -> {
                    ActionInteract actionClick = new ActionInteract(data.secretPoint);
                    actionClick.setPredicate(PredicateArmorStand.INSTANCE);
                    actionClick.setRadius(3);
                    return actionClick;
                })
                .requires(new ActionMoveNearestAir(data.secretPoint))
                .build("MoveAndInteract"), algorithmSetting
        );

        for (String str : data.preRequisite) {
            if (!str.isEmpty()) {
                String[] split = str.split(":");
                builder.optional(new ActionChangeState(split[0], split[1]), algorithmSetting);
            }
        }
        return;
    }

    @Override
    public void highlight(Color color, String name, UWorldRenderContext context, float partialTicks) {
        VectorI3D pos = data.secretPoint.getBlockPos(room);
        context.highlightBlock(pos, color, partialTicks, false);
        context.drawTextAtWorld("F-" + name, pos.getX() + 0.5f, pos.getY() + 0.375f, pos.getZ() + 0.5f, 0xFFFFFFFF, 0.03f, false, true, partialTicks);
        context.drawTextAtWorld(getCurrentState(), pos.getX() + 0.5f, pos.getY() + 0f, pos.getZ() + 0.5f, 0xFFFFFFFF, 0.03f, false, true, partialTicks);
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
        return Sets.newHashSet("no-state", "navigate");
    }

    @Override
    public OffsetPoint getRepresentingPoint() {
        return data.secretPoint;
    }

    @Data
    public static class DungeonFairySoulData implements DungeonMechanicData {
        private OffsetPoint secretPoint = new OffsetPoint(0, 0, 0);
        private List<String> preRequisite = new ArrayList<String>();


        public DungeonFairySoulData() {
        }

        @Override
        public DungeonFairySoulState createState(DungeonRoom room) {
            return new DungeonFairySoulState(this, room);
        }

        public DungeonFairySoulData clone() throws CloneNotSupportedException {
            DungeonFairySoulData data = new DungeonFairySoulData();
            data.secretPoint = (OffsetPoint) secretPoint.clone();
            data.preRequisite = new ArrayList<String>(preRequisite);
            ;
            return data;
        }
    }
}
