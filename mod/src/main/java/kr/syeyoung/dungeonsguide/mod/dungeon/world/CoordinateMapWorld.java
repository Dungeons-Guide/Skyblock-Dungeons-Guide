package kr.syeyoung.dungeonsguide.mod.dungeon.world;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.*;
import net.minecraft.world.chunk.IChunkProvider;

public class CoordinateMapWorld extends World {
    private ICoordinateMap<BlockState> map;

    public CoordinateMapWorld(ICoordinateMap<BlockState> map) {
        super(null, null, new WorldProviderSurface(), null, true);
        this.map = map;
    }

    @Override
    protected IChunkProvider createChunkProvider() {
        throw new UnsupportedOperationException("Yikes");
    }

    @Override
    protected int getRenderDistanceChunks() {
        throw new UnsupportedOperationException("Yikes");
    }

    @Override
    public boolean extendedLevelsInChunkCache() {
         throw new UnsupportedOperationException("Yikes");
    }

    @Override
    public BlockEntity getTileEntity(BlockPos pos) {
        throw new UnsupportedOperationException("Yikes");
    }

    @Override
    public int getCombinedLight(BlockPos pos, int lightValue) {
        throw new UnsupportedOperationException("Yikes");
    }

    @Override
    public BlockState getBlockState(BlockPos pos) {
        return map.getBlock(pos.getX(), pos.getY(), pos.getZ());
    }

    @Override
    public int getLightFor(EnumSkyBlock type, BlockPos pos) {
        throw new UnsupportedOperationException("Yikes");
    }

    @Override
    public boolean isAirBlock(BlockPos pos) {
        BlockState blockState = getBlockState(pos);
        return blockState == null || blockState.getBlock() == Blocks.air;
    }

    @Override
    public int getStrongPower(BlockPos pos, Direction direction) {
        throw new UnsupportedOperationException("Yikes");
    }

    @Override
    public boolean isSideSolid(BlockPos pos, Direction side, boolean _default) {
        if (pos.getY() >= 0 && pos.getY() < 256) {
            return _default;
        } else {
            BlockState blockState = map.getBlock(pos.getX(), pos.getY(), pos.getZ());

            return blockState == null ? _default : blockState.getBlock().isSideSolid(this, pos, side);
        }
    }
}
