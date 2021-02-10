package kr.syeyoung.dungeonsguide.dungeon.actions;

import kr.syeyoung.dungeonsguide.dungeon.roomfinder.DungeonRoom;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;

import java.util.Set;

public interface Action {
    Set<Action> getPreRequisites(DungeonRoom dungeonRoom);

    void onPlayerInteract(DungeonRoom dungeonRoom, PlayerInteractEvent event);
    void onLivingDeath(DungeonRoom dungeonRoom, LivingDeathEvent event);
    void onRenderWorld(DungeonRoom dungeonRoom, float partialTicks);
    void onRenderScreen(DungeonRoom dungeonRoom, float partialTicks);
    void onTick(DungeonRoom dungeonRoom);

    boolean isComplete(DungeonRoom dungeonRoom);
}
