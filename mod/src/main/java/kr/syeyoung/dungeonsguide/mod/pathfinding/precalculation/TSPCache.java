package kr.syeyoung.dungeonsguide.mod.pathfinding.precalculation;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import kr.syeyoung.dungeonsguide.mod.dungeon.data.OffsetVec3;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomfinder.DungeonRoom;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomprocessor.GeneralRoomProcessor;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.util.Vec3;

import java.io.IOException;
import java.util.*;


public class TSPCache {
    @Getter
    private String id;
    @Getter
    private final List<OffsetVec3> locationsInCache;

    @Setter @Getter
    private Map<String, double[]> cache = new HashMap<>();

    private static final Comparator<OffsetVec3> vec3Comparator = new Comparator<OffsetVec3>() {
        @Override
        public int compare(OffsetVec3 o1, OffsetVec3 o2) {
            int comp = Double.compare(o1.xCoord, o2.xCoord);
            if (comp != 0) return comp;
            comp = Double.compare(o1.yCoord, o2.yCoord);
            if (comp != 0) return comp;
            return Double.compare(o1.zCoord, o2.zCoord);
        }
    };


    public TSPCache(@JsonProperty("locationsInCache") List<OffsetVec3> locs, @JsonProperty("id") String id) {
        this.locationsInCache = new ArrayList<>();
        this.id = id;
        for (OffsetVec3 loc : locs) {
            locationsInCache.add(loc);
        }
        locationsInCache.sort(vec3Comparator);
    }

    public void addToCache(PathfindPrecalculation precalculation) throws IOException {
        PrecalculatedPathfinder iPathfinder = (PrecalculatedPathfinder) precalculation.createPathfinder(0);
        iPathfinder.init2();
        double[] arr = new double[locationsInCache.size()];
        for (int i = 0; i < locationsInCache.size(); i++) {
            OffsetVec3 offsetVec3 = locationsInCache.get(i);
            arr[i] = iPathfinder.getCost(new Vec3(offsetVec3.xCoord, offsetVec3.yCoord+70, offsetVec3.zCoord));
        }
        cache.put(precalculation.getTargetHash(), arr);

        iPathfinder.close();
    }

    private int binarySearchIndexOf(OffsetVec3 pos) {
        int low = 0, high = locationsInCache.size()-1;
        while (low <= high) {
            int mid = (high + low) / 2;

            OffsetVec3 elem = locationsInCache.get(mid);
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

    public double getCost(String precalcId, OffsetVec3 pos) {
        int index = binarySearchIndexOf(pos);
        if (index == -1) return -1;
        double[] cache = this.cache.get(precalcId);
        if (cache == null) return -2;
        return cache[index];
    }
}
