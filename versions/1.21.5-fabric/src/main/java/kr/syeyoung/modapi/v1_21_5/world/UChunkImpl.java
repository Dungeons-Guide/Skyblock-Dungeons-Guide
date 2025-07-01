package kr.syeyoung.modapi.v1_21_5.world;

import kr.syeyoung.modapi.data.VectorI3D;
import kr.syeyoung.modapi.v1_8_9.world.entities.UTileEntityChestImpl;
import kr.syeyoung.modapi.v1_8_9.world.entities.UTileEntityImpl;
import kr.syeyoung.modapi.v1_8_9.world.entities.UTileEntitySkullImpl;
import kr.syeyoung.modapi.world.UBlockState;
import kr.syeyoung.modapi.world.UChunk;
import kr.syeyoung.modapi.world.UTileEntity;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.tileentity.TileEntitySkull;
import net.minecraft.util.BlockPos;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;

public class UChunkImpl implements UChunk {
    private Chunk delegate;
    private BlockStateRegistryImpl registry;

    public UChunkImpl(Chunk chunk, BlockStateRegistryImpl registry) {
        this.delegate = chunk;
        this.registry = registry;
    }

    @Override
    public int getChunkX() {
        return delegate.getChunkCoordIntPair().chunkXPos;
    }

    @Override
    public int getChunkZ() {
        return delegate.getChunkCoordIntPair().chunkZPos;
    }

    private ThreadLocal<BlockPos.MutableBlockPos> posThreadLocal = ThreadLocal.withInitial(() -> new BlockPos.MutableBlockPos());

    @Override
    public UBlockState getRelativeBlockAt(int x, int y, int z) {
        BlockPos.MutableBlockPos pos = posThreadLocal.get();
        pos.set(x, y, z);
        IBlockState blockState = delegate.getBlockState(pos);
        return registry.getByStateId(Block.BLOCK_STATE_IDS.get(blockState));
    }

    @Override
    public UBlockState getBlockStateAt(int x, int y, int z) {
        return getRelativeBlockAt(x, y, z);
    }

    @Override
    public UBlockState getBlockStateAt(VectorI3D blockPos) {
        return getBlockStateAt(blockPos.x, blockPos.y, blockPos.z);
    }

    public boolean isEmpty() {
        boolean empty =  delegate.isEmpty();
        if (empty) return true;
        for (ExtendedBlockStorage extendedBlockStorage : delegate.getBlockStorageArray()) {
            if (extendedBlockStorage != null) return false;
        }
        return true;
    }

    @Override
    public int getLenX() {
        return 16;
    }

    @Override
    public int getLenY() {
        return 256;
    }

    @Override
    public int getLenZ() {
        return 16;
    }

    @Override
    public int getMinX() {
        return delegate.getChunkCoordIntPair().getXStart();
    }

    @Override
    public int getMinY() {
        return 0;
    }

    @Override
    public int getMinZ() {
        return delegate.getChunkCoordIntPair().getZStart();
    }

    @Override
    public UTileEntity getTileEntityAt(VectorI3D blockPos) {
        return getTileEntityAt(blockPos.x, blockPos.y, blockPos.z);
    }

    @Override
    public UTileEntity getTileEntityAt(int x, int y, int z) {
        BlockPos.MutableBlockPos pos = posThreadLocal.get();
        pos.set(x, y, z);
        TileEntity tileEntity = delegate.getTileEntity(pos, Chunk.EnumCreateEntityType.CHECK);
        if (tileEntity instanceof TileEntityChest)
            return new UTileEntityChestImpl((TileEntityChest) tileEntity);
        else if (tileEntity instanceof TileEntitySkull)
            return new UTileEntitySkullImpl((TileEntitySkull) tileEntity);
        else if (tileEntity != null)
            return new UTileEntityImpl(tileEntity);
        return null;
    }
}
