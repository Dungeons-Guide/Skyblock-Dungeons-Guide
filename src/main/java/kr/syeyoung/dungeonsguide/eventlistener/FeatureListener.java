package kr.syeyoung.dungeonsguide.eventlistener;

import kr.syeyoung.dungeonsguide.SkyblockStatus;
import kr.syeyoung.dungeonsguide.e;
import kr.syeyoung.dungeonsguide.features.*;
import kr.syeyoung.dungeonsguide.features.AbstractFeature;
import kr.syeyoung.dungeonsguide.features.listener.*;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class FeatureListener {
    @SubscribeEvent
    public void onRender(RenderGameOverlayEvent.Post postRender) {
        try {
            if (postRender.type != RenderGameOverlayEvent.ElementType.ALL) return;
            SkyblockStatus skyblockStatus = e.getDungeonsGuide().getSkyblockStatus();
            if (!skyblockStatus.isOnSkyblock()) return;

            for (AbstractFeature abstractFeature : FeatureRegistry.getFeatureList()) {
                if (abstractFeature instanceof ScreenRenderListener) {
                    ((ScreenRenderListener) abstractFeature).drawScreen(postRender.partialTicks);
                }
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }


    @SubscribeEvent
    public void onRenderWorld(RenderWorldLastEvent postRender) {
        try {
            SkyblockStatus skyblockStatus = e.getDungeonsGuide().getSkyblockStatus();
            if (!skyblockStatus.isOnSkyblock()) return;

            for (AbstractFeature abstractFeature : FeatureRegistry.getFeatureList()) {
                if (abstractFeature instanceof WorldRenderListener) {
                    ((WorldRenderListener) abstractFeature).drawWorld(postRender.partialTicks);
                }
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }


    @SubscribeEvent
    public void onRenderWorld(ClientChatReceivedEvent postRender) {
        try {
            SkyblockStatus skyblockStatus = e.getDungeonsGuide().getSkyblockStatus();
            if (!skyblockStatus.isOnSkyblock()) return;

            for (AbstractFeature abstractFeature : FeatureRegistry.getFeatureList()) {
                if (abstractFeature instanceof ChatListener) {
                    ((ChatListener) abstractFeature).onChat(postRender);
                }
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    @SubscribeEvent
    public void dungeonTooltip(ItemTooltipEvent event) {
        try {
            SkyblockStatus skyblockStatus = e.getDungeonsGuide().getSkyblockStatus();
            if (!skyblockStatus.isOnSkyblock()) return;

            for (AbstractFeature abstractFeature : FeatureRegistry.getFeatureList()) {
                if (abstractFeature instanceof TooltipListener) {
                    ((TooltipListener) abstractFeature).onTooltip(event);
                }
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent tick) {
        if (tick.phase == TickEvent.Phase.END && tick.type == TickEvent.Type.CLIENT ) {
            try {
                SkyblockStatus skyblockStatus = e.getDungeonsGuide().getSkyblockStatus();
                if (!skyblockStatus.isOnSkyblock()) return;

                for (AbstractFeature abstractFeature : FeatureRegistry.getFeatureList()) {
                    if (abstractFeature instanceof TickListener) {
                        ((TickListener) abstractFeature).onTick();
                    }
                }
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }
    }
}
