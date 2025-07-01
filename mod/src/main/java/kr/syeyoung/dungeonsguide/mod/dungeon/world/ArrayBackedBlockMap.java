package kr.syeyoung.dungeonsguide.mod.dungeon.world;

import kr.syeyoung.modapi.ModAPI;
import kr.syeyoung.modapi.data.VectorI3D;
import kr.syeyoung.modapi.world.*;
import lombok.Getter;

public class ArrayBackedBlockMap implements ICoordinateMap<UBlockState>, IBlockAccessible {
    private UBlockState[] arr;

    @Getter
    private int minX, minY, minZ, maxX, maxY, maxZ, lenX, lenY, lenZ;

    public ArrayBackedBlockMap(int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
        this.minX = minX; this.minY = minY; this.minZ = minZ;
        this.maxX = maxX; this.maxY = maxY; this.maxZ = maxZ;
        this.lenX = maxX - minX;
        this.lenY = maxY - minY;
        this.lenZ = maxZ - minZ;



        arr = new UBlockState[lenX * lenY * lenZ];
    }


    private UBlockState air = ModAPI.getAPI().getBlockRegistry().oneFromWellknown(BlockType.AIR);

    @Override
    public UBlockState getBlock(int x, int y, int z) {
        int dx = x-minX, dy = y-minY, dz = z-minZ;
        if (dx < 0 || dy < 0 || dz < 0 || dx >= lenX || dy >= lenY || dz >= lenZ) return air;
        return arr[dy * lenZ * lenX + lenZ * dx + dz];
    }

    public void setBlock(int x, int y, int z, UBlockState blockState) {
        int dx = x-minX, dy = y-minY, dz = z-minZ;
        if (dx < 0 || dy < 0 || dz < 0 || dx >= lenX || dy >= lenY || dz >= lenZ) throw new IllegalArgumentException("Invalid coordinates: "+x+","+y+","+z);
        arr[dy * lenZ * lenX + lenZ * dx + dz] = blockState;
    }

    public void migrateFromWorld(UWorld uWorld) {
        int idx = 0;
        for (int dy = 0; dy < lenY; dy++) {
            for (int dx = 0; dx < lenX; dx ++) {
                for (int dz = 0; dz < lenZ; dz++) {
                    UBlockState blockState = uWorld.getBlockStateAt(dx+minX, dy+minY, dz+minZ);
                    arr[idx++] = blockState;
                }
            }
        }
    }


    @Override
    public boolean isInScope(int x, int y, int z) {
        return !(x < minX || z < minZ || x >= maxX || z >= maxZ || y < minY || y >= maxY);
    }

    @Override
    public UBlockState getBlockStateAt(int x, int y, int z) {
        return getBlock(x, y, z);
    }

    @Override
    public UBlockState getBlockStateAt(VectorI3D blockPos) {
        return getBlockStateAt(blockPos.x, blockPos.y, blockPos.z);
    }

    @Override
    public UTileEntity getTileEntityAt(int x, int y, int z) {
        return null;
    }

    @Override
    public UTileEntity getTileEntityAt(VectorI3D blockPos) {
        return null;
    }

    public void updateChunk(UChunk uChunk) {
        int idx = 0;
        for (int dy = 0, y = uChunk.getMinY(); dy < uChunk.getLenY(); dy++, y++) {
            for (int dx = 0, x = uChunk.getMinX(); dx < uChunk.getLenX(); dx ++, x++) {
                for (int dz = 0, z = uChunk.getMinZ(); dz < uChunk.getLenZ(); dz++, z++) {
                    if (!isInScope(x,y,z)) continue;
                    setBlock(x,y,z, uChunk.getRelativeBlockAt(dx, dy, dz));
                }
            }
        }
    }
}
