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

import kr.syeyoung.dungeonsguide.mod.events.impl.WorldUnloadEvent;
import kr.syeyoung.dungeonsguide.mod.events.impl.LivingTickEvent;
import kr.syeyoung.dungeonsguide.mod.events.impl.LivingDeathEvent;
import kr.syeyoung.dungeonsguide.mod.events.impl.EntityEnterWorldEvent;
import kr.syeyoung.dungeonsguide.mod.events.impl.ScreenRenderEvent;
import kr.syeyoung.dungeonsguide.mod.dungeon.data.OffsetPoint;
import kr.syeyoung.dungeonsguide.mod.DungeonsGuide;
import kr.syeyoung.dungeonsguide.mod.SkyblockStatus;
import kr.syeyoung.dungeonsguide.mod.chat.ChatTransmitter;
import kr.syeyoung.dungeonsguide.mod.config.Config;
import kr.syeyoung.dungeonsguide.mod.dungeon.DungeonActionContext;
import kr.syeyoung.dungeonsguide.mod.dungeon.DungeonContext;
import kr.syeyoung.dungeonsguide.mod.dungeon.dataprovider.DungeonDoor;
// import kr.syeyoung.dungeonsguide.mod.dungeon.roomedit.EditingContext;
// import kr.syeyoung.dungeonsguide.mod.dungeon.roomedit.gui.GuiDungeonAddSet;
// import kr.syeyoung.dungeonsguide.mod.dungeon.roomedit.gui.GuiDungeonParameterEdit;
// import kr.syeyoung.dungeonsguide.mod.dungeon.roomedit.gui.GuiDungeonRoomEdit;
// import kr.syeyoung.dungeonsguide.mod.dungeon.roomedit.gui.GuiDungeonValueEdit;
// import kr.syeyoung.dungeonsguide.mod.dungeon.roomedit.valueedit.ValueEdit;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomfinder.DungeonRoom;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomprocessor.GeneralRoomProcessor;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomprocessor.RoomProcessor;
import kr.syeyoung.dungeonsguide.mod.dungeon.world.CollisionStateCalculatingCoordinateMap;
import kr.syeyoung.dungeonsguide.mod.dungeon.world.PearlCalculatingCoordinateMap;
import kr.syeyoung.dungeonsguide.mod.events.impl.*;
// import kr.syeyoung.dungeonsguide.mod.fakeserver.DungeonServerLaunchUtils;
import kr.syeyoung.dungeonsguide.mod.features.FeatureRegistry;
import kr.syeyoung.dungeonsguide.mod.features.impl.etc.FeatureCollectDiagnostics;
import kr.syeyoung.dungeonsguide.mod.parallelUniverse.scoreboard.ScoreboardManager;
import kr.syeyoung.dungeonsguide.mod.parallelUniverse.tab.TabList;
import kr.syeyoung.dungeonsguide.mod.parallelUniverse.teams.TeamManager;
import kr.syeyoung.dungeonsguide.mod.utils.MapUtils;
import kr.syeyoung.dungeonsguide.mod.utils.RenderUtils;
import lombok.Getter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ambient.Bat;
import net.minecraft.util.profiling.Profiler;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.phys.AABB;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.phys.Vec3;
import kr.syeyoung.dungeonsguide.mod.events.impl.ChatReceivedEvent;
import kr.syeyoung.dungeonsguide.mod.events.impl.RenderHUDEvent;
import kr.syeyoung.dungeonsguide.mod.events.impl.RenderWorldEvent;
import kr.syeyoung.dungeonsguide.mod.events.impl.PlayerInteractEvent;
import kr.syeyoung.dungeonsguide.mod.events.ListenerPriority;
import kr.syeyoung.dungeonsguide.mod.events.annotations.SubscribeEvent;

import java.awt.*;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import com.mojang.blaze3d.platform.InputConstants;
import kr.syeyoung.dungeonsguide.mod.utils.TextUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.util.Tuple;
import java.util.HashSet;
import java.util.Set;
import kr.syeyoung.dungeonsguide.mod.dungeon.world.MinecraftWorld;
import kr.syeyoung.dungeonsguide.mod.parallelUniverse.bossbar.BossBarManager;
import kr.syeyoung.dungeonsguide.mod.render.world.DGWorldRenderer;
import kr.syeyoung.dungeonsguide.mod.render.world.state.BlockModelState;
import org.joml.Matrix4f;

