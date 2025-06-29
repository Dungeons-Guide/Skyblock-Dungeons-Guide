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

package kr.syeyoung.dungeonsguide.mod.events.listener;

import kr.syeyoung.dungeonsguide.mod.DungeonsGuide;
import kr.syeyoung.dungeonsguide.mod.SkyblockStatus;
import kr.syeyoung.dungeonsguide.mod.chat.ChatTransmitter;
import kr.syeyoung.dungeonsguide.mod.config.Config;
import kr.syeyoung.dungeonsguide.mod.dungeon.DungeonActionContext;
import kr.syeyoung.dungeonsguide.mod.dungeon.DungeonContext;
import kr.syeyoung.dungeonsguide.mod.dungeon.data.OffsetPoint;
import kr.syeyoung.dungeonsguide.mod.dungeon.dataprovider.DungeonDoor;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomedit.EditingContext;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomedit.gui.GuiDungeonAddSet;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomedit.gui.GuiDungeonParameterEdit;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomedit.gui.GuiDungeonRoomEdit;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomedit.gui.GuiDungeonValueEdit;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomedit.valueedit.ValueEdit;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomfinder.DungeonRoom;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomprocessor.GeneralRoomProcessor;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomprocessor.RoomProcessor;
import kr.syeyoung.dungeonsguide.mod.dungeon.world.CollisionStateCalculatingCoordinateMap;
import kr.syeyoung.dungeonsguide.mod.dungeon.world.PearlCalculatingCoordinateMap;
import kr.syeyoung.dungeonsguide.mod.events.impl.*;
import kr.syeyoung.dungeonsguide.mod.utils.DungeonServerLaunchUtils;
import kr.syeyoung.dungeonsguide.mod.features.FeatureRegistry;
import kr.syeyoung.dungeonsguide.mod.features.impl.etc.FeatureCollectDiagnostics;
import kr.syeyoung.dungeonsguide.mod.parallelUniverse.scoreboard.ScoreboardManager;
import kr.syeyoung.dungeonsguide.mod.parallelUniverse.tab.TabList;
import kr.syeyoung.dungeonsguide.mod.parallelUniverse.teams.TeamManager;
import kr.syeyoung.dungeonsguide.mod.utils.MapUtils;
import kr.syeyoung.dungeonsguide.mod.utils.RenderUtils;
import kr.syeyoung.modapi.ModAPI;
import kr.syeyoung.modapi.data.AABB;
import kr.syeyoung.modapi.data.Vector3D;
import kr.syeyoung.modapi.data.VectorI3D;
import kr.syeyoung.modapi.entity.EntityType;
import kr.syeyoung.modapi.entity.UEntity;
import kr.syeyoung.modapi.entity.UPlayerSelf;
import kr.syeyoung.modapi.event.events.*;
import kr.syeyoung.modapi.world.UBlockState;
import kr.syeyoung.modapi.world.UChunk;
import lombok.Getter;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.profiler.Profiler;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.Vec3;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;

import java.awt.*;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;

public class DungeonListener {


    @kr.syeyoung.modapi.event.SubscribeEvent
    public void onWorldLoad(WorldUnloadEvent event) {
        TabList.INSTANCE.clear();
        TeamManager.INSTANCE.clear();
        ScoreboardManager.INSTANCE.clear();
        Config.scheduleConfigSave();
        DungeonActionContext.getSpawnLocation().clear();
        DungeonActionContext.getKilleds().clear();
    }

    @SubscribeEvent
    public void onPostDraw(GuiScreenEvent.DrawScreenEvent.Post e) {
        if (!SkyblockStatus.isOnDungeon()) return;


        Profiler profiler = Minecraft.getMinecraft().mcProfiler;

        DungeonContext context = DungeonsGuide.getDungeonsGuide().getDungeonFacade().getContext();

        if (context != null) {

            UPlayerSelf thePlayer = ModAPI.getAPI().getPlayer();
            if (thePlayer == null) {
                return;
            }
            profiler.startSection("Dungeons Guide - DrawScreen.Post: Bossfight Processor");
            if (context.getBossfightProcessor() != null) {
                context.getBossfightProcessor().onPostGuiRender(e);
            }
            profiler.endStartSection("Dungeons Guide - DrawScreen.Post: Room Processor");

            if (context.getScaffoldParser() != null) {
                Point roomPt = context.getScaffoldParser().getDungeonMapLayout().worldPointToRoomPoint(thePlayer.getPositionVector());

                DungeonRoom dungeonRoom = context.getScaffoldParser().getRoomMap().get(roomPt);
                if (dungeonRoom != null && dungeonRoom.getRoomProcessor() != null) {
                    dungeonRoom.getRoomProcessor().onPostGuiRender(e);
                }
            }
            profiler.endSection();
        }

        GlStateManager.enableBlend();
        GlStateManager.color(1, 1, 1, 1);
        GL14.glBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GlStateManager.enableAlpha();
    }

