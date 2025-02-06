package kr.syeyoung.dungeonsguide.mod.dungeon.pathfinding.pathfindcache;

import kr.syeyoung.dungeonsguide.mod.dungeon.data.OffsetVec3;
import kr.syeyoung.dungeonsguide.mod.dungeon.pathfinding.abilitysetting.AlgorithmSetting;
import kr.syeyoung.dungeonsguide.mod.dungeon.pathfinding.algorithms.IPathfinder;
import lombok.Data;
import org.apache.commons.io.input.CountingInputStream;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.zip.InflaterInputStream;

@Data
public class PathfindPrecalculation {
    private int version;
    private String id;
    private String file;

    private String generatedFrom;

    private AlgorithmSetting algorithmSetting;

    private String targetHash;
    private String targetId;
    private UUID roomUID;
    private String roomState;
    private List<OffsetVec3> targetLocations = new ArrayList<>();

    private int start;
    private boolean compressed;


    public PathfindPrecalculation(File f) throws IOException {
        parsePathfindV2Header(f);
        this.file = f.getAbsolutePath();
    }

    // file structure

    // R2DGPF
    // int version
    // utf ID
    // utf HASH
    // utf ROOM UID
    // utf ROOM STATE
    // utf generated From

    // ALGO
    // algorithm settings

    // TRGT
    // list TARGETS

    // NODE


    private void expectMagicValue(DataInputStream dis, String magicValue) throws IOException {
        byte[] bytes = new byte[magicValue.length()];
        int read = dis.read(bytes);
        if (read != bytes.length) throw new IllegalStateException("Expected magic value "+magicValue+" Instead got EOF?");
        String actual = new String(bytes);
        if (!actual.equals(magicValue)) throw new IllegalStateException("Expected magic value "+magicValue+" Instead got "+actual);
    }

    private void parsePathfindV2Header(File f) throws IOException {
        try (FileInputStream fis = new FileInputStream(f)) {
            BufferedInputStream bufferedInputStream = new BufferedInputStream(fis);
            CountingInputStream countingInputStream = new CountingInputStream(bufferedInputStream);
            DataInputStream dis = new DataInputStream(countingInputStream);


            expectMagicValue(dis, "DGPFRES2");
            this.version = dis.readInt();
            this.id = dis.readUTF();
            this.targetHash = dis.readUTF();
            this.targetId = dis.readUTF();
            this.roomUID = UUID.fromString(dis.readUTF());
            this.roomState = dis.readUTF();
            this.generatedFrom = dis.readUTF();


            expectMagicValue(dis, "ALGO");
            algorithmSetting = AlgorithmSetting.deserialize(dis);

            expectMagicValue(dis, "TRGT");
            int targetSize = dis.readInt();
            targetLocations = new ArrayList<>();
            for (int i = 0; i < targetSize; i ++) {
                targetLocations.add(new OffsetVec3(
                        dis.readInt() / 2.0, (dis.readInt() - 140) / 2.0, dis.readInt() / 2.0
                ));
            }

            expectMagicValue(dis, "NODE");
            this.compressed = dis.readBoolean();
            this.start = countingInputStream.getCount();
        }
    }


    public IPathfinder createPathfinder(int rotation) throws IOException {
        try (FileInputStream fileInputStream = new FileInputStream(file)) {
            fileInputStream.skip(start);
            DataInputStream dataInputStream;
            if (compressed) {
                BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream);
                InflaterInputStream gzipInputStream = new InflaterInputStream(bufferedInputStream);
                dataInputStream = new DataInputStream(gzipInputStream);
            } else {
                BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream);
                dataInputStream = new DataInputStream(bufferedInputStream);
            }
            int xStart = dataInputStream.readShort();
            int yStart = dataInputStream.readShort();
            int zStart = dataInputStream.readShort();
            int xLen = dataInputStream.readShort();
            int yLen = dataInputStream.readShort();
            int zLen = dataInputStream.readShort();

//            byte[] b = new byte[xLen * yLen * zLen * 8];
//            dataInputStream.readFully(b);
            ByteBuffer buffer = ByteBuffer.allocateDirect(xLen * yLen * zLen * 8); // use off-heap buffer.
            ReadableByteChannel channel = Channels.newChannel(dataInputStream);
            while (channel.read(buffer) > 0);

            return new CachedPathfinder(rotation, xStart, yStart, zStart, xLen, yLen, zLen, buffer);
        }
    }
}
