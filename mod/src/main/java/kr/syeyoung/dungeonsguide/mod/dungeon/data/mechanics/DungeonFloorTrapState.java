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
import kr.syeyoung.dungeonsguide.mod.dungeon.data.mechanics.dunegonmechanic.DungeonMechanicState;
import kr.syeyoung.dungeonsguide.mod.dungeon.data.mechanics.dunegonmechanic.WorldMutatingMechanicData;
import kr.syeyoung.dungeonsguide.mod.dungeon.data.mechanics.dunegonmechanic.WorldMutatingMechanicState;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomfinder.DungeonRoom;
import kr.syeyoung.dungeonsguide.mod.pathfinding.abilitysetting.AlgorithmSetting;
import kr.syeyoung.dungeonsguide.mod.utils.RenderUtils;
import lombok.Data;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Data
public class DungeonFloorTrapState implements DungeonMechanicState, WorldMutatingMechanicState {
    private final DungeonFloorTrapData data;
    private final DungeonRoom room;


    public DungeonFloorTrapState(DungeonFloorTrapData data, DungeonRoom room) {
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
        if (!"triggered".equalsIgnoreCase(action)) {
            throw new PathfindImpossibleException(action + " is not valid state for tomb");
        }
        if (!isBlocking(room)) {
            return;
        }

        builder = builder
                .requires(new ActionMoveNearestAir(data.secretPoint.getOffsetPointList().get(0)), algorithmSetting);

        for (String str : data.preRequisite) {
            if (str.isEmpty()) continue;
            builder.optional(new ActionChangeState(str.split(":")[0], str.split(":")[1]), algorithmSetting);
        }
    }

    @Override
    public void highlight(Color color, String name, float partialTicks) {
        if (data.secretPoint.getOffsetPointList().isEmpty()) return;
        OffsetPoint firstPoint = data.secretPoint.getOffsetPointList().get(0);
        BlockPos pos = firstPoint.getBlockPos(room);
        RenderUtils.drawTextAtWorld(name, pos.getX() + 0.5f, pos.getY() + 0.75f, pos.getZ() + 0.5f, 0xFFFFFFFF, 0.03f, false, true, partialTicks);
        RenderUtils.drawTextAtWorld(getCurrentState(), pos.getX() + 0.5f, pos.getY() + 0.25f, pos.getZ() + 0.5f, 0xFFFFFFFF, 0.03f, false, true, partialTicks);

        List<BlockPos> list = new ArrayList<>();
        for (OffsetPoint offsetPoint : data.secretPoint.getOffsetPointList()) {
            list.add(offsetPoint.getBlockPos(room));
        }
        RenderUtils.highlightBlocksStencil(list, partialTicks, color, false);
    }

    @Override
    public boolean isBlocking(DungeonRoom dungeonRoom) {
        for (OffsetPoint offsetPoint : data.secretPoint.getOffsetPointList()) {
            if (offsetPoint.getBlock(dungeonRoom) != Blocks.air) return true;
        }
        return false;
    }

    @Override
    public List<OffsetPoint> blockedPoints() {
        return data.secretPoint.getOffsetPointList();
    }

    @Override
    public String getCurrentState() {
        Block b = Blocks.air;
        if (!data.secretPoint.getOffsetPointList().isEmpty())
            b = data.secretPoint.getOffsetPointList().get(0).getBlock(room);
        return b == Blocks.air ? "triggered" : "untriggered";
    }

    @Override
    public Set<String> getAvailableActions() {
        return isBlocking(room) ? Sets.newHashSet("triggered", "navigate") : Sets.newHashSet("navigate");
    }

    @Override
    public Set<String> getTotalPossibleStates() {
        return Sets.newHashSet("triggered", "untriggered");
    }


    @Override
    public OffsetPoint getRepresentingPoint() {
        return data.secretPoint.getOffsetPointList().size() == 0 ? null : data.secretPoint.getOffsetPointList().get(0);
    }

    @Data
    public static class DungeonFloorTrapData implements WorldMutatingMechanicData {
        private OffsetPointSet secretPoint = new OffsetPointSet();
        private List<String> preRequisite = new ArrayList<>();


        public DungeonFloorTrapData() {
        }

        @Override
        public DungeonFloorTrapState createState(DungeonRoom room) {
            return new DungeonFloorTrapState(this, room);
        }

        public DungeonFloorTrapData clone() throws CloneNotSupportedException {
            DungeonFloorTrapData data = new DungeonFloorTrapData();
            data.secretPoint = (OffsetPointSet) secretPoint.clone();
            data.preRequisite = new ArrayList<>(preRequisite);
            ;
            return data;
        }

        @Override
        public List<OffsetPoint> blockedPoints() {
            return secretPoint.getOffsetPointList();
        }
    }

}
