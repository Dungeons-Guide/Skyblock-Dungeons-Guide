package kr.syeyoung.dungeonsguide.roomprocessor;

import com.google.common.base.Predicate;
import kr.syeyoung.dungeonsguide.config.Config;
import kr.syeyoung.dungeonsguide.config.types.AColor;
import kr.syeyoung.dungeonsguide.dungeon.roomfinder.DungeonRoom;
import kr.syeyoung.dungeonsguide.features.FeatureRegistry;
import kr.syeyoung.dungeonsguide.utils.RenderUtils;
import kr.syeyoung.dungeonsguide.utils.TextUtils;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.entity.monster.EntityBlaze;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemBow;
import net.minecraft.util.*;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class RoomProcessorBlazeSolver extends GeneralRoomProcessor {

    private boolean highToLow = false;

    private List<EntityArmorStand> entityList = new ArrayList<EntityArmorStand>();
    private List<EntityBlaze> blazeList = new ArrayList<>();
    private EntityArmorStand next;
    private EntityBlaze nextBlaze;
    public RoomProcessorBlazeSolver(DungeonRoom dungeonRoom) {
        super(dungeonRoom);
        Object highToLow = dungeonRoom.getDungeonRoomInfo().getProperties().get("order");
        if (highToLow == null) this.highToLow = false;
        else this.highToLow = (Boolean) highToLow;
    }

    @Override
    public void tick() {
        super.tick();

        DungeonRoom dungeonRoom = getDungeonRoom();
        World w = dungeonRoom.getContext().getWorld();
        final BlockPos low = dungeonRoom.getMin();
        final BlockPos high = dungeonRoom.getMax();
        entityList = new ArrayList<EntityArmorStand>(w.getEntities(EntityArmorStand.class, input -> {
            BlockPos pos = input.getPosition();
            return low.getX() < pos.getX() && pos.getX() < high.getX()
                    && low.getZ() < pos.getZ() && pos.getZ() < high.getZ() && input.getName().toLowerCase().contains("blaze");
        }));
        blazeList = new ArrayList<EntityBlaze>(w.getEntities(EntityBlaze.class, input -> {
            BlockPos pos = input.getPosition();
            return low.getX() < pos.getX() && pos.getX() < high.getX()
                    && low.getZ() < pos.getZ() && pos.getZ() < high.getZ() && input.getName().toLowerCase().contains("blaze");
        }));

        EntityArmorStand semi_target = null;
        int health = (highToLow ? Integer.MIN_VALUE : Integer.MAX_VALUE);
        for (EntityArmorStand ea : entityList) {
            String name = ea.getName();
            String colorGone = TextUtils.stripColor(name);
            String health2 = TextUtils.keepIntegerCharactersOnly(colorGone.split("/")[1]);
            try {
                int heal = Integer.parseInt(health2);
                if (highToLow && heal > health) {
                    health = heal;
                    semi_target = ea;
                } else if (!highToLow && heal < health) {
                    health = heal;
                    semi_target = ea;
                }
            } catch (Exception e){}

        }
        if (semi_target != null) {
            EntityArmorStand finalSemi_target = semi_target;
            nextBlaze = blazeList.stream().filter(e -> e.getDistanceSqToEntity(finalSemi_target) < 9).findFirst().orElse(null);
        }

        next = semi_target;
    }


    @Override
    public void drawWorld(float partialTicks) {
        super.drawWorld(partialTicks);
        if (!FeatureRegistry.SOLVER_BLAZE.isEnabled()) return;
        if (next == null) return;
        Vec3 pos = next.getPositionEyes(partialTicks);
        RenderUtils.drawTextAtWorld("NEXT", (float)pos.xCoord, (float)pos.yCoord, (float)pos.zCoord, 0xFFFF0000, 0.5f, true, false, partialTicks);

        Entity viewing_from = Minecraft.getMinecraft().getRenderViewEntity();

        double x_fix = viewing_from.lastTickPosX + ((viewing_from.posX - viewing_from.lastTickPosX) * partialTicks);
        double y_fix = viewing_from.lastTickPosY + ((viewing_from.posY - viewing_from.lastTickPosY) * partialTicks);
        double z_fix = viewing_from.lastTickPosZ + ((viewing_from.posZ - viewing_from.lastTickPosZ) * partialTicks);


        for (EntityBlaze entity : blazeList) {
            float f = entity.prevRotationYaw + (entity.rotationYaw - entity.prevRotationYaw) * partialTicks;
            double x = entity.prevPosX + (entity.posX - entity.prevPosX) * partialTicks;
            double y = entity.prevPosY + (entity.posY - entity.prevPosY) * partialTicks;
            double z = entity.prevPosZ + (entity.posZ - entity.prevPosZ) * partialTicks;


            GL11.glEnable(GL11.GL_STENCIL_TEST);
            GL11.glClearStencil(0);
            GlStateManager.disableDepth();
            GL11.glClear(GL11.GL_STENCIL_BUFFER_BIT);

            GL11.glStencilMask(0xFF);
            GL11.glStencilFunc(GL11.GL_ALWAYS, 1, 0xFF);
            GL11.glStencilOp(GL11.GL_KEEP, GL11.GL_REPLACE, GL11.GL_REPLACE);

            GlStateManager.pushMatrix();
            GlStateManager.pushAttrib();
            GlStateManager.translate(-x_fix, -y_fix, -z_fix);

            GlStateManager.colorMask(false, false, false, false);
            Minecraft.getMinecraft().getRenderManager().doRenderEntity(entity, x,y,z,f,partialTicks, true);
            GlStateManager.colorMask(true, true, true, true);

            GlStateManager.popMatrix();
            GlStateManager.popAttrib();

            GL11.glStencilFunc(GL11.GL_EQUAL, 1, 0xFF);
            GL11.glStencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_KEEP);

//            RenderUtils.drawRectSafe(-9999,-9999, 9999, 9999, 0xFFFFFFFF);

            boolean border = true;

            if (entity != nextBlaze)
                RenderUtils.highlightBox(entity, AxisAlignedBB.fromBounds(-0.8,0, -0.8, 0.8, 2, 0.8), new Color(255,255,255,255), partialTicks, false);
            else
                RenderUtils.highlightBox(entity, AxisAlignedBB.fromBounds(-0.8,0, -0.8, 0.8, 2, 0.8), new Color(0,255,0,255), partialTicks, false);
            GlStateManager.color(1,1,1,1);


            GL11.glStencilFunc(GL11.GL_NOTEQUAL, 3, 0x01);
            GL11.glStencilOp(GL11.GL_KEEP, GL11.GL_REPLACE, GL11.GL_REPLACE);
            GlStateManager.pushMatrix();
            GlStateManager.pushAttrib();
            GlStateManager.translate(-x_fix, -y_fix, -z_fix);
            GlStateManager.translate(x, y + 0.7, z);
            GlStateManager.scale(1.1f, 1.1f, 1.1f);

            GlStateManager.colorMask(false, false, false, false);
            Minecraft.getMinecraft().getRenderManager().doRenderEntity(entity, 0, -0.7, 0, f, partialTicks, true);
            GlStateManager.colorMask(true, true, true, true);

            GlStateManager.popMatrix();
            GlStateManager.popAttrib();


            GL11.glStencilFunc(GL11.GL_EQUAL, 3, 0xFF);
            GL11.glStencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_KEEP);

            RenderUtils.highlightBox(entity, AxisAlignedBB.fromBounds(-1, 0, -1, 1, 2, 1), FeatureRegistry.SOLVER_BLAZE.<AColor>getParameter("blazeborder").getValue(), partialTicks, false);


            GL11.glDisable(GL11.GL_STENCIL_TEST);
            GlStateManager.enableDepth();
        }
    }



    public static class Generator implements RoomProcessorGenerator<RoomProcessorBlazeSolver> {
        @Override
        public RoomProcessorBlazeSolver createNew(DungeonRoom dungeonRoom) {
            RoomProcessorBlazeSolver defaultRoomProcessor = new RoomProcessorBlazeSolver(dungeonRoom);
            return defaultRoomProcessor;
        }
    }
}
