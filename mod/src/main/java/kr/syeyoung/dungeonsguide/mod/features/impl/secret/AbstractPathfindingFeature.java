package kr.syeyoung.dungeonsguide.mod.features.impl.secret;

import kr.syeyoung.dungeonsguide.mod.DungeonsGuide;
import kr.syeyoung.dungeonsguide.mod.dungeon.DungeonContext;
import kr.syeyoung.dungeonsguide.mod.dungeon.actions.ActionComplete;
import kr.syeyoung.dungeonsguide.mod.dungeon.actions.route.ActionRoute;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomfinder.DungeonRoom;
import kr.syeyoung.dungeonsguide.mod.events.annotations.DGEventHandler;
import kr.syeyoung.dungeonsguide.mod.events.impl.DGTickEvent;
import kr.syeyoung.dungeonsguide.mod.features.AbstractFeature;
import kr.syeyoung.dungeonsguide.mod.features.impl.secret.lineproperties.styles.ClassicPathDisplayEngine;
import kr.syeyoung.dungeonsguide.mod.features.impl.secret.lineproperties.styles.IPathDisplayEngine;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Vec3;
import net.minecraftforge.client.event.RenderWorldLastEvent;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.WeakHashMap;

public class AbstractPathfindingFeature extends AbstractFeature {
    protected final WeakHashMap<DungeonRoom, List<IPathDisplayEngine<?>>> triggeredPathfinds = new WeakHashMap<>();

    public AbstractPathfindingFeature(String category, String name, String description, String key) {
        super(category, name, description, key);
    }

    public void triggerPathfind(IPathDisplayEngine<?> engine) {
        ActionRoute route = engine.getActionRoute();
        triggeredPathfinds.computeIfAbsent(route.getDungeonRoom(), (__) -> new ArrayList<>()).add(engine);
    }
    public void cancelAll(DungeonRoom dungeonRoom) {
        triggeredPathfinds.remove(dungeonRoom); // lol
    }
    public List<IPathDisplayEngine<?>> getPathfinds(DungeonRoom room) {
        return triggeredPathfinds.getOrDefault(room, Collections.emptyList());
    }



    @DGEventHandler
    public final void tickRoutes(DGTickEvent event) {
        DungeonContext context = DungeonsGuide.getDungeonsGuide().getDungeonFacade().getContext();
        if (context == null) return;
        Point pos = context.getScaffoldParser().getDungeonMapLayout().worldPointToRoomPoint(Minecraft.getMinecraft().thePlayer.getPositionVector());
        DungeonRoom dungeonRoom = context.getScaffoldParser().getRoomMap().get(pos);
        if (dungeonRoom == null) return;
        List<IPathDisplayEngine<?>> currentEngines = triggeredPathfinds.get(dungeonRoom);
        if (currentEngines == null) return;

        currentEngines.removeIf(a -> a.getActionRoute().getCurrentAction() instanceof ActionComplete);

        for (IPathDisplayEngine<?> value : currentEngines) {
            value.tick();
        }
    }

    @DGEventHandler
    public final void renderRoutes(RenderWorldLastEvent event) {
        DungeonContext context = DungeonsGuide.getDungeonsGuide().getDungeonFacade().getContext();
        if (context == null) return;
        Point pos = context.getScaffoldParser().getDungeonMapLayout().worldPointToRoomPoint(Minecraft.getMinecraft().thePlayer.getPositionVector());
        DungeonRoom dungeonRoom = context.getScaffoldParser().getRoomMap().get(pos);
        if (dungeonRoom == null) return;
        List<IPathDisplayEngine<?>> currentEngines = triggeredPathfinds.get(dungeonRoom);
        if (currentEngines == null) return;

        for (IPathDisplayEngine<?> value : currentEngines) {
            value.renderActionRoute(event.partialTicks);
        }
    }
}
