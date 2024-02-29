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

package kr.syeyoung.dungeonsguide.mod.dungeon.pathfinding;

import kr.syeyoung.dungeonsguide.dungeon.data.DungeonRoomInfo;
import kr.syeyoung.dungeonsguide.dungeon.data.OffsetVec3;
import kr.syeyoung.dungeonsguide.mod.dungeon.mocking.DRIWorld;
import kr.syeyoung.dungeonsguide.mod.features.impl.secret.FeaturePathfindSettings;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.Vec3;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Getter @Setter
@AllArgsConstructor
public class PathfindRequest {
    private FeaturePathfindSettings.AlgorithmSettings algorithmSettings;
    private DungeonRoomInfo dungeonRoomInfo;
    private Set<String> openMech; // excludes superboomable things.
    private List<OffsetVec3> target;

    public String getId() {
        String idStart = dungeonRoomInfo.getUuid().toString();
        idStart += ":";
        idStart += openMech.stream().sorted(String::compareTo).collect(Collectors.joining(","));
        idStart += ":";
        idStart += target.stream().sorted(
                Comparator
                        .<OffsetVec3>comparingDouble(a -> a.xCoord)
                        .thenComparingDouble(a -> a.zCoord)
                        .thenComparingDouble(a -> a.yCoord))
                .map(a -> a.xCoord+","+a.yCoord+","+a.zCoord).collect(Collectors.joining(";"));
        return idStart;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        return Objects.equals(getId(), ((PathfindRequest) o).getId());
    }

    @Override
    public int hashCode() {
        String id = getId();
        return id == null ? 0 : id.hashCode();
    }

    public void write(DRIWorld driWorld, DataOutputStream dataOutputStream) throws IOException {
        dataOutputStream.writeUTF("DGPF");
        dataOutputStream.writeUTF(getId());
        dataOutputStream.writeUTF(dungeonRoomInfo.getUuid().toString());
        dataOutputStream.writeUTF(dungeonRoomInfo.getName());
        // export algorithm settings
        dataOutputStream.writeUTF("ALGO");
        dataOutputStream.writeBoolean(algorithmSettings.isEnderpearl());
        dataOutputStream.writeBoolean(algorithmSettings.isTntpearl());
        dataOutputStream.writeBoolean(algorithmSettings.isStonkDown());
        dataOutputStream.writeBoolean(algorithmSettings.isStonkEChest());
        dataOutputStream.writeBoolean(algorithmSettings.isStonkTeleport());
        dataOutputStream.writeBoolean(algorithmSettings.isRouteEtherwarp());
        dataOutputStream.writeInt(algorithmSettings.getMaxStonk());
        dataOutputStream.writeInt(algorithmSettings.getEtherwarpRadius());
        dataOutputStream.writeFloat((float) algorithmSettings.getEtherwarpLeeway());
        dataOutputStream.writeFloat((float) algorithmSettings.getEtherwarpOffset());
        // export targetsO
        dataOutputStream.writeUTF("TRGT");
        dataOutputStream.writeInt(target.size());
        for (OffsetVec3 offsetVec3 : target) {
            dataOutputStream.writeInt((int) (offsetVec3.xCoord * 2));
            dataOutputStream.writeInt((int) (offsetVec3.yCoord * 2) + 140);
            dataOutputStream.writeInt((int) (offsetVec3.zCoord * 2));
        }
        // export blockage map.

        // export world itself first.
        dataOutputStream.writeUTF("WRLD");
        dataOutputStream.writeInt(dungeonRoomInfo.getWidth()); // x len
        dataOutputStream.writeInt(dungeonRoomInfo.getLength()); // z len
        dataOutputStream.writeInt(256); // y len, why not lol.
        // write data
        for (int y = 0; y < 256; y++) {
            for (int z = 0; z < dungeonRoomInfo.getLength(); z++) {
                for (int x = 0; x < dungeonRoomInfo.getWidth(); x++) {
                    IBlockState blockState = driWorld.getBlockState(new BlockPos(x,y,z));
                    dataOutputStream.write((byte) Block.getIdFromBlock(blockState.getBlock()));
                    dataOutputStream.write((byte) blockState.getBlock().getMetaFromState(blockState));
                }
            }
        }


        // export nodestatemap
        dataOutputStream.writeUTF("CLPL");
        dataOutputStream.writeInt(dungeonRoomInfo.getWidth()*2); // x len
        dataOutputStream.writeInt(dungeonRoomInfo.getLength()*2); // z len
        dataOutputStream.writeInt(512); // y len, why not lol.
        // write data
        for (int y = 0; y < 512; y++) {
            for (int z = 0; z < dungeonRoomInfo.getLength()*2; z++) {
                for (int x = 0; x < dungeonRoomInfo.getWidth()*2; x++) {
                    byte data = (byte) (driWorld.getBlock(x,y,z).ordinal() - 1); // 0 is uncached. you're never gonna get that. // 4bit
                    byte pearl = (byte) (driWorld.getPearl(x,y,z).ordinal() - 1); // 3 bit
                    boolean isInsta = driWorld.isInstabreak(x,y,z);
                    byte ultimateData = (byte) ((isInsta ? 1<<7 : 0) | pearl << 4 | data);
                    dataOutputStream.write(ultimateData);
                }
            }
        }
        dataOutputStream.flush();
//        dataOutputStream.write
    }
}
