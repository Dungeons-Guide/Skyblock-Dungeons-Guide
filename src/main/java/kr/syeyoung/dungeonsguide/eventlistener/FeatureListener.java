package kr.syeyoung.dungeonsguide.eventlistener;

import kr.syeyoung.dungeonsguide.SkyblockStatus;
import kr.syeyoung.dungeonsguide.config.guiconfig.GuiGuiLocationConfig;
import kr.syeyoung.dungeonsguide.e;
import kr.syeyoung.dungeonsguide.events.*;
import kr.syeyoung.dungeonsguide.features.*;
import kr.syeyoung.dungeonsguide.features.AbstractFeature;
import kr.syeyoung.dungeonsguide.features.listener.*;
import kr.syeyoung.dungeonsguide.utils.RenderUtils;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.util.*;
import net.minecraftforge.client.event.*;
import net.minecraftforge.client.event.sound.PlaySoundEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

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
            {

                if (Minecraft.getMinecraft().thePlayer.getItemInUse() != null){
                    float partialTicks = postRender.partialTicks;
                    List<Vec3> locations = new ArrayList<>();

                    EntityPlayer shooter = Minecraft.getMinecraft().thePlayer;
                    Vec3 startLoc = new Vec3(shooter.posX, shooter.posY + (double)shooter.getEyeHeight(), shooter.posZ);
                    float rotationYaw = shooter.rotationYaw;
                    float rotationPitch = shooter.rotationPitch;
                    startLoc = startLoc.addVector((double)(MathHelper.cos(rotationYaw / 180.0F * (float)Math.PI) * 0.16F),
                            0.10000000149011612D, (double)(MathHelper.sin(rotationYaw / 180.0F * (float)Math.PI) * 0.16F));

                    double motionX = (double)(-MathHelper.sin(rotationYaw / 180.0F * (float)Math.PI) * MathHelper.cos(rotationPitch / 180.0F * (float)Math.PI));
                    double motionZ = (double)(MathHelper.cos(rotationYaw / 180.0F * (float)Math.PI) * MathHelper.cos(rotationPitch / 180.0F * (float)Math.PI));
                    double motionY = (double)(-MathHelper.sin(rotationPitch / 180.0F * (float)Math.PI));

                    float f = MathHelper.sqrt_double(motionX * motionX + motionY * motionY + motionZ * motionZ);
                    motionX = motionX / (double)f;
                    motionY = motionY / (double)f;
                    motionZ = motionZ / (double)f;

                    int i = Items.bow.getMaxItemUseDuration(Minecraft.getMinecraft().thePlayer.getItemInUse()) - Minecraft.getMinecraft().thePlayer.getItemInUseCount();
                    float velocity = (float)i / 20.0F;
                    velocity  = (velocity  * velocity  + velocity  * 2.0F) / 3.0F;

                    if (velocity > 1.0F)
                    {
                        velocity = 1.0F;
                    }

                    motionX = motionX * (double)velocity * 2 * 1.5;
                    motionY = motionY * (double)velocity* 2 * 1.5;
                    motionZ = motionZ * (double)velocity* 2 * 1.5;


                    boolean water;

                    // do calc

                    for (int index = 0; index < 9999; index++) {
                        IBlockState iBlockState = Minecraft.getMinecraft().theWorld.getBlockState(new BlockPos(startLoc));
                        Block block = iBlockState.getBlock();
                        water = block == Blocks.water || block == Blocks.flowing_water;

                        if (block.getMaterial() != Material.air)
                        {
                            AxisAlignedBB axisalignedbb = block.getCollisionBoundingBox(Minecraft.getMinecraft().theWorld, new BlockPos(startLoc), iBlockState);

                            if (axisalignedbb != null && axisalignedbb.isVecInside(startLoc))
                            {
                                RenderUtils.highlightBlock(new BlockPos(startLoc), new Color(0,255,0,100), postRender.partialTicks);
                                break;
                            }
                        }

                        {
                            // do magic

                            Vec3 vec31 = startLoc;
                            Vec3 vec3 = startLoc.addVector(motionX, motionY, motionZ);
                            MovingObjectPosition movingobjectposition = Minecraft.getMinecraft().theWorld.rayTraceBlocks(vec31, vec3, false, true, false);
                            vec31 = startLoc;
                            vec3 = startLoc.addVector(motionX, motionY, motionZ);

                            System.out.println(movingobjectposition);

                        }

                        locations.add(startLoc);
                        startLoc = startLoc.addVector(motionX, motionY, motionZ); // it

                        float f4 = 0.99F;
                        float f6 = 0.05F;

                        if (water) {
                            f4 = 0.6F;
                        }

                        motionX *= (double) f4;
                        motionY *= (double) f4;
                        motionZ *= (double) f4;
                        motionY -= (double) f6;
                    }
                    {
                        Entity render = Minecraft.getMinecraft().getRenderViewEntity();
                        WorldRenderer worldRenderer = Tessellator.getInstance().getWorldRenderer();

                        double realX = render.lastTickPosX + (render.posX - render.lastTickPosX) * partialTicks;
                        double realY = render.lastTickPosY + (render.posY - render.lastTickPosY) * partialTicks;
                        double realZ = render.lastTickPosZ + (render.posZ - render.lastTickPosZ) * partialTicks;

                        GlStateManager.pushMatrix();
                        GlStateManager.translate(-realX, -realY, -realZ);
                        GlStateManager.disableTexture2D();
                        GlStateManager.enableBlend();
                        GlStateManager.disableAlpha();
                        GL11.glLineWidth(2);
                        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);

                        GlStateManager.color(1,1,1,1);
                        worldRenderer.begin(GL11.GL_LINE_STRIP, DefaultVertexFormats.POSITION);
                        for (Vec3 pos2:locations) {
                            worldRenderer.pos(pos2.xCoord, pos2.yCoord, pos2.zCoord).endVertex();
                        }
                        Tessellator.getInstance().draw();

                        GlStateManager.translate(realX, realY, realZ);
                        GlStateManager.disableBlend();
                        GlStateManager.enableAlpha();
                        GlStateManager.enableTexture2D();
                        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                        GlStateManager.popMatrix();
                    }
                }
            }
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
