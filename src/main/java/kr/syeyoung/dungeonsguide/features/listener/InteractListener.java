package kr.syeyoung.dungeonsguide.features.listener;

import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;

public interface InteractListener {
    void onInteract(PlayerInteractEvent event);
}
