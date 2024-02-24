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

package kr.syeyoung.dungeonsguide.dungeon.mechanics;

import com.google.common.collect.Sets;
import kr.syeyoung.dungeonsguide.dungeon.data.OffsetPoint;
import kr.syeyoung.dungeonsguide.dungeon.data.OffsetPointSet;
import kr.syeyoung.dungeonsguide.dungeon.mechanics.dunegonmechanic.DungeonMechanic;
import kr.syeyoung.dungeonsguide.mod.dungeon.actions.ActionMove;
import kr.syeyoung.dungeonsguide.mod.dungeon.actions.ActionMoveNearestAir;
import kr.syeyoung.dungeonsguide.mod.dungeon.actions.PathfindImpossibleException;
import kr.syeyoung.dungeonsguide.mod.dungeon.actions.tree.ActionDAGBuilder;
import kr.syeyoung.dungeonsguide.mod.dungeon.doorfinder.DungeonDoor;
import kr.syeyoung.dungeonsguide.mod.dungeon.doorfinder.EDungeonDoorType;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomfinder.DungeonRoom;
import kr.syeyoung.dungeonsguide.mod.utils.RenderUtils;
import lombok.Getter;
import net.minecraft.util.BlockPos;
import net.minecraft.util.Tuple;

import javax.vecmath.Vector2d;
import java.awt.*;
import java.util.ArrayList;
import java.util.Set;

public class DungeonRoomDoor2 implements DungeonMechanic {
    private static final long serialVersionUID = 5154467820268491579L;
    @Getter
    private OffsetPoint pfPoint = new OffsetPoint(0,0,0);
    @Getter
    private OffsetPointSet blocks = new OffsetPointSet();


    private Vector2d getIdentifier(DungeonRoom dungeonRoom) {
        BlockPos pos = pfPoint.getBlockPos(dungeonRoom).subtract(dungeonRoom.getMin());
        double xWat = Math.round(pos.getX() / 16) / 2.0 - 0.5;
        double zWat = Math.round(pos.getZ() / 16) / 2.0 - 0.5;
        return new Vector2d(xWat, zWat);
    }

    @Override
    public void buildAction(String state, DungeonRoom dungeonRoom, ActionDAGBuilder builder) throws PathfindImpossibleException {
        if (!"navigate".equalsIgnoreCase(state))
            throw new PathfindImpossibleException(state + " is not valid state for secret");
        builder.requires(new ActionMoveNearestAir(pfPoint));
    }

    @Override
    public void highlight(Color color, String name, DungeonRoom dungeonRoom, float partialTicks) {
        BlockPos pos = pfPoint.getBlockPos(dungeonRoom);
        RenderUtils.highlightBlock(pos, color, partialTicks);
        RenderUtils.drawTextAtWorld(name, pos.getX() + 0.5f, pos.getY() + 0.75f, pos.getZ() + 0.5f, 0xFFFFFFFF, 0.03f, false, true, partialTicks);
        RenderUtils.drawTextAtWorld(getCurrentState(dungeonRoom), pos.getX() + 0.5f, pos.getY() + 0.25f, pos.getZ() + 0.5f, 0xFFFFFFFF, 0.03f, false, true, partialTicks);
    }

    @Override
    public String getCurrentState(DungeonRoom dungeonRoom) {
//        return doorfinder.getType().isKeyRequired() ? "key" : "normal";
        Vector2d id = getIdentifier(dungeonRoom);
        for (Tuple<Vector2d, EDungeonDoorType> doorsAndState : dungeonRoom.getDoorsAndStates()) {
            if (doorsAndState.getFirst().equals(id)) {
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
    public Set<String> getPossibleStates(DungeonRoom dungeonRoom) {
        return Sets.newHashSet("navigate");
    }

    @Override
    public Set<String> getTotalPossibleStates(DungeonRoom dungeonRoom) {
        return Sets.newHashSet("key-open", "key-closed", "normal");
    }

    @Override
    public OffsetPoint getRepresentingPoint(DungeonRoom dungeonRoom) {
        return pfPoint;
    }


    @Override
    public Object clone() throws CloneNotSupportedException {
        DungeonRoomDoor2 dungeonSecret = new DungeonRoomDoor2();
        dungeonSecret.pfPoint = (OffsetPoint) pfPoint.clone();
        dungeonSecret.blocks = (OffsetPointSet) blocks.clone();
        return dungeonSecret;
    }

}
