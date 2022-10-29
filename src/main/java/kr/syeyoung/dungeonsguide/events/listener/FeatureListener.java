/*
 *     Dungeons Guide - The most intelligent Hypixel Skyblock Dungeons Mod
 *     Copyright (C) 2021  cyoung06
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Affero General Public License as published
 *     by the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Affero General Public License for more details.
 *
 *     You should have received a copy of the GNU Affero General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package kr.syeyoung.dungeonsguide.events.listener;

import kr.syeyoung.dungeonsguide.SkyblockStatus;
import kr.syeyoung.dungeonsguide.config.guiconfig.location.GuiGuiLocationConfig;
import kr.syeyoung.dungeonsguide.events.impl.*;
import kr.syeyoung.dungeonsguide.features.AbstractFeature;
import kr.syeyoung.dungeonsguide.features.FeatureRegistry;
import kr.syeyoung.dungeonsguide.features.GuiFeature;
import kr.syeyoung.dungeonsguide.features.listener.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
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

            if (!(postRender.type == RenderGameOverlayEvent.ElementType.EXPERIENCE || postRender.type == RenderGameOverlayEvent.ElementType.JUMPBAR)) return;

            if (!SkyblockStatus.isOnSkyblock()) return;

            for (AbstractFeature abstractFeature : FeatureRegistry.getFeatureList()) {
                if (abstractFeature instanceof ScreenRenderListener && (!isLocConfig || !(abstractFeature instanceof GuiFeature))) {
                    ((ScreenRenderListener) abstractFeature).drawScreen(postRender.partialTicks);
                }
            }
            GlStateManager.enableBlend();
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    @SubscribeEvent
    public void onKeybindPress(KeyBindPressedEvent keyBindPressedEvent) {
        try {
            for (AbstractFeature abstractFeature : FeatureRegistry.getFeatureList()) {
                if (abstractFeature instanceof KeybindPressedListener) {
                    ((KeybindPressedListener) abstractFeature).onKeybindPress(keyBindPressedEvent);
                }
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    @SubscribeEvent
    public void onStomp(StompConnectedEvent stompConnectedEvent) {
        try {
            for (AbstractFeature abstractFeature : FeatureRegistry.getFeatureList()) {
                if (abstractFeature instanceof StompConnectedListener) {
                    ((StompConnectedListener) abstractFeature).onStompConnected(stompConnectedEvent);
                }
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }
    @SubscribeEvent
    public void onStomp(TextureStitchEvent.Pre textureStitchEvent) {
        try {
            for (AbstractFeature abstractFeature : FeatureRegistry.getFeatureList()) {
                if (abstractFeature instanceof TextureStichListener) {
                    ((TextureStichListener) abstractFeature).onTextureStitch(textureStitchEvent);
                }
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }
    @SubscribeEvent
    public void onStomp(TextureStitchEvent.Post textureStitchEvent) {
        try {
            for (AbstractFeature abstractFeature : FeatureRegistry.getFeatureList()) {
                if (abstractFeature instanceof TextureStichListener) {
                    ((TextureStichListener) abstractFeature).onTextureStitch(textureStitchEvent);
                }
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }
    @SubscribeEvent
    public void onDiscordUserUpdate(DiscordUserUpdateEvent discordUserUpdateEvent) {
        try {
            for (AbstractFeature abstractFeature : FeatureRegistry.getFeatureList()) {
                if (abstractFeature instanceof DiscordUserUpdateListener) {
                    ((DiscordUserUpdateListener) abstractFeature).onDiscordUserUpdate(discordUserUpdateEvent);
                }
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }
    @SubscribeEvent
    public void onDiscordUserUpdate(DiscordUserJoinRequestEvent discordUserUpdateEvent) {
        try {
            for (AbstractFeature abstractFeature : FeatureRegistry.getFeatureList()) {
                if (abstractFeature instanceof DiscordUserJoinRequestListener) {
                    ((DiscordUserJoinRequestListener) abstractFeature).onDiscordUserJoinRequest(discordUserUpdateEvent);
                }
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }
    @SubscribeEvent
    public void onWindowUpdate(WindowUpdateEvent windowUpdateEvent) {
        try {

            if (!SkyblockStatus.isOnSkyblock()) return;
            for (AbstractFeature abstractFeature : FeatureRegistry.getFeatureList()) {
                if (abstractFeature instanceof GuiUpdateListener) {
                    ((GuiUpdateListener) abstractFeature).onGuiUpdate(windowUpdateEvent);
                }
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }
    @SubscribeEvent
    public void onRender(RenderLivingEvent.Pre preRender) {
        try {

            if (!SkyblockStatus.isOnSkyblock()) return;

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

            if (!SkyblockStatus.isOnSkyblock()) return;

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

            if (!SkyblockStatus.isOnSkyblock()) return;

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

            if (!SkyblockStatus.isOnSkyblock()) return;

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

            if (!SkyblockStatus.isOnSkyblock()) return;

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

            if (!SkyblockStatus.isOnSkyblock()) return;

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

            if (!SkyblockStatus.isOnSkyblock()) return;

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

            if (!SkyblockStatus.isOnSkyblock()) return;

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

            if (!SkyblockStatus.isOnSkyblock()) return;

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
    public void onChatGlobal(ClientChatReceivedEvent postRender) {
        try {
            for (AbstractFeature abstractFeature : FeatureRegistry.getFeatureList()) {
                if (abstractFeature instanceof ChatListenerGlobal) {
                    ((ChatListenerGlobal) abstractFeature).onChat(postRender);
                }
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    @SubscribeEvent
    public void dungeonTooltip(ItemTooltipEvent event) {
        try {

            if (!SkyblockStatus.isOnSkyblock()) return;

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

                if (!SkyblockStatus.isOnSkyblock()) return;

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

                if (!SkyblockStatus.isOnSkyblock()) return;

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

            if (!SkyblockStatus.isOnSkyblock()) return;

            for (AbstractFeature abstractFeature : FeatureRegistry.getFeatureList()) {
                if (abstractFeature instanceof GuiPostRenderListener) {
                    ((GuiPostRenderListener) abstractFeature).onGuiPostRender(render);
                }
            }
            GlStateManager.enableBlend();
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }
    @SubscribeEvent
    public void onGuiRender(GuiScreenEvent.DrawScreenEvent.Pre render) {
        try {

            if (!SkyblockStatus.isOnSkyblock()) return;

            for (AbstractFeature abstractFeature : FeatureRegistry.getFeatureList()) {
                if (abstractFeature instanceof GuiPreRenderListener) {
                    ((GuiPreRenderListener) abstractFeature).onGuiPreRender(render);
                }
            }
            GlStateManager.enableBlend();
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }
    @SubscribeEvent
    public void onGuiRender(GuiScreenEvent.BackgroundDrawnEvent render) {
        try {

            if (!SkyblockStatus.isOnSkyblock()) return;

            for (AbstractFeature abstractFeature : FeatureRegistry.getFeatureList()) {
                if (abstractFeature instanceof GuiBackgroundRenderListener) {
                    ((GuiBackgroundRenderListener) abstractFeature).onGuiBGRender(render);
                }
            }
            GlStateManager.enableBlend();
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    @SubscribeEvent(receiveCanceled = true, priority = EventPriority.HIGH)
    public void onGuiEvent(GuiScreenEvent.MouseInputEvent.Pre input) {
        try {

            if (!SkyblockStatus.isOnSkyblock()) return;

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
    public void onKey(GuiScreenEvent.KeyboardInputEvent event) {
        try {
            for (AbstractFeature abstractFeature : FeatureRegistry.getFeatureList()) {
                if (abstractFeature instanceof KeyInputListener) {
                    ((KeyInputListener) abstractFeature).onKeyInput(event);
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
