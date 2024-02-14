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
import kr.syeyoung.dungeonsguide.dungeon.mechanics.dunegonmechanic.DungeonMechanic;
import kr.syeyoung.dungeonsguide.mod.dungeon.actions.ActionMove;
import kr.syeyoung.dungeonsguide.mod.dungeon.actions.ActionMoveNearestAir;
import kr.syeyoung.dungeonsguide.mod.dungeon.actions.PathfindImpossibleException;
import kr.syeyoung.dungeonsguide.mod.dungeon.actions.tree.ActionDAGBuilder;
import kr.syeyoung.dungeonsguide.mod.dungeon.doorfinder.DungeonDoor;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomfinder.DungeonRoom;
import kr.syeyoung.dungeonsguide.mod.utils.RenderUtils;
import lombok.Getter;
import net.minecraft.util.BlockPos;

import java.awt.*;
import java.util.Set;

public class DungeonRoomDoor implements DungeonMechanic {
    @Getter
    private final DungeonDoor doorfinder;
    private OffsetPoint offsetPoint;

    public DungeonRoomDoor(DungeonRoom dungeonRoom, DungeonDoor doorfinder) {
        this.doorfinder = doorfinder;
        if (doorfinder.isZDir()) {
            if (dungeonRoom.canAccessAbsolute(doorfinder.getPosition().add(0, 0, 2)))
                offsetPoint = new OffsetPoint(dungeonRoom, doorfinder.getPosition().add(0, 0, 2));
            else if (dungeonRoom.canAccessAbsolute(doorfinder.getPosition().add(0, 0, -2)))
                offsetPoint = new OffsetPoint(dungeonRoom, doorfinder.getPosition().add(0, 0, -2));
        } else {
            if (dungeonRoom.canAccessAbsolute(doorfinder.getPosition().add(2, 0, 0)))
                offsetPoint = new OffsetPoint(dungeonRoom, doorfinder.getPosition().add(2, 0, 0));
            else if (dungeonRoom.canAccessAbsolute(doorfinder.getPosition().add(-2, 0, 0)))
                offsetPoint = new OffsetPoint(dungeonRoom, doorfinder.getPosition().add(-2, 0, 0));
        }
        if (offsetPoint == null) {
            offsetPoint = new OffsetPoint(dungeonRoom, doorfinder.getPosition());
        }
    }

    @Override
    public void buildAction(String state, DungeonRoom dungeonRoom, ActionDAGBuilder builder) throws PathfindImpossibleException {
        if (!"navigate".equalsIgnoreCase(state))
            throw new PathfindImpossibleException(state + " is not valid state for secret");
        builder.requires(new ActionMoveNearestAir(offsetPoint));
    }

    @Override
    public void highlight(Color color, String name, DungeonRoom dungeonRoom, float partialTicks) {
        BlockPos pos = offsetPoint.getBlockPos(dungeonRoom);
        RenderUtils.highlightBlock(pos, color, partialTicks);
        RenderUtils.drawTextAtWorld(name, pos.getX() + 0.5f, pos.getY() + 0.75f, pos.getZ() + 0.5f, 0xFFFFFFFF, 0.03f, false, true, partialTicks);
        RenderUtils.drawTextAtWorld(getCurrentState(dungeonRoom), pos.getX() + 0.5f, pos.getY() + 0.25f, pos.getZ() + 0.5f, 0xFFFFFFFF, 0.03f, false, true, partialTicks);
    }

    @Override
    public String getCurrentState(DungeonRoom dungeonRoom) {
        return doorfinder.getType().isKeyRequired() ? "key" : "normal";
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
        return offsetPoint;
    }
}
