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

package kr.syeyoung.dungeonsguide.mod.dungeon.pathfinding.cachedpathfind;

import io.netty.buffer.ByteBuf;
import kr.syeyoung.dungeonsguide.dungeon.data.DungeonRoomInfo;
import kr.syeyoung.dungeonsguide.dungeon.data.OffsetVec3;
import kr.syeyoung.dungeonsguide.mod.dungeon.mocking.DRIWorld;
import kr.syeyoung.dungeonsguide.mod.dungeon.pathfinding.algorithms.FineGridStonkingBFS;
import kr.syeyoung.dungeonsguide.mod.dungeon.pathfinding.algorithms.IPathfinder;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomfinder.DungeonRoomInfoRegistry;
import kr.syeyoung.dungeonsguide.mod.features.impl.secret.FeaturePathfindSettings;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;
import org.apache.commons.io.input.CountingInputStream;
import sun.nio.ch.DirectBuffer;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.zip.DeflaterInputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.InflaterInputStream;

public class PathfindCache {
    @Getter
    private String id;
    @Getter
    private UUID roomId;
    private File file;
    @Getter
    private List<OffsetVec3> targets;

    private int gzipStart = 0;

    public PathfindCache(File f) throws IOException {
        this.file = f;

        try (FileInputStream fis = new FileInputStream(f)) {
            BufferedInputStream bufferedInputStream = new BufferedInputStream(fis);
            CountingInputStream countingInputStream = new CountingInputStream(bufferedInputStream);
            DataInputStream dis = new DataInputStream(countingInputStream);
            String magicValue = dis.readUTF();
            if (!magicValue.equals("RDGPF")) throw new IllegalStateException("Expected magic value RDGPF Instead got "+magicValue);

            this.id = dis.readUTF();
            this.roomId = UUID.fromString(dis.readUTF());
            dis.readUTF(); // room name
            magicValue = dis.readUTF();
            if (!magicValue.equals("ALGO")) throw new IllegalStateException("Expected magic value ALGO Instead got "+magicValue);
            dis.skipBytes(10); // skip algorithm settings
            magicValue = dis.readUTF();
            if (!magicValue.equals("TRGT")) throw new IllegalStateException("Expected magic value TRGT Instead got "+magicValue);

            int targetSize = dis.readInt();
            targets = new ArrayList<>();
            for (int i = 0; i < targetSize; i ++) {
                targets.add(new OffsetVec3(
                        dis.readInt() / 2.0, (dis.readInt() - 140) / 2.0, dis.readInt() / 2.0
                ));
            }
            magicValue = dis.readUTF();
            if (!magicValue.equals("NODE")) throw new IllegalStateException("Expected magic value NODE Instead got "+magicValue);
            this.gzipStart = countingInputStream.getCount();
        }

    }

    public IPathfinder createPathfinder(int rotation) throws IOException {
        try (FileInputStream fileInputStream = new FileInputStream(file)) {
            fileInputStream.skip(gzipStart);
            BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream);
            InflaterInputStream gzipInputStream = new InflaterInputStream(bufferedInputStream);
            DataInputStream dataInputStream = new DataInputStream(gzipInputStream);
            int xStart = dataInputStream.readShort();
            int yStart = dataInputStream.readShort();
            int zStart = dataInputStream.readShort();
            int xLen = dataInputStream.readShort();
            int yLen = dataInputStream.readShort();
            int zLen = dataInputStream.readShort();

            byte[] b = new byte[xLen * yLen * zLen * 8];
            dataInputStream.readFully(b);
            ByteBuffer buffer = ByteBuffer.allocateDirect(xLen * yLen * zLen * 8); // use off-heap buffer.
            buffer.put(b);

            return new CachedPathfinder(this, rotation, xStart, yStart, zStart, xLen, yLen, zLen, buffer);
        }
    }


}
