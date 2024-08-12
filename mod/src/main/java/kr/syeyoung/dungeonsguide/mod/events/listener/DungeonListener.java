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

import kr.syeyoung.dungeonsguide.dungeon.data.OffsetPoint;
import kr.syeyoung.dungeonsguide.mod.DungeonsGuide;
import kr.syeyoung.dungeonsguide.mod.SkyblockStatus;
import kr.syeyoung.dungeonsguide.mod.chat.ChatTransmitter;
import kr.syeyoung.dungeonsguide.mod.config.Config;
import kr.syeyoung.dungeonsguide.mod.dungeon.DungeonActionContext;
import kr.syeyoung.dungeonsguide.mod.dungeon.DungeonContext;
import kr.syeyoung.dungeonsguide.mod.dungeon.doorfinder.DungeonDoor;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomedit.EditingContext;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomedit.gui.GuiDungeonAddSet;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomedit.gui.GuiDungeonParameterEdit;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomedit.gui.GuiDungeonRoomEdit;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomedit.gui.GuiDungeonValueEdit;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomedit.valueedit.ValueEdit;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomfinder.DungeonRoom;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomprocessor.RoomProcessor;
import kr.syeyoung.dungeonsguide.mod.events.impl.*;
import kr.syeyoung.dungeonsguide.mod.features.FeatureRegistry;
import kr.syeyoung.dungeonsguide.mod.features.impl.advanced.FeatureCompareRoom;
import kr.syeyoung.dungeonsguide.mod.features.impl.etc.FeatureCollectDiagnostics;
import kr.syeyoung.dungeonsguide.mod.parallelUniverse.scoreboard.ScoreboardManager;
import kr.syeyoung.dungeonsguide.mod.parallelUniverse.tab.TabList;
import kr.syeyoung.dungeonsguide.mod.parallelUniverse.teams.TeamManager;
import kr.syeyoung.dungeonsguide.mod.utils.MapUtils;
import kr.syeyoung.dungeonsguide.mod.utils.RenderUtils;
import lombok.Getter;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.EntityBat;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S21PacketChunkData;
import net.minecraft.network.play.server.S26PacketMapChunkBulk;
import net.minecraft.profiler.Profiler;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.Vec3;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;

