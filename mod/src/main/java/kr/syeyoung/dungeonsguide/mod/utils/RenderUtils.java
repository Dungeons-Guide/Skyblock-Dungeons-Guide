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

package kr.syeyoung.dungeonsguide.mod.utils;

import kr.syeyoung.dungeonsguide.mod.config.types.AColor;
import kr.syeyoung.dungeonsguide.mod.dungeon.dataprovider.DungeonDoor;
import kr.syeyoung.dungeonsguide.mod.gui.renderer.RenderingContext;
import kr.syeyoung.modapi.ModAPI;
import kr.syeyoung.modapi.data.AABB;
import kr.syeyoung.modapi.data.ResourceIdentifier;
import kr.syeyoung.modapi.data.Vector3D;
import kr.syeyoung.modapi.data.VectorI3D;
import kr.syeyoung.modapi.entity.UEntity;
import kr.syeyoung.modapi.rendering.UWorldRenderContext;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.Arrays;

public class RenderUtils {
    public static final ResourceIdentifier icons = new ResourceIdentifier("textures/gui/icons.png");
    private static final ResourceLocation beaconBeam = new ResourceLocation("textures/entity/beacon_beam.png");

    /**
     * Taken from NotEnoughUpdates under Creative Commons Attribution-NonCommercial 3.0
     * And modified to fit out need.
     * https://github.com/Moulberry/NotEnoughUpdates/blob/master/LICENSE
     * @author Moulberry
     */
//    public static void _renderBeaconBeam(double x, double y, double z, AColor aColor, float partialTicks) {
//        int height = 300;
//        int bottomOffset = 0;
//        int topOffset = bottomOffset + height;
//
//        Tessellator tessellator = Tessellator.getInstance();
//        WorldRenderer worldrenderer = tessellator.getWorldRenderer();
//
//        Minecraft.getMinecraft().getTextureManager().bindTexture(beaconBeam);
//        GL11.glTexParameterf(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, 10497.0F);
//        GL11.glTexParameterf(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, 10497.0F);
//        GlStateManager.disableLighting();
//        GlStateManager.disableDepth();
//        GlStateManager.enableTexture2D();
//        GlStateManager.tryBlendFuncSeparate(770, 1, 1, 0);
//        GlStateManager.enableBlend();
//        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
//
//        double time = System.currentTimeMillis() / 50 + (double)partialTicks;
//        double d1 = MathUtils.frac(-time * 0.2D - Math.floor(-time * 0.1D));
//
//        int c = getColorAt(x,y,z, aColor);
//        float alpha = ((c >> 24) & 0xFF) / 255.0f;
//
//        float r = ((c >> 16) & 0xFF) / 255f;
//        float g = ((c >> 8) & 0xFF) / 255f;
//        float b = (c & 0xFF) / 255f;
//        double d2 = time * 0.025D * -1.5D;
//        double d4 = 0.5D + Math.cos(d2 + 2.356194490192345D) * 0.2D;
//        double d5 = 0.5D + Math.sin(d2 + 2.356194490192345D) * 0.2D;
//        double d6 = 0.5D + Math.cos(d2 + (Math.PI / 4D)) * 0.2D;
//        double d7 = 0.5D + Math.sin(d2 + (Math.PI / 4D)) * 0.2D;
//        double d8 = 0.5D + Math.cos(d2 + 3.9269908169872414D) * 0.2D;
//        double d9 = 0.5D + Math.sin(d2 + 3.9269908169872414D) * 0.2D;
//        double d10 = 0.5D + Math.cos(d2 + 5.497787143782138D) * 0.2D;
//        double d11 = 0.5D + Math.sin(d2 + 5.497787143782138D) * 0.2D;
//        double d14 = -1.0D + d1;
//        double d15 = (double)(height) * 2.5D + d14;
//        worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
//        worldrenderer.pos(x + d4, y + topOffset, z + d5).tex(1.0D, d15).color(r, g, b, alpha).endVertex();
//        worldrenderer.pos(x + d4, y + bottomOffset, z + d5).tex(1.0D, d14).color(r, g, b, 1.0F).endVertex();
//        worldrenderer.pos(x + d6, y + bottomOffset, z + d7).tex(0.0D, d14).color(r, g, b, 1.0F).endVertex();
//        worldrenderer.pos(x + d6, y + topOffset, z + d7).tex(0.0D, d15).color(r, g, b, alpha).endVertex();
//        worldrenderer.pos(x + d10, y + topOffset, z + d11).tex(1.0D, d15).color(r, g, b, alpha).endVertex();
//        worldrenderer.pos(x + d10, y + bottomOffset, z + d11).tex(1.0D, d14).color(r, g, b, 1.0F).endVertex();
//        worldrenderer.pos(x + d8, y + bottomOffset, z + d9).tex(0.0D, d14).color(r, g, b, 1.0F).endVertex();
//        worldrenderer.pos(x + d8, y + topOffset, z + d9).tex(0.0D, d15).color(r, g, b, alpha).endVertex();
//        worldrenderer.pos(x + d6, y + topOffset, z + d7).tex(1.0D, d15).color(r, g, b, alpha).endVertex();
//        worldrenderer.pos(x + d6, y + bottomOffset, z + d7).tex(1.0D, d14).color(r, g, b, 1.0F).endVertex();
//        worldrenderer.pos(x + d10, y + bottomOffset, z + d11).tex(0.0D, d14).color(r, g, b, 1.0F).endVertex();
//        worldrenderer.pos(x + d10, y + topOffset, z + d11).tex(0.0D, d15).color(r, g, b, alpha).endVertex();
//        worldrenderer.pos(x + d8, y + topOffset, z + d9).tex(1.0D, d15).color(r, g, b, alpha).endVertex();
//        worldrenderer.pos(x + d8, y + bottomOffset, z + d9).tex(1.0D, d14).color(r, g, b, 1.0F).endVertex();
//        worldrenderer.pos(x + d4, y + bottomOffset, z + d5).tex(0.0D, d14).color(r, g, b, 1.0F).endVertex();
//        worldrenderer.pos(x + d4, y + topOffset, z + d5).tex(0.0D, d15).color(r, g, b, alpha).endVertex();
//        tessellator.draw();
//
//        double d12 = -1.0D + d1;
//        double d13 = height + d12;
//
//        worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
//        worldrenderer.pos(x + 0.2D, y + topOffset, z + 0.2D).tex(1.0D, d13).color(r, g, b, 0.25F*alpha).endVertex();
//        worldrenderer.pos(x + 0.2D, y + bottomOffset, z + 0.2D).tex(1.0D, d12).color(r, g, b, 0.25F).endVertex();
//        worldrenderer.pos(x + 0.8D, y + bottomOffset, z + 0.2D).tex(0.0D, d12).color(r, g, b, 0.25F).endVertex();
//        worldrenderer.pos(x + 0.8D, y + topOffset, z + 0.2D).tex(0.0D, d13).color(r, g, b, 0.25F*alpha).endVertex();
//        worldrenderer.pos(x + 0.8D, y + topOffset, z + 0.8D).tex(1.0D, d13).color(r, g, b, 0.25F*alpha).endVertex();
//        worldrenderer.pos(x + 0.8D, y + bottomOffset, z + 0.8D).tex(1.0D, d12).color(r, g, b, 0.25F).endVertex();
//        worldrenderer.pos(x + 0.2D, y + bottomOffset, z + 0.8D).tex(0.0D, d12).color(r, g, b, 0.25F).endVertex();
//        worldrenderer.pos(x + 0.2D, y + topOffset, z + 0.8D).tex(0.0D, d13).color(r, g, b, 0.25F*alpha).endVertex();
//        worldrenderer.pos(x + 0.8D, y + topOffset, z + 0.2D).tex(1.0D, d13).color(r, g, b, 0.25F*alpha).endVertex();
//        worldrenderer.pos(x + 0.8D, y + bottomOffset, z + 0.2D).tex(1.0D, d12).color(r, g, b, 0.25F).endVertex();
//        worldrenderer.pos(x + 0.8D, y + bottomOffset, z + 0.8D).tex(0.0D, d12).color(r, g, b, 0.25F).endVertex();
//        worldrenderer.pos(x + 0.8D, y + topOffset, z + 0.8D).tex(0.0D, d13).color(r, g, b, 0.25F*alpha).endVertex();
//        worldrenderer.pos(x + 0.2D, y + topOffset, z + 0.8D).tex(1.0D, d13).color(r, g, b, 0.25F*alpha).endVertex();
//        worldrenderer.pos(x + 0.2D, y + bottomOffset, z + 0.8D).tex(1.0D, d12).color(r, g, b, 0.25F).endVertex();
//        worldrenderer.pos(x + 0.2D, y + bottomOffset, z + 0.2D).tex(0.0D, d12).color(r, g, b, 0.25F).endVertex();
//        worldrenderer.pos(x + 0.2D, y + topOffset, z + 0.2D).tex(0.0D, d13).color(r, g, b, 0.25F*alpha).endVertex();
//        tessellator.draw();
//
//        GlStateManager.enableDepth();
//    }