    @kr.syeyoung.modapi.event.SubscribeEvent
    public void onEntityUpdate(LivingEntityTickEvent e) {
        if (!SkyblockStatus.isOnDungeon()) return;

        DungeonContext context = DungeonsGuide.getDungeonsGuide().getDungeonFacade().getContext();
        if (context != null) {
            UPlayerSelf thePlayer = ModAPI.getAPI().getPlayer();
            if (thePlayer == null) return;
            if (context.getBossfightProcessor() != null) context.getBossfightProcessor().onEntityUpdate(e);
            if (context.getScaffoldParser() != null) {
                Point roomPt = context.getScaffoldParser().getDungeonMapLayout().worldPointToRoomPoint(thePlayer.getPositionVector());

                DungeonRoom dungeonRoom = context.getScaffoldParser().getRoomMap().get(roomPt);
                if (dungeonRoom != null && dungeonRoom.getRoomProcessor() != null) {
                    dungeonRoom.getRoomProcessor().onEntityUpdate(e);
                }
            }
        }
    }

    @kr.syeyoung.modapi.event.SubscribeEvent
    public void onDungeonLeave(DungeonLeftEvent ev) {
        DungeonsGuide.getDungeonsGuide().getDungeonFacade().setContext(null);
        if (!FeatureRegistry.ADVANCED_DEBUGGABLE_MAP.isEnabled()) {
            MapUtils.clearMap();
        }
    }





    @kr.syeyoung.modapi.event.SubscribeEvent
    public void onTick(ClientTickEvent ev) {
        if (SkyblockStatus.isOnSkyblock() || SkyblockStatus.isOnDungeon()) {
            DungeonContext context = DungeonsGuide.getDungeonsGuide().getDungeonFacade().getContext();
            if (context != null) {
                context.getMapPlayerMarkerProcessor().tick();
                context.tick();
            } else {
                try {
                    if (DungeonsGuide.getDungeonsGuide().getSkyblockStatus().isForceIsOnDungeon()) {
                        DungeonServerLaunchUtils.createContext();
                        ModAPI.getAPI().getEventBus().fireEvent(new DungeonStartedEvent());
                    } else if (SkyblockStatus.isOnDungeon()) {
                        DungeonsGuide.getDungeonsGuide().getDungeonFacade().setContext(new DungeonContext(
                                SkyblockStatus.getLocationName(),
                                ModAPI.getAPI().getWorld()));
                        ModAPI.getAPI().getEventBus().fireEvent(new DungeonStartedEvent());
                    }
                } catch (IllegalStateException e) {
                    if (! "?".equals(e.getMessage()) && !"No door finder found".equals(e.getMessage())) {
                        e.printStackTrace();
                        FeatureCollectDiagnostics.queueSendLogAsync(e);
                    }
                }
            }
        }


        DungeonContext context = DungeonsGuide.getDungeonsGuide().getDungeonFacade().getContext();
        if (SkyblockStatus.isOnDungeon() && context != null) {

            UPlayerSelf thePlayer = ModAPI.getAPI().getPlayer();
            if (thePlayer == null) {
                return;
            }


            if (context.getBossfightProcessor() != null) {
                context.getBossfightProcessor().tick();
            }
            if (context.getScaffoldParser() != null) {
                Point roomPt = context.getScaffoldParser().getDungeonMapLayout().worldPointToRoomPoint(thePlayer.getPositionVector());

                DungeonRoom dungeonRoom = context.getScaffoldParser().getRoomMap().get(roomPt);

                if (dungeonRoom != null && dungeonRoom.getRoomProcessor() != null) {
                    dungeonRoom.getRoomProcessor().tick();
                }
            }

        }

    }

