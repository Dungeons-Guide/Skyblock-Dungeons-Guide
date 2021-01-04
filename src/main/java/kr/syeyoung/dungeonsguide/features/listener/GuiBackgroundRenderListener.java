package kr.syeyoung.dungeonsguide.features.listener;

import net.minecraftforge.client.event.GuiScreenEvent;

public interface GuiBackgroundRenderListener {
    void onGuiBGRender(GuiScreenEvent.BackgroundDrawnEvent rendered);
}
