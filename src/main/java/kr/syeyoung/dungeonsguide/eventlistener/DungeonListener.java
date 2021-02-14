package kr.syeyoung.dungeonsguide.eventlistener;

import com.google.gson.JsonObject;
import kr.syeyoung.dungeonsguide.config.Config;
import kr.syeyoung.dungeonsguide.Keybinds;
import kr.syeyoung.dungeonsguide.SkyblockStatus;
import kr.syeyoung.dungeonsguide.dungeon.DungeonContext;
import kr.syeyoung.dungeonsguide.dungeon.DungeonActionManager;
import kr.syeyoung.dungeonsguide.dungeon.doorfinder.DungeonDoor;
import kr.syeyoung.dungeonsguide.dungeon.roomfinder.DungeonRoom;
import kr.syeyoung.dungeonsguide.e;
import kr.syeyoung.dungeonsguide.events.*;
import kr.syeyoung.dungeonsguide.features.FeatureRegistry;
import kr.syeyoung.dungeonsguide.roomedit.EditingContext;
import kr.syeyoung.dungeonsguide.roomedit.gui.GuiDungeonAddSet;
import kr.syeyoung.dungeonsguide.roomedit.gui.GuiDungeonParameterEdit;
import kr.syeyoung.dungeonsguide.roomedit.gui.GuiDungeonRoomEdit;
import kr.syeyoung.dungeonsguide.roomedit.gui.GuiDungeonValueEdit;
import kr.syeyoung.dungeonsguide.roomedit.valueedit.ValueEdit;
import kr.syeyoung.dungeonsguide.roomprocessor.RoomProcessor;
import kr.syeyoung.dungeonsguide.utils.MapUtils;
import kr.syeyoung.dungeonsguide.utils.RenderUtils;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiErrorScreen;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.passive.EntityBat;
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
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.client.CustomModLoadingErrorDisplayException;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.awt.*;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class DungeonListener {
    @SubscribeEvent
    public void onWorldLoad(WorldEvent.Unload event) {
        try {
            Config.saveConfig();
        } catch (IOException e) {
            e.printStackTrace();
        }
        DungeonActionManager.getSpawnLocation().clear();
        DungeonActionManager.getKilleds().clear();
    }

    @SubscribeEvent
    public void onPostDraw(GuiScreenEvent.DrawScreenEvent.Post e) {
        try {
                SkyblockStatus skyblockStatus = (SkyblockStatus) kr.syeyoung.dungeonsguide.e.getDungeonsGuide().getSkyblockStatus();

                if (!skyblockStatus.isOnDungeon()) return;

                if (skyblockStatus.getContext() != null) {
                    DungeonContext context = skyblockStatus.getContext();
                    EntityPlayerSP thePlayer = Minecraft.getMinecraft().thePlayer;
                    if (thePlayer == null) return;
                    if (context.getBossfightProcessor() != null) context.getBossfightProcessor().onPostGuiRender(e);
                    Point roomPt = context.getMapProcessor().worldPointToRoomPoint(thePlayer.getPosition());

                    DungeonRoom dungeonRoom = context.getRoomMapper().get(roomPt);
                    if (dungeonRoom != null && dungeonRoom.getRoomProcessor() != null) {
                        dungeonRoom.getRoomProcessor().onPostGuiRender(e);
                    }
                }
        } catch (Throwable e2) {e2.printStackTrace();}
    }
    @SubscribeEvent
    public void onEntityUpdate(LivingEvent.LivingUpdateEvent e) {
        try {
            SkyblockStatus skyblockStatus = (SkyblockStatus) kr.syeyoung.dungeonsguide.e.getDungeonsGuide().getSkyblockStatus();

            if (!skyblockStatus.isOnDungeon()) return;

            if (skyblockStatus.getContext() != null) {
                DungeonContext context = skyblockStatus.getContext();
                EntityPlayerSP thePlayer = Minecraft.getMinecraft().thePlayer;
                if (thePlayer == null) return;
                if (context.getBossfightProcessor() != null) context.getBossfightProcessor().onEntityUpdate(e);
                Point roomPt = context.getMapProcessor().worldPointToRoomPoint(thePlayer.getPosition());

                DungeonRoom dungeonRoom = context.getRoomMapper().get(roomPt);
                if (dungeonRoom != null && dungeonRoom.getRoomProcessor() != null) {
                    dungeonRoom.getRoomProcessor().onEntityUpdate(e);
                }
            }
        } catch (Throwable e2) {e2.printStackTrace();}
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent ev) throws Throwable {
        try {
            if (ev.phase == TickEvent.Phase.START) {


                JsonObject obj = e.getDungeonsGuide().getAuthenticator().a(e.getDungeonsGuide().getAuthenticator().c());
                if (!obj.get("uuid").getAsString().equals(Minecraft.getMinecraft().getSession().getProfile().getId().toString())) {
                    if (Minecraft.getMinecraft().currentScreen instanceof GuiErrorScreen) return;

                    final String[] a = new String[]{
                            "User has changed current Minecraft session.",
                            "Please restart mc to revalidate Dungeons Guide"
                    };
                    final GuiScreen b = new GuiErrorScreen(null, null) {
                        @Override
                        public void drawScreen(int par1, int par2, float par3) {
                            super.drawScreen(par1, par2, par3);
                            for (int i = 0; i < a.length; ++i) {
                                drawCenteredString(fontRendererObj, a[i], width / 2, height / 3 + 12 * i, 0xFFFFFFFF);
                            }
                        }

                        @Override
                        public void initGui() {
                            super.initGui();
                            this.buttonList.clear();
                            this.buttonList.add(new GuiButton(0, width / 2 - 50, height - 50, 100,20, "close"));
                        }

                        @Override
                        protected void actionPerformed(GuiButton button) throws IOException {
                            System.exit(-1);
                        }
                    };
                    Minecraft.getMinecraft().displayGuiScreen(b);
                    return;
                }



                SkyblockStatus skyblockStatus = (SkyblockStatus) kr.syeyoung.dungeonsguide.e.getDungeonsGuide().getSkyblockStatus();
                 {
                    boolean isOnDungeon = skyblockStatus.isOnDungeon();
                    boolean isOnSkyblock = skyblockStatus.isOnSkyblock();
                    skyblockStatus.updateStatus();

                    if (isOnSkyblock && !skyblockStatus.isOnSkyblock()) {
                        MinecraftForge.EVENT_BUS.post(new SkyblockLeftEvent());
                    } else if (!isOnSkyblock && skyblockStatus.isOnSkyblock()) {
                        MinecraftForge.EVENT_BUS.post(new SkyblockJoinedEvent());
                    }

                    if ((isOnDungeon && !skyblockStatus.isOnDungeon())) {
                        MinecraftForge.EVENT_BUS.post(new DungeonLeftEvent());
                        skyblockStatus.setContext(null);
                        MapUtils.clearMap();
                        return;
                    }
                    if (isOnSkyblock) {
                        if (skyblockStatus.getContext() != null) {
                            skyblockStatus.getContext().tick();
                        } else if (skyblockStatus.isOnDungeon()){
                            skyblockStatus.setContext(new DungeonContext(Minecraft.getMinecraft().thePlayer.worldObj));
                            MinecraftForge.EVENT_BUS.post(new DungeonStartedEvent());
                        }
                    }
                }

                if (!skyblockStatus.isOnDungeon()) return;

                if (skyblockStatus.getContext() != null) {
                    DungeonContext context = skyblockStatus.getContext();
                    EntityPlayerSP thePlayer = Minecraft.getMinecraft().thePlayer;
                    if (thePlayer == null) return;
                    if (context.getBossfightProcessor() != null) context.getBossfightProcessor().tick();
                    Point roomPt = context.getMapProcessor().worldPointToRoomPoint(thePlayer.getPosition());

                    DungeonRoom dungeonRoom = context.getRoomMapper().get(roomPt);
                    if (dungeonRoom != null && dungeonRoom.getRoomProcessor() != null) {
                            dungeonRoom.getRoomProcessor().tick();
                    }
                }
            }
        } catch (Throwable e2) {
            if (e2 instanceof CustomModLoadingErrorDisplayException) throw e2;
            e2.printStackTrace();
        }
    }

    @SubscribeEvent
    public void onRender(RenderGameOverlayEvent.Post postRender) {
        try {
            if (postRender.type != RenderGameOverlayEvent.ElementType.TEXT) return;

            JsonObject obj = e.getDungeonsGuide().getAuthenticator().a(e.getDungeonsGuide().getAuthenticator().c());
            FontRenderer fr = Minecraft.getMinecraft().fontRendererObj;
            if (obj.get("plan").getAsString().equalsIgnoreCase("TRIAL")) {
                fr.drawString("Using trial Version of Dungeons Guide", 0,0, 0xFFFFFFFF);
                fr.drawString("Trial version bound to: "+obj.get("nickname").getAsString(), 0,10, 0xFFFFFFFF);
            }


            SkyblockStatus skyblockStatus = (SkyblockStatus) e.getDungeonsGuide().getSkyblockStatus();
            if (!skyblockStatus.isOnDungeon()) return;

            if (skyblockStatus.getContext() != null) {
                DungeonContext context = skyblockStatus.getContext();
                EntityPlayerSP thePlayer = Minecraft.getMinecraft().thePlayer;
                Point roomPt = context.getMapProcessor().worldPointToRoomPoint(thePlayer.getPosition());

                if (context.getBossfightProcessor() != null) context.getBossfightProcessor().drawScreen(postRender.partialTicks);
                DungeonRoom dungeonRoom = context.getRoomMapper().get(roomPt);
                if (dungeonRoom != null) {
                    if (dungeonRoom.getRoomProcessor() != null) {
                            dungeonRoom.getRoomProcessor().drawScreen(postRender.partialTicks);
                    }
                }

            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    @SubscribeEvent(receiveCanceled = true, priority = EventPriority.HIGHEST)
    public void onChatReceived(ClientChatReceivedEvent clientChatReceivedEvent) {
        try {
            SkyblockStatus skyblockStatus = (SkyblockStatus) e.getDungeonsGuide().getSkyblockStatus();
            if (!skyblockStatus.isOnDungeon()) return;

            if (clientChatReceivedEvent.type != 2 && clientChatReceivedEvent.message.getFormattedText().contains("§6> §e§lEXTRA STATS §6<")) {
                MinecraftForge.EVENT_BUS.post(new DungeonEndedEvent());
            }

            DungeonContext context = skyblockStatus.getContext();

            if (skyblockStatus.getContext() != null) {
                EntityPlayerSP thePlayer = Minecraft.getMinecraft().thePlayer;
                Point roomPt = context.getMapProcessor().worldPointToRoomPoint(thePlayer.getPosition());

                try {
                    context.onChat(clientChatReceivedEvent);
                } catch (Throwable t) {
                    t.printStackTrace();
                }

                if (context.getBossfightProcessor() != null) {
                    if (clientChatReceivedEvent.type == 2)
                        context.getBossfightProcessor().actionbarReceived(clientChatReceivedEvent.message);
                    else
                        context.getBossfightProcessor().chatReceived(clientChatReceivedEvent.message);
                }
                RoomProcessor roomProcessor = null;
                try {
                    DungeonRoom dungeonRoom = context.getRoomMapper().get(roomPt);
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
                } catch (Throwable t) {
                    t.printStackTrace();
                }
                if (clientChatReceivedEvent.type == 2) return;
                for (RoomProcessor globalRoomProcessor : context.getGlobalRoomProcessors()) {
                    if (globalRoomProcessor == roomProcessor) continue;;
                    try {
                        globalRoomProcessor.chatReceived(clientChatReceivedEvent.message);
                    } catch (Throwable t) {
                        t.printStackTrace();
                    }
                }
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }


    @SubscribeEvent
    public void onWorldRender(RenderWorldLastEvent renderWorldLastEvent) {
        try {
            SkyblockStatus skyblockStatus = (SkyblockStatus) e.getDungeonsGuide().getSkyblockStatus();
            if (!skyblockStatus.isOnDungeon()) return;

            DungeonContext context = skyblockStatus.getContext();
            if (context == null) return;
            if (FeatureRegistry.DEBUG.isEnabled()) {
                for (DungeonRoom dungeonRoom : context.getDungeonRoomList()) {
                    for(DungeonDoor door : dungeonRoom.getDoors()) {
                        RenderUtils.renderDoor(door, renderWorldLastEvent.partialTicks);
                    }
                }
            }


            if (skyblockStatus.getContext() != null) {

                if (context.getBossfightProcessor() != null) {
                        context.getBossfightProcessor().drawWorld(renderWorldLastEvent.partialTicks);
                }

                EntityPlayerSP thePlayer = Minecraft.getMinecraft().thePlayer;
                Point roomPt = context.getMapProcessor().worldPointToRoomPoint(thePlayer.getPosition());

                DungeonRoom dungeonRoom = context.getRoomMapper().get(roomPt);
                FontRenderer fontRenderer = Minecraft.getMinecraft().fontRendererObj;
                if (dungeonRoom != null) {
                    if (dungeonRoom.getRoomProcessor() != null) {
                            dungeonRoom.getRoomProcessor().drawWorld(renderWorldLastEvent.partialTicks);
                    }
                }

            }

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
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }
    @SubscribeEvent()
    public void onKey2(InputEvent.KeyInputEvent keyInputEvent) {
        try {
            SkyblockStatus skyblockStatus = (SkyblockStatus) e.getDungeonsGuide().getSkyblockStatus();
            if (!skyblockStatus.isOnDungeon()) return;

            DungeonContext context = skyblockStatus.getContext();

            if (skyblockStatus.getContext() != null) {
                EntityPlayerSP thePlayer = Minecraft.getMinecraft().thePlayer;
                Point roomPt = context.getMapProcessor().worldPointToRoomPoint(thePlayer.getPosition());

                if (context.getBossfightProcessor() != null) {
                    context.getBossfightProcessor().onKeyPress(keyInputEvent);
                }
                RoomProcessor roomProcessor = null;
                try {
                    DungeonRoom dungeonRoom = context.getRoomMapper().get(roomPt);
                    if (dungeonRoom != null) {
                        if (dungeonRoom.getRoomProcessor() != null) {
                            dungeonRoom.getRoomProcessor().onKeyPress(keyInputEvent);
                        }
                    }
                } catch (Throwable t) {
                    t.printStackTrace();
                }
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }
    @SubscribeEvent()
    public void onInteract(PlayerInteractEntityEvent interact) {
        try {
            SkyblockStatus skyblockStatus = (SkyblockStatus) e.getDungeonsGuide().getSkyblockStatus();
            if (!skyblockStatus.isOnDungeon()) return;

            DungeonContext context = skyblockStatus.getContext();

            if (skyblockStatus.getContext() != null) {
                EntityPlayerSP thePlayer = Minecraft.getMinecraft().thePlayer;
                Point roomPt = context.getMapProcessor().worldPointToRoomPoint(thePlayer.getPosition());

                if (context.getBossfightProcessor() != null) {
                    context.getBossfightProcessor().onInteract(interact);
                }
                RoomProcessor roomProcessor = null;
                try {
                    DungeonRoom dungeonRoom = context.getRoomMapper().get(roomPt);
                    if (dungeonRoom != null) {
                        if (dungeonRoom.getRoomProcessor() != null) {
                            dungeonRoom.getRoomProcessor().onInteract(interact);
                        }
                    }
                } catch (Throwable t) {
                    t.printStackTrace();
                }
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    @SubscribeEvent
    public void onKeyInput(InputEvent.KeyInputEvent keyInputEvent) {
        if (FeatureRegistry.ADVANCED_ROOMEDIT.isEnabled() && Keybinds.editingSession.isKeyDown() ){
            EditingContext ec = EditingContext.getEditingContext();
            if (ec == null) {
                DungeonContext context = e.getDungeonsGuide().getSkyblockStatus().getContext();
                if (context == null) {
                    Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText("Not in dungeons"));
                    return;
                }
                EntityPlayerSP thePlayer = Minecraft.getMinecraft().thePlayer;
                Point roomPt = context.getMapProcessor().worldPointToRoomPoint(thePlayer.getPosition());
                DungeonRoom dungeonRoom = context.getRoomMapper().get(roomPt);

                if (dungeonRoom == null) {
                    Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText("Can't determine the dungeon room you're in"));
                    return;
                }

                if (EditingContext.getEditingContext() != null) {
                    Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText("There is an editing session currently open."));
                    return;
                }

                EditingContext.createEditingContext(dungeonRoom);
                EditingContext.getEditingContext().openGui(new GuiDungeonRoomEdit(dungeonRoom));
            } else ec.reopen();
        }
    }
    @SubscribeEvent
    public void onInteract(PlayerInteractEvent keyInputEvent) {
        try {
            SkyblockStatus skyblockStatus = (SkyblockStatus) e.getDungeonsGuide().getSkyblockStatus();
            if (!skyblockStatus.isOnDungeon()) return;

            DungeonContext context = skyblockStatus.getContext();

            if (skyblockStatus.getContext() != null) {
                EntityPlayerSP thePlayer = Minecraft.getMinecraft().thePlayer;
                Point roomPt = context.getMapProcessor().worldPointToRoomPoint(thePlayer.getPosition());

                if (context.getBossfightProcessor() != null) {
                    context.getBossfightProcessor().onInteractBlock(keyInputEvent);
                }
                RoomProcessor roomProcessor = null;
                try {
                    DungeonRoom dungeonRoom = context.getRoomMapper().get(roomPt);
                    if (dungeonRoom != null) {
                        if (dungeonRoom.getRoomProcessor() != null) {
                            dungeonRoom.getRoomProcessor().onInteractBlock(keyInputEvent);
                        }
                    }
                } catch (Throwable t) {
                    t.printStackTrace();
                }
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    @Getter
    private Map<Integer, Vec3> entityIdToPosMap = new HashMap<Integer, Vec3>();
    @SubscribeEvent
    public void onEntitySpawn(EntityJoinWorldEvent spawn) {
        DungeonActionManager.getSpawnLocation().put(spawn.entity.getEntityId(), new Vec3(spawn.entity.posX, spawn.entity.posY, spawn.entity.posZ));
    }


    @SubscribeEvent
    public void onEntityDeSpawn(LivingDeathEvent deathEvent) {
        if (deathEvent.entityLiving instanceof EntityBat)
            DungeonActionManager.getKilleds().add(deathEvent.entity.getEntityId());

        try {
            SkyblockStatus skyblockStatus = (SkyblockStatus) e.getDungeonsGuide().getSkyblockStatus();
            if (!skyblockStatus.isOnDungeon()) return;

            DungeonContext context = skyblockStatus.getContext();

            if (skyblockStatus.getContext() != null) {
                EntityPlayerSP thePlayer = Minecraft.getMinecraft().thePlayer;
                Point roomPt = context.getMapProcessor().worldPointToRoomPoint(thePlayer.getPosition());

                if (context.getBossfightProcessor() != null) {
                    context.getBossfightProcessor().onEntityDeath(deathEvent);
                }
                RoomProcessor roomProcessor = null;
                try {
                    DungeonRoom dungeonRoom = context.getRoomMapper().get(roomPt);
                    if (dungeonRoom != null) {
                        if (dungeonRoom.getRoomProcessor() != null) {
                            dungeonRoom.getRoomProcessor().onEntityDeath(deathEvent);
                        }
                    }
                } catch (Throwable t) {
                    t.printStackTrace();
                }
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }

        DungeonActionManager.getSpawnLocation().remove(deathEvent.entity.getEntityId());
    }

}
