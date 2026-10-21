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
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.monster.Blaze;
import net.minecraft.world.phys.AABB;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.Level;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class RoomProcessorBlazeSolver extends GeneralRoomProcessor {

    private boolean highToLow = false;

    private List<ArmorStand> entityList = new ArrayList<ArmorStand>();
    private List<Blaze> blazeList = new ArrayList<>();
    private ArmorStand next;
    private Blaze currentBlaze, nextBlaze;
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
        Level w = Minecraft.getInstance().level;
        final BlockPos low = dungeonRoom.getRoomBounds().getMin();
        final BlockPos high = dungeonRoom.getRoomBounds().getMax();
        // the room's bounds (D114)
        AABB roomBox = new AABB(low.getX(), w.getMinY(), low.getZ(), high.getX() + 1, w.getMaxY() + 1, high.getZ() + 1);
        // TODO: D5 entity name stopgap (D116)
        entityList = new ArrayList<ArmorStand>(w.getEntitiesOfClass(ArmorStand.class, roomBox, input -> {
            BlockPos pos = input.blockPosition();
            return low.getX() < pos.getX() && pos.getX() < high.getX()
                    && low.getZ() < pos.getZ() && pos.getZ() < high.getZ() && TextUtils.getFormattedText(input.getName()).toLowerCase().contains("blaze");
        }));
        blazeList = new ArrayList<Blaze>(w.getEntitiesOfClass(Blaze.class, roomBox, input -> {
            BlockPos pos = input.blockPosition();
            return low.getX() < pos.getX() && pos.getX() < high.getX()
                    && low.getZ() < pos.getZ() && pos.getZ() < high.getZ();
        }));

        Comparator<ArmorStand> comparator = Comparator.comparingInt(a -> {
            // TODO: D5 entity name stopgap (D116)
            String name = TextUtils.getFormattedText(a.getName());
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
            currentBlaze = blazeList.stream().min(Comparator.comparingDouble(e -> e.distanceToSqr(next))).orElse(null);
        } else {
            next = null;
            currentBlaze = null;
        }
        if (entityList.size() > 1) {
            ArmorStand theNextOne = entityList.get(1);
            nextBlaze = blazeList.stream().min(Comparator.comparingDouble(e -> e.distanceToSqr(theNextOne))).orElse(null);
        } else {
            nextBlaze = null;
        }
    }


    @Override
    public void drawWorld(float partialTicks) {
        super.drawWorld(partialTicks);
        if (!FeatureRegistry.SOLVER_BLAZE.isEnabled()) return;
        if (next == null) return;
        Vec3 pos = next.getPositionEyes(partialTicks);
        RenderUtils.drawTextAtWorld("NEXT", (float)pos.x, (float)pos.y, (float)pos.z, 0xFFFF0000, 0.5f, true, false, partialTicks);

        Entity viewing_from = Minecraft.getInstance().getCameraEntity();

        double x_fix = viewing_from.lastTickPosX + ((viewing_from.getX() - viewing_from.lastTickPosX) * partialTicks);
        double y_fix = viewing_from.lastTickPosY + ((viewing_from.getY() - viewing_from.lastTickPosY) * partialTicks);
        double z_fix = viewing_from.lastTickPosZ + ((viewing_from.getZ() - viewing_from.lastTickPosZ) * partialTicks);


        for (Blaze entity : blazeList) {
            GlStateManager.pushMatrix();
            float f = entity.prevRotationYaw + (entity.rotationYaw - entity.prevRotationYaw) * partialTicks;
            double x = entity.prevPosX + (entity.getX() - entity.prevPosX) * partialTicks;
            double y = entity.prevPosY + (entity.getY() - entity.prevPosY) * partialTicks;
            double z = entity.prevPosZ + (entity.getZ() - entity.prevPosZ) * partialTicks;


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
            Minecraft.getInstance().getEntityRenderDispatcher().doRenderEntity(entity, x,y,z,f,partialTicks, true);
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
                Minecraft.getInstance().getEntityRenderDispatcher().doRenderEntity(entity, 0, -0.7, 0, f, partialTicks, true);
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
