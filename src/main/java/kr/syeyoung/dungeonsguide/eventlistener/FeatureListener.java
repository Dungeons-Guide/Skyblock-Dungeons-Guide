package kr.syeyoung.dungeonsguide.eventlistener;

import kr.syeyoung.dungeonsguide.SkyblockStatus;
import kr.syeyoung.dungeonsguide.config.guiconfig.GuiGuiLocationConfig;
import kr.syeyoung.dungeonsguide.e;
import kr.syeyoung.dungeonsguide.events.*;
import kr.syeyoung.dungeonsguide.features.*;
import kr.syeyoung.dungeonsguide.features.AbstractFeature;
import kr.syeyoung.dungeonsguide.features.listener.*;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.*;
import net.minecraftforge.client.event.sound.PlaySoundEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class FeatureListener {
    @SubscribeEvent
    public void onRender(RenderGameOverlayEvent.Post postRender) {
        try {
            boolean isLocConfig = Minecraft.getMinecraft().currentScreen instanceof GuiGuiLocationConfig;

            if (postRender.type != RenderGameOverlayEvent.ElementType.ALL) return;
            SkyblockStatus skyblockStatus = e.getDungeonsGuide().getSkyblockStatus();
            if (!skyblockStatus.isOnSkyblock()) return;

            for (AbstractFeature abstractFeature : FeatureRegistry.getFeatureList()) {
                if (abstractFeature instanceof ScreenRenderListener && (!isLocConfig || !(abstractFeature instanceof GuiFeature))) {
                    ((ScreenRenderListener) abstractFeature).drawScreen(postRender.partialTicks);
                }
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }
    @SubscribeEvent
    public void onRender(RenderLivingEvent.Pre preRender) {
        try {
            SkyblockStatus skyblockStatus = e.getDungeonsGuide().getSkyblockStatus();
            if (!skyblockStatus.isOnSkyblock()) return;

            for (AbstractFeature abstractFeature : FeatureRegistry.getFeatureList()) {
                if (abstractFeature instanceof EntityLivingRenderListener) {
                    ((EntityLivingRenderListener) abstractFeature).onEntityRenderPre(preRender);
                }
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    @SubscribeEvent
    public void onSound(PlaySoundEvent soundEvent) {
        try {
            SkyblockStatus skyblockStatus = e.getDungeonsGuide().getSkyblockStatus();
            if (!skyblockStatus.isOnSkyblock()) return;

            for (AbstractFeature abstractFeature : FeatureRegistry.getFeatureList()) {
                if (abstractFeature instanceof SoundListener) {
                    ((SoundListener) abstractFeature).onSound(soundEvent);
                }
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    @SubscribeEvent
    public void onRender(RenderLivingEvent.Post preRender) {
        try {
            SkyblockStatus skyblockStatus = e.getDungeonsGuide().getSkyblockStatus();
            if (!skyblockStatus.isOnSkyblock()) return;

            for (AbstractFeature abstractFeature : FeatureRegistry.getFeatureList()) {
                if (abstractFeature instanceof EntityLivingRenderListener) {
                    ((EntityLivingRenderListener) abstractFeature).onEntityRenderPost(preRender);
                }
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }
    @SubscribeEvent
    public void onRender(TitleEvent titleEvent) {
        try {
            SkyblockStatus skyblockStatus = e.getDungeonsGuide().getSkyblockStatus();
            if (!skyblockStatus.isOnSkyblock()) return;

            for (AbstractFeature abstractFeature : FeatureRegistry.getFeatureList()) {
                if (abstractFeature instanceof TitleListener) {
                    ((TitleListener) abstractFeature).onTitle(titleEvent.getPacketTitle());
                }
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }
    @SubscribeEvent
    public void onRender(RenderPlayerEvent.Pre preRender) {
        try {
            SkyblockStatus skyblockStatus = e.getDungeonsGuide().getSkyblockStatus();
            if (!skyblockStatus.isOnSkyblock()) return;

            for (AbstractFeature abstractFeature : FeatureRegistry.getFeatureList()) {
                if (abstractFeature instanceof PlayerRenderListener) {
                    ((PlayerRenderListener) abstractFeature).onEntityRenderPre(preRender);
                }
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }
    @SubscribeEvent
    public void onRender(RenderPlayerEvent.Post preRender) {
        try {
            SkyblockStatus skyblockStatus = e.getDungeonsGuide().getSkyblockStatus();
            if (!skyblockStatus.isOnSkyblock()) return;

            for (AbstractFeature abstractFeature : FeatureRegistry.getFeatureList()) {
                if (abstractFeature instanceof PlayerRenderListener) {
                    ((PlayerRenderListener) abstractFeature).onEntityRenderPost(preRender);
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
    public void onInteract(PlayerInteractEvent postRender) {
        try {
            SkyblockStatus skyblockStatus = e.getDungeonsGuide().getSkyblockStatus();
            if (!skyblockStatus.isOnSkyblock()) return;

            for (AbstractFeature abstractFeature : FeatureRegistry.getFeatureList()) {
                if (abstractFeature instanceof InteractListener) {
                    ((InteractListener) abstractFeature).onInteract(postRender);
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
    @SubscribeEvent
    public void onGuiOpen(GuiOpenEvent tick) {
            try {
                SkyblockStatus skyblockStatus = e.getDungeonsGuide().getSkyblockStatus();
                if (!skyblockStatus.isOnSkyblock()) return;

                for (AbstractFeature abstractFeature : FeatureRegistry.getFeatureList()) {
                    if (abstractFeature instanceof GuiOpenListener) {
                        ((GuiOpenListener) abstractFeature).onGuiOpen(tick);
                    }
                }
            } catch (Throwable t) {
                t.printStackTrace();
            }
    }
    @SubscribeEvent
    public void onGuiRender(GuiScreenEvent.DrawScreenEvent.Post render) {
        try {
            SkyblockStatus skyblockStatus = e.getDungeonsGuide().getSkyblockStatus();
            if (!skyblockStatus.isOnSkyblock()) return;

            for (AbstractFeature abstractFeature : FeatureRegistry.getFeatureList()) {
                if (abstractFeature instanceof GuiPostRenderListener) {
                    ((GuiPostRenderListener) abstractFeature).onGuiPostRender(render);
                }
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }
    @SubscribeEvent
    public void onGuiRender(GuiScreenEvent.DrawScreenEvent.Pre render) {
        try {
            SkyblockStatus skyblockStatus = e.getDungeonsGuide().getSkyblockStatus();
            if (!skyblockStatus.isOnSkyblock()) return;

            for (AbstractFeature abstractFeature : FeatureRegistry.getFeatureList()) {
                if (abstractFeature instanceof GuiPreRenderListener) {
                    ((GuiPreRenderListener) abstractFeature).onGuiPreRender(render);
                }
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }
    @SubscribeEvent
    public void onGuiRender(GuiScreenEvent.BackgroundDrawnEvent render) {
        try {
            SkyblockStatus skyblockStatus = e.getDungeonsGuide().getSkyblockStatus();
            if (!skyblockStatus.isOnSkyblock()) return;

            for (AbstractFeature abstractFeature : FeatureRegistry.getFeatureList()) {
                if (abstractFeature instanceof GuiBackgroundRenderListener) {
                    ((GuiBackgroundRenderListener) abstractFeature).onGuiBGRender(render);
                }
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    @SubscribeEvent(receiveCanceled = true, priority = EventPriority.HIGH)
    public void onGuiEvent(GuiScreenEvent.MouseInputEvent.Pre input) {
        try {
            SkyblockStatus skyblockStatus = e.getDungeonsGuide().getSkyblockStatus();
            if (!skyblockStatus.isOnSkyblock()) return;

            for (AbstractFeature abstractFeature : FeatureRegistry.getFeatureList()) {
                if (abstractFeature instanceof GuiClickListener) {
                    ((GuiClickListener) abstractFeature).onMouseInput(input);
                }
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }
    @SubscribeEvent
    public void onSkyblockJoin(SkyblockJoinedEvent joinedEvent) {
        try {
            for (AbstractFeature abstractFeature : FeatureRegistry.getFeatureList()) {
                if (abstractFeature instanceof SkyblockJoinListener) {
                    ((SkyblockJoinListener) abstractFeature).onSkyblockJoin();
                }
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }
    @SubscribeEvent
    public void onSkyblockQuit(SkyblockLeftEvent leftEvent) {
        try {
            for (AbstractFeature abstractFeature : FeatureRegistry.getFeatureList()) {
                if (abstractFeature instanceof SkyblockLeaveListener) {
                    ((SkyblockLeaveListener) abstractFeature).onSkyblockQuit();
                }
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }
    @SubscribeEvent
    public void onDungeonStart(DungeonStartedEvent leftEvent) {
        try {
            for (AbstractFeature abstractFeature : FeatureRegistry.getFeatureList()) {
                if (abstractFeature instanceof DungeonStartListener) {
                    ((DungeonStartListener) abstractFeature).onDungeonStart();
                }
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }
    @SubscribeEvent
    public void onDungeonLeft(DungeonLeftEvent leftEvent) {
        try {
            for (AbstractFeature abstractFeature : FeatureRegistry.getFeatureList()) {
                if (abstractFeature instanceof DungeonQuitListener) {
                    ((DungeonQuitListener) abstractFeature).onDungeonQuit();
                }
                if (abstractFeature instanceof DungeonEndListener) {
                    ((DungeonEndListener) abstractFeature).onDungeonEnd();
                }
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }
    @SubscribeEvent
    public void onDungeonInitialize(DungeonContextInitializationEvent leftEvent) {
        try {
            for (AbstractFeature abstractFeature : FeatureRegistry.getFeatureList()) {
                if (abstractFeature instanceof DungeonContextInitializationListener) {
                    ((DungeonContextInitializationListener) abstractFeature).onDungeonInitialize();
                }
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }
    @SubscribeEvent
    public void onDungeonInitialize(BossroomEnterEvent enterEvent) {
        try {
            for (AbstractFeature abstractFeature : FeatureRegistry.getFeatureList()) {
                if (abstractFeature instanceof BossroomEnterListener) {
                    ((BossroomEnterListener) abstractFeature).onBossroomEnter();
                }
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }
    @SubscribeEvent
    public void onDungeonInitialize(DungeonEndedEvent endedEvent) {
        try {
            for (AbstractFeature abstractFeature : FeatureRegistry.getFeatureList()) {
                if (abstractFeature instanceof DungeonEndListener) {
                    ((DungeonEndListener) abstractFeature).onDungeonEnd();
                }
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }
}