import java.awt.*;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class DungeonListener {


    @SubscribeEvent
    public void onWorldLoad(WorldEvent.Unload event) {
        TabList.INSTANCE.clear();
        TeamManager.INSTANCE.clear();
        ScoreboardManager.INSTANCE.clear();
        try {
            Config.saveConfig();
        } catch (IOException e) {
            FeatureCollectDiagnostics.queueSendLogAsync(e);
            e.printStackTrace();
        }
        DungeonActionContext.getSpawnLocation().clear();
        DungeonActionContext.getKilleds().clear();
    }

    @SubscribeEvent
    public void onPostDraw(GuiScreenEvent.DrawScreenEvent.Post e) {
        if (!SkyblockStatus.isOnDungeon()) return;


        Profiler profiler = Minecraft.getMinecraft().mcProfiler;

        DungeonContext context = DungeonsGuide.getDungeonsGuide().getDungeonFacade().getContext();

        if (context != null) {

            EntityPlayerSP thePlayer = Minecraft.getMinecraft().thePlayer;
            if (thePlayer == null) {
                return;
            }
            profiler.startSection("Dungeons Guide - DrawScreen.Post: Bossfight Processor");
            if (context.getBossfightProcessor() != null) {
                context.getBossfightProcessor().onPostGuiRender(e);
            }
            profiler.endStartSection("Dungeons Guide - DrawScreen.Post: Room Processor");

            if (context.getScaffoldParser() != null) {
                Point roomPt = context.getScaffoldParser().getDungeonMapLayout().worldPointToRoomPoint(thePlayer.getPosition());

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

    @SubscribeEvent
    public void onEntityUpdate(LivingEvent.LivingUpdateEvent e) {
        if (!SkyblockStatus.isOnDungeon()) return;

        DungeonContext context = DungeonsGuide.getDungeonsGuide().getDungeonFacade().getContext();
        if (context != null) {
            EntityPlayerSP thePlayer = Minecraft.getMinecraft().thePlayer;
            if (thePlayer == null) return;
            if (context.getBossfightProcessor() != null) context.getBossfightProcessor().onEntityUpdate(e);
            if (context.getScaffoldParser() != null) {
                Point roomPt = context.getScaffoldParser().getDungeonMapLayout().worldPointToRoomPoint(thePlayer.getPosition());

                DungeonRoom dungeonRoom = context.getScaffoldParser().getRoomMap().get(roomPt);
                if (dungeonRoom != null && dungeonRoom.getRoomProcessor() != null) {
                    dungeonRoom.getRoomProcessor().onEntityUpdate(e);
                }
            }
        }
    }

    @SubscribeEvent
    public void onDungeonLeave(DungeonLeftEvent ev) {
        DungeonsGuide.getDungeonsGuide().getDungeonFacade().setContext(null);
        if (!FeatureRegistry.ADVANCED_DEBUGGABLE_MAP.isEnabled()) {
            MapUtils.clearMap();
        }
    }





    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent ev) {
        if (ev.side == Side.SERVER || ev.phase != TickEvent.Phase.START) return;
        MinecraftForge.EVENT_BUS.post(new DGTickEvent());

        if (SkyblockStatus.isOnSkyblock() || SkyblockStatus.isOnDungeon()) {
            DungeonContext context = DungeonsGuide.getDungeonsGuide().getDungeonFacade().getContext();
            if (context != null) {
                context.getMapPlayerMarkerProcessor().tick();
                context.tick();
            } else {
                try {
                    if (SkyblockStatus.isOnDungeon()) {
                        DungeonsGuide.getDungeonsGuide().getDungeonFacade().setContext(new DungeonContext(
                                SkyblockStatus.locationName,
                                Minecraft.getMinecraft().thePlayer.worldObj));
                        MinecraftForge.EVENT_BUS.post(new DungeonStartedEvent());
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

            EntityPlayerSP thePlayer = Minecraft.getMinecraft().thePlayer;
            if (thePlayer == null) {
                return;
            }


            if (context.getBossfightProcessor() != null) {
                context.getBossfightProcessor().tick();
            }
            if (context.getScaffoldParser() != null) {
                Point roomPt = context.getScaffoldParser().getDungeonMapLayout().worldPointToRoomPoint(thePlayer.getPosition());

                DungeonRoom dungeonRoom = context.getScaffoldParser().getRoomMap().get(roomPt);

                if (dungeonRoom != null && dungeonRoom.getRoomProcessor() != null) {
                    dungeonRoom.getRoomProcessor().tick();
                }
            }

        }

    }


    @SubscribeEvent
    public void onRender(RenderGameOverlayEvent.Post postRender) {
        if (!(postRender.type == RenderGameOverlayEvent.ElementType.ALL))
            return;

        if (!SkyblockStatus.isOnDungeon()) return;
        Profiler profiler = Minecraft.getMinecraft().mcProfiler;

        DungeonContext context = DungeonsGuide.getDungeonsGuide().getDungeonFacade().getContext();
        if (context != null) {

            EntityPlayerSP thePlayer = Minecraft.getMinecraft().thePlayer;

            profiler.startSection("Dungeons Guide - RenderGameOverlay.Post :: Bossfight Processor");
            if (context.getBossfightProcessor() != null)
                context.getBossfightProcessor().drawScreen(postRender.partialTicks);

            profiler.endStartSection("Dungeons Guide - RenderGameOverlay.Post :: Room Processor");
            if (context.getScaffoldParser() != null) {
                Point roomPt = context.getScaffoldParser().getDungeonMapLayout().worldPointToRoomPoint(thePlayer.getPosition());
                DungeonRoom dungeonRoom = context.getScaffoldParser().getRoomMap().get(roomPt);
                if (dungeonRoom != null) {
                    if (dungeonRoom.getRoomProcessor() != null) {
                        dungeonRoom.getRoomProcessor().drawScreen(postRender.partialTicks);
                    }
                }
            }
            profiler.endSection();

        }
        GlStateManager.enableBlend();
        GlStateManager.color(1, 1, 1, 1);
        GL14.glBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ONE_MINUS_SRC_ALPHA);
        Minecraft.getMinecraft().entityRenderer.setupOverlayRendering();
        GlStateManager.enableAlpha();
    }

    @SubscribeEvent()
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
            MinecraftForge.EVENT_BUS.post(new DungeonEndedEvent());
        }

        DungeonContext context = DungeonsGuide.getDungeonsGuide().getDungeonFacade().getContext();

        if (context != null) {

            EntityPlayerSP thePlayer = Minecraft.getMinecraft().thePlayer;
            context.onChat(clientChatReceivedEvent);

            if (context.getBossfightProcessor() != null) {
                if (clientChatReceivedEvent.type == 2) {
                    context.getBossfightProcessor().actionbarReceived(clientChatReceivedEvent.message);
                } else {
                    context.getBossfightProcessor().chatReceived(clientChatReceivedEvent.message);
                }
            }
            if (context.getScaffoldParser() != null) {
                Point roomPt = context.getScaffoldParser().getDungeonMapLayout().worldPointToRoomPoint(thePlayer.getPosition());


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
                EntityPlayerSP thePlayer = Minecraft.getMinecraft().thePlayer;
                Point roomPt = context.getScaffoldParser().getDungeonMapLayout().worldPointToRoomPoint(thePlayer.getPosition());

                DungeonRoom dungeonRoom = context.getScaffoldParser().getRoomMap().get(roomPt);
                if (dungeonRoom != null) {
                    if (dungeonRoom.getRoomProcessor() != null) {
                        dungeonRoom.getRoomProcessor().drawWorld(renderWorldLastEvent.partialTicks);
                    }
                }


                if (FeatureRegistry.DEBUG.isEnabled() && dungeonRoom != null) {

                    Vec3 player = Minecraft.getMinecraft().thePlayer.getPositionVector();
                    BlockPos real = new BlockPos(player.xCoord * 2, player.yCoord * 2, player.zCoord * 2);
                    try {
                        for (BlockPos allInBox : BlockPos.getAllInBox(real.add(-1, -1, -1), real.add(1, 1, 1))) {
                            DungeonRoom.CollisionState blocked = dungeonRoom.getBlock(allInBox.getX(), allInBox.getY(), allInBox.getZ());
                            RenderUtils.highlightBox(
                                    AxisAlignedBB.fromBounds(
                                            allInBox.getX() / 2.0 - 0.1, allInBox.getY() / 2.0 - 0.1, allInBox.getZ() / 2.0 - 0.1,
                                            allInBox.getX() / 2.0 + 0.1, allInBox.getY() / 2.0 + 0.1, allInBox.getZ() / 2.0 + 0.1
                                    ), blocked.getColor(), renderWorldLastEvent.partialTicks, false);
                            DungeonRoom.PearlLandType type = dungeonRoom.getPearl(allInBox.getX(), allInBox.getY(), allInBox.getZ());
                            RenderUtils.drawTextAtWorld(type.name(), (float) (allInBox.getX() / 2.0 - 0.1), (float) (allInBox.getY() / 2.0 - 0.1), (float) (allInBox.getZ() / 2.0 - 0.1),
                                    0xFFFFFFFF,0.01f, false, true, renderWorldLastEvent.partialTicks);
                        }
                    } catch (Exception ignored) {}

                    if (FeatureRegistry.COMPARE_ROOM.toggleCompareStatus && dungeonRoom.getDungeonRoomInfo().hasSchematic()) {
                        OffsetPoint offsetPoint = new OffsetPoint(dungeonRoom, new BlockPos(0,0,0));
                        for (BlockPos allInBox : BlockPos.getAllInBox(dungeonRoom.getMin().add(0, -60, 0), dungeonRoom.getMax().add(0, 180, 0))) {
                            offsetPoint.setPosInWorld(dungeonRoom, allInBox);
                            IBlockState blockState = dungeonRoom.getDungeonRoomInfo().getBlock(offsetPoint, dungeonRoom.getRoomMatcher().getRotation());
                            if (!blockState.equals(dungeonRoom.getCachedWorld().getBlockState(allInBox))) {
                                RenderUtils.highlightBlock(allInBox, new Color(0x70FF0000,true), renderWorldLastEvent.partialTicks, false);
                                Minecraft.getMinecraft().renderEngine.bindTexture(TextureMap.locationBlocksTexture);
                                float partialTicks = renderWorldLastEvent.partialTicks;
                                Entity viewing_from = Minecraft.getMinecraft().getRenderViewEntity();

                                double x_fix = viewing_from.lastTickPosX + ((viewing_from.posX - viewing_from.lastTickPosX) * partialTicks);
                                double y_fix = viewing_from.lastTickPosY + ((viewing_from.posY - viewing_from.lastTickPosY) * partialTicks);
                                double z_fix = viewing_from.lastTickPosZ + ((viewing_from.posZ - viewing_from.lastTickPosZ) * partialTicks);

                                GlStateManager.pushMatrix();
                                GlStateManager.translate(-x_fix, -y_fix, -z_fix);
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
                                        blockrendererdispatcher.getBlockModelShapes().getModelForState(blockState),
                                        blockState, new BlockPos(0,0,0), vertexBuffer, false);
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

    @SubscribeEvent()
    public void onKey2(KeyBindPressedEvent keyInputEvent) {
        if (!SkyblockStatus.isOnDungeon()) return;

        DungeonContext context = DungeonsGuide.getDungeonsGuide().getDungeonFacade().getContext();

        if (DungeonsGuide.getDungeonsGuide().getDungeonFacade().getContext() != null) {
            EntityPlayerSP thePlayer = Minecraft.getMinecraft().thePlayer;

            if (context.getBossfightProcessor() != null) {
                context.getBossfightProcessor().onKeybindPress(keyInputEvent);
            }
            if (context.getScaffoldParser() != null) {
                Point roomPt = context.getScaffoldParser().getDungeonMapLayout().worldPointToRoomPoint(thePlayer.getPosition());
                DungeonRoom dungeonRoom = context.getScaffoldParser().getRoomMap().get(roomPt);
                if (dungeonRoom != null) {
                    if (dungeonRoom.getRoomProcessor() != null) {
                        dungeonRoom.getRoomProcessor().onKeybindPress(keyInputEvent);
                    }
                }
            }
        }
    }

    @SubscribeEvent()
    public void onInteract(PlayerInteractEntityEvent interact) {
        if (!SkyblockStatus.isOnDungeon()) return;

        DungeonContext context = DungeonsGuide.getDungeonsGuide().getDungeonFacade().getContext();

        if (DungeonsGuide.getDungeonsGuide().getDungeonFacade().getContext() != null) {
            EntityPlayerSP thePlayer = Minecraft.getMinecraft().thePlayer;

            if (context.getBossfightProcessor() != null) {
                context.getBossfightProcessor().onInteract(interact);
            }
            if (context.getScaffoldParser() != null) {
                Point roomPt = context.getScaffoldParser().getDungeonMapLayout().worldPointToRoomPoint(thePlayer.getPosition());
                DungeonRoom dungeonRoom = context.getScaffoldParser().getRoomMap().get(roomPt);
                if (dungeonRoom != null) {
                    if (dungeonRoom.getRoomProcessor() != null) {
                        dungeonRoom.getRoomProcessor().onInteract(interact);
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public void onBlockChange(BlockUpdateEvent.Post postInteract) {
        if (!SkyblockStatus.isOnDungeon()) return;


        DungeonContext context = DungeonsGuide.getDungeonsGuide().getDungeonFacade().getContext();

        if (DungeonsGuide.getDungeonsGuide().getDungeonFacade().getContext() != null) {

            EntityPlayerSP thePlayer = Minecraft.getMinecraft().thePlayer;

            if (context.getBossfightProcessor() != null) {
                context.getBossfightProcessor().onBlockUpdate(postInteract);
            }
            if (context.getScaffoldParser() != null) {
                Point roomPt = context.getScaffoldParser().getDungeonMapLayout().worldPointToRoomPoint(thePlayer.getPosition());

                DungeonRoom dungeonRoom = context.getScaffoldParser().getRoomMap().get(roomPt);
                if (dungeonRoom != null) {
                    if (dungeonRoom.getRoomProcessor() != null) {
                        dungeonRoom.getRoomProcessor().onBlockUpdate(postInteract);
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public void onKeyInput(KeyBindPressedEvent keyInputEvent) {
        if (FeatureRegistry.DEBUG.isEnabled() && FeatureRegistry.ADVANCED_ROOMEDIT.isEnabled() && keyInputEvent.getKey() == FeatureRegistry.ADVANCED_ROOMEDIT.<Integer>getParameter("key").getValue()) {
            EditingContext ec = EditingContext.getEditingContext();
            if (ec == null) {
                DungeonContext context = DungeonsGuide.getDungeonsGuide().getDungeonFacade().getContext();
                if (context == null) {
                    ChatTransmitter.addToQueue(new ChatComponentText("Not in dungeons"));
                    return;
                }
                EntityPlayerSP thePlayer = Minecraft.getMinecraft().thePlayer;
                if (context.getScaffoldParser() != null) {
                    Point roomPt = context.getScaffoldParser().getDungeonMapLayout().worldPointToRoomPoint(thePlayer.getPosition());
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

    @SubscribeEvent
    public void onInteract(PlayerInteractEvent keyInputEvent) {
        if (!keyInputEvent.world.isRemote) return;
        if (!SkyblockStatus.isOnDungeon()) return;

        DungeonContext context = DungeonsGuide.getDungeonsGuide().getDungeonFacade().getContext();

        if (context != null) {
            EntityPlayerSP thePlayer = Minecraft.getMinecraft().thePlayer;

            if (context.getBossfightProcessor() != null) {
                context.getBossfightProcessor().onInteractBlock(keyInputEvent);
            }

            if (context.getScaffoldParser() != null) {
                Point roomPt = context.getScaffoldParser().getDungeonMapLayout().worldPointToRoomPoint(thePlayer.getPosition());
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

    @SubscribeEvent
    public void onEntitySpawn(EntityJoinWorldEvent spawn) {
        if (spawn.entity instanceof EntityBat)
            System.out.println(spawn.entity +" Spawned!! dist: "+spawn.entity.getDistanceToEntity(Minecraft.getMinecraft().thePlayer));

        DungeonActionContext.getSpawnLocation().put(spawn.entity.getEntityId(), new Vec3(spawn.entity.posX, spawn.entity.posY, spawn.entity.posZ));
    }

    @SubscribeEvent
    public void onItemPickup(ItemPickupEvent event) {
        if (Minecraft.getMinecraft().theWorld.getEntityByID(event.getItemId()) instanceof EntityItem)
            DungeonActionContext.getPickedups().add(event.getItemId());
    }

    @SubscribeEvent
    public void onEntityDespawn2(EntityExitWorldEvent worldEvent) {
        for (int entityId : worldEvent.getEntityIds()) {
            Entity en = Minecraft.getMinecraft().theWorld.getEntityByID(entityId);
            if (en instanceof EntityBat && en.getDistanceSqToEntity(Minecraft.getMinecraft().thePlayer) < 3025)
                DungeonActionContext.getKilleds().add(entityId);
        }
    }

    @SubscribeEvent
    public void onChunkUpdate(PacketProcessedEvent.Post post) {
        if (!SkyblockStatus.isOnDungeon()) return;
        if (post.packet instanceof S21PacketChunkData) {
            S21PacketChunkData p = (S21PacketChunkData) post.packet;
            if (p.func_149274_i() && p.getExtractedSize() == 0) return; // IGNORE unloading chunks. :D

            DungeonContext context = DungeonsGuide.getDungeonsGuide().getDungeonFacade().getContext();

            if (context != null) {
                if (context.getScaffoldParser() != null) {
                    for (DungeonRoom dungeonRoom : context.getScaffoldParser().getDungeonRoomList()) {
                        dungeonRoom.chunkUpdate(p.getChunkX(), p.getChunkZ());
                    }
                }
            }
        } else if (post.packet instanceof S26PacketMapChunkBulk) {
            S26PacketMapChunkBulk p = (S26PacketMapChunkBulk) post.packet;

            DungeonContext context = DungeonsGuide.getDungeonsGuide().getDungeonFacade().getContext();

            if (context != null) {
                if (context.getScaffoldParser() != null) {
                    for (DungeonRoom dungeonRoom : context.getScaffoldParser().getDungeonRoomList()) {
                        for (int i = 0; i < p.getChunkCount(); i++) {
                            dungeonRoom.chunkUpdate(p.getChunkX(i), p.getChunkZ(i));
                        }
                    }
                }
            }
        }
    }
    @SubscribeEvent
    public void onEntityDeSpawn(LivingDeathEvent deathEvent) {
        if (deathEvent.entityLiving instanceof EntityBat)
            DungeonActionContext.getKilleds().add(deathEvent.entity.getEntityId());

        if (!SkyblockStatus.isOnDungeon()) return;

        DungeonContext context = DungeonsGuide.getDungeonsGuide().getDungeonFacade().getContext();

        if (context != null) {
            EntityPlayerSP thePlayer = Minecraft.getMinecraft().thePlayer;

            if (context.getBossfightProcessor() != null) {
                context.getBossfightProcessor().onEntityDeath(deathEvent);
            }
            if (context.getScaffoldParser() != null) {
                Point roomPt = context.getScaffoldParser().getDungeonMapLayout().worldPointToRoomPoint(thePlayer.getPosition());
                DungeonRoom dungeonRoom = context.getScaffoldParser().getRoomMap().get(roomPt);
                if (dungeonRoom != null) {
                    if (dungeonRoom.getRoomProcessor() != null) {
                        dungeonRoom.getRoomProcessor().onEntityDeath(deathEvent);
                    }
                }
            }
        }

        if (!(deathEvent.entityLiving instanceof EntityBat))
            DungeonActionContext.getSpawnLocation().remove(deathEvent.entity.getEntityId());
    }

}
