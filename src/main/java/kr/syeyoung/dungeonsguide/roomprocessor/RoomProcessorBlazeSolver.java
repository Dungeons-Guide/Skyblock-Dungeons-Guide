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

package kr.syeyoung.dungeonsguide.roomprocessor;

import com.google.common.base.Predicate;
import kr.syeyoung.dungeonsguide.config.Config;
import kr.syeyoung.dungeonsguide.config.types.AColor;
import kr.syeyoung.dungeonsguide.dungeon.roomfinder.DungeonRoom;
import kr.syeyoung.dungeonsguide.features.FeatureRegistry;
import kr.syeyoung.dungeonsguide.features.impl.solvers.FeatureSolverBlaze;
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
import java.util.Comparator;
import java.util.List;

public class RoomProcessorBlazeSolver extends GeneralRoomProcessor {

    private boolean highToLow = false;

    private List<EntityArmorStand> entityList = new ArrayList<EntityArmorStand>();
    private List<EntityBlaze> blazeList = new ArrayList<>();
    private EntityArmorStand next;
    private EntityBlaze nextBlaze, theoneafterit;
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
                    && low.getZ() < pos.getZ() && pos.getZ() < high.getZ();
        }));

        Comparator<EntityArmorStand> comparator = Comparator.comparingInt(a -> {
            String name = a.getName();
            String colorGone = TextUtils.stripColor(name);
            String health2 = TextUtils.keepIntegerCharactersOnly(colorGone.split("/")[1]);
            try {
                return Integer.parseInt(health2);
            } catch (Exception e) {return -1;}
        });
        if (highToLow) {
            entityList.sort(comparator.reversed());
        } else {
            entityList.sort(comparator);
        }

        if (entityList.size() > 0) {
            next = entityList.get(0);
            nextBlaze  = blazeList.stream().min(Comparator.comparingDouble(e -> e.getDistanceSqToEntity(next))).orElse(null);
        } else {
            nextBlaze = null;
        }
        if (entityList.size() > 1) {
            EntityArmorStand thenextone = entityList.get(1);
            theoneafterit  = blazeList.stream().min(Comparator.comparingDouble(e -> e.getDistanceSqToEntity(thenextone))).orElse(null);
        } else {
            theoneafterit = null;
        }
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
            GlStateManager.pushMatrix();
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

            GlStateManager.translate(-x_fix, -y_fix, -z_fix);

            GlStateManager.colorMask(false, false, false, false);
            Minecraft.getMinecraft().getRenderManager().doRenderEntity(entity, x,y,z,f,partialTicks, true);
            GlStateManager.colorMask(true, true, true, true);

            GlStateManager.popMatrix();


            GL11.glStencilFunc(GL11.GL_EQUAL, 1, 0xFF);
            GL11.glStencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_KEEP);

//            Gui.drawRect(-9999,-9999, 9999, 9999, 0xFFFFFFFF);

            boolean border = true;

            if (entity == theoneafterit) {
                RenderUtils.highlightBox(entity, AxisAlignedBB.fromBounds(-0.8,0, -0.8, 0.8, 2, 0.8), FeatureRegistry.SOLVER_BLAZE.getNextUpBlazeColor(), partialTicks, false);
            } else if (entity == nextBlaze)
                RenderUtils.highlightBox(entity, AxisAlignedBB.fromBounds(-0.8,0, -0.8, 0.8, 2, 0.8), FeatureRegistry.SOLVER_BLAZE.getNextBlazeColor(), partialTicks, false);
            else
                RenderUtils.highlightBox(entity, AxisAlignedBB.fromBounds(-0.8,0, -0.8, 0.8, 2, 0.8), FeatureRegistry.SOLVER_BLAZE.getBlazeColor(), partialTicks, false);
            GlStateManager.color(1,1,1,1);


            if (FeatureRegistry.SOLVER_BLAZE.<AColor>getParameter("blazeborder").getValue().getAlpha() > 0x10) {
                GL11.glStencilFunc(GL11.GL_NOTEQUAL, 3, 0x01);
                GL11.glStencilOp(GL11.GL_KEEP, GL11.GL_REPLACE, GL11.GL_REPLACE);
                GlStateManager.pushMatrix();

                GlStateManager.translate(-x_fix, -y_fix, -z_fix);
                GlStateManager.translate(x, y + 0.7, z);
                GlStateManager.scale(1.1f, 1.1f, 1.1f);

                GlStateManager.colorMask(false, false, false, false);
                Minecraft.getMinecraft().getRenderManager().doRenderEntity(entity, 0, -0.7, 0, f, partialTicks, true);
                GlStateManager.colorMask(true, true, true, true);

                GlStateManager.popMatrix();



                GL11.glStencilFunc(GL11.GL_EQUAL, 3, 0xFF);
                GL11.glStencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_KEEP);

                RenderUtils.highlightBox(entity, AxisAlignedBB.fromBounds(-1, 0, -1, 1, 2, 1), FeatureRegistry.SOLVER_BLAZE.<AColor>getParameter("blazeborder").getValue(), partialTicks, false);


            }
            GL11.glDisable(GL11.GL_STENCIL_TEST);
            GlStateManager.enableDepth();
            GlStateManager.popMatrix();
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
