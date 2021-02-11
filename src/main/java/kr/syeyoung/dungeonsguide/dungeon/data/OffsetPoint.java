package kr.syeyoung.dungeonsguide.dungeon.data;

import kr.syeyoung.dungeonsguide.dungeon.roomfinder.DungeonRoom;
import kr.syeyoung.dungeonsguide.utils.VectorUtils;
import lombok.AllArgsConstructor;
import lombok.Data;
import net.minecraft.block.Block;
import net.minecraft.util.BlockPos;
import net.minecraft.util.Vec3;

import javax.vecmath.Vector2d;
import java.io.Serializable;

@Data
@AllArgsConstructor
public class OffsetPoint implements Cloneable, Serializable {
    private static final long serialVersionUID = 3102336358774967540L;

    private int x;
    private int y;
    private int z;

    public OffsetPoint(DungeonRoom dungeonRoom, BlockPos pos) {
        setPosInWorld(dungeonRoom, pos);
    }
    public OffsetPoint(DungeonRoom dungeonRoom, Vec3 pos) {
        setPosInWorld(dungeonRoom, new BlockPos((int)pos.xCoord, (int)pos.yCoord, (int)pos.zCoord));
    }


    public void setPosInWorld(DungeonRoom dungeonRoom, BlockPos pos) {
        Vector2d vector2d = new Vector2d(pos.getX() - dungeonRoom.getMin().getX(), pos.getZ() - dungeonRoom.getMin().getZ());
        for (int i = 0; i < dungeonRoom.getRoomMatcher().getRotation(); i++) {
            vector2d = VectorUtils.rotateClockwise(vector2d);
            if (i % 2 == 0) {
                vector2d.x += dungeonRoom.getDungeonRoomInfo().getBlocks().length - 1;
            } else {
                vector2d.x += dungeonRoom.getDungeonRoomInfo().getBlocks()[0].length - 1;
            }
        }

        this.x = (int) vector2d.x;
        this.z = (int) vector2d.y;
        this.y = pos.getY()-dungeonRoom.getMin().getY();
    }

    public BlockPos toRotatedRelBlockPos(DungeonRoom dungeonRoom) {
        int rot = dungeonRoom.getRoomMatcher().getRotation();
        Vector2d rot2 = new Vector2d(x,z);
        for (int i = 0; i < dungeonRoom.getRoomMatcher().getRotation(); i++) {
            rot2 = VectorUtils.rotateCounterClockwise(rot2);
            if (i % 2 == 0) {
                rot2.y += dungeonRoom.getMax().getX() - dungeonRoom.getMin().getX() + 1;
            } else {
                rot2.y += dungeonRoom.getMax().getZ() - dungeonRoom.getMin().getZ() + 1;
            }
        }

        return new BlockPos(rot2.x, y, rot2.y);
    }

    public Block getBlock(DungeonRoom dungeonRoom) {
        BlockPos relBp = toRotatedRelBlockPos(dungeonRoom);

        Block b = dungeonRoom.getRelativeBlockAt(relBp.getX(), relBp.getY(), relBp.getZ());
        return b;
    }
    public BlockPos getBlockPos(DungeonRoom dungeonRoom) {
        BlockPos relBp = toRotatedRelBlockPos(dungeonRoom);
        return dungeonRoom.getRelativeBlockPosAt(relBp.getX(), relBp.getY(), relBp.getZ());
    }

    public int getData(DungeonRoom dungeonRoom) {
        BlockPos relBp = toRotatedRelBlockPos(dungeonRoom);

        int b = dungeonRoom.getRelativeBlockDataAt(relBp.getX(), relBp.getY(), relBp.getZ());
        return b;
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
