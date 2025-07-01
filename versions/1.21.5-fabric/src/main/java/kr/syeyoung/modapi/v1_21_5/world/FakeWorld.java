package kr.syeyoung.modapi.v1_21_5.world;

import kr.syeyoung.modapi.world.BlockType;
import kr.syeyoung.modapi.world.IBlockAccessible;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.chunk.IChunkProvider;

public class FakeWorld extends World implements IBlockAccess {
    @Getter @Setter
    private IBlockAccessible accessible;

    public FakeWorld() {
        super(null, null, new WorldProviderSurface(), null, true);
    }
    public FakeWorld(WorldProvider provider) {
        super(null, null, provider, null, true);
    }

    @Override
    public TileEntity getTileEntity(BlockPos pos) {
        return null;
    }

    @Override
    protected int getRenderDistanceChunks() {
        return 0;
    }

    @Override
    public int getCombinedLight(BlockPos pos, int lightValue) {
        return 0;
    }

    @Override
    public IBlockState getBlockState(BlockPos pos) {
        return ((UBlockStateImpl)accessible.getBlockStateAt(pos.getX(), pos.getY(), pos.getZ())).getDelegate();
    }

    @Override
    public boolean isAirBlock(BlockPos pos) {
        return accessible.getBlockStateAt(pos.getX(), pos.getY(), pos.getZ()).isOf(BlockType.AIR);
    }

    @Override
    public BiomeGenBase getBiomeGenForCoords(BlockPos pos) {
        return BiomeGenBase.plains;
    }

    @Override
    protected IChunkProvider createChunkProvider() {
        return null;
    }

    @Override
    public boolean extendedLevelsInChunkCache() {
        return false;
    }

    @Override
    public int getStrongPower(BlockPos pos, net.minecraft.util.EnumFacing direction) {
        return 0;
    }

    @Override
    public WorldType getWorldType() {
        return null;
    }

    @Override
    public boolean isSideSolid(BlockPos blockPos, net.minecraft.util.EnumFacing enumFacing, boolean bl) {
        return true;
    }
}
