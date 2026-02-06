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
import kr.syeyoung.modapi.data.VectorI3D;
import kr.syeyoung.modapi.rendering.UWorldRenderContext;
import lombok.Data;
import lombok.Setter;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Data
public class DungeonWizardState implements DungeonMechanicState {
    private final DungeonWizardData data;
    private final DungeonRoom room;

    public DungeonWizardState(DungeonWizardData data, DungeonRoom room) {
        this.data = data;
        this.room = room;
    }


    @Override
    public void buildAction(String action, ActionDAGBuilder builder, AlgorithmSetting algorithmSetting) throws PathfindImpossibleException {
        if (!"navigate".equalsIgnoreCase(action) && !"quest".equalsIgnoreCase(action) && !"click".equalsIgnoreCase(action))
            throw new PathfindImpossibleException(action + " is not a valid state for secret");

        if ("click".equalsIgnoreCase(action) || "quest".equalsIgnoreCase(action)) {
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

        if ("quest".equalsIgnoreCase(action)) {
            builder = builder.requires(new ActionChangeState(data.crystal, "obtained-self"), algorithmSetting).end()
                    .requires(new ActionRoot(), algorithmSetting);
        }

        for (String str : data.preRequisite) {
            if (!str.isEmpty()) {
                String[] split = str.split(":");
                builder.optional(new ActionChangeState(split[0], split[1]), algorithmSetting);
            }
        }

    }

    @Override
    public void highlight(Color color, String name, UWorldRenderContext context, float partialTicks) {
        VectorI3D pos = data.secretPoint.getBlockPos(room);
        context.highlightBlock(pos, color, partialTicks, false);
        context.drawTextAtWorld("W-" + name, pos.getX() + 0.5f, pos.getY() + 0.375f, pos.getZ() + 0.5f, 0xFFFFFFFF, 0.03f, false, true, partialTicks);
        context.drawTextAtWorld(getCurrentState(), pos.getX() + 0.5f, pos.getY() + 0f, pos.getZ() + 0.5f, 0xFFFFFFFF, 0.03f, false, true, partialTicks);
    }

    @Setter
    private boolean didCompleteQuest = false;
    @Override
    public String getCurrentState() {
        if (didCompleteQuest) {
            return "quest";
        }
        return "no-state";
    }

    @Override
    public Set<String> getAvailableActions() {
        if (data.crystal != null) {
            DungeonMechanicState crystal1 = room.getMechanics().get(data.crystal);
            String crystalState = crystal1.getCurrentState();
            if (crystalState.equalsIgnoreCase("obtained-other")) {
                return Sets.newHashSet("navigate", "click");
            }
        }

        return getCurrentState().equalsIgnoreCase("quest") ?
                Sets.newHashSet("navigate", "click") : Sets.newHashSet("navigate", "click", "quest");
    }

    @Override
    public Set<String> getTotalPossibleStates() {
        return Sets.newHashSet("no-state", "click", "quest", "interact");
    }

    @Override
    public OffsetPoint getRepresentingPoint() {
        return data.secretPoint;
    }

    @Data
    public static class DungeonWizardData implements DungeonMechanicData {
        private OffsetPoint secretPoint = new OffsetPoint(0, 0, 0);
        private List<String> preRequisite = new ArrayList<String>();
        private String crystal = "";


        public DungeonWizardData() {
        }

        @Override
        public DungeonWizardState createState(DungeonRoom room) {
            return new DungeonWizardState(this, room);
        }

        public DungeonWizardData clone() throws CloneNotSupportedException {
            DungeonWizardData data = new DungeonWizardData();
            data.secretPoint = (OffsetPoint) secretPoint.clone();
            data.crystal = crystal;
            data.preRequisite = new ArrayList<String>(preRequisite);
            ;
            return data;
        }
    }
}
