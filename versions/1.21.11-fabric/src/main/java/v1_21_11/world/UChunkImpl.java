package v1_21_11.world;

import kr.syeyoung.modapi.data.VectorI3D;
import kr.syeyoung.modapi.v1_21_5.world.entities.UTileEntityChestImpl;
import kr.syeyoung.modapi.v1_21_5.world.entities.UTileEntityImpl;
import kr.syeyoung.modapi.v1_21_5.world.entities.UTileEntitySkullImpl;
import kr.syeyoung.modapi.world.UBlockState;
import kr.syeyoung.modapi.world.UChunk;
import kr.syeyoung.modapi.world.UTileEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.block.entity.SkullBlockEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkSection;

public class UChunkImpl implements UChunk {
    private Chunk delegate;
    private BlockStateRegistryImpl registry;

    public UChunkImpl(Chunk chunk, BlockStateRegistryImpl registry) {
        this.delegate = chunk;
        this.registry = registry;
    }

    @Override
    public int getChunkX() {
        return delegate.getPos().x;
    }

    @Override
    public int getChunkZ() {
        return delegate.getPos().z;
    }

    private ThreadLocal<BlockPos.Mutable> posThreadLocal = ThreadLocal.withInitial(() -> new BlockPos.Mutable());

    @Override
    public UBlockState getRelativeBlockAt(int x, int y, int z) {
        BlockPos.Mutable pos = posThreadLocal.get();
        pos.set(x, y, z);
        BlockState blockState = delegate.getBlockState(pos);
        return registry.getByStateId(Block.STATE_IDS.getRawId(blockState));
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
        for (ChunkSection section : delegate.getSectionArray()) {
            if (!section.isEmpty()) return false;
        }
        return true;
    }

    @Override
    public int getLenX() {
        return 16;
    }

    @Override
    public int getLenY() {
        return delegate.getHeight();
    }

    @Override
    public int getLenZ() {
        return 16;
    }

    @Override
    public int getMinX() {
        return delegate.getPos().getStartX();
    }

    @Override
    public int getMinY() {
        return delegate.getBottomY();
    }

    @Override
    public int getMinZ() {
        return delegate.getPos().getStartZ();
    }

    @Override
    public UTileEntity getTileEntityAt(VectorI3D blockPos) {
        return getTileEntityAt(blockPos.x, blockPos.y, blockPos.z);
    }

    @Override
    public UTileEntity getTileEntityAt(int x, int y, int z) {
        BlockPos.Mutable pos = posThreadLocal.get();
        pos.set(x, y, z);
        BlockEntity tileEntity = delegate.getBlockEntity(pos);
        if (tileEntity instanceof ChestBlockEntity)
            return new UTileEntityChestImpl((ChestBlockEntity) tileEntity);
        else if (tileEntity instanceof SkullBlockEntity)
            return new UTileEntitySkullImpl((SkullBlockEntity) tileEntity);
        else if (tileEntity != null)
            return new UTileEntityImpl(tileEntity);
        return null;
    }
}