public class DungeonListener {


    @SubscribeEvent
    public void onWorldLoad(WorldUnloadEvent event) {
        TabList.INSTANCE.clear();
        TeamManager.INSTANCE.clear();
        ScoreboardManager.INSTANCE.clear();
        BossBarManager.INSTANCE.clear();
        Config.scheduleConfigSave();
        DungeonActionContext.getSpawnLocation().clear();
        DungeonActionContext.getKilleds().clear();
    }

    @SubscribeEvent
    public void onPostDraw(ScreenRenderEvent.Post e) {
        if (!SkyblockStatus.isOnDungeon()) return;


        ProfilerFiller profiler = Profiler.get();

        DungeonContext context = DungeonsGuide.getDungeonsGuide().getDungeonFacade().getContext();

        if (context != null) {

            LocalPlayer thePlayer = Minecraft.getInstance().player;
            if (thePlayer == null) {
                return;
            }
            profiler.push("Dungeons Guide - DrawScreen.Post: Bossfight Processor");
            if (context.getBossfightProcessor() != null) {
                context.getBossfightProcessor().onPostGuiRender(e);
            }
            profiler.popPush("Dungeons Guide - DrawScreen.Post: Room Processor");

            if (context.getScaffoldParser() != null) {
                Point roomPt = context.getScaffoldParser().getDungeonMapLayout().worldPointToRoomPoint(thePlayer.position());

                DungeonRoom dungeonRoom = context.getScaffoldParser().getRoomMap().get(roomPt);
                if (dungeonRoom != null && dungeonRoom.getRoomProcessor() != null) {
                    dungeonRoom.getRoomProcessor().onPostGuiRender(e);
                }
            }
            profiler.pop();
        }

    }

