package kr.syeyoung.dungeonsguide.mod.fakeserver;

import kr.syeyoung.dungeonsguide.mod.dungeon.data.DungeonRoomInfo;
import kr.syeyoung.dungeonsguide.mod.dungeon.data.OffsetPoint;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockPos;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.MinecraftException;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.chunk.storage.IChunkLoader;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class DungeonRoomInfoChunkLoader implements IChunkLoader {
    private DungeonRoomInfo dungeonRoomInfo;
    private Map<ChunkCoordIntPair, Chunk> chunkCache = new HashMap<>();


    public DungeonRoomInfoChunkLoader(DungeonRoomInfo dungeonRoomInfo) {
        this.dungeonRoomInfo = dungeonRoomInfo;
        if (dungeonRoomInfo.getWorld() == null) throw new IllegalArgumentException("World is null");
    }


    @Override
    public Chunk loadChunk(World worldIn, int cx, int cz) throws IOException {
        ChunkCoordIntPair pair = new ChunkCoordIntPair(cx, cz);
        if (chunkCache.containsKey(pair)) return chunkCache.get(pair);

        if (0 <= cx && cx < dungeonRoomInfo.getWidth()/16 && 0 <= cz && cz < dungeonRoomInfo.getLength()/16) {
            ChunkPrimer chunkPrimer = new ChunkPrimer();

            for (int x = cx*16; x < cx*16+16; x++) {
                for (int y = 0; y < 256; y++) {
                    for (int z = cz*16; z < cz*16+16; z++) {
                        if (x == 0 || z == 0) continue;
                        OffsetPoint offsetPoint = new OffsetPoint(x,y,z);
                        BlockPos pos = offsetPoint.toRotatedRelBlockPos(0, dungeonRoomInfo.getLength(), dungeonRoomInfo.getWidth());

                        chunkPrimer.setBlockState(x & 0xF, y, z & 0xF,
                                (IBlockState) dungeonRoomInfo.getBlock(pos.getX(), pos.getY()-70, pos.getZ(), 0).getIBlockState());
                    }
                }
            }


            Chunk chunk = new Chunk(worldIn, chunkPrimer, cx, cz);
            chunk.generateSkylightMap();

            chunkCache.put(pair, chunk);
            return chunk;
        }

        Chunk c = generateEmptyChunk(worldIn, cx, cz);
        chunkCache.put(pair, c);
        return c;
    }

    private Chunk generateEmptyChunk(World world, int x, int z) {
        ChunkPrimer chunkPrimer = new ChunkPrimer();
        Chunk c = new Chunk(world, chunkPrimer, x, z);
        c.generateSkylightMap();

        return c;
    }

    @Override
    public void saveChunk(World worldIn, Chunk chunkIn) throws IOException, MinecraftException {
    }

    @Override
    public void saveExtraChunkData(World worldIn, Chunk chunkIn) throws IOException {
    }

    @Override
    public void chunkTick() {
    }

    @Override
    public void saveExtraData() {
    }
}
