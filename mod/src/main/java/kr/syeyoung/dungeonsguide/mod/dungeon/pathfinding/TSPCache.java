package kr.syeyoung.dungeonsguide.mod.dungeon.pathfinding;

import kr.syeyoung.dungeonsguide.dungeon.data.OffsetVec3;
import kr.syeyoung.dungeonsguide.mod.dungeon.pathfinding.algorithms.IPathfinder;
import kr.syeyoung.dungeonsguide.mod.dungeon.pathfinding.pathfindcache.PathfindPrecalculation;
import net.minecraft.util.Vec3;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TSPCache {
    private final List<OffsetVec3> locationsInCache;
    private Map<String, double[]> cache = new HashMap<>();

    public TSPCache(List<OffsetVec3> locs) {
        this.locationsInCache = new ArrayList<>(locs);
    }

    public void addToCache(PathfindPrecalculation precalculation) throws IOException {
        IPathfinder iPathfinder = precalculation.createPathfinder(0);
        double[] arr = new double[locationsInCache.size()];
        for (int i = 0; i < locationsInCache.size(); i++) {
            OffsetVec3 offsetVec3 = locationsInCache.get(i);
            arr[i] = iPathfinder.getCost(new Vec3(offsetVec3.xCoord, offsetVec3.yCoord+70, offsetVec3.zCoord));
        }
        cache.put(precalculation.getTargetHash(), arr);
    }

    public double getCost(String precalcId, OffsetVec3 pos) {
        int index = locationsInCache.indexOf(pos);
        if (index == -1) return -1;
        return cache.get(precalcId)[index];
    }
}
