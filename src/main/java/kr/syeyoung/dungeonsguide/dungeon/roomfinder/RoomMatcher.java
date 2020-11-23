package kr.syeyoung.dungeonsguide.dungeon.roomfinder;

import kr.syeyoung.dungeonsguide.dungeon.data.DungeonRoomInfo;
import kr.syeyoung.dungeonsguide.utils.ArrayUtils;
import kr.syeyoung.dungeonsguide.utils.ShortUtils;
import lombok.Getter;
import net.minecraft.block.Block;

import java.util.List;

public class RoomMatcher {
    private DungeonRoom dungeonRoom;

    @Getter
    private DungeonRoomInfo match;
    @Getter
    private int rotation;
    private boolean triedMatch = false;

    public RoomMatcher(DungeonRoom dungeonRoom) {
        this.dungeonRoom = dungeonRoom;
    }

    public DungeonRoomInfo match() {
        if (triedMatch) return match;
        triedMatch = true;
        for (int rotation = 0; rotation < 4; rotation++) {
            short shape = dungeonRoom.getShape();
            for (int j = 0; j<rotation; j++)
                shape = ShortUtils.rotateClockwise(shape);
            shape = ShortUtils.topLeftifyInt(shape);

            List<DungeonRoomInfo> roomInfoList = DungeonRoomInfoRegistry.getByShape(shape);
            for (DungeonRoomInfo roomInfo : roomInfoList) {
                if (tryMatching(roomInfo, rotation)) {
                    match = roomInfo;
                    this.rotation = rotation;
                    return match;
                }
            }
        }
        return null;
    }

    private boolean tryMatching(DungeonRoomInfo dungeonRoomInfo, int rotation) {
        if (dungeonRoomInfo.getColor() != dungeonRoom.getColor()) return false;

        int[][] res = dungeonRoomInfo.getBlocks();
        for (int i = 0; i < rotation; i++)
            res = ArrayUtils.rotateCounterClockwise(res);

        int y = dungeonRoom.getMin().getY();
        for (int z = 0; z < res.length; z ++) {
            for (int x = 0; x < res[0].length; x++) {
                int data = res[y][x];
                if (data == -1) continue;
                Block b = dungeonRoom.getRelativeBlockAt(x,y,z);
                if (b == null) return false;
                if (Block.getIdFromBlock(b) != data) return false;
            }
        }
        return true;
    }

    private static final int offset = 3;
    public DungeonRoomInfo createNew() {
        DungeonRoomInfo roomInfo = new DungeonRoomInfo(dungeonRoom.getShape(), dungeonRoom.getColor());

        int maxX = dungeonRoom.getMax().getX();
        int maxZ = dungeonRoom.getMax().getZ();
        int minX = dungeonRoom.getMin().getX();
        int minZ = dungeonRoom.getMin().getZ();
        int y = dungeonRoom.getMin().getY();
        int widthX = maxX - minX;
        int heightZ = maxZ - minZ;
        int[][] data = new int[dungeonRoom.getMax().getX() - dungeonRoom.getMin().getX() + 1][dungeonRoom.getMax().getZ() - dungeonRoom.getMin().getZ() +1];

        for (int z = 0; z < data.length; z++) {
            for (int x = 0; x < data[0].length; x++) {
                if (!(offset <= x && widthX - offset >= x && offset <= z && heightZ - offset >= z)) {
                    data[z][x] = -1;
                    continue;
                }
                if (!(dungeonRoom.canAccessRelative(x + offset, z + offset) && dungeonRoom.canAccessRelative(x - offset, z - offset))) {
                    data[z][x] = -1;
                    continue;
                }

                Block b = dungeonRoom.getRelativeBlockAt(x,y,z);
                if (b == null) {
                    data[z][x] = -1;
                } else {
                    data[z][x] = Block.getIdFromBlock(b);
                }
            }
        }

        roomInfo.setBlocks(data);
        return roomInfo;
    }
}
