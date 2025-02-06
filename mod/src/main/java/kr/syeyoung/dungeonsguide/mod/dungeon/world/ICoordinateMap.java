package kr.syeyoung.dungeonsguide.mod.dungeon.world;

import java.util.function.Consumer;

public interface ICoordinateMap<T> {
    T getBlock(int x, int y, int z);
    boolean isInScope(int x, int y, int z);

    int getMinX(); // inclusive
    int getMinY(); // inclusive
    int getMinZ(); // inclusive
    int getMaxX(); // exclusive
    int getMaxY(); // exclusive
    int getMaxZ(); // exclusive

    default int getLenX() {
        return getMaxX() - getMinX();
    }
    default int getLenY() {
        return getMaxY() - getMinY();
    }
    default int getLenZ() {
        return getMaxZ() - getMinZ();
    }
}
