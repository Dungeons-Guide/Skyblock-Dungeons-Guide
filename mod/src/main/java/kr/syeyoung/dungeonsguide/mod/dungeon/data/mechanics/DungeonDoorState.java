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
import kr.syeyoung.dungeonsguide.mod.dungeon.actions.PathfindImpossibleException;
import kr.syeyoung.dungeonsguide.mod.dungeon.actions.tree.ActionDAGBuilder;
import kr.syeyoung.dungeonsguide.mod.dungeon.data.OffsetPoint;
import kr.syeyoung.dungeonsguide.mod.dungeon.data.OffsetPointSet;
import kr.syeyoung.dungeonsguide.mod.dungeon.data.mechanics.dunegonmechanic.WorldMutatingMechanicData;
import kr.syeyoung.dungeonsguide.mod.dungeon.data.mechanics.dunegonmechanic.WorldMutatingMechanicState;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomfinder.DungeonRoom;
import kr.syeyoung.dungeonsguide.mod.pathfinding.abilitysetting.AlgorithmSetting;
import kr.syeyoung.dungeonsguide.mod.utils.RenderUtils;
import lombok.Data;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

@Data
public class DungeonDoorState implements WorldMutatingMechanicState {
    private final DungeonDoorData data;
    private final DungeonRoom room;
    public DungeonDoorState(DungeonDoorData data, DungeonRoom room) {
        this.data = data;
        this.room = room;
    }


    @Override
    public void buildAction(String action, ActionDAGBuilder builder, AlgorithmSetting algorithmSetting) throws PathfindImpossibleException {
        if (!("open".equalsIgnoreCase(action) || "closed".equalsIgnoreCase(action) || "navigate".equalsIgnoreCase(action))) throw new PathfindImpossibleException(action +" is not valid state for door");
        if (action.equalsIgnoreCase(getCurrentState())) return;
        if ("navigate".equalsIgnoreCase(action)) {
            builder = builder.requires(() -> {
                        int leastY = Integer.MAX_VALUE;
                        OffsetPoint thatPt = null;
                        for (OffsetPoint offsetPoint : data.secretPoint.getOffsetPointList()) {
                            if (offsetPoint.getY() < leastY) {
                                thatPt = offsetPoint;
                                leastY = offsetPoint.getY();
                            }
                        }
                        return new ActionMoveNearestAir(thatPt);
                    }, algorithmSetting);;
            for (String s : data.movePreRequisite) {
                if (s.isEmpty()) continue;
                builder.optional(new ActionChangeState(s.split(":")[0], s.split(":")[1]), algorithmSetting);
            }
            return;
        }

        {
            if (action.equalsIgnoreCase("open")) {
                for (String str : data.openPreRequisite) {
                    if (str.isEmpty()) continue;
                    ActionChangeState actionChangeState = new ActionChangeState(str.split(":")[0], str.split(":")[1]);
                    builder.requires(actionChangeState, algorithmSetting);
                }
            } else {
                for (String str : data.closePreRequisite) {
                    if (str.isEmpty()) continue;
                    ActionChangeState actionChangeState = new ActionChangeState(str.split(":")[0], str.split(":")[1]);
                    builder.requires(actionChangeState, algorithmSetting);
                }
            }
        }
        return;
    }

    @Override
    public void highlight(Color color, String name, float partialTicks) {
        if (data.secretPoint.getOffsetPointList().isEmpty()) return;
        OffsetPoint firstPoint = data.secretPoint.getOffsetPointList().get(0);
        BlockPos pos = firstPoint.getBlockPos(room);
        RenderUtils.drawTextAtWorld(name, pos.getX() +0.5f, pos.getY()+0.75f, pos.getZ()+0.5f, 0xFFFFFFFF, 0.03f, false, true, partialTicks);
        RenderUtils.drawTextAtWorld(getCurrentState(), pos.getX() +0.5f, pos.getY()+0.25f, pos.getZ()+0.5f, 0xFFFFFFFF, 0.03f, false, true, partialTicks);

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
        return isBlocking(room) ?"closed":"open";
    }


    @Override
    public Set<String> getAvailableActions() {
        String currentStatus = getCurrentState();
        if (currentStatus.equalsIgnoreCase("closed"))
            return Sets.newHashSet("navigate", "open");
        else if (currentStatus.equalsIgnoreCase("open"))
            return Sets.newHashSet("navigate", "closed");
        return Collections.singleton("navigate");
    }
    @Override
    public Set<String> getTotalPossibleStates() {
        return Sets.newHashSet("open", "closed", "nospawn");
    }

    @Override
    public OffsetPoint getRepresentingPoint() {
        int leastY = Integer.MAX_VALUE;
        OffsetPoint thatPt = null;
        for (OffsetPoint offsetPoint : data.secretPoint.getOffsetPointList()) {
            if (offsetPoint.getY() < leastY) {
                thatPt = offsetPoint;
                leastY = offsetPoint.getY();
            }
        }
        return thatPt;
    }

    @Data
    public static class DungeonDoorData implements WorldMutatingMechanicData {
        private OffsetPointSet secretPoint = new OffsetPointSet();
        private List<String> openPreRequisite = new ArrayList<String>();
        private List<String> closePreRequisite = new ArrayList<String>();
        private List<String> movePreRequisite = new ArrayList<String>();

        public DungeonDoorData() {
        }

        public DungeonDoorData clone() throws CloneNotSupportedException {
            DungeonDoorData dungeonSecret = new DungeonDoorData();
            dungeonSecret.secretPoint = (OffsetPointSet) secretPoint.clone();
            dungeonSecret.openPreRequisite = new ArrayList<String>(openPreRequisite);
            dungeonSecret.closePreRequisite = new ArrayList<String>(closePreRequisite);
            return dungeonSecret;
        }

        @Override
        public DungeonDoorState createState(DungeonRoom room) {
            return new DungeonDoorState(this, room);
        }

        @Override
        public List<OffsetPoint> blockedPoints() {
            return secretPoint.getOffsetPointList();
        }
    }
}
