package kr.syeyoung.dungeonsguide.mod.features.impl.secret.routedisplay;

import kr.syeyoung.dungeonsguide.mod.dungeon.actions.*;
import kr.syeyoung.dungeonsguide.mod.dungeon.actions.route.ActionRoute;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomfinder.DungeonRoom;
import kr.syeyoung.dungeonsguide.mod.events.impl.PlayerInteractEntityEvent;
import kr.syeyoung.dungeonsguide.mod.features.impl.secret.linestyle.IPathDisplayEngine;
import kr.syeyoung.dungeonsguide.mod.pathfinding.abilitysetting.AlgorithmSetting;
import lombok.Getter;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;

import java.util.*;
import java.util.function.Function;

public class RoomRouteHandler {
    @Getter
    private Map<String, IPathDisplayEngine<?>> path = new HashMap<>();
    @Getter
    private final DungeonRoom dungeonRoom;
    @Getter
    private AlgorithmSetting algorithmSetting;


    public RoomRouteHandler(DungeonRoom targetRoom) {
        this.dungeonRoom = targetRoom;
        algorithmSetting = targetRoom.getContext().getPreset().getAlgorithmSetting(); // TODO: fix
    }

    public void tick() {
        Set<String> toRemove = new HashSet<>();
        path.entrySet().forEach(a -> {
            a.getValue().tick();
            a.getValue().getActionRoute().onTick();
            if (a.getValue().getActionRoute().getCurrentAction() instanceof ActionComplete)
                toRemove.add(a.getKey());
        });
        toRemove.forEach(path::remove);
    }

    public void onWorldRenderLast(RenderWorldLastEvent event) {
        path.values().forEach(a -> {
            a.renderActionRoute(event.partialTicks);
        });
    }

    public void onInteract(PlayerInteractEntityEvent event) {
        path.values().forEach(a -> {
            a.getActionRoute().onLivingInteract(event);
        });
    }

    public void onInteract(PlayerInteractEvent event) {
        path.values().forEach(a -> {
            a.getActionRoute().onPlayerInteract(event);
        });
    }

    public void onEntityDeath(LivingDeathEvent event) {
        path.values().forEach(a -> {
            a.getActionRoute().onLivingDeath(event);
        });
    }

    public IPathDisplayEngine<?> getPath(String id){
        return path.get(id);
    }

    public String pathfind(String mechanic, String state, Function<ActionRoute, IPathDisplayEngine<?>> converter) throws PathfindImpossibleException {
        String str = UUID.randomUUID().toString();
        pathfind(str, mechanic, state, converter);
        return str;
    }

    public void pathfind(String id, String mechanic, String state, Function<ActionRoute, IPathDisplayEngine<?>> converter) throws PathfindImpossibleException {
        pathfind(id, converter.apply(new ActionRoute(dungeonRoom, mechanic, state, algorithmSetting)));
    }
    public void pathfind(String id, IPathDisplayEngine<?> pathDisplayEngine) {
        path.put(id, pathDisplayEngine);
    }


    public void cancelAll() {
        path.clear();
    }
    public void cancel(String id) {
        path.remove(id);
    }
}
