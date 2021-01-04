package kr.syeyoung.dungeonsguide.features.listener;

import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;

public interface GuiPostRenderListener {
    void onGuiPostRender(GuiScreenEvent.DrawScreenEvent.Post rendered);
}
