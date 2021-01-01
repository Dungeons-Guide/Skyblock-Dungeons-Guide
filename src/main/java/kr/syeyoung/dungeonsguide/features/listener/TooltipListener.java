package kr.syeyoung.dungeonsguide.features.listener;

import net.minecraftforge.event.entity.player.ItemTooltipEvent;

public interface TooltipListener {
    void onTooltip(ItemTooltipEvent event);
}
