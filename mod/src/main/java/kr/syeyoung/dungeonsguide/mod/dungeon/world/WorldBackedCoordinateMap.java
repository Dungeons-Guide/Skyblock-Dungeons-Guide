package kr.syeyoung.dungeonsguide.mod.dungeon.world;

import kr.syeyoung.modapi.world.IBlockAccessible;
import kr.syeyoung.modapi.world.UBlockState;
import lombok.Getter;

public class WorldBackedCoordinateMap implements ICoordinateMap<UBlockState>  {
    private IBlockAccessible world;

    @Getter
    private int minX, minY, minZ, maxX, maxY, maxZ, lenX, lenY, lenZ;

    public WorldBackedCoordinateMap(IBlockAccessible world, int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
        this.world = world;
        this.minX = minX; this.minY = minY; this.minZ = minZ;
        this.maxX = maxX; this.maxY = maxY; this.maxZ = maxZ;
        this.lenX = maxX - minX;
        this.lenY = maxY - minY;
        this.lenZ = maxZ - minZ;
    }

    @Override
    public UBlockState getBlock(int x, int y, int z) {
        return world.getBlockStateAt(x,y,z);
    }

    @Override
    public boolean isInScope(int x, int y, int z) {
        return !(x < minX || z < minZ || x >= maxX || z >= maxZ || y < minY || y >= maxY);
    }
}