    @SubscribeEvent
    public void onEntityUpdate(LivingTickEvent e) {
        if (!SkyblockStatus.isOnDungeon()) return;

        DungeonContext context = DungeonsGuide.getDungeonsGuide().getDungeonFacade().getContext();
        if (context != null) {
            LocalPlayer thePlayer = Minecraft.getInstance().player;
            if (thePlayer == null) return;
            if (context.getBossfightProcessor() != null) context.getBossfightProcessor().onEntityUpdate(e);
            if (context.getScaffoldParser() != null) {
                Point roomPt = context.getScaffoldParser().getDungeonMapLayout().worldPointToRoomPoint(thePlayer.position());

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
    public void onTick(ClientTickEvent ev) {
        if (ev.phase != ClientTickEvent.Phase.START) return;
        DungeonsGuide.getDungeonsGuide().getEventBus().fireEvent(new DGTickEvent());

        if (SkyblockStatus.isOnSkyblock() || SkyblockStatus.isOnDungeon()) {
            DungeonContext context = DungeonsGuide.getDungeonsGuide().getDungeonFacade().getContext();
            if (context != null) {
                context.getMapPlayerMarkerProcessor().tick();
                context.tick();
            } else {
                try {
                    if (DungeonsGuide.getDungeonsGuide().getSkyblockStatus().isForceIsOnDungeon()) {
                        // TODO: fakeserver is not in the first 26.1 build
//                        DungeonServerLaunchUtils.createContext();
                        DungeonsGuide.getDungeonsGuide().getEventBus().fireEvent(new DungeonStartedEvent());
                    } else if (SkyblockStatus.isOnDungeon()) {
                        DungeonsGuide.getDungeonsGuide().getDungeonFacade().setContext(new DungeonContext(
                                SkyblockStatus.getLocationName(),
                                new MinecraftWorld(Minecraft.getInstance().level)));
                        DungeonsGuide.getDungeonsGuide().getEventBus().fireEvent(new DungeonStartedEvent());
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

            LocalPlayer thePlayer = Minecraft.getInstance().player;
            if (thePlayer == null) {
                return;
            }


            if (context.getBossfightProcessor() != null) {
                context.getBossfightProcessor().tick();
            }
            if (context.getScaffoldParser() != null) {
                Point roomPt = context.getScaffoldParser().getDungeonMapLayout().worldPointToRoomPoint(thePlayer.position());

                DungeonRoom dungeonRoom = context.getScaffoldParser().getRoomMap().get(roomPt);

                if (dungeonRoom != null && dungeonRoom.getRoomProcessor() != null) {
                    dungeonRoom.getRoomProcessor().tick();
                }
            }

        }

    }

    private WeakReference<DungeonRoom> lastRoom = null;

    @SubscribeEvent
    public void onTickDetectRoomTransfer(ClientTickEvent ev) {
        if (ev.phase != ClientTickEvent.Phase.START) return;


        DungeonContext context = DungeonsGuide.getDungeonsGuide().getDungeonFacade().getContext();
        if (!SkyblockStatus.isOnDungeon() || context == null) return;
        LocalPlayer thePlayer = Minecraft.getInstance().player;
        if (thePlayer == null) return;
        if (context.getScaffoldParser() == null) return;
        Point roomPt = context.getScaffoldParser().getDungeonMapLayout().worldPointToRoomPoint(thePlayer.position());
        DungeonRoom currentRoom = context.getScaffoldParser().getRoomMap().get(roomPt);
        DungeonRoom oldRoom = lastRoom == null ? null : lastRoom.get();
        boolean isActuallyInCurrent = currentRoom == null || currentRoom.getRoomBounds().isFullyWithin(thePlayer.position());
        if (!isActuallyInCurrent) currentRoom = null;

        lastRoom = new WeakReference<>(currentRoom);

        if (oldRoom == currentRoom) return;
        if (oldRoom != null)
            DungeonsGuide.getDungeonsGuide().getEventBus().fireEvent(new DungeonRoomExitEvent(oldRoom));
        if (currentRoom != null)
            DungeonsGuide.getDungeonsGuide().getEventBus().fireEvent(new DungeonRoomEnterEvent(currentRoom));
    }

    @SubscribeEvent
    public void onRender(RenderHUDEvent.Post postRender) {

        if (!SkyblockStatus.isOnDungeon()) return;
        ProfilerFiller profiler = Profiler.get();

        DungeonContext context = DungeonsGuide.getDungeonsGuide().getDungeonFacade().getContext();
        if (context != null) {

            LocalPlayer thePlayer = Minecraft.getInstance().player;

            profiler.push("Dungeons Guide - RenderGameOverlay.Post :: Bossfight Processor");
            if (context.getBossfightProcessor() != null)
                context.getBossfightProcessor().drawScreen(postRender);

            profiler.popPush("Dungeons Guide - RenderGameOverlay.Post :: Room Processor");
            if (context.getScaffoldParser() != null) {
                Point roomPt = context.getScaffoldParser().getDungeonMapLayout().worldPointToRoomPoint(thePlayer.position());
                DungeonRoom dungeonRoom = context.getScaffoldParser().getRoomMap().get(roomPt);
                if (dungeonRoom != null) {
                    if (dungeonRoom.getRoomProcessor() != null) {
                        dungeonRoom.getRoomProcessor().drawScreen(postRender);
                    }
                }
            }
            profiler.pop();

            if (context.getDungeonName().equals("TEST DG")) {
                Font fr = Minecraft.getInstance().font;
                GuiGraphicsExtractor graphics = postRender.graphics;

                int width = fr.width("Dungeons Guide Mockup Dungeon");
                int width2 = fr.width("Preset: "+context.getPreset().getPresetName());
                int bigger = Math.max(width, width2);
                graphics.fill(
                        (graphics.guiWidth()-bigger - 10)/2,
                        (graphics.guiHeight()/9 - 5) ,
                        (graphics.guiWidth() + bigger + 10) /2,
                        (graphics.guiHeight()/9) + 5 + fr.lineHeight*2,
                        0x77111111
                );

                graphics.text(fr, "Dungeons Guide Mockup Dungeon", (graphics.guiWidth()-width)/2, graphics.guiHeight()/9, 0xFF00FF00, false);

                graphics.text(fr, "Preset: "+context.getPreset().getPresetName(), (graphics.guiWidth() - width2) / 2, graphics.guiHeight() / 9 + fr.lineHeight, 0xFF00FF00, false);
            }

        }
    }

    @SubscribeEvent()
    public  void onMapUpdate(MapUpdateEvent mapUpdateEvent) {
        if (!SkyblockStatus.isOnDungeon()) return;

        DungeonContext context = DungeonsGuide.getDungeonsGuide().getDungeonFacade().getContext();

        if (context != null) {
            context.onMapUpdate(mapUpdateEvent);
        }
    }
    @SubscribeEvent(receiveCanceled = true, priority = ListenerPriority.FIRST)
    public void onChatReceived(ChatReceivedEvent clientChatReceivedEvent) {
        if (!SkyblockStatus.isOnDungeon()) return;

        if (clientChatReceivedEvent.type != 2 && TextUtils.getFormattedText(clientChatReceivedEvent.message).contains("§6> §e§lEXTRA STATS §6<")) {
            DungeonsGuide.getDungeonsGuide().getEventBus().fireEvent(new DungeonEndedEvent());
        }

        DungeonContext context = DungeonsGuide.getDungeonsGuide().getDungeonFacade().getContext();

        if (context != null) {

            LocalPlayer thePlayer = Minecraft.getInstance().player;
            context.onChat(clientChatReceivedEvent);

            if (context.getBossfightProcessor() != null) {
                if (clientChatReceivedEvent.type == 2) {
                    context.getBossfightProcessor().actionbarReceived(clientChatReceivedEvent.message);
                } else {
                    context.getBossfightProcessor().chatReceived(clientChatReceivedEvent.message);
                }
            }
            if (context.getScaffoldParser() != null) {
                Point roomPt = context.getScaffoldParser().getDungeonMapLayout().worldPointToRoomPoint(thePlayer.position());


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
    public void onWorldRender(RenderWorldEvent renderWorldLastEvent) {
        if (!SkyblockStatus.isOnDungeon()) return;
        try {

            ProfilerFiller profiler = Profiler.get();

            DungeonContext context = DungeonsGuide.getDungeonsGuide().getDungeonFacade().getContext();
            if (context == null) {
                return;
            }


            if (FeatureRegistry.DEBUG.isEnabled()) {
                profiler.push("Dungeons Guide - Door Rendering");
                if (context.getScaffoldParser() != null) {
                    for (DungeonRoom dungeonRoom : context.getScaffoldParser().getDungeonRoomList()) {
                        for (DungeonDoor door : dungeonRoom.getDoors()) {
                            RenderUtils.renderDoor(door, renderWorldLastEvent.partialTicks);
                        }
                    }
                }
                profiler.pop();
            }



            profiler.push("Dungeons Guide - RenderWorldLast :: Bossfight Processor");
            if (context.getBossfightProcessor() != null) {
                context.getBossfightProcessor().drawWorld(renderWorldLastEvent);
            }

            profiler.popPush("Dungeons Guide - RenderWorldLast :: Room Processor");
            if (context.getScaffoldParser() != null) {
                LocalPlayer thePlayer = Minecraft.getInstance().player;
                Point roomPt = context.getScaffoldParser().getDungeonMapLayout().worldPointToRoomPoint(thePlayer.position());

                DungeonRoom dungeonRoom = context.getScaffoldParser().getRoomMap().get(roomPt);
                if (dungeonRoom != null) {
                    if (dungeonRoom.getRoomProcessor() != null) {
                        dungeonRoom.getRoomProcessor().drawWorld(renderWorldLastEvent);
                    }
                }



                if (FeatureRegistry.DEBUG.isEnabled() && dungeonRoom != null && dungeonRoom.getRoomProcessor() instanceof GeneralRoomProcessor) {

                    GeneralRoomProcessor roomProcessor = (GeneralRoomProcessor) dungeonRoom.getRoomProcessor();
                    Vec3 player = Minecraft.getInstance().player.position();
                    BlockPos real = BlockPos.containing(player.x * 2, player.y * 2, player.z * 2);
                    try {

                        for (BlockPos allInBox : BlockPos.betweenClosed(real.offset(-1, -1, -1), real.offset(1, 1, 1))) {
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
                        OffsetPoint offsetPoint = new OffsetPoint(dungeonRoom, new BlockPos(0,0,0));
                        for (BlockPos allInBox : BlockPos.betweenClosed(dungeonRoom.getRoomBounds().getMin().offset(0, -60, 0), dungeonRoom.getRoomBounds().getMax().offset(0, 180, 0))) {
                            offsetPoint.setPosInWorld(dungeonRoom, allInBox);
                            BlockState blockState = dungeonRoom.getDungeonRoomInfo().getBlock(offsetPoint, dungeonRoom.getRoomMatcher().getRotation());
                            if (!blockState.equals(dungeonRoom.getCachedWorld().getBlockState(allInBox))) {
                                RenderUtils.highlightBlock(allInBox, new Color(0x70FF0000,true), renderWorldLastEvent.partialTicks, false);
                                // the expected block at half size in the middle of the block
                                DGWorldRenderer.record(new BlockModelState(blockState,
                                        new Matrix4f().translate(allInBox.getX(), allInBox.getY(), allInBox.getZ()).scale(0.5f).translate(0.5f, 0.5f, 0.5f)));
                            }
                        }
                    }
                }
            }


            profiler.popPush("Dungeons Guide - RenderWorldLast :: Room Edit");
            // TODO: room editor is not in the first 26.1 build
//            if (EditingContext.getEditingContext() != null) {
//                GuiScreen guiScreen = EditingContext.getEditingContext().getCurrent();
//                if (guiScreen instanceof GuiDungeonParameterEdit) {
//                    ValueEdit valueEdit = ((GuiDungeonParameterEdit) guiScreen).getValueEdit();
//                    if (valueEdit != null) {
//                        valueEdit.renderWorld(renderWorldLastEvent.partialTicks);
//                    }
//                } else if (guiScreen instanceof GuiDungeonValueEdit) {
//                    ValueEdit valueEdit = ((GuiDungeonValueEdit) guiScreen).getValueEdit();
//                    if (valueEdit != null) {
//                        valueEdit.renderWorld(renderWorldLastEvent.partialTicks);
//                    }
//                } else if (guiScreen instanceof GuiDungeonAddSet) {
//                    ((GuiDungeonAddSet) guiScreen).onWorldRender(renderWorldLastEvent.partialTicks);
//                }
//            }
            profiler.pop();
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
            LocalPlayer thePlayer = Minecraft.getInstance().player;

            if (context.getBossfightProcessor() != null) {
                context.getBossfightProcessor().onKeybindPress(keyInputEvent);
            }
            if (context.getScaffoldParser() != null) {
                Point roomPt = context.getScaffoldParser().getDungeonMapLayout().worldPointToRoomPoint(thePlayer.position());
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
            LocalPlayer thePlayer = Minecraft.getInstance().player;

            if (context.getBossfightProcessor() != null) {
                context.getBossfightProcessor().onInteract(interact);
            }
            if (context.getScaffoldParser() != null) {
                Point roomPt = context.getScaffoldParser().getDungeonMapLayout().worldPointToRoomPoint(thePlayer.position());
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

            LocalPlayer thePlayer = Minecraft.getInstance().player;

            if (context.getBossfightProcessor() != null) {
                context.getBossfightProcessor().onBlockUpdate(postInteract);
            }
            if (context.getScaffoldParser() != null) {
                Point roomPt = context.getScaffoldParser().getDungeonMapLayout().worldPointToRoomPoint(thePlayer.position());

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
        // TODO: room editor is not in the first 26.1 build
//        if (FeatureRegistry.DEBUG.isEnabled() && FeatureRegistry.ADVANCED_ROOMEDIT.isEnabled() && keyInputEvent.getKey().equals(FeatureRegistry.ADVANCED_ROOMEDIT.<InputConstants.Key>getParameter("key").getValue())) {
//            EditingContext ec = EditingContext.getEditingContext();
//            if (ec == null) {
//                DungeonContext context = DungeonsGuide.getDungeonsGuide().getDungeonFacade().getContext();
//                if (context == null) {
//                    ChatTransmitter.addToQueue(Component.literal("Not in dungeons"));
//                    return;
//                }
//                LocalPlayer thePlayer = Minecraft.getInstance().player;
//                if (context.getScaffoldParser() != null) {
//                    Point roomPt = context.getScaffoldParser().getDungeonMapLayout().worldPointToRoomPoint(thePlayer.position());
//                    DungeonRoom dungeonRoom = context.getScaffoldParser().getRoomMap().get(roomPt);

//                    if (dungeonRoom == null) {
//                        ChatTransmitter.addToQueue(Component.literal("Can't determine the dungeon room you're in"));
//                        return;
//                    }

//                    if (EditingContext.getEditingContext() != null) {
//                        ChatTransmitter.addToQueue(Component.literal("There is an editing session currently open."));
//                        return;
//                    }

//                    EditingContext.createEditingContext(dungeonRoom);
//                    EditingContext.getEditingContext().openGui(new GuiDungeonRoomEdit(dungeonRoom));
//                }
//            } else ec.reopen();
//        }
    }

    @SubscribeEvent
    public void onInteract(PlayerInteractEvent keyInputEvent) {
        if (!keyInputEvent.world.isClientSide()) return;
        if (!SkyblockStatus.isOnDungeon()) return;

        DungeonContext context = DungeonsGuide.getDungeonsGuide().getDungeonFacade().getContext();

        if (context != null) {
            LocalPlayer thePlayer = Minecraft.getInstance().player;

            if (context.getBossfightProcessor() != null) {
                context.getBossfightProcessor().onInteractBlock(keyInputEvent);
            }

            if (context.getScaffoldParser() != null) {
                Point roomPt = context.getScaffoldParser().getDungeonMapLayout().worldPointToRoomPoint(thePlayer.position());
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
    public void onEntitySpawn(EntityEnterWorldEvent spawn) {
//        if (spawn.entity instanceof Bat)
//            System.out.println(spawn.entity +" Spawned!! dist: "+spawn.entity.getDistanceToEntity(Minecraft.getInstance().player));

        DungeonActionContext.getSpawnLocation().put(spawn.entity.getId(), new Vec3(spawn.entity.getX(), spawn.entity.getY(), spawn.entity.getZ()));
    }

    @SubscribeEvent
    public void onItemPickup(ItemPickupEvent event) {
        if (Minecraft.getInstance().level.getEntityByID(event.getItemId()) instanceof ItemEntity)
            DungeonActionContext.getPickedups().add(event.getItemId());
    }

    @SubscribeEvent
    public void onEntityDespawn2(EntityExitWorldEvent worldEvent) {
        for (int entityId : worldEvent.getEntityIds()) {
            Entity en = Minecraft.getInstance().level.getEntity(entityId);
            if (en instanceof Bat && en.distanceToSqr(Minecraft.getInstance().player) < 3025)
                DungeonActionContext.getKilleds().add(entityId);
        }
    }

    // 1.8.9 refreshed the rooms on chunk packets to see block changes, chunk resends now arrive as block updates (D105)
    @SubscribeEvent
    public void onChunkUpdate(BlockUpdateEvent.Post post) {
        if (!SkyblockStatus.isOnDungeon()) return;

        Set<ChunkPos> updatedChunks = new HashSet<>();
        for (Tuple<BlockPos, BlockState> updatedBlock : post.getUpdatedBlocks()) {
            updatedChunks.add(ChunkPos.containing(updatedBlock.getA()));
        }

        DungeonContext context = DungeonsGuide.getDungeonsGuide().getDungeonFacade().getContext();

        if (context != null) {
            if (context.getScaffoldParser() != null) {
                for (DungeonRoom dungeonRoom : context.getScaffoldParser().getDungeonRoomList()) {
                    for (ChunkPos chunk : updatedChunks) {
                        dungeonRoom.chunkUpdate(chunk.x(), chunk.z());

                        RoomProcessor roomProcessor = dungeonRoom.getRoomProcessor();
                        if (roomProcessor != null) {
                            roomProcessor.chunkUpdate(chunk.x(), chunk.z());
                        }
                    }
                }
            }
        }
    }
    @SubscribeEvent
    public void onEntityDeSpawn(LivingDeathEvent deathEvent) {
        if (deathEvent.entityLiving instanceof Bat)
            DungeonActionContext.getKilleds().add(deathEvent.entity.getId());

        if (!SkyblockStatus.isOnDungeon()) return;

        DungeonContext context = DungeonsGuide.getDungeonsGuide().getDungeonFacade().getContext();

        if (context != null) {
            LocalPlayer thePlayer = Minecraft.getInstance().player;

            if (context.getBossfightProcessor() != null) {
                context.getBossfightProcessor().onEntityDeath(deathEvent);
            }
            if (context.getScaffoldParser() != null) {
                Point roomPt = context.getScaffoldParser().getDungeonMapLayout().worldPointToRoomPoint(thePlayer.position());
                DungeonRoom dungeonRoom = context.getScaffoldParser().getRoomMap().get(roomPt);
                if (dungeonRoom != null) {
                    if (dungeonRoom.getRoomProcessor() != null) {
                        dungeonRoom.getRoomProcessor().onEntityDeath(deathEvent);
                    }
                }
            }
        }

        if (!(deathEvent.entityLiving instanceof Bat))
            DungeonActionContext.getSpawnLocation().remove(deathEvent.entity.getId());
    }

}