    private WeakReference<DungeonRoom> lastRoom = null;

    @kr.syeyoung.modapi.event.SubscribeEvent
    public void onTickDetectRoomTransfer(ClientTickEvent ev) {
        DungeonContext context = DungeonsGuide.getDungeonsGuide().getDungeonFacade().getContext();
        if (!SkyblockStatus.isOnDungeon() || context == null) return;
        UPlayerSelf thePlayer = ModAPI.getAPI().getPlayer();
        if (thePlayer == null) return;
        if (context.getScaffoldParser() == null) return;
        Point roomPt = context.getScaffoldParser().getDungeonMapLayout().worldPointToRoomPoint(thePlayer.getPositionVector());
        DungeonRoom currentRoom = context.getScaffoldParser().getRoomMap().get(roomPt);
        DungeonRoom oldRoom = lastRoom == null ? null : lastRoom.get();
        boolean isActuallyInCurrent = currentRoom == null || currentRoom.getRoomBounds().isFullyWithin(thePlayer.getPositionVector());
        if (!isActuallyInCurrent) currentRoom = null;

        lastRoom = new WeakReference<>(currentRoom);

        if (oldRoom == currentRoom) return;
        if (oldRoom != null)
            ModAPI.getAPI().getEventBus().fireEvent(new DungeonRoomExitEvent(oldRoom));
        if (currentRoom != null)
            ModAPI.getAPI().getEventBus().fireEvent(new DungeonRoomEnterEvent(currentRoom));
    }

