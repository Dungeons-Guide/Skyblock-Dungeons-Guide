package kr.syeyoung.modapi.v1_21_5.fakeserver;

import net.minecraft.block.Block;
import net.minecraft.profiler.Profiler;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.BlockPos;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.WorldServer;
import net.minecraft.world.storage.ISaveHandler;
import net.minecraft.world.storage.WorldInfo;

import java.io.File;

public class BlockAccessibleWorldServer extends WorldServer {
    public BlockAccessibleWorldServer(MinecraftServer server, ISaveHandler saveHandlerIn, WorldInfo info, int dimensionId, Profiler profilerIn) {
        super(server, saveHandlerIn, info, dimensionId, profilerIn);
    }

    @Override
    protected void updateBlocks() {
        setActivePlayerChunksAndCheckLight();
        for (ChunkCoordIntPair chunkcoordintpair1 : this.activeChunkSet) {
            this.getChunkFromChunkCoords(chunkcoordintpair1.chunkXPos, chunkcoordintpair1.chunkZPos).func_150804_b(false);
        }
    }

    @Override
    public void notifyNeighborsRespectDebug(BlockPos pos, Block blockType) {
        // nah.
    }

    @Override
    public void notifyBlockOfStateChange(BlockPos pos, Block blockIn) {

    }

    @Override
    public boolean tickUpdates(boolean p_72955_1_) {
        return false;
    }

    public File getChunkSaveLocation() {
        return null;
    }
}
