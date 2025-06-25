package kr.syeyoung.dungeonsguide.mod.pathfinding;

import kr.syeyoung.dungeonsguide.mod.dungeon.data.OffsetVec3;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomfinder.DungeonRoom;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomprocessor.GeneralRoomProcessor;
import kr.syeyoung.dungeonsguide.mod.pathfinding.precalculation.PathfindPrecalculation;
import kr.syeyoung.dungeonsguide.mod.pathfinding.precalculation.PrecalculatedPathfinder;
import kr.syeyoung.modapi.data.Vector3D;

import java.io.IOException;
import java.util.*;

public class TSPCache {
    private final List<Vector3D> locationsInCache;
    private Map<String, double[]> cache = new HashMap<>();
    private DungeonRoom dungeonRoom;

    private Comparator<Vector3D> vec3Comparator = new Comparator<Vector3D>() {
        @Override
        public int compare(Vector3D o1, Vector3D o2) {
            int comp = Double.compare(o1.x, o2.x);
            if (comp != 0) return comp;
            comp = Double.compare(o1.y, o2.y);
            if (comp != 0) return comp;
            return Double.compare(o1.z, o2.z);
        }
    };
    private GeneralRoomProcessor generalRoomProcessor;
    public TSPCache(GeneralRoomProcessor generalRoomProcessor, DungeonRoom dungeonRoom, List<OffsetVec3> locs, List<Vector3D> locs2) {
        this.dungeonRoom = dungeonRoom;
        this.generalRoomProcessor = generalRoomProcessor;
        this.locationsInCache = new ArrayList<>();
        for (OffsetVec3 loc : locs) {
            locationsInCache.add(loc.getPos(dungeonRoom));
        }
        for (Vector3D vec3 : locs2) {
            locationsInCache.add(vec3);
        }
        locationsInCache.sort(vec3Comparator);
    }

    public synchronized void addToCache(PathfindPrecalculation precalculation) throws IOException {
        try (PrecalculatedPathfinder iPathfinder = (PrecalculatedPathfinder) precalculation.createPathfinder(dungeonRoom.getRoomMatcher().getRotation())) {
            iPathfinder.init(generalRoomProcessor.getPathfinderWorld(), null);
            double[] arr = new double[locationsInCache.size()];
            for (int i = 0; i < locationsInCache.size(); i++) {
                Vector3D offsetVec3 = locationsInCache.get(i);
                arr[i] = iPathfinder.getCost(offsetVec3);
            }
            cache.put(precalculation.getTargetHash(), arr);
        }
    }

    private int binarySearchIndexOf(Vector3D pos) {
        int low = 0, high = locationsInCache.size()-1;
        while (low <= high) {
            int mid = (high + low) / 2;

            Vector3D elem = locationsInCache.get(mid);
            if (elem.x == pos.x && elem.y == pos.y && elem.z == pos.z) {
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

    public double getCost(String precalcId, Vector3D pos) {
        int index = binarySearchIndexOf(pos);
        if (index == -1) return -1;
        double[] cache = this.cache.get(precalcId);
        if (cache == null) return -2;
        return cache[index];
    }
}
