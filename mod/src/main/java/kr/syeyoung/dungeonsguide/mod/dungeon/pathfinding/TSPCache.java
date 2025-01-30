package kr.syeyoung.dungeonsguide.mod.dungeon.pathfinding;

import kr.syeyoung.dungeonsguide.dungeon.data.OffsetVec3;
import kr.syeyoung.dungeonsguide.mod.dungeon.pathfinding.algorithms.IPathfinder;
import kr.syeyoung.dungeonsguide.mod.dungeon.pathfinding.pathfindcache.PathfindPrecalculation;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomfinder.DungeonRoom;
import net.minecraft.util.Vec3;

import java.io.IOException;
import java.util.*;

public class TSPCache {
    private final List<Vec3> locationsInCache;
    private Map<String, double[]> cache = new HashMap<>();
    private DungeonRoom dungeonRoom;

    private Comparator<Vec3> vec3Comparator = new Comparator<Vec3>() {
        @Override
        public int compare(Vec3 o1, Vec3 o2) {
            int comp = Double.compare(o1.xCoord, o2.xCoord);
            if (comp != 0) return comp;
            comp = Double.compare(o1.yCoord, o2.yCoord);
            if (comp != 0) return comp;
            return Double.compare(o1.zCoord, o2.zCoord);
        }
    };
    public TSPCache(DungeonRoom dungeonRoom, List<OffsetVec3> locs) {
        this.dungeonRoom = dungeonRoom;
        this.locationsInCache = new ArrayList<>();
        for (OffsetVec3 loc : locs) {
            locationsInCache.add(loc.getPos(dungeonRoom));
        }
        locationsInCache.sort(vec3Comparator);
    }

    public void addToCache(PathfindPrecalculation precalculation) throws IOException {
        IPathfinder iPathfinder = precalculation.createPathfinder(dungeonRoom.getRoomMatcher().getRotation());
        double[] arr = new double[locationsInCache.size()];
        for (int i = 0; i < locationsInCache.size(); i++) {
            Vec3 offsetVec3 = locationsInCache.get(i);
            arr[i] = iPathfinder.getCost(offsetVec3);
        }
        cache.put(precalculation.getTargetHash(), arr);
    }

    private int binarySearchIndexOf(Vec3 pos) {
        int low = 0, high = locationsInCache.size();
        while (low <= high) {
            int mid = (high + low) / 2;

            Vec3 elem = locationsInCache.get(mid);
            if (elem.equals(pos)) {
                return mid;
            }

            if(vec3Comparator.compare(elem, pos) < 0) {
                low = mid + 1;
            } else {
                high = mid -1;
            }
        }
        return -1;
    }

    public double getCost(String precalcId, Vec3 pos) {
        int index = binarySearchIndexOf(pos);
        if (index == -1) return -1;
        return cache.get(precalcId)[index];
    }
}
