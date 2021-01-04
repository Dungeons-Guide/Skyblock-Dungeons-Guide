package kr.syeyoung.dungeonsguide.features.listener;

import net.minecraftforge.client.event.GuiScreenEvent;

public interface GuiPreRenderListener {
    void onGuiPreRender(GuiScreenEvent.DrawScreenEvent.Pre rendered);
}
