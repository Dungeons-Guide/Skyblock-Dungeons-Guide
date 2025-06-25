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

package kr.syeyoung.dungeonsguide.mod.dungeon.data;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomfinder.DungeonRoom;
import kr.syeyoung.dungeonsguide.mod.utils.VectorUtils;
import kr.syeyoung.modapi.data.Vector3D;
import kr.syeyoung.modapi.data.VectorI3D;
import lombok.Data;
import net.minecraft.block.Block;
import net.minecraft.util.BlockPos;

import javax.vecmath.Vector2d;

@Data
@JsonFormat(shape = JsonFormat.Shape.ARRAY)
@JsonPropertyOrder({"x", "y", "z"})
public class OffsetPoint implements Cloneable {

    private int x;
    private int y;
    private int z;

    public OffsetPoint() {}

    @JsonCreator
    public OffsetPoint(
            @JsonProperty("x") int x,
            @JsonProperty("y") int y,
            @JsonProperty("z") int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public OffsetPoint(DungeonRoom dungeonRoom, VectorI3D pos) {
        setPosInWorld(dungeonRoom, pos);
    }
    public OffsetPoint(DungeonRoom dungeonRoom, Vector3D pos) {
        setPosInWorld(dungeonRoom, new VectorI3D((int)pos.x, (int)pos.y, (int)pos.z));
    }


    public void setPosInWorld(DungeonRoom dungeonRoom, VectorI3D pos) {
        Vector2d vector2d = new Vector2d(pos.getX() - dungeonRoom.getRoomBounds().getMin().getX(), pos.getZ() - dungeonRoom.getRoomBounds().getMin().getZ());
        for (int i = 0; i < dungeonRoom.getRoomMatcher().getRotation(); i++) {
            vector2d = VectorUtils.rotateClockwise(vector2d);
            if (i % 2 == 0) {
                vector2d.x += dungeonRoom.getDungeonRoomInfo().getBlocks()[0].length - 1; // + Z
            } else {
                vector2d.x += dungeonRoom.getDungeonRoomInfo().getBlocks().length - 1; // + X
            }
        }

        this.x = (int) vector2d.x;
        this.z = (int) vector2d.y;
        this.y = pos.getY()-dungeonRoom.getRoomBounds().getMin().getY();
    }

    public VectorI3D toRotatedRelBlockPos(DungeonRoom dungeonRoom) {
        Vector2d rot = new Vector2d(x,z);
        for (int i = 0; i < dungeonRoom.getRoomMatcher().getRotation(); i++) {
            rot = VectorUtils.rotateCounterClockwise(rot);
            if (i % 2 == 0) {
                rot.y += dungeonRoom.getRoomBounds().getMax().getZ() - dungeonRoom.getRoomBounds().getMin().getZ() + 1; // + Z
            } else {
                rot.y += dungeonRoom.getRoomBounds().getMax().getX() - dungeonRoom.getRoomBounds().getMin().getX() + 1; // + X
            }
        }

        return new VectorI3D((int) rot.x, y, (int) rot.y);
    }

    public BlockPos toRotatedRelBlockPos(int rotation, int zLen, int xLen) {
        Vector2d rot = new Vector2d(x,z);
        for (int i = 0; i < rotation; i++) {
            rot = VectorUtils.rotateCounterClockwise(rot);
            if (i % 2 == 0) {
                rot.y += zLen; // + Z
            } else {
                rot.y += xLen; // + X
            }
        }

        return new BlockPos(rot.x, y, rot.y);
    }

    public Block getBlock(DungeonRoom dungeonRoom) {
        VectorI3D relBp = toRotatedRelBlockPos(dungeonRoom);

        return dungeonRoom.getRelativeBlockAt(relBp.getX(), relBp.getY(), relBp.getZ());
    }
    public VectorI3D getBlockPos(DungeonRoom dungeonRoom) {
        VectorI3D relBp = toRotatedRelBlockPos(dungeonRoom);
        return dungeonRoom.getRelativeBlockPosAt(relBp.getX(), relBp.getY(), relBp.getZ());
    }

    public int getData(DungeonRoom dungeonRoom) {
        VectorI3D relBp = toRotatedRelBlockPos(dungeonRoom);

        return dungeonRoom.getRelativeBlockDataAt(relBp.getX(), relBp.getY(), relBp.getZ());
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        return new OffsetPoint(x,y,z);
    }

    @Override
    public String toString() {
        return "OffsetPoint{x=" + x +
                ", y=" + y +
                ", z=" + z +
                '}';
    }
}