    public static int blendTwoColors(int background, int newColor) {
        float alpha = ((newColor >> 24) & 0xFF) /255.0f;
        int r1 = (background >> 16) & 0xFF, r2 = (newColor >> 16) & 0xFF;
        int g1 = (background >> 8) & 0xFF, g2 = (newColor >> 8) & 0xFF;
        int b1 = (background) & 0xFF, b2 = (newColor) & 0xFF;

        int rr = (int) (r1 + (r2-r1) * alpha) & 0xFF;
        int rg = (int) (g1 + (g2-g1) * alpha) & 0xFF;
        int rb = (int) (b1 + (b2-b1) * alpha) & 0xFF;
        return 0xFF000000 | ((rr << 16) & 0xFF0000) | ((rg << 8) & 0xFF00) | (rb & 0xFF);
    }

    public static int blendAlpha(int origColor, float alphaPerc) {
        return blendTwoColors(origColor, (int)(alphaPerc*255) << 24 | 0xFFFFFF);
    }

    public static void renderBar(RenderingContext context, float x, float y, float xSize, float completed) {
//        Minecraft.getMinecraft().getTextureManager().bindTexture(icons);
        completed = (float)Math.round(completed / 0.05F) * 0.05F;
        float notCompleted = 1.0F - completed;
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        float width = 0.0F;
        if (completed < 0.5F) {
            width = (0.5F - completed) * xSize;
            context.drawScaledCustomSizeModalRect(icons,
                     x + xSize * completed, y,
                    xSize * completed, 74,
                    (int) (xSize/2.0 - xSize * completed), 5,
                    width, 5.0F,
                    256, 256);

//                    xSize * completed / 256.0F, xSize / 2.0F / 256.0F, 0.2890625F, 0.30859375F, 9728);
        }

        if (completed < 1.0F) {
            width = Math.min(xSize * notCompleted, xSize / 2.0F);
            context.drawScaledCustomSizeModalRect(icons,
                    x + xSize / 2.0F + Math.max(xSize * (completed - 0.5F), 0.0F), y,
                    (182.0F - xSize / 2.0F + Math.max(xSize * (completed - 0.5F), 0.0F)), 74,
                    (int) (xSize / 2.0F - Math.max(xSize * (completed - 0.5F), 0.0F)), 5,
                    width, 5,
                    256, 256
            );
//            );
//            drawTexturedRect(, width, 5.0F,  / 256.0F, 0.7109375F, 0.2890625F, 0.30859375F, 9728);
        }

        if (completed > 0.0F) {
            width = Math.min(xSize * completed, xSize / 2.0F);
            context.drawScaledCustomSizeModalRect(icons,
                    x, y,
                    0, 79,
                    (int) width, 5,
                    width, 5,
                    256, 256
                    );
//            drawTexturedRect(x, y, width, 5.0F, 0.0F, width / 256.0F, 0.30859375F, 0.328125F, 9728);
        }

        if (completed > 0.5F) {
            width = Math.min(xSize * (completed - 0.5F), xSize / 2.0F);
            context.drawScaledCustomSizeModalRect(icons,
                    x + xSize/2.0, y,
                    (182-xSize/2), 79,
                    (int) width, 5,
                    width, 5,
                    256, 256
            );
//            drawTexturedRect(x + xSize / 2.0F, y, width, 5.0F, (182.0F - xSize / 2.0F) / 256.0F, (182.0F - xSize / 2.0F + width) / 256.0F, 0.30859375F, 0.328125F, 9728);
        }

    }


