/*
 * Dungeons Guide - The most intelligent Hypixel Skyblock Dungeons Mod
 * Copyright (C) 2024  cyoung06 (syeyoung)
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

package kr.syeyoung.dungeonsguide.dungeon.data;

import kr.syeyoung.dungeonsguide.mod.dungeon.roomfinder.DungeonRoom;
import kr.syeyoung.dungeonsguide.mod.utils.VectorUtils;
import lombok.AllArgsConstructor;
import lombok.Data;
import net.minecraft.util.Vec3;

import javax.vecmath.Vector2d;
import java.io.Serializable;

@Data
@AllArgsConstructor
public class OffsetVec3 implements Cloneable, Serializable {
    private static final long serialVersionUID = 3102336358774967540L;

    public double xCoord;
    public double yCoord;
    public double zCoord;

    public OffsetVec3(DungeonRoom dungeonRoom, Vec3 pos) {
        setPosInWorld(dungeonRoom, pos);
    }


    public void setPosInWorld(DungeonRoom dungeonRoom, Vec3 pos) {
        Vector2d vector2d = new Vector2d(pos.xCoord - dungeonRoom.getMin().getX(), pos.zCoord - dungeonRoom.getMin().getZ());
        for (int i = 0; i < dungeonRoom.getRoomMatcher().getRotation(); i++) {
            vector2d = VectorUtils.rotateClockwise(vector2d);
            if (i % 2 == 0) {
                vector2d.x += dungeonRoom.getDungeonRoomInfo().getBlocks()[0].length - 1; // + Z
            } else {
                vector2d.x += dungeonRoom.getDungeonRoomInfo().getBlocks().length - 1; // + X
            }
        }

        this.xCoord = vector2d.x;
        this.zCoord = vector2d.y;
        this.yCoord = pos.yCoord - dungeonRoom.getMin().getY();
    }

    public Vec3 toRotatedRelBlockPos(DungeonRoom dungeonRoom) {
        Vector2d rot = new Vector2d(xCoord, zCoord);
        for (int i = 0; i < dungeonRoom.getRoomMatcher().getRotation(); i++) {
            rot = VectorUtils.rotateCounterClockwise(rot);
            if (i % 2 == 0) {
                rot.y += dungeonRoom.getMax().getZ() - dungeonRoom.getMin().getZ() + 2; // + Z
            } else {
                rot.y += dungeonRoom.getMax().getX() - dungeonRoom.getMin().getX() + 2; // + X
            }
        }

        return new Vec3(rot.x, yCoord, rot.y);
    }

    public Vec3 getPos(DungeonRoom dungeonRoom) {
        Vec3 relBp = toRotatedRelBlockPos(dungeonRoom);
        return dungeonRoom.getRelativeVec3At(relBp.xCoord, relBp.yCoord, relBp.zCoord);
    }


    @Override
    public Object clone() throws CloneNotSupportedException {
        return new OffsetVec3(xCoord, yCoord, zCoord);
    }

    @Override
    public String toString() {
        return "OffsetPoint{x=" + xCoord +
                ", y=" + yCoord +
                ", z=" + zCoord +
                '}';
    }
}
