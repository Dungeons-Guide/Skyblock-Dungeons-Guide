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
import kr.syeyoung.dungeonsguide.mod.config.Config;
import kr.syeyoung.dungeonsguide.mod.dungeon.DungeonActionContext;
import kr.syeyoung.dungeonsguide.mod.dungeon.DungeonContext;
import kr.syeyoung.dungeonsguide.mod.dungeon.dataprovider.DungeonDoor;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomfinder.DungeonRoom;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomprocessor.RoomProcessor;
import kr.syeyoung.dungeonsguide.mod.events.impl.*;
import kr.syeyoung.dungeonsguide.mod.features.FeatureRegistry;
import kr.syeyoung.dungeonsguide.mod.features.impl.etc.FeatureCollectDiagnostics;
import kr.syeyoung.dungeonsguide.mod.gui.renderer.RenderingContext;
import kr.syeyoung.dungeonsguide.mod.utils.DungeonServerLaunchUtils;
import kr.syeyoung.dungeonsguide.mod.utils.MapUtils;
import kr.syeyoung.dungeonsguide.mod.utils.RenderUtils;
import kr.syeyoung.dungeonsguide.mod.utils.TextUtils;
import kr.syeyoung.modapi.ModAPI;
import kr.syeyoung.modapi.data.Vector3D;
import kr.syeyoung.modapi.entity.EntityType;
import kr.syeyoung.modapi.entity.UEntity;
import kr.syeyoung.modapi.entity.UPlayerSelf;
import kr.syeyoung.modapi.event.ListenerPriority;
import kr.syeyoung.modapi.event.events.*;
import kr.syeyoung.modapi.profiler.UProfiler;
import kr.syeyoung.modapi.rendering.UFontCalculator;
import kr.syeyoung.modapi.world.UChunk;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.awt.*;
import java.lang.ref.WeakReference;

public class DungeonListener {


