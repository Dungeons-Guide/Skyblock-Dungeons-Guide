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

import kr.syeyoung.dungeonsguide.mod.dungeon.data.DungeonRoomInfo;
import kr.syeyoung.dungeonsguide.mod.utils.ArrayUtils;
import kr.syeyoung.dungeonsguide.mod.utils.ShortUtils;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import kr.syeyoung.dungeonsguide.mod.dungeon.data.LegacyBlock;
import net.minecraft.world.level.block.state.BlockState;

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
        int diagCandidates = 0, diagColorOk = 0, diagRot = -1;
        DungeonRoomInfo diagInfo = null;
        for (int rotation = 0; rotation < 4; rotation++) {
            short shape = dungeonRoom.getRoomBounds().getShape();
            for (int j = 0; j<rotation; j++)
                shape = ShortUtils.rotateClockwise(shape);
            shape = ShortUtils.topLeftifyInt(shape);

            List<DungeonRoomInfo> roomInfoList = DungeonRoomInfoRegistry.getByShape(shape);
            for (DungeonRoomInfo roomInfo : roomInfoList) {
                diagCandidates++;
                if (roomInfo.getColor() == dungeonRoom.getColor()) {
                    diagColorOk++;
                    if (diagInfo == null) { diagInfo = roomInfo; diagRot = rotation; }
                }
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
        // diagnostics: a room that did not match exactly (or at all) is why secrets are not found, say why
        System.out.println("[DungeonsGuide] room match: shape=" + dungeonRoom.getRoomBounds().getShape() + " mapColor=" + dungeonRoom.getColor()
                + " candidates(any rotation)=" + diagCandidates + " sameColor=" + diagColorOk + " bestCost=" + lowestCost
                + " best=" + (bestMatch == null ? "NONE" : bestMatch.getName() + " rot " + lowestRot) + " registeredRooms=" + DungeonRoomInfoRegistry.getRegistered().size());
        try {
            describeMismatch(bestMatch != null ? bestMatch : diagInfo, bestMatch != null ? lowestRot : diagRot);
        } catch (Throwable t) {
            System.out.println("[DungeonsGuide] room match diagnostics failed: " + t);
        }
        return bestMatch;
    }

    private void describeMismatch(DungeonRoomInfo info, int rotation) {
        if (info == null || rotation < 0) return;
        int[][] res = info.getBlocks();
        for (int i = 0; i < rotation; i++)
            res = ArrayUtils.rotateCounterClockwise(res);
        int shown = 0, checked = 0, wrongs = 0, nulls = 0;
        for (int z = 0; z < res.length; z++) {
            for (int x = 0; x < res[0].length; x++) {
                int data = res[z][x];
                if (data == -1) continue;
                checked++;
                BlockState b = dungeonRoom.getRelativeBlockAt(x, 0, z);
                BlockState expected = info.getPaletteState(data);
                if (b == null) nulls++;
                if (b == null || LegacyBlock.legacyId(b) != LegacyBlock.legacyId(expected)) {
                    wrongs++;
                    if (shown++ < 6)
                        System.out.println("[DungeonsGuide]   mismatch (" + x + "," + z + ") live=" + b + " (legacy " + (b == null ? "-" : LegacyBlock.legacyId(b)) + ") expected=" + expected + " (legacy " + LegacyBlock.legacyId(expected) + ")");
                }
            }
        }
        System.out.println("[DungeonsGuide]   compared " + info.getName() + " rot " + rotation + ": checked=" + checked + " wrong=" + wrongs + " unloadedOrNull=" + nulls);
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
                BlockState b = dungeonRoom.getRelativeBlockAt(x,0,z);

                // D110: 1.8.9 compared block ids without meta, the same as comparing legacy groups
                if (b == null || LegacyBlock.legacyId(b) != LegacyBlock.legacyId(dungeonRoomInfo.getPaletteState(data))) {
                    wrongs++;

                    if (wrongs > 10) return wrongs;
                }
            }
        }
        return wrongs;
    }

    private static final int offset = 3;
    public DungeonRoomInfo createNew() {
        DungeonRoomInfo roomInfo = new DungeonRoomInfo(dungeonRoom.getRoomBounds().getShape(), dungeonRoom.getColor());
        // new rooms are format 2 (D111)
        roomInfo.setFormatVersion(2);

        int maxX = dungeonRoom.getRoomBounds().getMax().getX();
        int maxZ = dungeonRoom.getRoomBounds().getMax().getZ();
        int minX = dungeonRoom.getRoomBounds().getMin().getX();
        int minZ = dungeonRoom.getRoomBounds().getMin().getZ();
        int[][] data = new int[dungeonRoom.getRoomBounds().getMax().getZ() - dungeonRoom.getRoomBounds().getMin().getZ() +2][dungeonRoom.getRoomBounds().getMax().getX() - dungeonRoom.getRoomBounds().getMin().getX() + 2];

        for (int z = 0; z < data.length; z++) {
            for (int x = 0; x < data[0].length; x++) {
                if (!(dungeonRoom.getRoomBounds().canAccessRelative(x + offset, z + offset)
                        && dungeonRoom.getRoomBounds().canAccessRelative(x - offset -1 , z - offset-1)
                        && dungeonRoom.getRoomBounds().canAccessRelative(x + offset , z - offset-1)
                        && dungeonRoom.getRoomBounds().canAccessRelative(x - offset -1 , z + offset))) {
                    data[z][x] = -1;
                    continue;
                }

                BlockState b = dungeonRoom.getRelativeBlockAt(x,0,z);
                if (b == null || LegacyBlock.CHEST.is(b) || LegacyBlock.TRAPPED_CHEST.is(b)) {
                    data[z][x] = -1;
                } else {
                    data[z][x] = roomInfo.getPaletteIndex(b);
                }
            }
        }

        roomInfo.setBlocks(data);
        roomInfo.setUserMade(true);
        return roomInfo;
    }
}
