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
import kr.syeyoung.dungeonsguide.mod.dungeon.actions.ActionMoveNearestAir;
import kr.syeyoung.dungeonsguide.mod.dungeon.actions.PathfindImpossibleException;
import kr.syeyoung.dungeonsguide.mod.dungeon.actions.tree.ActionDAGBuilder;
import kr.syeyoung.dungeonsguide.mod.dungeon.data.OffsetPoint;
import kr.syeyoung.dungeonsguide.mod.dungeon.data.OffsetPointSet;
import kr.syeyoung.dungeonsguide.mod.dungeon.data.OffsetVec3;
import kr.syeyoung.dungeonsguide.mod.dungeon.data.mechanics.dunegonmechanic.DungeonMechanicData;
import kr.syeyoung.dungeonsguide.mod.dungeon.data.mechanics.dunegonmechanic.DungeonMechanicState;
import kr.syeyoung.dungeonsguide.mod.dungeon.dataprovider.EDungeonDoorType;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomfinder.DungeonRoom;
import kr.syeyoung.dungeonsguide.mod.pathfinding.abilitysetting.AlgorithmSetting;
import kr.syeyoung.dungeonsguide.mod.utils.RenderUtils;
import lombok.Data;
import net.minecraft.util.BlockPos;
import net.minecraft.util.Tuple;

import javax.vecmath.Vector2d;
import java.awt.*;
import java.util.Set;

@Data
public class DungeonRoomDoor2State implements DungeonMechanicState {
    private final DungeonRoomDoor2Data data;
    private final DungeonRoom room;

    public DungeonRoomDoor2State(DungeonRoomDoor2Data data, DungeonRoom room) {
        this.data = data;
        this.room = room;
    }


    private Vector2d getIdentifier(DungeonRoom dungeonRoom) {
        BlockPos pos = data.pfPoint.getBlockPos(dungeonRoom).subtract(dungeonRoom.getRoomBounds().getMin());
        double xWat = Math.round(pos.getX() / 16.0) / 2.0 - 0.5;
        double zWat = Math.round(pos.getZ() / 16.0) / 2.0 - 0.5;
        return new Vector2d(xWat, zWat);
    }

    @Override
    public void buildAction(String action, ActionDAGBuilder builder, AlgorithmSetting algorithmSetting) throws PathfindImpossibleException {
        if (!"navigate".equalsIgnoreCase(action))
            throw new PathfindImpossibleException(action + " is not valid state for secret");
        builder.requires(new ActionMoveNearestAir(new OffsetVec3(data.pfPoint.getX(), data.pfPoint.getY(), data.pfPoint.getZ())), algorithmSetting);
    }

    @Override
    public void highlight(Color color, String name, float partialTicks) {
        BlockPos pos = data.pfPoint.getBlockPos(room);
        RenderUtils.highlightBlock(pos, color, partialTicks);
        RenderUtils.drawTextAtWorld(name, pos.getX() + 0.5f, pos.getY() + 0.75f, pos.getZ() + 0.5f, 0xFFFFFFFF, 0.03f, false, true, partialTicks);
        RenderUtils.drawTextAtWorld(getCurrentState(), pos.getX() + 0.5f, pos.getY() + 0.25f, pos.getZ() + 0.5f, 0xFFFFFFFF, 0.03f, false, true, partialTicks);
    }

    @Override
    public String getCurrentState() {
//        return doorfinder.getType().isKeyRequired() ? "key" : "normal";
        Vector2d id = getIdentifier(room);
        for (Tuple<Vector2d, EDungeonDoorType> doorsAndState : room.getDoorsAndStates()) {
            if (doorsAndState.getFirst().equals(id) && doorsAndState.getSecond() != EDungeonDoorType.NONE) {
                return doorsAndState.getSecond().isKeyRequired() ? "key" : "normal";
            }
        }
        return "no-spawn";
    }

    public boolean isHeadtoBlood(DungeonRoom dungeonRoom) {
        Vector2d id = getIdentifier(dungeonRoom);
        for (Tuple<Vector2d, EDungeonDoorType> doorsAndState : dungeonRoom.getDoorsAndStates()) {
            if (doorsAndState.getFirst().equals(id)) {
                return doorsAndState.getSecond().isHeadToBlood();
            }
        }
        return false;
    }

    @Override
    public Set<String> getAvailableActions() {
        return Sets.newHashSet("navigate");
    }

    @Override
    public Set<String> getTotalPossibleStates() {
        return Sets.newHashSet("key-open", "key-closed", "normal");
    }

    @Override
    public OffsetPoint getRepresentingPoint() {
        return data.pfPoint;
    }

    @Data
    public static class DungeonRoomDoor2Data implements DungeonMechanicData {
        private OffsetPoint pfPoint = new OffsetPoint(0, 0, 0);
        private OffsetPointSet blocks = new OffsetPointSet();


        public DungeonRoomDoor2Data() {
        }

        @Override
        public DungeonRoomDoor2State createState(DungeonRoom room) {
            return new DungeonRoomDoor2State(this, room);
        }

        @Override
        public DungeonRoomDoor2Data clone() throws CloneNotSupportedException {
            DungeonRoomDoor2Data data = new DungeonRoomDoor2Data();
            data.pfPoint = (OffsetPoint) pfPoint.clone();
            data.blocks = (OffsetPointSet) blocks.clone();
            ;
            return data;
        }
    }

}
