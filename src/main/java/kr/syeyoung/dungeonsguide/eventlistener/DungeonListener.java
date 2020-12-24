package kr.syeyoung.dungeonsguide.eventlistener;

import kr.syeyoung.dungeonsguide.Config;
import kr.syeyoung.dungeonsguide.Keybinds;
import kr.syeyoung.dungeonsguide.SkyblockStatus;
import kr.syeyoung.dungeonsguide.dungeon.DungeonContext;
import kr.syeyoung.dungeonsguide.dungeon.doorfinder.DungeonDoor;
import kr.syeyoung.dungeonsguide.dungeon.roomfinder.DungeonRoom;
import kr.syeyoung.dungeonsguide.e;
import kr.syeyoung.dungeonsguide.roomedit.EditingContext;
import kr.syeyoung.dungeonsguide.roomedit.gui.GuiDungeonAddSet;
import kr.syeyoung.dungeonsguide.roomedit.gui.GuiDungeonParameterEdit;
import kr.syeyoung.dungeonsguide.roomedit.gui.GuiDungeonRoomEdit;
import kr.syeyoung.dungeonsguide.roomedit.gui.GuiDungeonValueEdit;
import kr.syeyoung.dungeonsguide.roomedit.valueedit.ValueEdit;
import kr.syeyoung.dungeonsguide.roomprocessor.RoomProcessor;
import kr.syeyoung.dungeonsguide.utils.MapUtils;
import kr.syeyoung.dungeonsguide.utils.RenderUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.awt.*;

public class DungeonListener {
    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent e) {
        try {
            if (e.phase == TickEvent.Phase.START) {
                SkyblockStatus skyblockStatus = (SkyblockStatus) kr.syeyoung.dungeonsguide.e.getDungeonsGuide().getSkyblockStatus();
                 {
                    boolean isOnDungeon = skyblockStatus.isOnDungeon();
                    skyblockStatus.updateStatus();
                    if (!skyblockStatus.isOnDungeon()) {
                        skyblockStatus.setContext(null);
                        MapUtils.clearMap();
                        return;
                    }
                            if (isOnDungeon) {
                                    skyblockStatus.getContext().tick();
                            }
                            else skyblockStatus.setContext(new DungeonContext(Minecraft.getMinecraft().thePlayer.worldObj));
                }

                if (!skyblockStatus.isOnDungeon()) return;

                if (skyblockStatus.getContext() != null) {
                    DungeonContext context = skyblockStatus.getContext();
                    EntityPlayerSP thePlayer = Minecraft.getMinecraft().thePlayer;
                    if (thePlayer == null) return;
                    Point roomPt = context.getMapProcessor().worldPointToRoomPoint(thePlayer.getPosition());

                    DungeonRoom dungeonRoom = context.getRoomMapper().get(roomPt);
                    if (dungeonRoom != null && dungeonRoom.getRoomProcessor() != null) {
                            dungeonRoom.getRoomProcessor().tick();
                    }
                }
            }
        } catch (Throwable e2) {e2.printStackTrace();}
    }

    DynamicTexture dynamicTexture = new DynamicTexture(128, 128);
    ResourceLocation location = Minecraft.getMinecraft().renderEngine.getDynamicTextureLocation("dungeons/map/", dynamicTexture);
    @SubscribeEvent
    public void onRender(RenderGameOverlayEvent.Post postRender) {
        try {
            if (postRender.type != RenderGameOverlayEvent.ElementType.TEXT) return;
            SkyblockStatus skyblockStatus = (SkyblockStatus) e.getDungeonsGuide().getSkyblockStatus();
            if (!skyblockStatus.isOnDungeon()) return;
            if (Config.DEBUG) {
                int[] textureData = dynamicTexture.getTextureData();
                MapUtils.getImage().getRGB(0, 0, 128, 128, textureData, 0, 128);
                dynamicTexture.updateDynamicTexture();
                Minecraft.getMinecraft().getTextureManager().bindTexture(location);
                GlStateManager.enableAlpha();
                GuiScreen.drawModalRectWithCustomSizedTexture(0, 0, 0, 0, 128, 128, 128, 128);
            }

            if (skyblockStatus.getContext() != null) {
                DungeonContext context = skyblockStatus.getContext();
                EntityPlayerSP thePlayer = Minecraft.getMinecraft().thePlayer;
                Point roomPt = context.getMapProcessor().worldPointToRoomPoint(thePlayer.getPosition());

                DungeonRoom dungeonRoom = context.getRoomMapper().get(roomPt);
                FontRenderer fontRenderer = Minecraft.getMinecraft().fontRendererObj;
                if (dungeonRoom == null) {
                    if (Config.DEBUG)
                        fontRenderer.drawString("Where are you?!", 5, 128, 0xFFFFFF);
                } else {
                    if (Config.DEBUG) {
                        fontRenderer.drawString("you're in the room... " + dungeonRoom.getColor() + " / " + dungeonRoom.getShape(), 5, 128, 0xFFFFFF);
                        fontRenderer.drawString("room uuid: " + dungeonRoom.getDungeonRoomInfo().getUuid() + (dungeonRoom.getDungeonRoomInfo().isRegistered() ? "" : " (not registered)"), 5, 138, 0xFFFFFF);
                        fontRenderer.drawString("room name: " + dungeonRoom.getDungeonRoomInfo().getName(), 5, 148, 0xFFFFFF);
                    }
                    if (dungeonRoom.getRoomProcessor() != null) {
                            dungeonRoom.getRoomProcessor().drawScreen(postRender.partialTicks);
                    }
                }

            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    @SubscribeEvent
    public void onChatReceived(ClientChatReceivedEvent clientChatReceivedEvent) {
        try {
            if (clientChatReceivedEvent.type == 2) return;
            SkyblockStatus skyblockStatus = (SkyblockStatus) e.getDungeonsGuide().getSkyblockStatus();
            if (!skyblockStatus.isOnDungeon()) return;

            DungeonContext context = skyblockStatus.getContext();

            if (skyblockStatus.getContext() != null) {
                EntityPlayerSP thePlayer = Minecraft.getMinecraft().thePlayer;
                Point roomPt = context.getMapProcessor().worldPointToRoomPoint(thePlayer.getPosition());

                RoomProcessor roomProcessor = null;
                try {
                    DungeonRoom dungeonRoom = context.getRoomMapper().get(roomPt);
                    if (dungeonRoom != null) {
                        if (dungeonRoom.getRoomProcessor() != null) {
                                dungeonRoom.getRoomProcessor().chatReceived(clientChatReceivedEvent.message);
                                roomProcessor = dungeonRoom.getRoomProcessor();
                        }
                    }
                } catch (Throwable t) {
                    t.printStackTrace();
                }
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
            if (Config.DEBUG) {
                for (DungeonRoom dungeonRoom : context.getDungeonRoomList()) {
                    for(DungeonDoor door : dungeonRoom.getDoors()) {
                        RenderUtils.renderDoor(door, renderWorldLastEvent.partialTicks);
                    }
                }
            }


            if (skyblockStatus.getContext() != null) {
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

    @SubscribeEvent
    public void onKeyInput(InputEvent.KeyInputEvent keyInputEvent) {
        if (Config.DEBUG && Keybinds.editingSession.isKeyDown() ){
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
}
