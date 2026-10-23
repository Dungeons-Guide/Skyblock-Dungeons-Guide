package kr.syeyoung.dungeonsguide.mod.features.impl.secret.routedisplay;

import kr.syeyoung.dungeonsguide.mod.events.impl.LivingDeathEvent;
import kr.syeyoung.dungeonsguide.mod.DungeonsGuide;
import kr.syeyoung.dungeonsguide.mod.SkyblockStatus;
import kr.syeyoung.dungeonsguide.mod.dungeon.DungeonContext;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomfinder.DungeonRoom;
import kr.syeyoung.dungeonsguide.mod.events.annotations.DGEventHandler;
import kr.syeyoung.dungeonsguide.mod.events.impl.DGTickEvent;
import kr.syeyoung.dungeonsguide.mod.events.impl.DungeonRoomDiscoveredEvent;
import kr.syeyoung.dungeonsguide.mod.events.impl.KeyBindPressedEvent;
import kr.syeyoung.dungeonsguide.mod.events.impl.PlayerInteractEntityEvent;
import kr.syeyoung.dungeonsguide.mod.features.SimpleFeature;
import kr.syeyoung.dungeonsguide.mod.features.impl.secret.linestyle.arrowpath.NeoRouteDisplayEngine;
import lombok.Data;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.Identifier;
import kr.syeyoung.dungeonsguide.mod.events.impl.RenderWorldEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import kr.syeyoung.dungeonsguide.mod.events.impl.PlayerInteractEvent;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class LiveRouteRegistry extends SimpleFeature {
    private Map<DungeonRoom, RoomRouteHandler> roomRoomRouteRegistryMap = new ConcurrentHashMap<>();

    public LiveRouteRegistry() {
        super("Pathfinding & Secrets", "Beacon&Route Displayer", "This is an internal feature that displays routes and beacons", "secrets.routeregistry");
    }

    @Override
    public boolean isDisableable() {
        return false;
    }

    public RoomRouteHandler getRoomHandler(DungeonRoom dungeonRoom) {
        if (dungeonRoom == null) return null;
        return roomRoomRouteRegistryMap.computeIfAbsent(dungeonRoom, RoomRouteHandler::new);
    }


    @DGEventHandler(triggerOutOfSkyblock = true, ignoreDisabled = true)
    public void onTick(DGTickEvent event) {
        DungeonContext context = DungeonsGuide.getDungeonsGuide().getDungeonFacade().getContext();
        if (context == null) {
            roomRoomRouteRegistryMap.clear();
            return;
        }

        Optional<DungeonRoom> dungeonRoomOpt = Optional.ofNullable(context)
                .map(DungeonContext::getScaffoldParser)
                .map(a->a.getDungeonMapLayout().worldPointToRoomPoint(Minecraft.getInstance().player.getPositionVector()))
                .map(a -> DungeonsGuide.getDungeonsGuide().getDungeonFacade().getContext().getScaffoldParser().getRoomMap().get(a))
                .filter(a -> a.getRoomProcessor() != null);
        if (!dungeonRoomOpt.isPresent()) {
            return;
        }
        if (roomRoomRouteRegistryMap.containsKey(dungeonRoomOpt.get())) return;

        roomRoomRouteRegistryMap.put(dungeonRoomOpt.get(), new RoomRouteHandler(dungeonRoomOpt.get()));
    }

    private DungeonRoom getRoomIn() {
        DungeonContext context = DungeonsGuide.getDungeonsGuide().getDungeonFacade().getContext();
        if (SkyblockStatus.isOnDungeon() && context != null) {

            EntityPlayerSP thePlayer = Minecraft.getInstance().player;
            if (thePlayer == null) {
                return null;
            }

            if (context.getBossfightProcessor() != null) {
                context.getBossfightProcessor().tick();
            }
            if (context.getScaffoldParser() != null) {
                Point roomPt = context.getScaffoldParser().getDungeonMapLayout().worldPointToRoomPoint(thePlayer.getPositionVector());

                return context.getScaffoldParser().getRoomMap().get(roomPt);
            }

        }
        return null;
    }

    @DGEventHandler
    public void onTick2(DGTickEvent event) {
        RoomRouteHandler roomRouteHandler = getRoomHandler(getRoomIn());
        if (roomRouteHandler == null) return;
        roomRouteHandler.tick();
    }

    @DGEventHandler
    public void onWorldRenderLast(RenderWorldEvent event) {
        RoomRouteHandler roomRouteHandler = getRoomHandler(getRoomIn());
        if (roomRouteHandler == null) return;
        roomRouteHandler.onWorldRenderLast(event);
    }

    @DGEventHandler
    public void onInteract(PlayerInteractEntityEvent event) {
        RoomRouteHandler roomRouteHandler = getRoomHandler(getRoomIn());
        if (roomRouteHandler == null) return;
        roomRouteHandler.onInteract(event);
    }
    @DGEventHandler
    public void onInteract(PlayerInteractEvent event) {
        RoomRouteHandler roomRouteHandler = getRoomHandler(getRoomIn());
        if (roomRouteHandler == null) return;
        roomRouteHandler.onInteract(event);
    }

    @DGEventHandler
    public void onDeath(LivingDeathEvent event) {
        RoomRouteHandler roomRouteHandler = getRoomHandler(getRoomIn());
        if (roomRouteHandler == null) return;
        roomRouteHandler.onEntityDeath(event);
    }

    public TextureAtlasSprite sprite;

    // TODO: 1.8.9 hack to get the arrow sprite into the block atlas. 26.1 has no stitch event, decided later
    @DGEventHandler(triggerOutOfSkyblock = true, ignoreDisabled = true)
    public void onTextureStitch(TextureStitchEvent.Pre event) {
         sprite = event.map.registerSprite(Identifier.fromNamespaceAndPath("dungeonsguide", "arrow"));
    }

    @Override
    public boolean shouldShowOnConfig() {
        return false;
    }
}
