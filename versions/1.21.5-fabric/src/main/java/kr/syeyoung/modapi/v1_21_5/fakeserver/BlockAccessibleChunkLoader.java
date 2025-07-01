package kr.syeyoung.modapi.v1_21_5.fakeserver;

import kr.syeyoung.modapi.v1_8_9.world.UBlockStateImpl;
import kr.syeyoung.modapi.world.IBlockAccessible;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.MinecraftException;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.chunk.storage.IChunkLoader;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class BlockAccessibleChunkLoader implements IChunkLoader {
    private IBlockAccessible dungeonRoomInfo;
    private Map<ChunkCoordIntPair, Chunk> chunkCache = new HashMap<>();


    public BlockAccessibleChunkLoader(IBlockAccessible dungeonRoomInfo) {
        this.dungeonRoomInfo = dungeonRoomInfo;
    }


    @Override
    public Chunk loadChunk(World worldIn, int cx, int cz) throws IOException {
        ChunkCoordIntPair pair = new ChunkCoordIntPair(cx, cz);
        if (chunkCache.containsKey(pair)) return chunkCache.get(pair);
        ChunkPrimer chunkPrimer = new ChunkPrimer();

        for (int x = cx*16; x < cx*16+16; x++) {
            for (int y = 0; y < 256; y++) {
                for (int z = cz*16; z < cz*16+16; z++) {
                    if (x == 0 || z == 0) continue;

                    chunkPrimer.setBlockState(x&0xF, y, z&0xF, ((UBlockStateImpl)dungeonRoomInfo.getBlockStateAt(x,y,z)).getIBlockState());
                }
            }
        }


        Chunk chunk = new Chunk(worldIn, chunkPrimer, cx, cz);
        chunk.generateSkylightMap();

        chunkCache.put(pair, chunk);
        return chunk;
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