    public static int getChromaColorAt(int x, int y, float speed, float s, float b, float alpha) {
        double blah = ((double)(speed) * (System.currentTimeMillis() / 2)) % 360;
        return (Color.HSBtoRGB((float) (((blah - (x + y) / 2.0f) % 360) / 360.0f), s,b) & 0xffffff)
                | (((int)(alpha * 255)<< 24) & 0xff000000);
    }
    public static int getColorAt(double x, double y, AColor color) {
        if (!color.isChroma())
            return color.getRGB();

        double blah = ((double)(color.getChromaSpeed()) * (System.currentTimeMillis() / 2)) % 360;
        float[] hsv = new float[3];
        Color.RGBtoHSB(color.getRed(), color.getBlue(), color.getGreen(), hsv);


        return (Color.HSBtoRGB((float) (((blah - (x + y) / 2.0f) % 360) / 360.0f), hsv[1],hsv[2]) & 0xffffff)
                | ((color.getAlpha() << 24) & 0xff000000);
    }
    public static int getColorAt(double x, double y,double z, AColor color) {
        if (!color.isChroma())
            return color.getRGB();

        double blah = ((double)(color.getChromaSpeed()) * (System.currentTimeMillis() / 2)) % 360;
        float[] hsv = new float[3];
        Color.RGBtoHSB(color.getRed(), color.getBlue(), color.getGreen(), hsv);


        return (Color.HSBtoRGB((float) (((blah - ((x + y+z) / 2.0f) % 360)) / 360.0f), hsv[1],hsv[2]) & 0xffffff)
                | ((color.getAlpha() << 24) & 0xff000000);
    }


