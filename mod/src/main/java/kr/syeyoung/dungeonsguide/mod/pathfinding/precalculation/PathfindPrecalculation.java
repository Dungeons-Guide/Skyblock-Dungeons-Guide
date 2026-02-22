package kr.syeyoung.dungeonsguide.mod.pathfinding.precalculation;

import kr.syeyoung.dungeonsguide.mod.dungeon.data.OffsetVec3;
import kr.syeyoung.dungeonsguide.mod.pathfinding.abilitysetting.AlgorithmSetting;
import kr.syeyoung.dungeonsguide.mod.pathfinding.pathfinder.IPathfinder;
import lombok.Data;
import lombok.Getter;

import java.io.*;
import java.lang.ref.SoftReference;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
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

    private int xStart, yStart, zStart, xLen, yLen, zLen;


    private static class FullCountingInputStream extends InputStream {
        private final InputStream in;
        private long count = 0;
        private long markCount = -1;

        public FullCountingInputStream(InputStream in) {
            this.in = in;
        }

        @Override
        public int read() throws IOException {
            int b = in.read();
            if (b != -1) count++;
            return b;
        }

        @Override
        public int read(byte[] b, int off, int len) throws IOException {
            int n = in.read(b, off, len);
            if (n > 0) count += n;
            return n;
        }

        @Override
        public long skip(long n) throws IOException {
            long skipped = in.skip(n);
            count += skipped;
            return skipped;
        }

        @Override
        public int available() throws IOException {
            return in.available();
        }

        @Override
        public void close() throws IOException {
            in.close();
        }

        @Override
        public synchronized void mark(int readlimit) {
            in.mark(readlimit);
            markCount = count;
        }

        @Override
        public synchronized void reset() throws IOException {
            in.reset();
            if (markCount != -1) {
                count = markCount;
            }
        }

        @Override
        public boolean markSupported() {
            return in.markSupported();
        }

        public long getByteCount() {
            return count;
        }
    }

    private void parsePathfindV2Header(File f) throws IOException {
        try (FileInputStream fis = new FileInputStream(f)) {
            BufferedInputStream bufferedInputStream = new BufferedInputStream(fis);
            FullCountingInputStream countingInputStream = new FullCountingInputStream(bufferedInputStream);
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
            this.start = (int) countingInputStream.getByteCount();

            DataInputStream dataInputStream;
            if (compressed) {
                InflaterInputStream gzipInputStream = new InflaterInputStream(dis);
                dataInputStream = new DataInputStream(gzipInputStream);
            } else {
                dataInputStream = new DataInputStream(dis);
            }
            this.xStart = dataInputStream.readShort();
            this.yStart = dataInputStream.readShort();
            this.zStart = dataInputStream.readShort();
            this.xLen = dataInputStream.readShort();
            this.yLen = dataInputStream.readShort();
            this.zLen = dataInputStream.readShort();
            dataInputStream.close();
        }
    }

    private SoftReference<ReferenceCountedByteBufferWrapper> byteBufferWeakReference = new SoftReference<>(null);

    public static class ReferenceCountedByteBufferWrapper {
        private AtomicInteger reference = new AtomicInteger(0);
        private AtomicBoolean killed = new AtomicBoolean(false);

        @Getter
        private ByteBuffer byteBuffer;

        public ReferenceCountedByteBufferWrapper(ByteBuffer byteBuffer) {
            this.byteBuffer = byteBuffer;
        }


        private boolean use() {
            if (killed.get()) return false;
            if (reference.getAndIncrement() == 0) return false;
            return true;
        }

        public void release() {
            if (reference.decrementAndGet() == 0) {
                cleanup();
                byteBuffer = null;
            }
        }

        private void cleanup() {
            try {
                if (killed.compareAndSet(false, true))
                    destroyDirectByteBuffer(byteBuffer);
            } catch (IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        }
        public static void destroyDirectByteBuffer(ByteBuffer toBeDestroyed)
                throws IllegalArgumentException, IllegalAccessException,
                InvocationTargetException, SecurityException, NoSuchMethodException {
            Method cleanerMethod = toBeDestroyed.getClass().getMethod("cleaner");
            cleanerMethod.setAccessible(true);
            Object cleaner = cleanerMethod.invoke(toBeDestroyed);
            Method cleanMethod = cleaner.getClass().getMethod("clean");
            cleanMethod.setAccessible(true);
            cleanMethod.invoke(cleaner);
        }

    }

    public IPathfinder createPathfinder(int rotation) throws IOException {
        {
            ReferenceCountedByteBufferWrapper buffer = byteBufferWeakReference.get();
            if (buffer != null && buffer.use()) {
                return new PrecalculatedPathfinder(rotation, xStart, yStart, zStart, xLen, yLen, zLen, buffer);
            }
        }

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
            ReferenceCountedByteBufferWrapper wrapper = new ReferenceCountedByteBufferWrapper(buffer);
            wrapper.use();
            byteBufferWeakReference = new SoftReference<>(wrapper);

            return new PrecalculatedPathfinder(rotation, xStart, yStart, zStart, xLen, yLen, zLen, wrapper);
        }
    }
}
