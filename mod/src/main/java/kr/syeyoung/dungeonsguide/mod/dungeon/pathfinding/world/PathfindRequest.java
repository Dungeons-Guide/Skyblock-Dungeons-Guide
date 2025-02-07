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

package kr.syeyoung.dungeonsguide.mod.dungeon.pathfinding.world;

import kr.syeyoung.dungeonsguide.mod.dungeon.data.DungeonRoomInfo;
import kr.syeyoung.dungeonsguide.mod.dungeon.data.OffsetVec3;
import kr.syeyoung.dungeonsguide.mod.dungeon.mocking.DRIWorld;
import kr.syeyoung.dungeonsguide.mod.dungeon.pathfinding.abilitysetting.AlgorithmSetting;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.BlockPos;
import org.apache.commons.codec.binary.Hex;

import java.io.DataOutputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.stream.Collectors;

@Getter @Setter
public class PathfindRequest {
    private final AlgorithmSetting algorithmSetting;
    private final DungeonRoomInfo dungeonRoomInfo;
    private final Set<String> openMech; // excludes superboomable things.
    private final List<OffsetVec3> target;

    public PathfindRequest(AlgorithmSetting algorithmSetting, DungeonRoomInfo dungeonRoomInfo, Set<String> openMech, List<OffsetVec3> target) {
        this.algorithmSetting = algorithmSetting;
        this.dungeonRoomInfo = dungeonRoomInfo;
        this.openMech = openMech;
        this.target = target;
    }

    private String cachedId = null;

    public String getId() {
        if (cachedId != null) return cachedId;
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
        return cachedId = idStart;
    }

