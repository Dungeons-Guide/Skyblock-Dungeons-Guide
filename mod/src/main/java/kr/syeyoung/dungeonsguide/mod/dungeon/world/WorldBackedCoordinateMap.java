package kr.syeyoung.dungeonsguide.mod.dungeon.world;

import lombok.Getter;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;

public class WorldBackedCoordinateMap implements ICoordinateMap<IBlockState>  {
    private World world;

    @Getter
    private int minX, minY, minZ, maxX, maxY, maxZ, lenX, lenY, lenZ;

    public WorldBackedCoordinateMap(World world, int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
        this.world = world;
        this.minX = minX; this.minY = minY; this.minZ = minZ;
        this.maxX = maxX; this.maxY = maxY; this.maxZ = maxZ;
        this.lenX = maxX - minX;
        this.lenY = maxY - minY;
        this.lenZ = maxZ - minZ;
    }


    private ThreadLocal<BlockPos.MutableBlockPos> blockPosThreadLocal = ThreadLocal.withInitial(BlockPos.MutableBlockPos::new);
    @Override
    public IBlockState getBlock(int x, int y, int z) {
        BlockPos.MutableBlockPos blockPos = blockPosThreadLocal.get();
        blockPos.set(x, y, z);
        return world.getBlockState(blockPos);
    }

    @Override
    public boolean isInScope(int x, int y, int z) {
        return !(x < minX || z < minZ || x >= maxX || z >= maxZ || y < minY || y >= maxY);
    }
}
