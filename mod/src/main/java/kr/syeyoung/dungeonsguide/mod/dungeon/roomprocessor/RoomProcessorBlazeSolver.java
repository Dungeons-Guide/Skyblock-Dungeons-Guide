/*
 * Dungeons Guide - The most intelligent Hypixel Skyblock Dungeons Mod
 * Copyright (C) 2021  cyoung06
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package kr.syeyoung.dungeonsguide.mod.dungeon.roomprocessor;


import kr.syeyoung.dungeonsguide.mod.config.types.AColor;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomfinder.DungeonRoom;
import kr.syeyoung.dungeonsguide.mod.features.FeatureRegistry;
import kr.syeyoung.dungeonsguide.mod.utils.RenderUtils;
import kr.syeyoung.dungeonsguide.mod.utils.TextUtils;
import kr.syeyoung.modapi.ModAPI;
import kr.syeyoung.modapi.data.AABB;
import kr.syeyoung.modapi.data.Vector3D;
import kr.syeyoung.modapi.data.VectorI3D;
import kr.syeyoung.modapi.entity.EntityType;
import kr.syeyoung.modapi.entity.UEntity;
import kr.syeyoung.modapi.entity.UEntityArmorStand;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class RoomProcessorBlazeSolver extends GeneralRoomProcessor {

    private boolean highToLow = false;

    private List<UEntityArmorStand> entityList = new ArrayList<>();
    private List<UEntity> blazeList = new ArrayList<>();
    private UEntityArmorStand next;
    private UEntity currentBlaze, nextBlaze;
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
        final VectorI3D low = dungeonRoom.getRoomBounds().getMin();
        final VectorI3D high = dungeonRoom.getRoomBounds().getMax();
        entityList.clear();
        for (UEntity uEntity : dungeonRoom.getContext().getUworld().getEntitiesWithinAabb(EntityType.ARMOR_STAND, new AABB(low.getX(), 0, low.getZ(), high.getX(), 256, high.getZ()))) {
            if (uEntity.getName().toLowerCase().contains("blaze")) {
                entityList.add((UEntityArmorStand) uEntity);
            }
        }
        blazeList = dungeonRoom.getContext().getUworld().getEntitiesWithinAabb(EntityType.BLAZE, new AABB(low.getX(), 0, low.getZ(), high.getX(), 256, high.getZ()));

        Comparator<UEntityArmorStand> comparator = Comparator.comparingInt(a -> {
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
            currentBlaze = blazeList.stream().min(Comparator.comparingDouble(e -> e.getPositionVector().distanceSq(next.getPositionVector()))).orElse(null);
        } else {
            next = null;
            currentBlaze = null;
        }
        if (entityList.size() > 1) {
            UEntityArmorStand theNextOne = entityList.get(1);
            nextBlaze = blazeList.stream().min(Comparator.comparingDouble(e -> e.getPositionVector().distanceSq(theNextOne.getPositionVector()))).orElse(null);
        } else {
            nextBlaze = null;
        }
    }


    @Override
    public void drawWorld(float partialTicks) {
        super.drawWorld(partialTicks);
        if (!FeatureRegistry.SOLVER_BLAZE.isEnabled()) return;
        if (next == null) return;
        Vector3D pos = next.getPositionEyes(partialTicks);
        RenderUtils.drawTextAtWorld("NEXT", (float)pos.x, (float)pos.y, (float)pos.z, 0xFFFF0000, 0.5f, true, false, partialTicks);

        Entity viewing_from = Minecraft.getMinecraft().getRenderViewEntity();

        double x_fix = viewing_from.lastTickPosX + ((viewing_from.posX - viewing_from.lastTickPosX) * partialTicks);
        double y_fix = viewing_from.lastTickPosY + ((viewing_from.posY - viewing_from.lastTickPosY) * partialTicks);
        double z_fix = viewing_from.lastTickPosZ + ((viewing_from.posZ - viewing_from.lastTickPosZ) * partialTicks);


        for (UEntity entity : blazeList) {
            GlStateManager.pushMatrix();
            float f = entity.getPrevRotationYaw() + (entity.getRotationYaw() - entity.getPrevRotationYaw()) * partialTicks;
            double x = entity.getPrevPosX() + (entity.getPosX() - entity.getPrevPosX()) * partialTicks;
            double y = entity.getPrevPosY() + (entity.getPosY() - entity.getPrevPosY()) * partialTicks;
            double z = entity.getPrevPosZ() + (entity.getPosZ() - entity.getPrevPosZ()) * partialTicks;


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
            ModAPI.getAPI().getRenderManager().doRenderEntity(entity, x,y,z,f,partialTicks, true);
            GlStateManager.colorMask(true, true, true, true);

            GlStateManager.popMatrix();


            GL11.glStencilFunc(GL11.GL_EQUAL, 1, 0xFF);
            GL11.glStencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_KEEP);

//            Gui.drawRect(-9999,-9999, 9999, 9999, 0xFFFFFFFF);

            boolean border = true;

            RenderUtils.highlightBox(entity, new AABB(-0.8,0, -0.8, 0.8, 2, 0.8), FeatureRegistry.SOLVER_BLAZE.getBlazeColor(), partialTicks, false);
            if (entity == nextBlaze) {
                RenderUtils.highlightBox(entity, new AABB(-0.8,0, -0.8, 0.8, 2, 0.8), FeatureRegistry.SOLVER_BLAZE.getNextUpBlazeColor(), partialTicks, false);
            } else if (entity == currentBlaze)
                RenderUtils.highlightBox(entity, new AABB(-0.8,0, -0.8, 0.8, 2, 0.8), FeatureRegistry.SOLVER_BLAZE.getNextBlazeColor(), partialTicks, false);

            GlStateManager.color(1,1,1,1);


            if (FeatureRegistry.SOLVER_BLAZE.<AColor>getParameter("blazeborder").getValue().getAlpha() > 0x10) {
                GL11.glStencilFunc(GL11.GL_NOTEQUAL, 3, 0x01);
                GL11.glStencilOp(GL11.GL_KEEP, GL11.GL_REPLACE, GL11.GL_REPLACE);
                GlStateManager.pushMatrix();

                GlStateManager.translate(-x_fix, -y_fix, -z_fix);
                GlStateManager.translate(x, y + 0.7, z);
                GlStateManager.scale(1.1f, 1.1f, 1.1f);

                GlStateManager.colorMask(false, false, false, false);
                ModAPI.getAPI().getRenderManager().doRenderEntity(entity, 0, -0.7, 0, f, partialTicks, true);
                GlStateManager.colorMask(true, true, true, true);

                GlStateManager.popMatrix();



                GL11.glStencilFunc(GL11.GL_EQUAL, 3, 0xFF);
                GL11.glStencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_KEEP);

                RenderUtils.highlightBox(entity, new AABB(-1, 0, -1, 1, 2, 1), FeatureRegistry.SOLVER_BLAZE.<AColor>getParameter("blazeborder").getValue(), partialTicks, false);


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