    private String hash = null;
    public String getHash() {
        if (this.hash != null) return hash;
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            String hash = Hex.encodeHexString(md.digest(getId().getBytes()));
            return this.hash = hash;
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
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

    /*    Bytes. BTW. Version 1 Format.
    *      0                                       1
    *      0   1   2   3   4   5   6   7   8   9   0   1   2   3   4   5
    *    +---+---+---+---+---+---+---+---+---+---+---+---+---+---+---+---+
    *    |   real Magic Value (DGPFREQ2) |    Version    |               |
    *    +---+---+---+---+---+---+---+---+---+---+---+---+               | // following is version 1
    *    |           Target ID (Variable Size) (Java UTF8)               |
    *    +---+---+---+---+---+---+---+---+---+---+---+---+---+---+---+---+
    *    |         Target Hash (Variable Size) (Java UTF8)               |
    *    +---+---+---+---+---+---+---+---+---+---+---+---+---+---+---+---+
    *    |           Room UID  (Variable Size) (Java UTF8)               | // ik this is stupid
    *    +---+---+---+---+---+---+---+---+---+---+---+---+---+---+---+---+
    *    |           Room Name (Variable Size) (Java UTF8)               |
    *    +---+---+---+---+---+---+---+---+---+---+---+---+---+---+---+---+
    *    |           Room State(Variable Size) (Java UTF8)               |
    *    +---+---+---+---+---+---+---+---+---+---+---+---+---+---+---+---+
    *    |     X Len     |     Y Len     |     Z Len     |Magic Val(ALGO)| // You may ask why it moved here.
    *    +---+---+---+---+---+---+---+---+---+---+---+---+---+---+---+---+ // Well it's easier to validate on python this way
    *    |              Algorithm Settings (Var Size) (NBT)              |
    *    +---+---+---+---+---+---+---+---+---+---+---+---+---+---+---+---+
    *    |Magic Val(TRGT)| Target Len (#)|  Target[0] X  |  Target[0] Y  |
    *    +---+---+---+---+---+---+---+---+---+---+---+---+---+---+---+---+
    *    |  Target[0] Z  |  Target[1] X  |  Target[1] Y  |  Target[1] Z  |
    *    +---+---+---+---+---+---+---+---+---+---+---+---+---+---+---+---+
    *    |Magic Val(WRLD)|                                               |
    *    +---+---+---+---+                                               |
    *    |                                                               |
    *    |                            World                              |
    *    |                                                               |
    *    +---+---+---+---+---+---+---+---+---+---+---+---+---+---+---+---+
    *    |Magic Val(CLPL)|                                               |
    *    +---+---+---+---+                                               |
    *    |                                                               |
    *    |                       Calculated World                        |
    *    |                                                               |
    *    +---+---+---+---+---+---+---+---+---+---+---+---+---+---+---+---+
    * */

    public void write(DRIWorld driWorld, DataOutputStream dataOutputStream) throws IOException {
        dataOutputStream.writeBytes("DGPFREQ2");
        dataOutputStream.writeInt(1); // versioning

        dataOutputStream.writeUTF(getId());
        dataOutputStream.writeUTF(getHash());

        dataOutputStream.writeUTF(dungeonRoomInfo.getUuid().toString());
        dataOutputStream.writeUTF(dungeonRoomInfo.getName());
        dataOutputStream.writeUTF(openMech.stream().sorted(String::compareTo).collect(Collectors.joining(",")));

        dataOutputStream.writeInt(dungeonRoomInfo.getWidth()); // x len
        dataOutputStream.writeInt(dungeonRoomInfo.getLength()); // z len
        dataOutputStream.writeInt(256); // y len, why not lol.

        // export algorithm settings
        dataOutputStream.writeBytes("ALGO");
        NBTTagCompound tagCompound = algorithmSetting.serializeToNBT();
        CompressedStreamTools.write(tagCompound, dataOutputStream);

        // export targets
        dataOutputStream.writeBytes("TRGT");
        dataOutputStream.writeInt(target.size());
        for (OffsetVec3 offsetVec3 : target) {
            dataOutputStream.writeInt((int) (offsetVec3.xCoord * 2));
            dataOutputStream.writeInt((int) (offsetVec3.yCoord * 2) + 140);
            dataOutputStream.writeInt((int) (offsetVec3.zCoord * 2));
        }
        // export blockage map.

        // export world itself first.
        dataOutputStream.writeBytes("WRLD");
        if (dungeonRoomInfo.getWidth() == 0 || dungeonRoomInfo.getLength() == 0 || dungeonRoomInfo.getWorld() == null) throw new IllegalStateException("World doesnot have blocks");
        // write data

            for (int y = 0; y < 256; y++) {
                for (int z = 0; z < dungeonRoomInfo.getLength(); z++) {
                    for (int x = 0; x < dungeonRoomInfo.getWidth(); x++) {
                        IBlockState blockState = driWorld.getBlockState(new BlockPos(x, y, z));
                        dataOutputStream.write((byte) Block.getIdFromBlock(blockState.getBlock()));
                        dataOutputStream.write((byte) blockState.getBlock().getMetaFromState(blockState));
                    }
                }
            }


        // export nodestatemap
        dataOutputStream.writeBytes("CLPL");

            // write data
            for (int y = 0; y < 512; y++) {
                for (int z = 0; z < dungeonRoomInfo.getLength() * 2; z++) {
                    for (int x = 0; x < dungeonRoomInfo.getWidth() * 2; x++) {
                        byte data = (byte) (driWorld.getPathfindWorld().getBlock(x, y, z).ordinal() - 1); // 0 is uncached. you're never gonna get that. // 4bit
                        byte pearl = (byte) (driWorld.getPathfindWorld().getPearl(x, y, z).ordinal() - 1); // 3 bit
                        boolean isInsta = driWorld.getPathfindWorld().isInstabreak(x, y, z);
                        byte ultimateData = (byte) ((isInsta ? 1 << 7 : 0) | pearl << 4 | data);
                        dataOutputStream.write(ultimateData);
                    }
                }
            }


        dataOutputStream.flush();
//        dataOutputStream.write
    }
}
