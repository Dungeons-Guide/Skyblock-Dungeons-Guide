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

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import kr.syeyoung.dungeonsguide.mod.dungeon.actions.route.RoomState;
import kr.syeyoung.dungeonsguide.mod.dungeon.data.OffsetPoint;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomfinder.DungeonRoom;
import kr.syeyoung.dungeonsguide.mod.pathfinding.TSPCache;
import kr.syeyoung.dungeonsguide.mod.pathfinding.preset.RoomPresetPathPlanner;
import kr.syeyoung.modapi.data.AABB;
import kr.syeyoung.modapi.data.VectorI3D;
import kr.syeyoung.modapi.entity.EntityType;
import kr.syeyoung.modapi.entity.UEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = false)
public class ActionDropItem extends AbstractAction {
    private OffsetPoint target;
    private Predicate<UEntity> predicate = Predicates.alwaysTrue();

    public ActionDropItem(OffsetPoint target) {
        this.target = target;
    }

    @Override
    public boolean isComplete(DungeonRoom dungeonRoom) {
        VectorI3D secretLocation = target.getBlockPos(dungeonRoom);
        List<UEntity> item = dungeonRoom.getContext().getUworld().getEntitiesWithinAabb(EntityType.ITEM,
                new AABB(
                        secretLocation.getX(),
                        secretLocation.getY(),
                        secretLocation.getZ(),
                        secretLocation.getX() + 1,
                        secretLocation.getY() + 1,
                        secretLocation.getZ() + 1));
        if (item.isEmpty()) {
            return false;
        }
        return (predicate == null || predicate.apply(item.get(0)));
    }

    @Override
    public String toString() {
        return "DropItem\n- target: " + target.toString() + "\n- predicate: " + predicate.getClass().getSimpleName();
    }

    @Override
    public double evalulateCost(RoomState state, DungeonRoom room, TSPCache tspCache, RoomPresetPathPlanner pathPlanner) {
        return 50;
    }
}
