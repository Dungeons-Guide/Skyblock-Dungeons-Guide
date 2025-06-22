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
import kr.syeyoung.dungeonsguide.mod.dungeon.actions.ActionChangeState;
import kr.syeyoung.dungeonsguide.mod.dungeon.actions.ActionMoveNearestAir;
import kr.syeyoung.dungeonsguide.mod.dungeon.actions.ActionUtils;
import kr.syeyoung.dungeonsguide.mod.dungeon.actions.PathfindImpossibleException;
import kr.syeyoung.dungeonsguide.mod.dungeon.actions.tree.ActionDAGBuilder;
import kr.syeyoung.dungeonsguide.mod.dungeon.data.OffsetPoint;
import kr.syeyoung.dungeonsguide.mod.dungeon.data.PrecalculatedStonk;
import kr.syeyoung.dungeonsguide.mod.dungeon.data.mechanics.dunegonmechanic.DungeonMechanicData;
import kr.syeyoung.dungeonsguide.mod.dungeon.data.mechanics.dunegonmechanic.DungeonMechanicState;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomfinder.DungeonRoom;
import kr.syeyoung.dungeonsguide.mod.features.FeatureRegistry;
import kr.syeyoung.dungeonsguide.mod.pathfinding.abilitysetting.AlgorithmSetting;
import kr.syeyoung.dungeonsguide.mod.utils.RenderUtils;
import lombok.Data;
import net.minecraft.util.BlockPos;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

@Data
public class DungeonLeverState implements DungeonMechanicState {
    private final DungeonLeverData data;
    private final DungeonRoom room;


    public DungeonLeverState(DungeonLeverData data, DungeonRoom room) {
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

        if (action.equalsIgnoreCase(getCurrentState())) return;


        if (data.leverCache != null)
            ActionUtils.buildActionMoveAndClick(builder, room, data.leverCache, data.preRequisite, Collections.emptyList(), algorithmSetting);
        else
            ActionUtils.buildActionMoveAndClick(builder, room, data.leverPoint, builder1 -> {
                for (String str : data.preRequisite) {
                    if (str.isEmpty()) continue;
                    builder1.optional(new ActionChangeState(str.split(":")[0], str.split(":")[1]), algorithmSetting);
                }
                return null;
            }, algorithmSetting);
    }

    @Override
    public void highlight(Color color, String name, float partialTicks) {
        BlockPos pos = data.leverPoint.getBlockPos(room);
        RenderUtils.highlightBlockStencil(pos, partialTicks, color, false);
        RenderUtils.drawTextAtWorld(name, pos.getX() + 0.5f, pos.getY() + 0.75f, pos.getZ() + 0.5f, 0xFFFFFFFF, 0.03f, false, true, partialTicks);
        RenderUtils.drawTextAtWorld(getCurrentState(), pos.getX() + 0.5f, pos.getY() + 0.25f, pos.getZ() + 0.5f, 0xFFFFFFFF, 0.03f, false, true, partialTicks);


        if (data.leverCache != null && FeatureRegistry.DEBUG_ST.isEnabled())
            data.leverCache.render(partialTicks, room);
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
        if (currentStatus.equalsIgnoreCase("untriggered"))
            return Sets.newHashSet("navigate", "triggered");
        else if (currentStatus.equalsIgnoreCase("triggered"))
            return Sets.newHashSet("navigate", "untriggered");
        return Sets.newHashSet("navigate");
    }

    @Override
    public Set<String> getTotalPossibleStates() {
        return Sets.newHashSet("triggered", "untriggered");
    }

    @Override
    public OffsetPoint getRepresentingPoint() {
        return data.leverPoint;
    }

    @Data
    public static class DungeonLeverData implements DungeonMechanicData {
        private OffsetPoint leverPoint = new OffsetPoint(0, 0, 0);
        private PrecalculatedStonk leverCache;
        private List<String> preRequisite = new ArrayList<String>();
        private String triggering = "";


        public DungeonLeverData() {
        }

        @Override
        public DungeonLeverState createState(DungeonRoom room) {
            return new DungeonLeverState(this, room);
        }

        public DungeonLeverData clone() throws CloneNotSupportedException {
            DungeonLeverData data = new DungeonLeverData();
            data.leverPoint = (OffsetPoint) leverPoint.clone();
            data.triggering = triggering;
            data.leverCache = leverCache;
            data.preRequisite = new ArrayList<String>(preRequisite);
            ;
            return data;
        }
    }
}
