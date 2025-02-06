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
import kr.syeyoung.dungeonsguide.mod.dungeon.data.PrecalculatedStonk;
import kr.syeyoung.dungeonsguide.mod.dungeon.data.mechanics.dunegonmechanic.DungeonMechanicData;
import kr.syeyoung.dungeonsguide.mod.dungeon.data.mechanics.dunegonmechanic.DungeonMechanicState;
import kr.syeyoung.dungeonsguide.mod.dungeon.actions.*;
import kr.syeyoung.dungeonsguide.mod.dungeon.actions.tree.ActionDAGBuilder;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomfinder.DungeonRoom;
import kr.syeyoung.dungeonsguide.mod.features.FeatureRegistry;
import kr.syeyoung.dungeonsguide.mod.utils.RenderUtils;
import lombok.Data;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;

import java.awt.*;
import java.util.*;
import java.util.List;

@Data
public class DungeonRedstoneKeyState implements DungeonMechanicState {
    private final DungeonRedstoneKeyData data;
    private final DungeonRoom room;

    public DungeonRedstoneKeyState(DungeonRedstoneKeyData data, DungeonRoom room) {
        this.data = data;
        this.room = room;
    }

    @Override
    public void buildAction(String state, ActionDAGBuilder builder) throws PathfindImpossibleException {
        if (state.equals(getCurrentState())) return;
        if (state.equalsIgnoreCase("navigate")) {
            builder = builder
                    .requires(new ActionMoveNearestAir(getRepresentingPoint()));
            for (String str : data.preRequisite) {
                if (str.isEmpty()) continue;
                builder.optional(new ActionChangeState(str.split(":")[0], str.split(":")[1]));
            }
            ;
        }

        if (!("obtained-self".equalsIgnoreCase(state) || "placed".equalsIgnoreCase(state)))
            throw new PathfindImpossibleException(state + " is not valid state for secret");

        if (state.equalsIgnoreCase("obtained-self")) {
            if (!getCurrentState().equalsIgnoreCase("unobtained")) {
                throw new PathfindImpossibleException(state + " is not valid state for secret");
            }

            if (data.secretCache != null)
                ActionUtils.buildActionMoveAndClick(builder, room, data.secretCache, data.preRequisite, Collections.emptyList());
            else
                ActionUtils.buildActionMoveAndClick(builder, room, data.secretPoint, builder1 -> {
                    for (String str : data.preRequisite) {
                        if (str.isEmpty()) continue;
                        builder1.optional(new ActionChangeState(str.split(":")[0], str.split(":")[1]));
                    }
                    return null;
                });
        } else { // placed
            if (!getCurrentState().equalsIgnoreCase("obtained-self")) {
                throw new PathfindImpossibleException(state + " is not valid state for secret");
            }
            builder.requires(new ActionChangeState(data.triggering, "triggered"));
        }
    }

    @Override
    public void highlight(Color color, String name, float partialTicks) {
        BlockPos pos = data.secretPoint.getBlockPos(room);
        RenderUtils.highlightBlock(pos, color, partialTicks);
        RenderUtils.drawTextAtWorld(name, pos.getX() + 0.5f, pos.getY() + 0.75f, pos.getZ() + 0.5f, 0xFFFFFFFF, 0.03f, false, true, partialTicks);
        RenderUtils.drawTextAtWorld(getCurrentState(), pos.getX() + 0.5f, pos.getY() + 0.25f, pos.getZ() + 0.5f, 0xFFFFFFFF, 0.03f, false, true, partialTicks);


        if (data.secretCache != null && FeatureRegistry.DEBUG_ST.isEnabled())
            data.secretCache.render(partialTicks, room);
    }

    @Override
    public String getCurrentState() {

        if (data.triggering == null) data.triggering = "null";
        DungeonMechanicState mechanic = room.getMechanics().get(data.triggering);
        if (mechanic == null) {
            return "undeterminable";
        }
        String state = mechanic.getCurrentState();
        if ("triggered".equalsIgnoreCase(state)) {
            return "placed";
        }
        if (room.getRoomContext().containsKey("redstonekey")) {
            return "obtained-self";
        }
        if (data.secretPoint.getBlock(room) == Blocks.skull) {
            return "unobtained";
        }
        return "obtained-other";
    }

    @Override
    public Set<String> getPossibleStates() {
        String currentState = getCurrentState();
        if (currentState.equalsIgnoreCase("obtained-self")) {
            return Sets.newHashSet("placed", "navigate");
        }
        if (currentState.equalsIgnoreCase("unobtained")) {
            return Sets.newHashSet("obtained-self", "navigate");
        }
        return Sets.newHashSet("navigate");
    }

    @Override
    public Set<String> getTotalPossibleStates() {
        return Sets.newHashSet("placed", "obtained-self", "unobtained", "obtained-other");
    }

    @Override
    public OffsetPoint getRepresentingPoint() {
        return data.secretPoint;
    }

    @Data
    public static class DungeonRedstoneKeyData implements DungeonMechanicData {
        private OffsetPoint secretPoint = new OffsetPoint(0, 0, 0);
        private PrecalculatedStonk secretCache;
        private List<String> preRequisite = new ArrayList<String>();
        private String triggering = "";


        public DungeonRedstoneKeyData() {
        }

        @Override
        public DungeonRedstoneKeyState createState(DungeonRoom room) {
            return new DungeonRedstoneKeyState(this, room);
        }

        @Override
        public DungeonRedstoneKeyData clone() throws CloneNotSupportedException {
            DungeonRedstoneKeyData data = new DungeonRedstoneKeyData();
            data.secretPoint = (OffsetPoint) secretPoint.clone();
            data.triggering = triggering;
            data.secretCache = secretCache;
            data.preRequisite = new ArrayList<String>(preRequisite);
            ;
            return data;
        }
    }
}
