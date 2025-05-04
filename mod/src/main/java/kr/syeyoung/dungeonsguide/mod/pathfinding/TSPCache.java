package kr.syeyoung.dungeonsguide.mod.pathfinding;

import kr.syeyoung.dungeonsguide.mod.dungeon.data.OffsetVec3;
import kr.syeyoung.dungeonsguide.mod.pathfinding.precalculation.PathfindPrecalculation;
import kr.syeyoung.dungeonsguide.mod.pathfinding.precalculation.PrecalculatedPathfinder;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomfinder.DungeonRoom;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomprocessor.GeneralRoomProcessor;
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
    private GeneralRoomProcessor generalRoomProcessor;
    public TSPCache(GeneralRoomProcessor generalRoomProcessor, DungeonRoom dungeonRoom, List<OffsetVec3> locs, List<Vec3> locs2) {
        this.dungeonRoom = dungeonRoom;
        this.generalRoomProcessor = generalRoomProcessor;
        this.locationsInCache = new ArrayList<>();
        for (OffsetVec3 loc : locs) {
            locationsInCache.add(loc.getPos(dungeonRoom));
        }
        for (Vec3 vec3 : locs2) {
            locationsInCache.add(vec3);
        }
        locationsInCache.sort(vec3Comparator);
    }

    public synchronized void addToCache(PathfindPrecalculation precalculation) throws IOException {
        try (PrecalculatedPathfinder iPathfinder = (PrecalculatedPathfinder) precalculation.createPathfinder(dungeonRoom.getRoomMatcher().getRotation())) {
            iPathfinder.init(generalRoomProcessor.getPathfinderWorld(), null);
            double[] arr = new double[locationsInCache.size()];
            for (int i = 0; i < locationsInCache.size(); i++) {
                Vec3 offsetVec3 = locationsInCache.get(i);
                arr[i] = iPathfinder.getCost(offsetVec3);
            }
            cache.put(precalculation.getTargetHash(), arr);
        }
    }

    private int binarySearchIndexOf(Vec3 pos) {
        int low = 0, high = locationsInCache.size()-1;
        while (low <= high) {
            int mid = (high + low) / 2;

            Vec3 elem = locationsInCache.get(mid);
            if (elem.xCoord == pos.xCoord && elem.yCoord == pos.yCoord && elem.zCoord == pos.zCoord) {
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
        double[] cache = this.cache.get(precalcId);
        if (cache == null) return -2;
        return cache[index];
    }
}
