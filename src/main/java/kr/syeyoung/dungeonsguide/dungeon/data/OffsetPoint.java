package kr.syeyoung.dungeonsguide.dungeon.data;

import kr.syeyoung.dungeonsguide.dungeon.roomfinder.DungeonRoom;
import kr.syeyoung.dungeonsguide.utils.VectorUtils;
import lombok.AllArgsConstructor;
import lombok.Data;
import net.minecraft.block.Block;
import net.minecraft.util.BlockPos;

import javax.vecmath.Vector2d;

@Data
@AllArgsConstructor
public class OffsetPoint {
    private int x;
    private int y;
    private int z;

    public OffsetPoint(DungeonRoom dungeonRoom, BlockPos pos) {
        Vector2d vector2d = new Vector2d(pos.getX(), pos.getZ());
        for (int i = 0; i < dungeonRoom.getRoomMatcher().getRotation(); i++)
            vector2d = VectorUtils.rotateClockwise(vector2d);

        if (vector2d.x < 0) vector2d.x += dungeonRoom.getDungeonRoomInfo().getBlocks()[0].length - 1;
        if (vector2d.y < 0) vector2d.y += dungeonRoom.getDungeonRoomInfo().getBlocks().length - 1;

        this.x = (int) vector2d.x;
        this.z = (int) vector2d.y;
        this.y = dungeonRoom.getMin().getY() + pos.getY();
    }

    public BlockPos toRotatedRelBlockPos(DungeonRoom dungeonRoom) {
        int rot = dungeonRoom.getRoomMatcher().getRotation();
        Vector2d rot2 = new Vector2d(x,z);
        for (int i = 0; i < dungeonRoom.getRoomMatcher().getRotation(); i++) {
            rot2 = VectorUtils.rotateCounterClockwise(rot2);
        }
        if (rot2.x < 0) rot2.x += dungeonRoom.getMax().getX() - dungeonRoom.getMin().getX() + 2;
        if (rot2.y < 0) rot2.y += dungeonRoom.getMax().getZ() - dungeonRoom.getMin().getZ() + 2;

        return new BlockPos(rot2.x, y, rot2.y);
    }

    public Block getBlock(DungeonRoom dungeonRoom) {
        BlockPos relBp = toRotatedRelBlockPos(dungeonRoom);

        Block b = dungeonRoom.getRelativeBlockAt(relBp.getX(), relBp.getY(), relBp.getZ());
        return b;
    }

    public int getData(DungeonRoom dungeonRoom) {
        BlockPos relBp = toRotatedRelBlockPos(dungeonRoom);

        int b = dungeonRoom.getRelativeBlockDataAt(relBp.getX(), relBp.getY(), relBp.getZ());
        return b;
    }
}
