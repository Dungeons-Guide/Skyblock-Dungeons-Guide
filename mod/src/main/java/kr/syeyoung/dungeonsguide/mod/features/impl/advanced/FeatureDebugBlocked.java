package kr.syeyoung.dungeonsguide.mod.features.impl.advanced;

import kr.syeyoung.dungeonsguide.mod.DungeonsGuide;
import kr.syeyoung.dungeonsguide.mod.dungeon.DungeonContext;
import kr.syeyoung.dungeonsguide.mod.dungeon.map.DungeonRoomScaffoldParser;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomfinder.DungeonRoom;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomprocessor.GeneralRoomProcessor;
import kr.syeyoung.dungeonsguide.mod.dungeon.world.CollisionStateCalculatingCoordinateMap;
import kr.syeyoung.dungeonsguide.mod.dungeon.world.PearlCalculatingCoordinateMap;
import kr.syeyoung.dungeonsguide.mod.events.annotations.DGEventHandler;
import kr.syeyoung.dungeonsguide.mod.features.FeatureRegistry;
import kr.syeyoung.dungeonsguide.mod.features.SimpleFeature;
import kr.syeyoung.modapi.ModAPI;
import kr.syeyoung.modapi.data.AABB;
import kr.syeyoung.modapi.data.Vector3D;
import kr.syeyoung.modapi.data.VectorI3D;
import kr.syeyoung.modapi.entity.UPlayerSelf;
import kr.syeyoung.modapi.event.events.RenderWorldEvent;

import java.awt.*;

public class FeatureDebugBlocked extends SimpleFeature {
    public FeatureDebugBlocked() {
        super("Debug", "Debug Blocked/Enderpearl", "Toggles debug blocked/enderpearl", "debug.blocked", false);
    }

    @DGEventHandler
    public void onWorldRenderLast(RenderWorldEvent event) {
        if (!FeatureRegistry.DEBUG.isEnabled()) return;
        DungeonContext context = DungeonsGuide.getDungeonsGuide().getDungeonFacade().getContext();
        if (context == null) return;
        DungeonRoomScaffoldParser scaffoldParser = context.getScaffoldParser();
        if (scaffoldParser == null) return;
        UPlayerSelf thePlayer = ModAPI.getAPI().getPlayer();
        Point roomPt = context.getScaffoldParser().getDungeonMapLayout().worldPointToRoomPoint(thePlayer.getPositionVector());

        DungeonRoom dungeonRoom = context.getScaffoldParser().getRoomMap().get(roomPt);
        if (dungeonRoom == null) return;
        if (!(dungeonRoom.getRoomProcessor() instanceof GeneralRoomProcessor)) return;
        GeneralRoomProcessor roomProcessor = (GeneralRoomProcessor) dungeonRoom.getRoomProcessor();

        Vector3D player = ModAPI.getAPI().getPlayer().getPositionVector();
        VectorI3D real = new VectorI3D(player.x * 2, player.y * 2, player.z * 2);
        try {

            for (VectorI3D allInBox : VectorI3D.getAllInBox(real.add(-1, -1, -1), real.add(1, 1, 1))) {
                CollisionStateCalculatingCoordinateMap.CollisionState blocked = roomProcessor.getPathfinderWorld().getBlock(allInBox.getX(), allInBox.getY(), allInBox.getZ());
                event.getContext().highlightBox(
                        new AABB(
                                allInBox.getX() / 2.0 - 0.1, allInBox.getY() / 2.0 - 0.1, allInBox.getZ() / 2.0 - 0.1,
                                allInBox.getX() / 2.0 + 0.1, allInBox.getY() / 2.0 + 0.1, allInBox.getZ() / 2.0 + 0.1
                        ), blocked.getColor().getRGB(), event.getPartialTicks(), false);
                PearlCalculatingCoordinateMap.PearlLandType type = roomProcessor.getPathfinderWorld().getPearl(allInBox.getX(), allInBox.getY(), allInBox.getZ());
                event.getContext().drawTextAtWorld(type.name(), (float) (allInBox.getX() / 2.0 - 0.1), (float) (allInBox.getY() / 2.0 - 0.1), (float) (allInBox.getZ() / 2.0 - 0.1),
                        0xFFFFFFFF,0.01f, false, true, event.getPartialTicks());
            }
        } catch (Exception ignored) {}

    }
}
