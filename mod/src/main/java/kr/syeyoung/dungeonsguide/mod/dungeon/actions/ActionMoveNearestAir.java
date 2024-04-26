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

package kr.syeyoung.dungeonsguide.mod.dungeon.actions;


import kr.syeyoung.dungeonsguide.dungeon.data.OffsetPoint;
import kr.syeyoung.dungeonsguide.dungeon.data.OffsetVec3;
import kr.syeyoung.dungeonsguide.dungeon.mechanics.DungeonBreakableWall;
import kr.syeyoung.dungeonsguide.dungeon.mechanics.DungeonDoor;
import kr.syeyoung.dungeonsguide.dungeon.mechanics.DungeonOnewayDoor;
import kr.syeyoung.dungeonsguide.dungeon.mechanics.DungeonTomb;
import kr.syeyoung.dungeonsguide.dungeon.mechanics.dunegonmechanic.RouteBlocker;
import kr.syeyoung.dungeonsguide.mod.dungeon.actions.route.ActionRouteProperties;
import kr.syeyoung.dungeonsguide.mod.dungeon.actions.route.RoomState;
import kr.syeyoung.dungeonsguide.mod.dungeon.pathfinding.BoundingBox;
import kr.syeyoung.dungeonsguide.mod.dungeon.pathfinding.DungeonRoomButOpen;
import kr.syeyoung.dungeonsguide.mod.dungeon.pathfinding.PathfindRequest;
import kr.syeyoung.dungeonsguide.mod.dungeon.pathfinding.PathfindResult;
import kr.syeyoung.dungeonsguide.mod.dungeon.pathfinding.algorithms.FineGridStonkingBFS;
import kr.syeyoung.dungeonsguide.mod.dungeon.pathfinding.algorithms.PathfinderExecutor;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomfinder.DungeonRoom;
import kr.syeyoung.dungeonsguide.mod.features.FeatureRegistry;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.Vec3;
import scala.actors.threadpool.Arrays;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.stream.Collectors;

@EqualsAndHashCode(callSuper=false)
public class ActionMoveNearestAir extends AbstractActionMove {
    @Getter
    private OffsetPoint target;

    public ActionMoveNearestAir(OffsetPoint target) {
        super(new OffsetVec3(target.getX(), target.getY(), target.getZ()), target);
        this.target = target;
    }

    @Override
    public boolean isComplete(DungeonRoom dungeonRoom) {
        return target.getBlockPos(dungeonRoom).distanceSq(Minecraft.getMinecraft().thePlayer.getPosition()) < 25;
    }

    @Override
    public String toString() {
        return "MoveNearestAir\n- target: "+target.toString();
    }

}
