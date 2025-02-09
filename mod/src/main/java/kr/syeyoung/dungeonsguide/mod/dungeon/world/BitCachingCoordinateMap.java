package kr.syeyoung.dungeonsguide.mod.dungeon.world;

import lombok.Getter;

public class BitCachingCoordinateMap<T extends Enum<T>> implements ICoordinateMap<Enum<T>> {
    private final ICoordinateMap<T> backingMap;
    private final T outOfScope;
    private final T[] values;

    private final BitStorage bitStorage;

    /**
     * Special Contract: values[0] is for UNCACHED.
     *
     * @param backingMap
     * @param values
     * @param outOfScope
     */
    public BitCachingCoordinateMap(ICoordinateMap<T> backingMap, T[] values, T outOfScope) {
        this.backingMap = backingMap;
        this.values = values;
        this.outOfScope = outOfScope;

        minX = backingMap.getMinX();
        minY = backingMap.getMinY();
        minZ = backingMap.getMinZ();
        maxX = backingMap.getMaxX();
        maxY = backingMap.getMaxY();
        maxZ = backingMap.getMaxZ();
        lenX = maxX - minX;
        lenY = maxY - minY;
        lenZ = maxZ - minZ;

        int bitsPer = (int) Math.ceil(Math.log(values.length) / Math.log(2));

        this.bitStorage = new BitStorage(lenX, lenY, lenZ, bitsPer);
    }

    @Getter
    private int minX, minY, minZ, maxX, maxY, maxZ, lenX, lenY, lenZ;

    @Override
    public T getBlock(int x, int y, int z) {
        if (x < minX || z < minZ || x >= maxX || z >= maxZ || y < minY || y >= maxY) return outOfScope;
        int dx = x - minX, dy = y - minY, dz = z - minZ;
        int data = bitStorage.read(dx, dy, dz);
        if (data != 0) return values[data];
        T val = backingMap.getBlock(x, y, z);
        bitStorage.store(dx,dy,dz, val.ordinal());
        return val;
    }


    public boolean update(int x, int y, int z) {
        if (x < minX || z < minZ || x >= maxX || z >= maxZ || y < minY || y >= maxY) return false;
        int dx = x - minX, dy = y - minY, dz = z - minZ;

        T val = backingMap.getBlock(x, y, z);
        return bitStorage.store(dx,dy,dz, val.ordinal());
    }

    public void invalidateCache(int x, int y, int z) {
        if (x < minX || z < minZ || x >= maxX || z >= maxZ || y < minY || y >= maxY) return;
        bitStorage.store(x, y, z, 0);
    }

    @Override
    public boolean isInScope(int x, int y, int z) {
        return backingMap.isInScope(x, y, z);
    }
}
