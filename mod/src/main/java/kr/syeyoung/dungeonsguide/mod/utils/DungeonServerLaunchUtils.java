package kr.syeyoung.dungeonsguide.mod.utils;

import com.google.common.collect.Sets;
import kr.syeyoung.dungeonsguide.mod.DungeonsGuide;
import kr.syeyoung.dungeonsguide.mod.dungeon.DungeonContext;
import kr.syeyoung.dungeonsguide.mod.dungeon.data.DungeonRoomInfo;
import kr.syeyoung.dungeonsguide.mod.dungeon.map.DungeonMapLayout;
import kr.syeyoung.dungeonsguide.mod.dungeon.map.DungeonRoomScaffoldParser;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomfinder.DungeonRoom;
import kr.syeyoung.dungeonsguide.mod.dungeon.world.DRIBackedBlockMap;
import kr.syeyoung.dungeonsguide.mod.events.impl.DungeonLeftEvent;
import kr.syeyoung.dungeonsguide.mod.pathfinding.preset.PathfindPreset;
import kr.syeyoung.modapi.ModAPI;
import kr.syeyoung.modapi.data.VectorI3D;
import lombok.Getter;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DungeonServerLaunchUtils {
    public static void launchDungeonServerAndJoin(DungeonRoomInfo dungeonRoomInfo, PathfindPreset preset) {
        if (dungeonRoomInfo.getWorld() == null) throw new IllegalArgumentException("Invalid DRI");
        lastLoadedRoom = dungeonRoomInfo;
        lastLoadedPreset = preset;

        ModAPI.getAPI().getEventBus().fireEvent(new DungeonLeftEvent());
        DungeonsGuide.getDungeonsGuide().getDungeonFacade().setContext(null);


        ModAPI.getAPI().getFakeServerUtils().launchFakeServerAndJoin(new DRIBackedBlockMap(dungeonRoomInfo));
        isRunning = true;
    }
    private static boolean isRunning = false;

    public static void createContext() {
        if (lastLoadedRoom == null) return;
        DungeonRoomInfo dungeonRoomInfo = lastLoadedRoom;
        short shape = dungeonRoomInfo.getShape();
        DungeonContext fakeContext = new DungeonContext("TEST DG", ModAPI.getAPI().getWorld(), lastLoadedPreset);
        DungeonsGuide.getDungeonsGuide().getDungeonFacade().setContext(fakeContext);
        DungeonsGuide.getDungeonsGuide().getSkyblockStatus().setForceIsOnDungeon(true);
        DungeonMapLayout dungeonMapLayout = new DungeonMapLayout(
                new Dimension(16, 16),
                5,
                new Point(0,0),
                new VectorI3D(0,70,0)
        );
        DungeonRoomScaffoldParser scaffoldParser1 = new DungeonRoomScaffoldParser(dungeonMapLayout, fakeContext);
        fakeContext.setScaffoldParser(scaffoldParser1);

        List<Point> points = new ArrayList<>();

        for (int dy = 0; dy < 4; dy++) {
            for (int dx = 0; dx < 4; dx++) {
                boolean isSet = ((shape>> (dy * 4 + dx)) & 0x1) != 0;
                if (isSet) {
                    points.add(new Point(dx, dy));
                }
            }
        }

        DungeonRoom dungeonRoom = new DungeonRoom(
                Sets.newHashSet(points),
                shape,
                dungeonRoomInfo.getColor(),
                new VectorI3D(0, 70, 0),
                new VectorI3D(32 * (dungeonRoomInfo.getWidth()/32) - 1, 70, 32 * (dungeonRoomInfo.getLength()/32) - 1),
                fakeContext,
                Collections.emptySet());

        fakeContext.getScaffoldParser().insertRoom(dungeonRoom);
    }


    @Getter
    private static DungeonRoomInfo lastLoadedRoom;
    private static PathfindPreset lastLoadedPreset;

    public static boolean isDungeonIntegratedServerRunning() {
        return ModAPI.getAPI().getFakeServerUtils().isRunning();
    }
}