    @SubscribeEvent
    public void onRender(RenderGameOverlayEvent.Post postRender) {
        if (!(postRender.type == RenderGameOverlayEvent.ElementType.ALL))
            return;

        if (!SkyblockStatus.isOnDungeon()) return;
        Profiler profiler = Minecraft.getMinecraft().mcProfiler;

        DungeonContext context = DungeonsGuide.getDungeonsGuide().getDungeonFacade().getContext();
        if (context != null) {

            UPlayerSelf thePlayer = ModAPI.getAPI().getPlayer();

            profiler.startSection("Dungeons Guide - RenderGameOverlay.Post :: Bossfight Processor");
            if (context.getBossfightProcessor() != null)
                context.getBossfightProcessor().drawScreen(postRender.partialTicks);

            profiler.endStartSection("Dungeons Guide - RenderGameOverlay.Post :: Room Processor");
            if (context.getScaffoldParser() != null) {
                Point roomPt = context.getScaffoldParser().getDungeonMapLayout().worldPointToRoomPoint(thePlayer.getPositionVector());
                DungeonRoom dungeonRoom = context.getScaffoldParser().getRoomMap().get(roomPt);
                if (dungeonRoom != null) {
                    if (dungeonRoom.getRoomProcessor() != null) {
                        dungeonRoom.getRoomProcessor().drawScreen(postRender.partialTicks);
                    }
                }
            }
            profiler.endSection();

            if (context.getDungeonName().equals("TEST DG")) {
                FontRenderer fr = Minecraft.getMinecraft().fontRendererObj;
                ScaledResolution sr = new ScaledResolution(Minecraft.getMinecraft());

                int width = fr.getStringWidth("Dungeons Guide Mockup Dungeon");
                int width2 = fr.getStringWidth("Preset: "+context.getPreset().getPresetName());
                int bigger = Math.max(width, width2);
                Gui.drawRect(
                        (sr.getScaledWidth()-bigger - 10)/2,
                        (sr.getScaledHeight()/9 - 5) ,
                        (sr.getScaledWidth() + bigger + 10) /2,
                        (sr.getScaledHeight()/9) + 5 + fr.FONT_HEIGHT*2,
                        0x77111111
                );

                fr.drawString("Dungeons Guide Mockup Dungeon", (sr.getScaledWidth()-width)/2, sr.getScaledHeight()/9, 0xFF00FF00);

                fr.drawString("Preset: "+context.getPreset().getPresetName(), (sr.getScaledWidth() - width2) / 2, sr.getScaledHeight() / 9 + fr.FONT_HEIGHT, 0xFF00FF00);
            }

        }
        GlStateManager.enableBlend();
        GlStateManager.color(1, 1, 1, 1);
        GL14.glBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ONE_MINUS_SRC_ALPHA);
        Minecraft.getMinecraft().entityRenderer.setupOverlayRendering();
        GlStateManager.enableAlpha();
    }

    @kr.syeyoung.modapi.event.SubscribeEvent
    public  void onMapUpdate(MapUpdateEvent mapUpdateEvent) {
        if (!SkyblockStatus.isOnDungeon()) return;

        DungeonContext context = DungeonsGuide.getDungeonsGuide().getDungeonFacade().getContext();

        if (context != null) {
            context.onMapUpdate(mapUpdateEvent);
        }
    }
    @SubscribeEvent(receiveCanceled = true, priority = EventPriority.HIGHEST)
    public void onChatReceived(ClientChatReceivedEvent clientChatReceivedEvent) {
        if (!SkyblockStatus.isOnDungeon()) return;

        if (clientChatReceivedEvent.type != 2 && clientChatReceivedEvent.message.getFormattedText().contains("§6> §e§lEXTRA STATS §6<")) {
            ModAPI.getAPI().getEventBus().fireEvent(new DungeonEndedEvent());
        }

        DungeonContext context = DungeonsGuide.getDungeonsGuide().getDungeonFacade().getContext();

        if (context != null) {

            UPlayerSelf thePlayer = ModAPI.getAPI().getPlayer();
            context.onChat(clientChatReceivedEvent);

            if (context.getBossfightProcessor() != null) {
                if (clientChatReceivedEvent.type == 2) {
                    context.getBossfightProcessor().actionbarReceived(clientChatReceivedEvent.message);
                } else {
                    context.getBossfightProcessor().chatReceived(clientChatReceivedEvent.message);
                }
            }
            if (context.getScaffoldParser() != null) {
                Point roomPt = context.getScaffoldParser().getDungeonMapLayout().worldPointToRoomPoint(thePlayer.getPositionVector());


                RoomProcessor roomProcessor = null;
                DungeonRoom dungeonRoom = context.getScaffoldParser().getRoomMap().get(roomPt);
                if (dungeonRoom != null) {
                    if (dungeonRoom.getRoomProcessor() != null) {
                        if (clientChatReceivedEvent.type == 2) {
                            dungeonRoom.getRoomProcessor().actionbarReceived(clientChatReceivedEvent.message);
                            roomProcessor = dungeonRoom.getRoomProcessor();
                        } else {
                            dungeonRoom.getRoomProcessor().chatReceived(clientChatReceivedEvent.message);
                            roomProcessor = dungeonRoom.getRoomProcessor();
                        }
                    }
                }
                if (clientChatReceivedEvent.type == 2) {
                    return;
                }

                for (RoomProcessor globalRoomProcessor : context.getGlobalRoomProcessors()) {
                    if (globalRoomProcessor != roomProcessor) {
                        globalRoomProcessor.chatReceived(clientChatReceivedEvent.message);
                    }
                }
            }
        }
    }


    @SubscribeEvent
    public void onWorldRender(RenderWorldLastEvent renderWorldLastEvent) {
        if (!SkyblockStatus.isOnDungeon()) return;
        try {

            Profiler profiler = Minecraft.getMinecraft().mcProfiler;

            DungeonContext context = DungeonsGuide.getDungeonsGuide().getDungeonFacade().getContext();
            if (context == null) {
                return;
            }


            if (FeatureRegistry.DEBUG.isEnabled()) {
                profiler.startSection("Dungeons Guide - Door Rendering");
                if (context.getScaffoldParser() != null) {
                    for (DungeonRoom dungeonRoom : context.getScaffoldParser().getDungeonRoomList()) {
                        for (DungeonDoor door : dungeonRoom.getDoors()) {
                            RenderUtils.renderDoor(door, renderWorldLastEvent.partialTicks);
                        }
                    }
                }
                profiler.endSection();
            }



            profiler.startSection("Dungeons Guide - RenderWorldLast :: Bossfight Processor");
            if (context.getBossfightProcessor() != null) {
                context.getBossfightProcessor().drawWorld(renderWorldLastEvent.partialTicks);
            }

            profiler.endStartSection("Dungeons Guide - RenderWorldLast :: Room Processor");
            if (context.getScaffoldParser() != null) {
                UPlayerSelf thePlayer = ModAPI.getAPI().getPlayer();
                Point roomPt = context.getScaffoldParser().getDungeonMapLayout().worldPointToRoomPoint(thePlayer.getPositionVector());

                DungeonRoom dungeonRoom = context.getScaffoldParser().getRoomMap().get(roomPt);
                if (dungeonRoom != null) {
                    if (dungeonRoom.getRoomProcessor() != null) {
                        dungeonRoom.getRoomProcessor().drawWorld(renderWorldLastEvent.partialTicks);
                    }
                }



                if (FeatureRegistry.DEBUG.isEnabled() && dungeonRoom != null && dungeonRoom.getRoomProcessor() instanceof GeneralRoomProcessor) {

                    GeneralRoomProcessor roomProcessor = (GeneralRoomProcessor) dungeonRoom.getRoomProcessor();
                    Vector3D player = ModAPI.getAPI().getPlayer().getPositionVector();
                    VectorI3D real = new VectorI3D(player.x * 2, player.y * 2, player.z * 2);
                    try {

                        for (VectorI3D allInBox : VectorI3D.getAllInBox(real.add(-1, -1, -1), real.add(1, 1, 1))) {
                            CollisionStateCalculatingCoordinateMap.CollisionState blocked = roomProcessor.getPathfinderWorld().getBlock(allInBox.getX(), allInBox.getY(), allInBox.getZ());
                            RenderUtils.highlightBox(
                                    new AABB(
                                            allInBox.getX() / 2.0 - 0.1, allInBox.getY() / 2.0 - 0.1, allInBox.getZ() / 2.0 - 0.1,
                                            allInBox.getX() / 2.0 + 0.1, allInBox.getY() / 2.0 + 0.1, allInBox.getZ() / 2.0 + 0.1
                                    ), blocked.getColor(), renderWorldLastEvent.partialTicks, false);
                            PearlCalculatingCoordinateMap.PearlLandType type = roomProcessor.getPathfinderWorld().getPearl(allInBox.getX(), allInBox.getY(), allInBox.getZ());
                            RenderUtils.drawTextAtWorld(type.name(), (float) (allInBox.getX() / 2.0 - 0.1), (float) (allInBox.getY() / 2.0 - 0.1), (float) (allInBox.getZ() / 2.0 - 0.1),
                                    0xFFFFFFFF,0.01f, false, true, renderWorldLastEvent.partialTicks);
                        }
                    } catch (Exception ignored) {}

                    if (FeatureRegistry.COMPARE_ROOM.toggleCompareStatus && dungeonRoom.getDungeonRoomInfo().hasSchematic()) {
                        OffsetPoint offsetPoint = new OffsetPoint(dungeonRoom, new VectorI3D(0,0,0));
                        for (VectorI3D allInBox : VectorI3D.getAllInBox(dungeonRoom.getRoomBounds().getMin().add(0, -60, 0), dungeonRoom.getRoomBounds().getMax().add(0, 180, 0))) {
                            offsetPoint.setPosInWorld(dungeonRoom, allInBox);
                            UBlockState blockState = dungeonRoom.getDungeonRoomInfo().getBlock(offsetPoint, dungeonRoom.getRoomMatcher().getRotation());
                            if (blockState != dungeonRoom.getRoomWorld().getBlockStateAt(allInBox)) {
                                RenderUtils.highlightBlock(allInBox, new Color(0x70FF0000,true), renderWorldLastEvent.partialTicks, false);
                                Minecraft.getMinecraft().renderEngine.bindTexture(TextureMap.locationBlocksTexture);
                                float partialTicks = renderWorldLastEvent.partialTicks;
                                RenderUtils.pushAndTranslateAccordingToRenderViewEntity(partialTicks);
                                GlStateManager.translate(allInBox.getX(), allInBox.getY(), allInBox.getZ());
                                GlStateManager.scale(0.5f, 0.5f, 0.5f);
                                GlStateManager.translate(0.5f,0.5f,0.5f);
                                GlStateManager.disableLighting();
                                GlStateManager.enableAlpha();
                                GlStateManager.enableDepth();
                                GlStateManager.depthMask(true);
//                        GlStateManager.disableDepth();
//                        GlStateManager.depthMask(false);
                                GlStateManager.enableBlend();

                                Tessellator tessellator = Tessellator.getInstance();
                                WorldRenderer vertexBuffer = tessellator.getWorldRenderer();
                                vertexBuffer.begin(7, DefaultVertexFormats.BLOCK);
                                BlockRendererDispatcher blockrendererdispatcher = Minecraft.getMinecraft().getBlockRendererDispatcher();
//                        GlStateManager.color(1.0f,1.0f,1.0f,0.1f);
                                blockrendererdispatcher.getBlockModelRenderer().renderModel(Minecraft.getMinecraft().theWorld,
                                        blockrendererdispatcher.getBlockModelShapes().getModelForState((IBlockState) blockState.getIBlockState()),
                                        (IBlockState) blockState.getIBlockState(), new BlockPos(0,0,0), vertexBuffer, false);
                                tessellator.draw();

                                GlStateManager.enableLighting();
                                GlStateManager.popMatrix();
                            }
                        }
                    }
                }
            }


            profiler.endStartSection("Dungeons Guide - RenderWorldLast :: Room Edit");
            if (EditingContext.getEditingContext() != null) {
                GuiScreen guiScreen = EditingContext.getEditingContext().getCurrent();
                if (guiScreen instanceof GuiDungeonParameterEdit) {
                    ValueEdit valueEdit = ((GuiDungeonParameterEdit) guiScreen).getValueEdit();
                    if (valueEdit != null) {
                        valueEdit.renderWorld(renderWorldLastEvent.partialTicks);
                    }
                } else if (guiScreen instanceof GuiDungeonValueEdit) {
                    ValueEdit valueEdit = ((GuiDungeonValueEdit) guiScreen).getValueEdit();
                    if (valueEdit != null) {
                        valueEdit.renderWorld(renderWorldLastEvent.partialTicks);
                    }
                } else if (guiScreen instanceof GuiDungeonAddSet) {
                    ((GuiDungeonAddSet) guiScreen).onWorldRender(renderWorldLastEvent.partialTicks);
                }
            }
            profiler.endSection();
        } catch (Exception e) {
            FeatureCollectDiagnostics.queueSendLogAsync(e);
            e.printStackTrace();
        }
    }

    @kr.syeyoung.modapi.event.SubscribeEvent
    public void onKey2(KeyBindPressedEvent keyInputEvent) {
        if (!SkyblockStatus.isOnDungeon()) return;

        DungeonContext context = DungeonsGuide.getDungeonsGuide().getDungeonFacade().getContext();

        if (DungeonsGuide.getDungeonsGuide().getDungeonFacade().getContext() != null) {
            UPlayerSelf thePlayer = ModAPI.getAPI().getPlayer();

            if (context.getBossfightProcessor() != null) {
                context.getBossfightProcessor().onKeybindPress(keyInputEvent);
            }
            if (context.getScaffoldParser() != null) {
                Point roomPt = context.getScaffoldParser().getDungeonMapLayout().worldPointToRoomPoint(thePlayer.getPositionVector());
                DungeonRoom dungeonRoom = context.getScaffoldParser().getRoomMap().get(roomPt);
                if (dungeonRoom != null) {
                    if (dungeonRoom.getRoomProcessor() != null) {
                        dungeonRoom.getRoomProcessor().onKeybindPress(keyInputEvent);
                    }
                }
            }
        }
    }

    @kr.syeyoung.modapi.event.SubscribeEvent
    public void onInteract(PlayerInteractEntityEvent interact) {
        if (!SkyblockStatus.isOnDungeon()) return;

        DungeonContext context = DungeonsGuide.getDungeonsGuide().getDungeonFacade().getContext();

        if (DungeonsGuide.getDungeonsGuide().getDungeonFacade().getContext() != null) {
            UPlayerSelf thePlayer = ModAPI.getAPI().getPlayer();

            if (context.getBossfightProcessor() != null) {
                context.getBossfightProcessor().onInteract(interact);
            }
            if (context.getScaffoldParser() != null) {
                Point roomPt = context.getScaffoldParser().getDungeonMapLayout().worldPointToRoomPoint(thePlayer.getPositionVector());
                DungeonRoom dungeonRoom = context.getScaffoldParser().getRoomMap().get(roomPt);
                if (dungeonRoom != null) {
                    if (dungeonRoom.getRoomProcessor() != null) {
                        dungeonRoom.getRoomProcessor().onInteract(interact);
                    }
                }
            }
        }
    }

    @kr.syeyoung.modapi.event.SubscribeEvent
    public void onBlockChange(BlockUpdateEvent.Post postInteract) {
        if (!SkyblockStatus.isOnDungeon()) return;


        DungeonContext context = DungeonsGuide.getDungeonsGuide().getDungeonFacade().getContext();

        if (DungeonsGuide.getDungeonsGuide().getDungeonFacade().getContext() != null) {

            UPlayerSelf thePlayer = ModAPI.getAPI().getPlayer();

            if (context.getBossfightProcessor() != null) {
                context.getBossfightProcessor().onBlockUpdate(postInteract);
            }
            if (context.getScaffoldParser() != null) {
                Point roomPt = context.getScaffoldParser().getDungeonMapLayout().worldPointToRoomPoint(thePlayer.getPositionVector());

                DungeonRoom dungeonRoom = context.getScaffoldParser().getRoomMap().get(roomPt);
                if (dungeonRoom != null) {
                    if (dungeonRoom.getRoomProcessor() != null) {
                        dungeonRoom.getRoomProcessor().onBlockUpdate(postInteract);
                    }
                }
            }
        }
    }

    @kr.syeyoung.modapi.event.SubscribeEvent
    public void onKeyInput(KeyBindPressedEvent keyInputEvent) {
        if (FeatureRegistry.DEBUG.isEnabled() && FeatureRegistry.ADVANCED_ROOMEDIT.isEnabled() && keyInputEvent.getKey() == FeatureRegistry.ADVANCED_ROOMEDIT.<Integer>getParameter("key").getValue()) {
            EditingContext ec = EditingContext.getEditingContext();
            if (ec == null) {
                DungeonContext context = DungeonsGuide.getDungeonsGuide().getDungeonFacade().getContext();
                if (context == null) {
                    ChatTransmitter.addToQueue(new ChatComponentText("Not in dungeons"));
                    return;
                }
                UPlayerSelf thePlayer = ModAPI.getAPI().getPlayer();
                if (context.getScaffoldParser() != null) {
                    Point roomPt = context.getScaffoldParser().getDungeonMapLayout().worldPointToRoomPoint(thePlayer.getPositionVector());
                    DungeonRoom dungeonRoom = context.getScaffoldParser().getRoomMap().get(roomPt);

                    if (dungeonRoom == null) {
                        ChatTransmitter.addToQueue(new ChatComponentText("Can't determine the dungeon room you're in"));
                        return;
                    }

                    if (EditingContext.getEditingContext() != null) {
                        ChatTransmitter.addToQueue(new ChatComponentText("There is an editing session currently open."));
                        return;
                    }

                    EditingContext.createEditingContext(dungeonRoom);
                    EditingContext.getEditingContext().openGui(new GuiDungeonRoomEdit(dungeonRoom));
                }
            } else ec.reopen();
        }
    }

    @kr.syeyoung.modapi.event.SubscribeEvent
    public void onInteract(PlayerInteractEvent keyInputEvent) {
        if (!SkyblockStatus.isOnDungeon()) return;

        DungeonContext context = DungeonsGuide.getDungeonsGuide().getDungeonFacade().getContext();

        if (context != null) {
            UPlayerSelf thePlayer = ModAPI.getAPI().getPlayer();

            if (context.getBossfightProcessor() != null) {
                context.getBossfightProcessor().onInteractBlock(keyInputEvent);
            }

            if (context.getScaffoldParser() != null) {
                Point roomPt = context.getScaffoldParser().getDungeonMapLayout().worldPointToRoomPoint(thePlayer.getPositionVector());
                DungeonRoom dungeonRoom = context.getScaffoldParser().getRoomMap().get(roomPt);
                if (dungeonRoom != null) {
                    if (dungeonRoom.getRoomProcessor() != null) {
                        dungeonRoom.getRoomProcessor().onInteractBlock(keyInputEvent);
                    }
                }
            }
        }
    }

    @Getter
    private final Map<Integer, Vec3> entityIdToPosMap = new HashMap<>();

    @kr.syeyoung.modapi.event.SubscribeEvent
    public void onEntitySpawn(EntityEnterWorldEvent spawn) {
//        if (spawn.entity instanceof EntityBat)
//            System.out.println(spawn.entity +" Spawned!! dist: "+spawn.entity.getDistanceToEntity(Minecraft.getMinecraft().thePlayer));

        DungeonActionContext.getSpawnLocation().put(spawn.getEntity().getEntityId(), new Vector3D(spawn.getEntity().getPosX(), spawn.getEntity().getPosY(), spawn.getEntity().getPosZ()));
    }

    @kr.syeyoung.modapi.event.SubscribeEvent
    public void onItemPickup(ItemPickupEvent event) {
        DungeonActionContext.getPickedups().add(event.getItem().getEntityId());
    }

    @kr.syeyoung.modapi.event.SubscribeEvent
    public void onEntityDespawn2(EntityExitWorldEvent worldEvent) {
        for (int entityId : worldEvent.getEntityIds()) {
            UEntity en = ModAPI.getAPI().getWorld().getEntityById(entityId);
            if (en != null && en.getEntityType() == EntityType.BAT && en.getPositionVector().distanceSq(ModAPI.getAPI().getPlayer().getPositionVector()) < 3025)
                DungeonActionContext.getKilleds().add(entityId);
        }
    }

    @kr.syeyoung.modapi.event.SubscribeEvent
    public void onChunkUpdate(ChunkUpdateEvent.Post post) {
        if (!SkyblockStatus.isOnDungeon()) return;
        DungeonContext context = DungeonsGuide.getDungeonsGuide().getDungeonFacade().getContext();

        if (context != null) {
            if (context.getScaffoldParser() != null) {
                for (DungeonRoom dungeonRoom : context.getScaffoldParser().getDungeonRoomList()) {
                    for (UChunk chunk : post.getUpdatedChunks()) {
                        dungeonRoom.chunkUpdate(chunk.getChunkX(), chunk.getChunkZ());

                        RoomProcessor roomProcessor = dungeonRoom.getRoomProcessor();
                        if (roomProcessor != null) {
                            roomProcessor.chunkUpdate(chunk.getChunkX(), chunk.getChunkZ());
                        }
                    }
                }
            }
        }

    }
    @kr.syeyoung.modapi.event.SubscribeEvent
    public void onEntityDeSpawn(LivingEntityDeathEvent deathEvent) {
        if (deathEvent.getEntityLiving().getEntityType() == EntityType.BAT)
            DungeonActionContext.getKilleds().add(deathEvent.getEntityLiving().getEntityId());

        if (!SkyblockStatus.isOnDungeon()) return;

        DungeonContext context = DungeonsGuide.getDungeonsGuide().getDungeonFacade().getContext();

        if (context != null) {
            UPlayerSelf thePlayer = ModAPI.getAPI().getPlayer();

            if (context.getBossfightProcessor() != null) {
                context.getBossfightProcessor().onEntityDeath(deathEvent);
            }
            if (context.getScaffoldParser() != null) {
                Point roomPt = context.getScaffoldParser().getDungeonMapLayout().worldPointToRoomPoint(thePlayer.getPositionVector());
                DungeonRoom dungeonRoom = context.getScaffoldParser().getRoomMap().get(roomPt);
                if (dungeonRoom != null) {
                    if (dungeonRoom.getRoomProcessor() != null) {
                        dungeonRoom.getRoomProcessor().onEntityDeath(deathEvent);
                    }
                }
            }
        }

        if (!(deathEvent.getEntityLiving().getEntityType() == EntityType.BAT))
            DungeonActionContext.getSpawnLocation().remove(deathEvent.getEntityLiving().getEntityId());
    }

}
