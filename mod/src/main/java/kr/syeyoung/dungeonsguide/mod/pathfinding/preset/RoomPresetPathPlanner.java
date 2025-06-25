package kr.syeyoung.dungeonsguide.mod.pathfinding.preset;

import kr.syeyoung.dungeonsguide.mod.chat.ChatTransmitter;
import kr.syeyoung.dungeonsguide.mod.dungeon.data.DungeonRoomInfo;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomfinder.DungeonRoom;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomfinder.DungeonRoomInfoRegistry;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomprocessor.GeneralRoomProcessor;
import kr.syeyoung.dungeonsguide.mod.pathfinding.BoundingBox;
import kr.syeyoung.dungeonsguide.mod.pathfinding.abilitysetting.AlgorithmSetting;
import kr.syeyoung.dungeonsguide.mod.pathfinding.pathfinder.IPathfinder;
import kr.syeyoung.dungeonsguide.mod.pathfinding.pathfinder.PathfinderExecutor;
import kr.syeyoung.dungeonsguide.mod.pathfinding.precalculation.PathfindPrecalculation;
import kr.syeyoung.dungeonsguide.mod.pathfinding.precalculation.PathfindPrecalculationRegistry;
import kr.syeyoung.dungeonsguide.mod.pathfinding.precalculation.TSPCache;
import kr.syeyoung.dungeonsguide.mod.pathfinding.precalculation.TSPCacheRegistry;
import kr.syeyoung.modapi.data.AABB;
import lombok.Getter;
import sun.misc.Cleaner;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class RoomPresetPathPlanner {
    private RoomPreset preset;
    private DungeonRoomInfo dungeonRoomInfo;
    @Getter
    private AlgorithmSetting algorithmSetting;

    private Map<String, PathfindPrecalculation> byHash = new HashMap<>();

    public RoomPresetPathPlanner(RoomPreset roomPreset) {
        dungeonRoomInfo = DungeonRoomInfoRegistry.getByUUID(roomPreset.getRoomId());
        this.algorithmSetting = roomPreset.getEffectiveAlgorithmSetting(dungeonRoomInfo);

        Set<String> pathfinders = roomPreset.getPrecalculations();
        if (pathfinders != null) {
            for (String precalcId : pathfinders) {
                PathfindPrecalculation cachedPathfinder = PathfindPrecalculationRegistry.getINSTANCE().getById(precalcId);
                if (cachedPathfinder == null) return;
                if (byHash.containsKey(cachedPathfinder.getTargetHash())) return;
                byHash.put(cachedPathfinder.getTargetHash(), cachedPathfinder);
            }
        }
        this.preset = roomPreset;
    }

    private long nextWarning = 0L;

    public PathfindPrecalculation getPrecalcByHash(String hash) {
        System.out.println("LOADING:: "+hash);
        if (!byHash.containsKey(hash)) {
            if (nextWarning < System.currentTimeMillis()) {
                ChatTransmitter.addToQueue("§eDungeons Guide §7:: §cPrecalculation "+hash+" in room "+dungeonRoomInfo.getName()+" is §4§lMISSING §cin currently applied preset §e"+preset.getParent().getPresetName()+"§c. There may be some problems in pathfinding. Please add precalculations at /dg -> Pathfinding & Secrets -> Precalculations");
                nextWarning = System.currentTimeMillis() + 30000L;
            }
            return null;
        }
        return byHash.get(hash);
    }

    public PathfinderExecutor loadPrecalculatedByHash(String hash, DungeonRoom dungeonRoom) {
        PathfindPrecalculation precalculation = getPrecalcByHash(hash);

        try {
            IPathfinder pathfinder = precalculation.createPathfinder(dungeonRoom.getRoomMatcher().getRotation());
            PathfinderExecutor executor1 = new PathfinderExecutor(pathfinder, BoundingBox.of(new AABB(0,0,0,0,0,0)),
                    ((GeneralRoomProcessor)dungeonRoom.getRoomProcessor()).getPathfinderWorld());
            executor1.doStep();

            Cleaner.create(executor1, pathfinder::close); // WELLLLLL... well... well...

            return executor1;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private TSPCache tspCache;
    public TSPCache getTSPCache() {
        if (tspCache != null) return tspCache;
        return tspCache = TSPCacheRegistry.getINSTANCE().getTSPCache(preset.getTSPCache());
    }

}
