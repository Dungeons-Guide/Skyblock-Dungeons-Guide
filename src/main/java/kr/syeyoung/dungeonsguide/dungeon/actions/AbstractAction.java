package kr.syeyoung.dungeonsguide.dungeon.actions;

import kr.syeyoung.dungeonsguide.dungeon.roomfinder.DungeonRoom;
import kr.syeyoung.dungeonsguide.events.PlayerInteractEntityEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;

public abstract class AbstractAction implements Action {
    @Override
    public void onPlayerInteract(DungeonRoom dungeonRoom, PlayerInteractEvent event) {

    }

    @Override
    public void onRenderWorld(DungeonRoom dungeonRoom, float partialTicks) {

    }

    @Override
    public void onLivingDeath(DungeonRoom dungeonRoom, LivingDeathEvent event) {

    }

    @Override
    public void onRenderScreen(DungeonRoom dungeonRoom, float partialTicks) {

    }

    @Override
    public void onLivingInteract(DungeonRoom dungeonRoom, PlayerInteractEntityEvent event) {

    }

    @Override
    public void onTick(DungeonRoom dungeonRoom) {

    }
}
