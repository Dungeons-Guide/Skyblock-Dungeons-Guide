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

package kr.syeyoung.dungeonsguide.mod.dungeon.roomfinder;

import kr.syeyoung.dungeonsguide.dungeon.data.DungeonRoomInfo;
import kr.syeyoung.dungeonsguide.mod.chat.ChatTransmitter;
import kr.syeyoung.dungeonsguide.mod.utils.ArrayUtils;
import kr.syeyoung.dungeonsguide.mod.utils.ShortUtils;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.util.ChatComponentText;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;

import java.util.List;

public class RoomMatcher {
    private final DungeonRoom dungeonRoom;
    @Getter @Setter
    private DungeonRoomInfo match;
    @Getter @Setter
    private int rotation; // how much the "found room" has to rotate clockwise to match the given dungeon room info. !
    private boolean triedMatch = false;


    public RoomMatcher(DungeonRoom dungeonRoom) {
        this.dungeonRoom = dungeonRoom;
    }

    public DungeonRoomInfo match() {
        if (triedMatch) return match;

        triedMatch = true;
        int lowestCost = 10;
        int lowestRot = 0;
        DungeonRoomInfo bestMatch = null;
        for (int rotation = 0; rotation < 4; rotation++) {
            short shape = dungeonRoom.getShape();
            for (int j = 0; j<rotation; j++)
                shape = ShortUtils.rotateClockwise(shape);
            shape = ShortUtils.topLeftifyInt(shape);

            List<DungeonRoomInfo> roomInfoList = DungeonRoomInfoRegistry.getByShape(shape);
            for (DungeonRoomInfo roomInfo : roomInfoList) {
                int cost = tryMatching(roomInfo, rotation);
                if (cost == 0) {
                    match = roomInfo;
                    this.rotation = rotation;
                    return match;
                }
                if (cost < lowestCost) {
                    lowestCost = cost;
                    bestMatch = roomInfo;
                    lowestRot = rotation;
                }
            }
        }
        match = bestMatch;
        this.rotation = lowestRot;
        return bestMatch;
    }

    private int tryMatching(DungeonRoomInfo dungeonRoomInfo, int rotation) {
        if (dungeonRoomInfo.getColor() != dungeonRoom.getColor()) return Integer.MAX_VALUE;

        int[][] res = dungeonRoomInfo.getBlocks();
        for (int i = 0; i < rotation; i++)
            res = ArrayUtils.rotateCounterClockwise(res);

        int wrongs = 0;
        for (int z = 0; z < res.length; z ++) {
            for (int x = 0; x < res[0].length; x++) {
                int data = res[z][x];
                if (data == -1) continue;
                Block b = dungeonRoom.getRelativeBlockAt(x,0,z);

                if (b == null || Block.getIdFromBlock(b) != data) {
                    wrongs++;

                    if (wrongs > 10) return wrongs;
                }
            }
        }
        return wrongs;
    }

    private static final int offset = 3;
    public DungeonRoomInfo createNew() {
        DungeonRoomInfo roomInfo = new DungeonRoomInfo(dungeonRoom.getShape(), dungeonRoom.getColor());

        int maxX = dungeonRoom.getMax().getX();
        int maxZ = dungeonRoom.getMax().getZ();
        int minX = dungeonRoom.getMin().getX();
        int minZ = dungeonRoom.getMin().getZ();
        int[][] data = new int[dungeonRoom.getMax().getZ() - dungeonRoom.getMin().getZ() +2][dungeonRoom.getMax().getX() - dungeonRoom.getMin().getX() + 2];

        for (int z = 0; z < data.length; z++) {
            for (int x = 0; x < data[0].length; x++) {
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