    public static void renderDoor(DungeonDoor dungeonDoor, UWorldRenderContext context, float partialTicks) {

        double x = dungeonDoor.getPosition().getX() + 0.5;
        double y = dungeonDoor.getPosition().getY() -0.99;
        double z = dungeonDoor.getPosition().getZ() + 0.5;



        context.highlightBox(new AABB(
                x-2.5, y, z-2.5, x+2.5, y+0.01, z+2.5
        ), dungeonDoor.getType().isExist() ? 0xFF00FF00 : 0xFFFF0000, partialTicks, true);


        if (dungeonDoor.getType().isExist()) {
            if (dungeonDoor.isZDir()) {
                context.highlightBox(new AABB(
                        x-0.5, y+0.1, z-2.5, x+0.5, y+0.11, z+2.5
                ), 0xFF0000FF, partialTicks, true);
            } else {
                context.highlightBox(new AABB(
                        x-2.5, y+0.1, z-0.5, x+2.5, y+0.11, z+0.5
                ), 0xFF0000FF, partialTicks, true);
            }
        } else {
            context.drawLinesVec3(Arrays.asList(
                    new Vector3D(x - 2.5, y, z - 2.5),
                    new Vector3D(x + 2.5, y + 5, z - 2.5),
                    new Vector3D(x + 2.5, y, z + 2.5),
                    new Vector3D(x - 2.5, y + 5, z + 2.5),
                    new Vector3D(x - 2.5, y, z - 2.5)
            ), 0xFFFF0000, false, 0, 5.0f, partialTicks, true);
            context.drawLinesVec3(Arrays.asList(
                    new Vector3D(x - 2.5, y +5, z - 2.5),
                    new Vector3D(x + 2.5, y, z - 2.5),
                    new Vector3D(x + 2.5, y + 5, z + 2.5),
                    new Vector3D(x - 2.5, y, z + 2.5),
                    new Vector3D(x - 2.5, y +5, z - 2.5)
            ), 0xFFFF0000, false, 0, 5.0f, partialTicks, true);
        }
//        GlStateManager.disableAlpha();

        context.drawTextAtWorld("Type: "+dungeonDoor.getType(), dungeonDoor.getPosition().x, dungeonDoor.getPosition().y, dungeonDoor.getPosition().z, 0xFF00FFFF, 0.02f, false, false, partialTicks, true);
    }


