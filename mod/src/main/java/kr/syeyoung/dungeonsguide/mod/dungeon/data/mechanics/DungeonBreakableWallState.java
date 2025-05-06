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
import kr.syeyoung.dungeonsguide.mod.dungeon.data.OffsetPoint;
import kr.syeyoung.dungeonsguide.mod.dungeon.data.OffsetPointSet;
import kr.syeyoung.dungeonsguide.mod.dungeon.data.mechanics.dunegonmechanic.WorldMutatingMechanicData;
import kr.syeyoung.dungeonsguide.mod.dungeon.data.mechanics.dunegonmechanic.WorldMutatingMechanicState;
import kr.syeyoung.dungeonsguide.mod.dungeon.actions.*;
import kr.syeyoung.dungeonsguide.mod.dungeon.actions.tree.ActionDAGBuilder;
import kr.syeyoung.dungeonsguide.mod.pathfinding.abilitysetting.AlgorithmSetting;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomfinder.DungeonRoom;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomprocessor.GeneralRoomProcessor;
import kr.syeyoung.dungeonsguide.mod.utils.RenderUtils;
import lombok.Data;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;

import java.awt.*;
import java.util.List;
import java.util.*;

@Data
public class DungeonBreakableWallState implements WorldMutatingMechanicState {
    private final DungeonBreakableWallData data;
    private final DungeonRoom room;

    public DungeonBreakableWallState(DungeonBreakableWallData data, DungeonRoom room) {
        this.data = data;
        this.room = room;
    }

    @Override
    public void buildAction(String action, ActionDAGBuilder builder, AlgorithmSetting algorithmSetting) throws PathfindImpossibleException {
        if (action.equalsIgnoreCase("navigate")) {
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
                    }, algorithmSetting);


            for (String str : data.preRequisite) {
                if (str.isEmpty()) continue;
                builder.requires(new ActionChangeState(str.split(":")[0], str.split(":")[1]), algorithmSetting);
            }
            return;
        }

        if (!"open".equalsIgnoreCase(action)) throw new PathfindImpossibleException(action +" is not valid state for breakable wall");
        if (!isBlocking(room)) {
            return;
        }

        builder = builder.requires(new AtomicAction.Builder()
                            .requires(new ActionBreakWithSuperBoom(data.secretPoint))
                            .requires(() -> {
                                int leastY = Integer.MAX_VALUE;
                                OffsetPoint thatPt = null;
                                for (OffsetPoint offsetPoint : data.secretPoint.getOffsetPointList()) {
                                    if (offsetPoint.getY() < leastY) {
                                        thatPt = offsetPoint;
                                        leastY = offsetPoint.getY();
                                    }
                                }
                                return new ActionMoveNearestAir(thatPt);
                            }).build("GoAndBreakWall"), algorithmSetting
        );


        for (String str : data.preRequisite) {
            if (str.isEmpty()) continue;
            builder.optional(new ActionChangeState(str.split(":")[0], str.split(":")[1]), algorithmSetting);
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

        for (OffsetPoint offsetPoint : data.secretPoint.getOffsetPointList()) {
            RenderUtils.highlightBlock(offsetPoint.getBlockPos(room), color,partialTicks);
        }
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

        return b == Blocks.air ?"open" :"closed";
    }

    @Override
    public Set<String> getAvailableActions() {
        return isBlocking(room) ? Sets.newHashSet("navigate", "open") : Sets.newHashSet("navigate");
    }

    @Override
    public Set<String> getTotalPossibleStates() {
        return Sets.newHashSet("open", "closed");
    }

    @Override
    public OffsetPoint getRepresentingPoint() {
        return data.secretPoint.getOffsetPointList().size() == 0 ? null : data.secretPoint.getOffsetPointList().get(data.secretPoint.getOffsetPointList().size() / 2);
    }

    @Data
    public static class DungeonBreakableWallData implements WorldMutatingMechanicData {
        private OffsetPointSet secretPoint = new OffsetPointSet();
        private List<String> preRequisite = new ArrayList<String>();

        public DungeonBreakableWallData() {
        }

        public DungeonBreakableWallData clone() throws CloneNotSupportedException {
            DungeonBreakableWallData dungeonSecret = new DungeonBreakableWallData();
            dungeonSecret.secretPoint = (OffsetPointSet) secretPoint.clone();
            dungeonSecret.preRequisite = new ArrayList<String>(preRequisite);
            return dungeonSecret;
        }

        @Override
        public DungeonBreakableWallState createState(DungeonRoom room) {
            return new DungeonBreakableWallState(this, room);
        }

        @Override
        public List<OffsetPoint> blockedPoints() {
            return secretPoint.getOffsetPointList();
        }
    }
}
