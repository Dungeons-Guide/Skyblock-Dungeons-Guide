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

import kr.syeyoung.dungeonsguide.DungeonsGuide;
import kr.syeyoung.dungeonsguide.SkyblockStatus;
import kr.syeyoung.dungeonsguide.config.Config;
import kr.syeyoung.dungeonsguide.dungeon.DungeonActionManager;
import kr.syeyoung.dungeonsguide.dungeon.DungeonContext;
import kr.syeyoung.dungeonsguide.dungeon.doorfinder.DungeonDoor;
import kr.syeyoung.dungeonsguide.dungeon.roomedit.EditingContext;
import kr.syeyoung.dungeonsguide.dungeon.roomedit.gui.GuiDungeonAddSet;
import kr.syeyoung.dungeonsguide.dungeon.roomedit.gui.GuiDungeonParameterEdit;
import kr.syeyoung.dungeonsguide.dungeon.roomedit.gui.GuiDungeonRoomEdit;
import kr.syeyoung.dungeonsguide.dungeon.roomedit.gui.GuiDungeonValueEdit;
import kr.syeyoung.dungeonsguide.dungeon.roomedit.valueedit.ValueEdit;
import kr.syeyoung.dungeonsguide.dungeon.roomfinder.DungeonRoom;
import kr.syeyoung.dungeonsguide.dungeon.roomprocessor.RoomProcessor;
import kr.syeyoung.dungeonsguide.events.impl.*;
import kr.syeyoung.dungeonsguide.features.FeatureRegistry;
import kr.syeyoung.dungeonsguide.utils.MapUtils;
import kr.syeyoung.dungeonsguide.utils.RenderUtils;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.passive.EntityBat;
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
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
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
        SkyblockStatus skyblockStatus = DungeonsGuide.getDungeonsGuide().getSkyblockStatus();

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
        GlStateManager.enableBlend();
        GlStateManager.color(1, 1, 1, 1);
        GL14.glBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GlStateManager.enableAlpha();
    }

    @SubscribeEvent
    public void onEntityUpdate(LivingEvent.LivingUpdateEvent e) {
        SkyblockStatus skyblockStatus = DungeonsGuide.getDungeonsGuide().getSkyblockStatus();

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
    }

    boolean wasOnHypixel = false;

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent ev) {
        if (ev.side == Side.SERVER || ev.phase != TickEvent.Phase.START) return;

        SkyblockStatus skyblockStatus = DungeonsGuide.getDungeonsGuide().getSkyblockStatus();
        boolean isOnDungeon = skyblockStatus.isOnDungeon();
        boolean isOnSkyblock = skyblockStatus.isOnSkyblock();
        skyblockStatus.updateStatus();

        if (!wasOnHypixel && skyblockStatus.isOnHypixel()) {
            MinecraftForge.EVENT_BUS.post(new HypixelJoinedEvent());
        }
        wasOnHypixel = skyblockStatus.isOnHypixel();

        if (isOnSkyblock && !skyblockStatus.isOnSkyblock()) {
            MinecraftForge.EVENT_BUS.post(new SkyblockLeftEvent());
        } else if (!isOnSkyblock && skyblockStatus.isOnSkyblock()) {
            MinecraftForge.EVENT_BUS.post(new SkyblockJoinedEvent());
        }

        if (isOnDungeon && !skyblockStatus.isOnDungeon()) {
            MinecraftForge.EVENT_BUS.post(new DungeonLeftEvent());
            skyblockStatus.setContext(null);
            if (!FeatureRegistry.ADVANCED_DEBUGGABLE_MAP.isEnabled()) {
                MapUtils.clearMap();
            }
            return;
        }
        if (isOnSkyblock) {
            if (skyblockStatus.getContext() != null) {
                skyblockStatus.getContext().tick();
            } else {
                if (skyblockStatus.isOnDungeon()) {
                    skyblockStatus.setContext(new DungeonContext(Minecraft.getMinecraft().thePlayer.worldObj));
                    MinecraftForge.EVENT_BUS.post(new DungeonStartedEvent());
                }
            }
        }


        if (skyblockStatus.isOnDungeon() && skyblockStatus.getContext() != null) {
            EntityPlayerSP thePlayer = Minecraft.getMinecraft().thePlayer;
            if (thePlayer == null) return;

            DungeonContext context = skyblockStatus.getContext();

            if (context.getBossfightProcessor() != null) {
                context.getBossfightProcessor().tick();
            }

            Point roomPt = context.getMapProcessor().worldPointToRoomPoint(thePlayer.getPosition());

            DungeonRoom dungeonRoom = context.getRoomMapper().get(roomPt);
            if (dungeonRoom != null && dungeonRoom.getRoomProcessor() != null) {
                dungeonRoom.getRoomProcessor().tick();
            }

        }

    }


    @SubscribeEvent
    public void onRender(RenderGameOverlayEvent.Post postRender) {
        if (!(postRender.type == RenderGameOverlayEvent.ElementType.EXPERIENCE || postRender.type == RenderGameOverlayEvent.ElementType.JUMPBAR))
            return;

        SkyblockStatus skyblockStatus = DungeonsGuide.getDungeonsGuide().getSkyblockStatus();
        if (!skyblockStatus.isOnDungeon()) return;

        if (skyblockStatus.getContext() != null) {
            DungeonContext context = skyblockStatus.getContext();
            EntityPlayerSP thePlayer = Minecraft.getMinecraft().thePlayer;
            Point roomPt = context.getMapProcessor().worldPointToRoomPoint(thePlayer.getPosition());

            if (context.getBossfightProcessor() != null)
                context.getBossfightProcessor().drawScreen(postRender.partialTicks);
            DungeonRoom dungeonRoom = context.getRoomMapper().get(roomPt);
            if (dungeonRoom != null) {
                if (dungeonRoom.getRoomProcessor() != null) {
                    dungeonRoom.getRoomProcessor().drawScreen(postRender.partialTicks);
                }
            }

        }
        GlStateManager.enableBlend();
        GlStateManager.color(1, 1, 1, 1);
        GL14.glBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ONE_MINUS_SRC_ALPHA);
        Minecraft.getMinecraft().entityRenderer.setupOverlayRendering();
        GlStateManager.enableAlpha();
    }

    @SubscribeEvent(receiveCanceled = true, priority = EventPriority.HIGHEST)
    public void onChatReceived(ClientChatReceivedEvent clientChatReceivedEvent) {
        SkyblockStatus skyblockStatus = DungeonsGuide.getDungeonsGuide().getSkyblockStatus();
        if (!skyblockStatus.isOnDungeon()) return;

        if (clientChatReceivedEvent.type != 2 && clientChatReceivedEvent.message.getFormattedText().contains("§6> §e§lEXTRA STATS §6<")) {
            MinecraftForge.EVENT_BUS.post(new DungeonEndedEvent());
        }

        DungeonContext context = skyblockStatus.getContext();

        if (skyblockStatus.getContext() != null) {
            EntityPlayerSP thePlayer = Minecraft.getMinecraft().thePlayer;
            Point roomPt = context.getMapProcessor().worldPointToRoomPoint(thePlayer.getPosition());

            context.onChat(clientChatReceivedEvent);

            if (context.getBossfightProcessor() != null) {
                if (clientChatReceivedEvent.type == 2)
                    context.getBossfightProcessor().actionbarReceived(clientChatReceivedEvent.message);
                else
                    context.getBossfightProcessor().chatReceived(clientChatReceivedEvent.message);
            }
            RoomProcessor roomProcessor = null;
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
            if (clientChatReceivedEvent.type == 2) return;
            for (RoomProcessor globalRoomProcessor : context.getGlobalRoomProcessors()) {
                if (globalRoomProcessor == roomProcessor) continue;
                globalRoomProcessor.chatReceived(clientChatReceivedEvent.message);
            }
        }
    }


    @SubscribeEvent
    public void onWorldRender(RenderWorldLastEvent renderWorldLastEvent) {
        SkyblockStatus skyblockStatus = DungeonsGuide.getDungeonsGuide().getSkyblockStatus();
        if (!skyblockStatus.isOnDungeon()) return;

        DungeonContext context = skyblockStatus.getContext();
        if (context == null) return;
        if (FeatureRegistry.DEBUG.isEnabled()) {
            for (DungeonRoom dungeonRoom : context.getDungeonRoomList()) {
                for (DungeonDoor door : dungeonRoom.getDoors()) {
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

            if (FeatureRegistry.DEBUG.isEnabled() && dungeonRoom != null) {

                Vec3 player = Minecraft.getMinecraft().thePlayer.getPositionVector();
                BlockPos real = new BlockPos(player.xCoord * 2, player.yCoord * 2, player.zCoord * 2);
                for (BlockPos allInBox : BlockPos.getAllInBox(real.add(-1, -1, -1), real.add(1, 1, 1))) {
                    boolean blocked = dungeonRoom.isBlocked(allInBox.getX(), allInBox.getY(), allInBox.getZ());

                    RenderUtils.highlightBox(
                            AxisAlignedBB.fromBounds(
                                    allInBox.getX() / 2.0 - 0.1, allInBox.getY() / 2.0 - 0.1, allInBox.getZ() / 2.0 - 0.1,
                                    allInBox.getX() / 2.0 + 0.1, allInBox.getY() / 2.0 + 0.1, allInBox.getZ() / 2.0 + 0.1
                            ), blocked ? new Color(0x55FF0000, true) : new Color(0x3300FF00, true), renderWorldLastEvent.partialTicks, false);

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
    }

    @SubscribeEvent()
    public void onKey2(KeyBindPressedEvent keyInputEvent) {
        SkyblockStatus skyblockStatus = DungeonsGuide.getDungeonsGuide().getSkyblockStatus();
        if (!skyblockStatus.isOnDungeon()) return;

        DungeonContext context = skyblockStatus.getContext();

        if (skyblockStatus.getContext() != null) {
            EntityPlayerSP thePlayer = Minecraft.getMinecraft().thePlayer;
            Point roomPt = context.getMapProcessor().worldPointToRoomPoint(thePlayer.getPosition());

            if (context.getBossfightProcessor() != null) {
                context.getBossfightProcessor().onKeybindPress(keyInputEvent);
            }
            RoomProcessor roomProcessor = null;
            DungeonRoom dungeonRoom = context.getRoomMapper().get(roomPt);
            if (dungeonRoom != null) {
                if (dungeonRoom.getRoomProcessor() != null) {
                    dungeonRoom.getRoomProcessor().onKeybindPress(keyInputEvent);
                }
            }
        }
    }

    @SubscribeEvent()
    public void onInteract(PlayerInteractEntityEvent interact) {
        SkyblockStatus skyblockStatus = DungeonsGuide.getDungeonsGuide().getSkyblockStatus();
        if (!skyblockStatus.isOnDungeon()) return;

        DungeonContext context = skyblockStatus.getContext();

        if (skyblockStatus.getContext() != null) {
            EntityPlayerSP thePlayer = Minecraft.getMinecraft().thePlayer;
            Point roomPt = context.getMapProcessor().worldPointToRoomPoint(thePlayer.getPosition());

            if (context.getBossfightProcessor() != null) {
                context.getBossfightProcessor().onInteract(interact);
            }
            RoomProcessor roomProcessor = null;
            DungeonRoom dungeonRoom = context.getRoomMapper().get(roomPt);
            if (dungeonRoom != null) {
                if (dungeonRoom.getRoomProcessor() != null) {
                    dungeonRoom.getRoomProcessor().onInteract(interact);
                }
            }
        }
    }

    @SubscribeEvent()
    public void onBlockChange(BlockUpdateEvent.Post postInteract) {
        SkyblockStatus skyblockStatus = DungeonsGuide.getDungeonsGuide().getSkyblockStatus();
        if (!skyblockStatus.isOnDungeon()) return;

        DungeonContext context = skyblockStatus.getContext();

        if (skyblockStatus.getContext() != null) {
            EntityPlayerSP thePlayer = Minecraft.getMinecraft().thePlayer;
            Point roomPt = context.getMapProcessor().worldPointToRoomPoint(thePlayer.getPosition());

            if (context.getBossfightProcessor() != null) {
                context.getBossfightProcessor().onBlockUpdate(postInteract);
            }
            RoomProcessor roomProcessor = null;
            DungeonRoom dungeonRoom = context.getRoomMapper().get(roomPt);
            if (dungeonRoom != null) {
                if (dungeonRoom.getRoomProcessor() != null) {
                    dungeonRoom.getRoomProcessor().onBlockUpdate(postInteract);
                }
            }
        }
    }

    @SubscribeEvent
    public void onKeyInput(KeyBindPressedEvent keyInputEvent) {
        if (FeatureRegistry.DEBUG.isEnabled() && FeatureRegistry.ADVANCED_ROOMEDIT.isEnabled() && keyInputEvent.getKey() == FeatureRegistry.ADVANCED_ROOMEDIT.<Integer>getParameter("key").getValue()) {
            EditingContext ec = EditingContext.getEditingContext();
            if (ec == null) {
                DungeonContext context = DungeonsGuide.getDungeonsGuide().getSkyblockStatus().getContext();
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
        if (!keyInputEvent.world.isRemote) return;
        SkyblockStatus skyblockStatus = DungeonsGuide.getDungeonsGuide().getSkyblockStatus();
        if (!skyblockStatus.isOnDungeon()) return;

        DungeonContext context = skyblockStatus.getContext();

        if (skyblockStatus.getContext() != null) {
            EntityPlayerSP thePlayer = Minecraft.getMinecraft().thePlayer;
            Point roomPt = context.getMapProcessor().worldPointToRoomPoint(thePlayer.getPosition());

            if (context.getBossfightProcessor() != null) {
                context.getBossfightProcessor().onInteractBlock(keyInputEvent);
            }
            RoomProcessor roomProcessor = null;
            DungeonRoom dungeonRoom = context.getRoomMapper().get(roomPt);
            if (dungeonRoom != null) {
                if (dungeonRoom.getRoomProcessor() != null) {
                    dungeonRoom.getRoomProcessor().onInteractBlock(keyInputEvent);
                }
            }
        }
    }

    @Getter
    private final Map<Integer, Vec3> entityIdToPosMap = new HashMap<Integer, Vec3>();

    @SubscribeEvent
    public void onEntitySpawn(EntityJoinWorldEvent spawn) {
        DungeonActionManager.getSpawnLocation().put(spawn.entity.getEntityId(), new Vec3(spawn.entity.posX, spawn.entity.posY, spawn.entity.posZ));
    }


    @SubscribeEvent
    public void onEntityDeSpawn(LivingDeathEvent deathEvent) {
        if (deathEvent.entityLiving instanceof EntityBat)
            DungeonActionManager.getKilleds().add(deathEvent.entity.getEntityId());

        SkyblockStatus skyblockStatus = DungeonsGuide.getDungeonsGuide().getSkyblockStatus();
        if (!skyblockStatus.isOnDungeon()) return;

        DungeonContext context = skyblockStatus.getContext();

        if (skyblockStatus.getContext() != null) {
            EntityPlayerSP thePlayer = Minecraft.getMinecraft().thePlayer;
            Point roomPt = context.getMapProcessor().worldPointToRoomPoint(thePlayer.getPosition());

            if (context.getBossfightProcessor() != null) {
                context.getBossfightProcessor().onEntityDeath(deathEvent);
            }
            RoomProcessor roomProcessor = null;
            DungeonRoom dungeonRoom = context.getRoomMapper().get(roomPt);
            if (dungeonRoom != null) {
                if (dungeonRoom.getRoomProcessor() != null) {
                    dungeonRoom.getRoomProcessor().onEntityDeath(deathEvent);
                }
            }
        }

        if (!(deathEvent.entityLiving instanceof EntityBat))
            DungeonActionManager.getSpawnLocation().remove(deathEvent.entity.getEntityId());
    }

}
