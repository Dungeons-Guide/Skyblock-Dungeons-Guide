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
import kr.syeyoung.dungeonsguide.mod.dungeon.data.OffsetPoint;
import kr.syeyoung.dungeonsguide.mod.dungeon.data.mechanics.dunegonmechanic.DungeonMechanicState;
import kr.syeyoung.dungeonsguide.mod.dungeon.actions.ActionMoveNearestAir;
import kr.syeyoung.dungeonsguide.mod.dungeon.actions.PathfindImpossibleException;
import kr.syeyoung.dungeonsguide.mod.dungeon.actions.tree.ActionDAGBuilder;
import kr.syeyoung.dungeonsguide.mod.dungeon.dataprovider.DungeonDoor;
import kr.syeyoung.dungeonsguide.mod.pathfinding.abilitysetting.AlgorithmSetting;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomfinder.DungeonRoom;
import kr.syeyoung.dungeonsguide.mod.utils.RenderUtils;
import lombok.Getter;
import net.minecraft.util.BlockPos;

import java.awt.*;
import java.util.Set;

public class DungeonRoomDoorState implements DungeonMechanicState {
    @Getter
    private final DungeonDoor doorfinder;
    private OffsetPoint offsetPoint;
    private DungeonRoom room;

    public DungeonRoomDoorState(DungeonRoom dungeonRoom, DungeonDoor doorfinder) {
        this.doorfinder = doorfinder;
        this.room = dungeonRoom;
        if (doorfinder.isZDir()) {
            if (dungeonRoom.getRoomBounds().canAccessAbsolute(doorfinder.getPosition().add(0, 0, 2)))
                offsetPoint = new OffsetPoint(dungeonRoom, doorfinder.getPosition().add(0, 0, 2));
            else if (dungeonRoom.getRoomBounds().canAccessAbsolute(doorfinder.getPosition().add(0, 0, -2)))
                offsetPoint = new OffsetPoint(dungeonRoom, doorfinder.getPosition().add(0, 0, -2));
        } else {
            if (dungeonRoom.getRoomBounds().canAccessAbsolute(doorfinder.getPosition().add(2, 0, 0)))
                offsetPoint = new OffsetPoint(dungeonRoom, doorfinder.getPosition().add(2, 0, 0));
            else if (dungeonRoom.getRoomBounds().canAccessAbsolute(doorfinder.getPosition().add(-2, 0, 0)))
                offsetPoint = new OffsetPoint(dungeonRoom, doorfinder.getPosition().add(-2, 0, 0));
        }
        if (offsetPoint == null) {
            offsetPoint = new OffsetPoint(dungeonRoom, doorfinder.getPosition());
        }
    }

    @Override
    public void buildAction(String action, ActionDAGBuilder builder, AlgorithmSetting algorithmSetting) throws PathfindImpossibleException {
        if (!"navigate".equalsIgnoreCase(action))
            throw new PathfindImpossibleException(action + " is not valid state for secret");
        builder.requires(new ActionMoveNearestAir(offsetPoint), algorithmSetting);
    }

    @Override
    public void highlight(Color color, String name, float partialTicks) {
        BlockPos pos = offsetPoint.getBlockPos(room);
        RenderUtils.highlightBlock(pos, color, partialTicks);
        RenderUtils.drawTextAtWorld(name, pos.getX() + 0.5f, pos.getY() + 0.75f, pos.getZ() + 0.5f, 0xFFFFFFFF, 0.03f, false, true, partialTicks);
        RenderUtils.drawTextAtWorld(getCurrentState(), pos.getX() + 0.5f, pos.getY() + 0.25f, pos.getZ() + 0.5f, 0xFFFFFFFF, 0.03f, false, true, partialTicks);
    }

    @Override
    public String getCurrentState() {
        return doorfinder.getType().isKeyRequired() ? "key" : "normal";
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
        return offsetPoint;
    }
}