    public static void _highlightBlock(VectorI3D blockpos, Color c, float partialTicks, boolean depth) {

        GlStateManager.disableLighting();
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GlStateManager.disableTexture2D();

        if (!depth) {
            GlStateManager.disableDepth(); GL11.glDisable(GL11.GL_DEPTH_TEST);
            GlStateManager.depthMask(false);
        }
        GlStateManager.color(c.getRed() /255.0f, c.getGreen() / 255.0f, c.getBlue()/ 255.0f, c.getAlpha()/ 255.0f);

        GlStateManager.translate(blockpos.getX(), blockpos.getY(), blockpos.getZ());

        GL11.glBegin(GL11.GL_QUADS);
        GL11.glVertex3d(0, 0, 0);
        GL11.glVertex3d(0, 0, 1);
        GL11.glVertex3d(0, 1, 1);
        GL11.glVertex3d(0, 1, 0); // TOP LEFT / BOTTOM LEFT / TOP RIGHT/ BOTTOM RIGHT

        GL11.glVertex3d(1, 0, 1);
        GL11.glVertex3d(1, 0, 0);
        GL11.glVertex3d(1, 1, 0);
        GL11.glVertex3d(1, 1, 1);

        GL11.glVertex3d(0, 1, 1);
        GL11.glVertex3d(0, 0, 1);
        GL11.glVertex3d(1, 0, 1);
        GL11.glVertex3d(1, 1, 1); // TOP LEFT / BOTTOM LEFT / TOP RIGHT/ BOTTOM RIGHT

        GL11.glVertex3d(0, 0, 0);
        GL11.glVertex3d(0, 1, 0);
        GL11.glVertex3d(1, 1, 0);
        GL11.glVertex3d(1, 0, 0);

        GL11.glVertex3d(0,1,0);
        GL11.glVertex3d(0,1,1);
        GL11.glVertex3d(1,1,1);
        GL11.glVertex3d(1,1,0);

        GL11.glVertex3d(0,0,1);
        GL11.glVertex3d(0,0,0);
        GL11.glVertex3d(1,0,0);
        GL11.glVertex3d(1,0,1);



        GL11.glEnd();


        if (!depth) {
            GlStateManager.enableDepth();
            GlStateManager.depthMask(true);
        }
        GlStateManager.enableTexture2D();
        GlStateManager.enableLighting();



//...

    }



//    public static void highlightBox(UEntity entity, AABB  axisAlignedBB, AColor c, float partialTicks, boolean depth) {
//        pushAndTranslateAccordingToRenderViewEntity(partialTicks);
//
//        GlStateManager.disableLighting();
//        GlStateManager.enableBlend();
//        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
//        GlStateManager.disableTexture2D();
//
//        if (!depth) {
//            GlStateManager.disableDepth();
//            GlStateManager.depthMask(false);
//        }
//        int rgb = RenderUtils.getColorAt(entity.getPosX() * 10,entity.getPosY() * 10,c);
//        GlStateManager.color(((rgb >> 16) &0XFF)/ 255.0f, ((rgb>>8) &0XFF)/ 255.0f, (rgb & 0xff)/ 255.0f, ((rgb >> 24) & 0xFF) / 255.0f);
//        if (axisAlignedBB == null) {
//            if (entity instanceof UEntityArmorStand) {
//                axisAlignedBB = new AABB(-0.4, -1.5, -0.4, 0.4, 0, 0.4);
//            } else if (entity.getEntityType() == EntityType.BAT) {
//                axisAlignedBB = new AABB(-0.4, -1.4, -0.4, 0.4, 0.4, 0.4);
//            } else {
//                axisAlignedBB = new AABB(-0.4, -1.5, -0.4, 0.4, 0, 0.4);
//            }
//        }
//
//        Vec3 renderPos = new Vec3(
//                (float) (entity.getPrevPosX() + (entity.getPosX() - entity.getPrevPosX()) * partialTicks),
//                (float) (entity.getPrevPosY() + (entity.getPosY() - entity.getPrevPosY()) * partialTicks),
//                (float) (entity.getPrevPosZ() + (entity.getPosZ() - entity.getPrevPosZ()) * partialTicks)
//        );
//        GlStateManager.translate(axisAlignedBB.minX + renderPos.xCoord, axisAlignedBB.minY + renderPos.yCoord, axisAlignedBB.minZ + renderPos.zCoord);
//
//        double x = axisAlignedBB.maxX - axisAlignedBB.minX;
//        double y = axisAlignedBB.maxY - axisAlignedBB.minY;
//        double z = axisAlignedBB.maxZ - axisAlignedBB.minZ;
//        GL11.glBegin(GL11.GL_QUADS);
//        GL11.glVertex3d(0, 0, 0);
//        GL11.glVertex3d(0, 0, z);
//        GL11.glVertex3d(0, y, z);
//        GL11.glVertex3d(0, y, 0); // TOP LEFT / BOTTOM LEFT / TOP RIGHT/ BOTTOM RIGHT
//
//        GL11.glVertex3d(x, 0, z);
//        GL11.glVertex3d(x, 0, 0);
//        GL11.glVertex3d(x, y, 0);
//        GL11.glVertex3d(x, y, z);
//
//        GL11.glVertex3d(0, y, z);
//        GL11.glVertex3d(0, 0, z);
//        GL11.glVertex3d(x, 0, z);
//        GL11.glVertex3d(x, y, z); // TOP LEFT / BOTTOM LEFT / TOP RIGHT/ BOTTOM RIGHT
//
//        GL11.glVertex3d(0, 0, 0);
//        GL11.glVertex3d(0, y, 0);
//        GL11.glVertex3d(x, y, 0);
//        GL11.glVertex3d(x, 0, 0);
//
//        GL11.glVertex3d(0,y,0);
//        GL11.glVertex3d(0,y,z);
//        GL11.glVertex3d(x,y,z);
//        GL11.glVertex3d(x,y,0);
//
//        GL11.glVertex3d(0,0,z);
//        GL11.glVertex3d(0,0,0);
//        GL11.glVertex3d(x,0,0);
//        GL11.glVertex3d(x,0,z);
//
//
//
//        GL11.glEnd();
//
//
//        if (!depth) {
//            GlStateManager.enableDepth();
//            GlStateManager.depthMask(true);
//        }
//        GlStateManager.enableTexture2D();
//        GlStateManager.enableLighting();
//        GlStateManager.popMatrix();
//
//    }

    public static void pushAndTranslateAccordingToRenderViewEntity(float partialTicks) {
        UEntity viewing_from = ModAPI.getAPI().getRenderViewEntity();

        double x_fix = viewing_from.getPrevPosX() + ((viewing_from.getPosX() - viewing_from.getPrevPosX()) * partialTicks);
        double y_fix = viewing_from.getPrevPosY() + ((viewing_from.getPosY() - viewing_from.getPrevPosY()) * partialTicks);
        double z_fix = viewing_from.getPrevPosZ() + ((viewing_from.getPosZ() - viewing_from.getPrevPosZ()) * partialTicks);

        GlStateManager.pushMatrix();
        GlStateManager.translate(-x_fix, -y_fix, -z_fix);
    }
}
