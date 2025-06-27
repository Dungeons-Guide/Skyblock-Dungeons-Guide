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

package kr.syeyoung.dungeonsguide.mod.dungeon.roomprocessor.bombdefuse.chambers;

import kr.syeyoung.dungeonsguide.mod.dungeon.data.OffsetPoint;
import kr.syeyoung.dungeonsguide.mod.dungeon.data.OffsetPointSet;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomfinder.DungeonRoom;
import kr.syeyoung.modapi.data.AABB;
import kr.syeyoung.modapi.data.VectorI3D;
import kr.syeyoung.modapi.entity.EntityType;
import kr.syeyoung.modapi.entity.UEntity;
import kr.syeyoung.modapi.world.UBlockState;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@AllArgsConstructor
@Data
public class BDChamber {
    private DungeonRoom room;
    private OffsetPointSet chamberBlocks;

    private boolean isLeft;
    private int level;

    private ChamberProcessor processor;

    // for whatever's sake, 6: z, 9: x. Starts from bottom right, left, then up

    public OffsetPoint getOffsetPoint(int x, int z) {
        return chamberBlocks.getOffsetPointList().get(z * 9 + x);
    }

    public VectorI3D getBlockPos(int x, int y, int z) {
        return getOffsetPoint(x,z).getBlockPos(room).add(0,y,0);
    }

    public UBlockState getBlock(int x, int y, int z) {
        return room.getRoomWorld().getBlockStateAt(x, y, z);
    }

    public boolean isWithinAbsolute(int x, int y, int z) {
        return isWithinAbsolute(new VectorI3D(x,68,z));
    }
    public boolean isWithinAbsolute(VectorI3D pos) {
        return chamberBlocks.getOffsetPointList().contains(new OffsetPoint(room, new VectorI3D(pos.getX(), 68, pos.getZ())));
    }


    public <T extends UEntity> T getEntityAt(EntityType entity, int x, int y, int z) {
        final VectorI3D pos = getBlockPos(x,y,z);
        return getEntityAt(entity, pos);
    }
    public <T extends UEntity> T getEntityAt(EntityType entity, final VectorI3D pos) {
        List<UEntity> entities = room.getContext().getUworld().getEntitiesWithinAabb(entity, new AABB(pos.getX(), pos.getY(), pos.getZ(), pos.getX()+1,pos.getY()+1, pos.getZ()+1));
        if (entities.size() == 0) return null;

        for (UEntity uEntity : entities) {
            VectorI3D pos1 = uEntity.getPosition();
            if (pos.equals(pos1)) return (T) uEntity;
        }

        return null;
    }
}
