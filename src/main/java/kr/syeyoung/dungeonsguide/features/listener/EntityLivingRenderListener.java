package kr.syeyoung.dungeonsguide.features.listener;

import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.client.event.RenderPlayerEvent;

public interface EntityLivingRenderListener {
    void onEntityRenderPre(RenderLivingEvent.Pre renderPlayerEvent);
    void onEntityRenderPost(RenderLivingEvent.Post renderPlayerEvent);
}
