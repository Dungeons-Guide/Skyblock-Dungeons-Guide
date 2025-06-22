package kr.syeyoung.dungeonsguide.mod.dungeon.world;

import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;
import net.minecraft.world.WorldProviderSurface;
import net.minecraft.world.chunk.IChunkProvider;

public class CoordinateMapWorld extends World {
    private ICoordinateMap<IBlockState> map;

    public CoordinateMapWorld(ICoordinateMap<IBlockState> map) {
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
    public TileEntity getTileEntity(BlockPos pos) {
        throw new UnsupportedOperationException("Yikes");
    }

    @Override
    public int getCombinedLight(BlockPos pos, int lightValue) {
        throw new UnsupportedOperationException("Yikes");
    }

    @Override
    public IBlockState getBlockState(BlockPos pos) {
        return map.getBlock(pos.getX(), pos.getY(), pos.getZ());
    }

    @Override
    public int getLightFor(EnumSkyBlock type, BlockPos pos) {
        throw new UnsupportedOperationException("Yikes");
    }

    @Override
    public boolean isAirBlock(BlockPos pos) {
        IBlockState blockState = getBlockState(pos);
        return blockState == null || blockState.getBlock() == Blocks.air;
    }

    @Override
    public int getStrongPower(BlockPos pos, EnumFacing direction) {
        throw new UnsupportedOperationException("Yikes");
    }

    @Override
    public boolean isSideSolid(BlockPos pos, EnumFacing side, boolean _default) {
        if (pos.getY() >= 0 && pos.getY() < 256) {
            return _default;
        } else {
            IBlockState blockState = map.getBlock(pos.getX(), pos.getY(), pos.getZ());

            return blockState == null ? _default : blockState.getBlock().isSideSolid(this, pos, side);
        }
    }
}
