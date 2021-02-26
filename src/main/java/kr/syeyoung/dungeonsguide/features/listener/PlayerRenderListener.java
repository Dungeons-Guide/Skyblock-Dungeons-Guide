package kr.syeyoung.dungeonsguide.features.listener;

import net.minecraftforge.client.event.RenderPlayerEvent;

public interface PlayerRenderListener {
    void onEntityRenderPre(RenderPlayerEvent.Pre renderPlayerEvent );
    void onEntityRenderPost(RenderPlayerEvent.Post renderPlayerEvent );
}
