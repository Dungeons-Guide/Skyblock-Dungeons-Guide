/*
 *     Dungeons Guide - The most intelligent Hypixel Skyblock Dungeons Mod
 *     Copyright (C) 2021  cyoung06
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Affero General Public License as published
 *     by the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Affero General Public License for more details.
 *
 *     You should have received a copy of the GNU Affero General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package kr.syeyoung.dungeonsguide.dungeon.roomfinder;

import kr.syeyoung.dungeonsguide.dungeon.data.DungeonRoomInfo;
import kr.syeyoung.dungeonsguide.utils.ArrayUtils;
import kr.syeyoung.dungeonsguide.utils.ShortUtils;
import lombok.Getter;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;

import java.util.List;

public class RoomMatcher {
    private final DungeonRoom dungeonRoom;

    @Getter
    private DungeonRoomInfo match;
    @Getter
    private int rotation; // how much the "found room" has to rotate clockwise to match the given dungeon room info. !
    private boolean triedMatch = false;

    private final World w;

    public RoomMatcher(DungeonRoom dungeonRoom) {
        this.dungeonRoom = dungeonRoom;
        w = dungeonRoom.getContext().getWorld();
    }

    public DungeonRoomInfo match() {
        if (triedMatch) return match;

        int zz = dungeonRoom.getMax().getZ() - dungeonRoom.getMin().getZ() + 1;
        int xx = dungeonRoom.getMax().getX() - dungeonRoom.getMin().getX() + 1;
        for (int z = 0; z < zz; z ++) {
            for (int x = 0; x < xx; x++) {
                if (x % 8 == 0 && z % 8 == 0 && dungeonRoom.getContext().getWorld().getChunkFromBlockCoords(dungeonRoom.getRelativeBlockPosAt(x, 0, z)).isEmpty()) {
                    throw new IllegalStateException("chunk is not loaded");

                }
            }
        }

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

        for (int z = 0; z < res.length; z ++) {
            for (int x = 0; x < res[0].length; x++) {
                int data = res[z][x];
                if (data == -1) continue;
                Block b = dungeonRoom.getRelativeBlockAt(x,0,z);

                if (b == null || Block.getIdFromBlock(b) != data) {
                    return false;
                }
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
        int widthX = maxX - minX + 2;
        int heightZ = maxZ - minZ + 2;
        int[][] data = new int[dungeonRoom.getMax().getZ() - dungeonRoom.getMin().getZ() +2][dungeonRoom.getMax().getX() - dungeonRoom.getMin().getX() + 2];

        for (int z = 0; z < data.length; z++) {
            for (int x = 0; x < data[0].length; x++) {
//                if (!(offset < x && widthX - offset > x && offset < z && heightZ - offset > z)) {
//                    data[z][x] = -1;
//                    continue;
//                }
                if (!(dungeonRoom.canAccessRelative(x + offset, z + offset)
                        && dungeonRoom.canAccessRelative(x - offset -1 , z - offset-1)
                        && dungeonRoom.canAccessRelative(x + offset , z - offset-1)
                        && dungeonRoom.canAccessRelative(x - offset -1 , z + offset))) {
                    data[z][x] = -1;
                    continue;
                }

                Block b = dungeonRoom.getRelativeBlockAt(x,0,z);
                if (b == null || b == Blocks.chest || b == Blocks.trapped_chest) {
                    data[z][x] = -1;
                } else {
                    data[z][x] = Block.getIdFromBlock(b);
                }
            }
        }

        roomInfo.setBlocks(data);
        roomInfo.setUserMade(true);
        return roomInfo;
    }
}