    @kr.syeyoung.modapi.event.SubscribeEvent
    public void onWorldLoad(WorldUnloadEvent event) {
        Config.scheduleConfigSave();
        DungeonActionContext.getSpawnLocation().clear();
        DungeonActionContext.getKilleds().clear();
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

    @kr.syeyoung.modapi.event.SubscribeEvent
    public void onRender(OverlayRenderEvent postRender) {

        if (!SkyblockStatus.isOnDungeon()) return;
        UProfiler profiler = ModAPI.getAPI().getProfiler();

        DungeonContext context = DungeonsGuide.getDungeonsGuide().getDungeonFacade().getContext();
        if (context != null) {

            UPlayerSelf thePlayer = ModAPI.getAPI().getPlayer();

            RenderingContext context1 = new RenderingContext(postRender.getRenderContext());
            profiler.startSection("Dungeons Guide - RenderGameOverlay.Post :: Bossfight Processor");
            if (context.getBossfightProcessor() != null)
                context.getBossfightProcessor().drawScreen(postRender.getPartialTicks(),context1);

            profiler.endStartSection("Dungeons Guide - RenderGameOverlay.Post :: Room Processor");
            if (context.getScaffoldParser() != null) {
                Point roomPt = context.getScaffoldParser().getDungeonMapLayout().worldPointToRoomPoint(thePlayer.getPositionVector());
                DungeonRoom dungeonRoom = context.getScaffoldParser().getRoomMap().get(roomPt);
                if (dungeonRoom != null) {
                    if (dungeonRoom.getRoomProcessor() != null) {
                        dungeonRoom.getRoomProcessor().drawScreen(postRender.getPartialTicks(), context1);
                    }
                }
            }
            profiler.endSection();

            if (context.getDungeonName().equals("TEST DG")) {
                int swidth = (int) (ModAPI.getAPI().getDisplayWidth() / ModAPI.getAPI().getScaleFactor());
                int sheight = (int) (ModAPI.getAPI().getDisplayHeight() / ModAPI.getAPI().getScaleFactor());

                UFontCalculator fr = ModAPI.getAPI().getFontCalculator();

                int width = fr.getStringWidth("Dungeons Guide Mockup Dungeon");
                int width2 = fr.getStringWidth("Preset: "+context.getPreset().getPresetName());
                int bigger = Math.max(width, width2);
                context1.drawRect(
                        (swidth-bigger - 10)/2,
                        (sheight/9 - 5) ,
                        (swidth + bigger + 10) /2,
                        (sheight/9) + 5 + fr.getFontHeight()*2,
                        0x77111111
                );

                context1.drawString("Dungeons Guide Mockup Dungeon", (swidth-width)/2, sheight/9, 0xFF00FF00);

                context1.drawString("Preset: "+context.getPreset().getPresetName(), (swidth - width2) / 2, sheight / 9 + fr.getFontHeight(), 0xFF00FF00);
            }
        }
    }

    @kr.syeyoung.modapi.event.SubscribeEvent
    public  void onMapUpdate(MapUpdateEvent mapUpdateEvent) {
        if (!SkyblockStatus.isOnDungeon()) return;

        DungeonContext context = DungeonsGuide.getDungeonsGuide().getDungeonFacade().getContext();

        if (context != null) {
            context.onMapUpdate(mapUpdateEvent);
        }
    }

    @kr.syeyoung.modapi.event.SubscribeEvent(receiveCanceled = true, priority = ListenerPriority.FIRST)
    public void onDGChatReceived(DGChatReceivedEvent receivedEvent) {
        if (!SkyblockStatus.isOnDungeon()) return;

        String format = receivedEvent.getOriginalFormattedText();
        if (TextUtils.contains(format, "§6> §e§lEXTRA STATS §6<")) {
            ModAPI.getAPI().getEventBus().fireEvent(new DungeonEndedEvent());
        }

        DungeonContext context = DungeonsGuide.getDungeonsGuide().getDungeonFacade().getContext();
        if (context != null) {
            UPlayerSelf thePlayer = ModAPI.getAPI().getPlayer();
            context.onChat(receivedEvent);

            if (context.getBossfightProcessor() != null) {
                context.getBossfightProcessor().chatReceived(receivedEvent);
            }
            if (context.getScaffoldParser() != null) {
                Point roomPt = context.getScaffoldParser().getDungeonMapLayout().worldPointToRoomPoint(thePlayer.getPositionVector());


                RoomProcessor roomProcessor = null;
                DungeonRoom dungeonRoom = context.getScaffoldParser().getRoomMap().get(roomPt);
                if (dungeonRoom != null) {
                    if (dungeonRoom.getRoomProcessor() != null) {
                        dungeonRoom.getRoomProcessor().chatReceived(receivedEvent);
                        roomProcessor = dungeonRoom.getRoomProcessor();
                    }
                }

                for (RoomProcessor globalRoomProcessor : context.getGlobalRoomProcessors()) {
                    if (globalRoomProcessor != roomProcessor) {
                        globalRoomProcessor.chatReceived(receivedEvent);
                    }
                }
            }
        }
    }

    @kr.syeyoung.modapi.event.SubscribeEvent(receiveCanceled = true, priority = ListenerPriority.FIRST)
    public void onActionBarReceived(ActionBarReceivedEvent receivedEvent) {
        if (!SkyblockStatus.isOnDungeon()) return;

        String format = TextUtils.getNearestFormattedText(receivedEvent.chat);
        if (format.contains("§6> §e§lEXTRA STATS §6<")) {
            ModAPI.getAPI().getEventBus().fireEvent(new DungeonEndedEvent());
        }

        DungeonContext context = DungeonsGuide.getDungeonsGuide().getDungeonFacade().getContext();

        if (context != null) {

            UPlayerSelf thePlayer = ModAPI.getAPI().getPlayer();

            if (context.getBossfightProcessor() != null) {
                context.getBossfightProcessor().actionbarReceived(receivedEvent);
            }
            if (context.getScaffoldParser() != null) {
                Point roomPt = context.getScaffoldParser().getDungeonMapLayout().worldPointToRoomPoint(thePlayer.getPositionVector());


                RoomProcessor roomProcessor = null;
                DungeonRoom dungeonRoom = context.getScaffoldParser().getRoomMap().get(roomPt);
                if (dungeonRoom != null) {
                    if (dungeonRoom.getRoomProcessor() != null) {
                        dungeonRoom.getRoomProcessor().actionbarReceived(receivedEvent);
                        roomProcessor = dungeonRoom.getRoomProcessor();
                    }
                }
            }
        }
    }



    @SubscribeEvent
    public void onWorldRender(RenderWorldLastEvent renderWorldLastEvent) {
        if (!SkyblockStatus.isOnDungeon()) return;
        try {

            UProfiler profiler = ModAPI.getAPI().getProfiler();

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
            }


            profiler.endStartSection("Dungeons Guide - RenderWorldLast :: Room Edit");
//            if (EditingContext.getEditingContext() != null) { $$ ROOMEDIT
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
//            EditingContext ec = EditingContext.getEditingContext(); $$ ROOMEDIT
//            if (ec == null) {
//                DungeonContext context = DungeonsGuide.getDungeonsGuide().getDungeonFacade().getContext();
//                if (context == null) {
//                    ChatTransmitter.addToQueue("Not in dungeons");
//                    return;
//                }
//                UPlayerSelf thePlayer = ModAPI.getAPI().getPlayer();
//                if (context.getScaffoldParser() != null) {
//                    Point roomPt = context.getScaffoldParser().getDungeonMapLayout().worldPointToRoomPoint(thePlayer.getPositionVector());
//                    DungeonRoom dungeonRoom = context.getScaffoldParser().getRoomMap().get(roomPt);
//
//                    if (dungeonRoom == null) {
//                        ChatTransmitter.addToQueue("Can't determine the dungeon room you're in");
//                        return;
//                    }
//
//                    if (EditingContext.getEditingContext() != null) {
//                        ChatTransmitter.addToQueue("There is an editing session currently open.");
//                        return;
//                    }
//
//                    EditingContext.createEditingContext(dungeonRoom);
//                    EditingContext.getEditingContext().openGui(new GuiDungeonRoomEdit(dungeonRoom));
//                }
//            } else ec.reopen();
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
