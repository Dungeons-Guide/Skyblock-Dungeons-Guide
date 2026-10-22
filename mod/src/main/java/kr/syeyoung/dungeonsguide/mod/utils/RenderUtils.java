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
import kr.syeyoung.dungeonsguide.mod.render.world.DGWorldRenderer;
import kr.syeyoung.dungeonsguide.mod.render.world.state.BeaconBeamState;
import kr.syeyoung.dungeonsguide.mod.render.world.state.FilledBoxState;
import kr.syeyoung.dungeonsguide.mod.render.world.state.LineStripState;
import kr.syeyoung.dungeonsguide.mod.render.world.state.QuadState;
import kr.syeyoung.dungeonsguide.mod.render.world.state.WorldTextState;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.ambient.Bat;
import org.joml.Vector3f;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.resources.Identifier;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * World drawing records DG world render states that DGWorldRenderer submits after RenderWorldEvent (D81).
 * They are in world coordinates, 1.8.9 translated by the interpolated camera here; the _ variants drew in the
 * caller's matrix, which is the same now that the camera is applied when the states are submitted.
 */
public class RenderUtils {
    public static final Identifier icons = Identifier.parse("textures/gui/icons.png");

    public static void renderBeaconBeam(double x, double y, double z, AColor aColor, float partialTicks) {
        _renderBeaconBeam(x, y, z, aColor, partialTicks);
    }

    public static void _renderBeaconBeam(double x, double y, double z, AColor aColor, float partialTicks) {
        DGWorldRenderer.record(new BeaconBeamState(x, y, z, getColorAt(x,y,z, aColor), partialTicks));
    }

    private static float zLevel = 0;
    public static int scrollY = 0;
    public static boolean allowScrolling;
    public static int scrollX = 0;

    // TODO: 4.12, the 2D GUI helpers below drew with GlStateManager / Tessellator; their callers (feature widgets) move onto RenderingContext
/*
    public static void drawTexturedRect(float x, float y, float width, float height, int filter) {
        drawTexturedRect(x, y, width, height, 0.0F, 1.0F, 0.0F, 1.0F, filter);
    }

    public static void drawGradientRect(int left, int top, int right, int bottom, int startColor, int endColor)
    {
        float f = (float)(startColor >> 24 & 255) / 255.0F;
        float f1 = (float)(startColor >> 16 & 255) / 255.0F;
        float f2 = (float)(startColor >> 8 & 255) / 255.0F;
        float f3 = (float)(startColor & 255) / 255.0F;
        float f4 = (float)(endColor >> 24 & 255) / 255.0F;
        float f5 = (float)(endColor >> 16 & 255) / 255.0F;
        float f6 = (float)(endColor >> 8 & 255) / 255.0F;
        float f7 = (float)(endColor & 255) / 255.0F;
        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.disableAlpha();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.shadeModel(7425);
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();
        worldrenderer.begin(7, DefaultVertexFormats.POSITION_COLOR);
        worldrenderer.pos((double)right, (double)top, (double)0).color(f1, f2, f3, f).endVertex();
        worldrenderer.pos((double)left, (double)top, (double)0).color(f1, f2, f3, f).endVertex();
        worldrenderer.pos((double)left, (double)bottom, (double)0).color(f5, f6, f7, f4).endVertex();
        worldrenderer.pos((double)right, (double)bottom, (double)0).color(f5, f6, f7, f4).endVertex();
        tessellator.draw();
        GlStateManager.shadeModel(7424);
        GlStateManager.enableAlpha();
        GlStateManager.enableTexture2D();
    }



    public static void drawRoundedRectangle(int x, int y, int width, int height, int radius, double delta, int color) {
        GlStateManager.pushMatrix();
        GlStateManager.translate(width/2.0+x, height/2.0+y, 0);
        Tessellator t = Tessellator.getInstance();
        GlStateManager.disableTexture2D();
        RenderUtils.GL_SETCOLOR(color);
        WorldRenderer wr = t.getWorldRenderer();
        wr.begin(GL11.GL_POLYGON, DefaultVertexFormats.POSITION);
        for (double i = 0.1; i < Math.PI*2; i+= delta) {
            double cos = Mth.cos((float) i);
            double sin = Mth.sin((float) i);
            if (cos * Mth.cos((float) (i + delta)) <= 0) {
                wr.pos(sin * radius + (sin > 0 ? 1 : -1) * (width/2.0 - radius),
                        cos * radius + (cos > 0 ? 1 : -1) * (height/2.0 - radius),0).endVertex();
                wr.pos(sin * radius + (sin > 0 ? 1 : -1) * (width/2.0 - radius),
                        cos * radius + (cos > 0 ? -1 : 1) * (height/2.0 - radius),0).endVertex();
            } else if (sin * Mth.sin((float) (i+delta)) <= 0) {
                wr.pos(sin * radius + (sin > 0 ? 1 : -1) * (width/2.0 - radius),
                        cos * radius + (cos > 0 ? 1 : -1) * (height/2.0 - radius), 0).endVertex();
                wr.pos(sin * radius + (sin > 0 ? -1 : 1) * (width/2.0 - radius),
                        cos * radius + (cos > 0 ? 1 : -1) * (height/2.0 - radius), 0).endVertex();
            } else {
                wr.pos(sin * radius + (sin > 0 ? 1 : -1) * (width/2.0 - radius),
                        cos * radius + (cos > 0 ? 1 : -1) * (height/2.0 - radius), 0).endVertex();
            }
        }
        t.draw();
        GlStateManager.popMatrix();
    }

    public static void GL_SETCOLOR(int color) {
        float f3 = (float)(color >> 24 & 255) / 255.0F;
        float f = (float)(color >> 16 & 255) / 255.0F;
        float f1 = (float)(color >> 8 & 255) / 255.0F;
        float f2 = (float)(color & 255) / 255.0F;
        GlStateManager.color(f, f1, f2, f3);
    }

*/

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

/*
    public static void drawTexturedRect(float x, float y, float width, float height, float uMin, float uMax, float vMin, float vMax, int filter) {
        GlStateManager.enableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 771);
        GL11.glTexParameteri(3553, 10241, filter);
        GL11.glTexParameteri(3553, 10240, filter);
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();
        worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX);
        worldrenderer.pos(x, y + height, 0.0D).tex(uMin, vMax).endVertex();
        worldrenderer.pos(x + width, y + height, 0.0D).tex(uMax, vMax).endVertex();
        worldrenderer.pos(x + width, y, 0.0D).tex(uMax, vMin).endVertex();
        worldrenderer.pos(x, y, 0.0D).tex(uMin, vMin).endVertex();
        tessellator.draw();
        GL11.glTexParameteri(3553, 10241, 9728);
        GL11.glTexParameteri(3553, 10240, 9728);
        GlStateManager.disableBlend();
    }



*/

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

    public static void renderDoor(DungeonDoor dungeonDoor, float partialTicks) {
        double x = dungeonDoor.getPosition().getX() + 0.5;
        double y = dungeonDoor.getPosition().getY() -0.99;
        double z = dungeonDoor.getPosition().getZ() + 0.5;

        DGWorldRenderer.record(new QuadState(new Vec3[] {
                new Vec3(x - 2.5, y, z - 2.5), new Vec3(x - 2.5, y, z + 2.5), new Vec3(x + 2.5, y, z + 2.5), new Vec3(x + 2.5, y, z - 2.5)
        }, dungeonDoor.getType().isExist() ? 0xFF00FF00 : 0xFFFF0000, true));

        if (dungeonDoor.getType().isExist()) {
            if (dungeonDoor.isZDir()) {
                DGWorldRenderer.record(new QuadState(new Vec3[] {
                        new Vec3(x - 0.5, y + 0.1, z - 2.5), new Vec3(x - 0.5, y+ 0.1, z + 2.5), new Vec3(x + 0.5, y+ 0.1, z + 2.5), new Vec3(x + 0.5, y+ 0.1, z - 2.5)
                }, 0xFF0000FF, true));
            } else {
                DGWorldRenderer.record(new QuadState(new Vec3[] {
                        new Vec3(x - 2.5, y+ 0.1, z - 0.5), new Vec3(x - 2.5, y+ 0.1, z + 0.5), new Vec3(x + 2.5, y+ 0.1, z + 0.5), new Vec3(x + 2.5, y+ 0.1, z - 0.5)
                }, 0xFF0000FF, true));
            }
        } else {
            int[] red = new int[] {0xFFFF0000, 0xFFFF0000, 0xFFFF0000, 0xFFFF0000};
            List<Vec3> loop1 = new ArrayList<>();
            loop1.add(new Vec3(x - 2.5, y, z - 2.5));
            loop1.add(new Vec3(x + 2.5, y + 5, z - 2.5));
            loop1.add(new Vec3(x + 2.5, y, z + 2.5));
            loop1.add(new Vec3(x - 2.5, y + 5, z + 2.5));
            DGWorldRenderer.record(new LineStripState(loop1, red, 5, true, true));
            List<Vec3> loop2 = new ArrayList<>();
            loop2.add(new Vec3(x - 2.5, y +5, z - 2.5));
            loop2.add(new Vec3(x + 2.5, y, z - 2.5));
            loop2.add(new Vec3(x + 2.5, y + 5, z + 2.5));
            loop2.add(new Vec3(x - 2.5, y, z + 2.5));
            DGWorldRenderer.record(new LineStripState(loop2, red, 5, true, true));
        }

        // 1.8.9 drew this with the depth test still on, at 0.02 scale
        DGWorldRenderer.record(new WorldTextState("Type: "+dungeonDoor.getType(),
                new Vec3(dungeonDoor.getPosition().getX(), dungeonDoor.getPosition().getY(), dungeonDoor.getPosition().getZ()),
                0xFF00FFFF, 0.02f, false, false, true));
    }

    public static void drawLine(Vec3 pos1, Vec3 pos2, Color colour, float partialTicks , boolean depth) {
        List<Vec3> points = new ArrayList<>();
        points.add(pos1);
        points.add(pos2);
        DGWorldRenderer.record(new LineStripState(points, new int[] {colour.getRGB(), colour.getRGB()}, 2, false, depth));
    }

    public static void drawLinesVec3(List<Vec3> poses, AColor colour, float thickness, float partialTicks, boolean depth) {
        int[] colors = new int[poses.size()];
        for (int num = 0; num < colors.length; num++) {
            colors[num] = getColorAt(num * 10,0, colour);
        }
        DGWorldRenderer.record(new LineStripState(new ArrayList<>(poses), colors, thickness, false, depth));
    }
    public static void drawLines(List<BlockPos> poses, AColor colour, float thickness, float partialTicks, boolean depth) {
        List<Vec3> points = new ArrayList<>();
        int[] colors = new int[poses.size()];
        int num = 0;
        for (BlockPos pos:poses) {
            points.add(new Vec3(pos.getX() +0.5, pos.getY() +0.5, pos.getZ() +0.5));
            colors[num] = getColorAt(num * 10,0, colour);
            num++;
        }
        DGWorldRenderer.record(new LineStripState(points, colors, thickness, false, depth));
    }

    public static void drawLines(List<BlockPos> poses, Color colour, float thickness, float partialTicks, boolean depth) {
        // 1.8.9 drew the AColor version and then this one over it
        if (colour instanceof AColor) drawLines(poses, (AColor)colour, thickness, partialTicks,depth);
        List<Vec3> points = new ArrayList<>();
        int[] colors = new int[poses.size()];
        int num = 0;
        for (BlockPos pos:poses) {
            points.add(new Vec3(pos.getX() +0.5, pos.getY() +0.5, pos.getZ() +0.5));
            colors[num++] = colour.getRGB();
        }
        DGWorldRenderer.record(new LineStripState(points, colors, thickness, false, depth));
    }

    public static void highlightBlock(BlockPos blockpos, Color c, float partialTicks) {
        highlightBlock(blockpos,c,partialTicks,false);
    }
    public static void _highlightBlock(BlockPos blockpos, Color c, float partialTicks, boolean depth) {
        highlightBlock(blockpos, c, partialTicks, depth);
    }

    public static void highlightBlock(BlockPos blockpos, Color c, float partialTicks, boolean depth) {
        DGWorldRenderer.record(new FilledBoxState(new AABB(blockpos), c.getRGB(), depth));
    }


    // TODO: D72, 1.8.9 masked the highlight to the block's model with the stencil buffer, 26.1 draws the box
    public static AABB highlightBlockStencil(BlockPos blockPos, float partialTicks, Color color, boolean depth) {
        AABB bb = new AABB(blockPos);
        RenderUtils.highlightBox(bb, color, partialTicks, false);
        return bb;
    }
    // TODO: D72, 1.8.9 masked the highlight to the blocks' models with the stencil buffer, 26.1 draws the box
    public static AABB highlightBlocksStencil(List<BlockPos> blockPos, float partialTicks, Color color, boolean depth) {
        double minX = 1e9, minY = 1e9, minZ = 1e9, maxX = -1e9, maxY = -1e9, maxZ = -1e9;
        for (BlockPos pos : blockPos) {
            minX = Math.min(minX, pos.getX());
            maxX = Math.max(maxX, pos.getX() + 1);
            minY = Math.min(minY, pos.getY());
            maxY = Math.max(maxY, pos.getY() + 1);
            minZ = Math.min(minZ, pos.getZ());
            maxZ = Math.max(maxZ, pos.getZ() + 1);
        }
        AABB bb= new AABB(minX, minY, minZ, maxX, maxY, maxZ);
        RenderUtils.highlightBox(bb, color, partialTicks, false);
        return bb;
    }

    public static void highlightBox(AABB  axisAlignedBB, Color c, float partialTicks, boolean depth) {
        DGWorldRenderer.record(new FilledBoxState(axisAlignedBB, c.getRGB(), depth));
    }
    public static void highlightBoxAColor(AABB  axisAlignedBB, AColor c, float partialTicks, boolean depth) {
        int rgb = RenderUtils.getColorAt(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.minZ, c);
        DGWorldRenderer.record(new FilledBoxState(axisAlignedBB, rgb, depth));
    }

    // 1.8.9's default box around an entity's position, by entity type
    private static AABB entityBox(Entity entity) {
        if (entity instanceof ArmorStand) {
            return new AABB(-0.4, -1.5, -0.4, 0.4, 0, 0.4);
        } else if (entity instanceof Bat) {
            return new AABB(-0.4, -1.4, -0.4, 0.4, 0.4, 0.4);
        } else {
            return new AABB(-0.4, -1.5, -0.4, 0.4, 0, 0.4);
        }
    }

    public static void highlightBox(Entity entity, AABB  axisAlignedBB, AColor c, float partialTicks, boolean depth) {
        int rgb = RenderUtils.getColorAt(entity.getX() * 10,entity.getY() * 10,c);
        if (axisAlignedBB == null) axisAlignedBB = entityBox(entity);
        DGWorldRenderer.record(new FilledBoxState(axisAlignedBB.move(entity.getPosition(partialTicks)), rgb, depth));
    }

    public static void highlightBox(Entity entity, AABB  axisAlignedBB, Color c, float partialTicks, boolean depth) {
        if (axisAlignedBB == null) axisAlignedBB = entityBox(entity);
        DGWorldRenderer.record(new FilledBoxState(axisAlignedBB.move(entity.getPosition(partialTicks)), c.getRGB(), depth));
    }
    public static void highlightBox(Entity entity, Color c, float partialTicks, boolean depth) {
        DGWorldRenderer.record(new FilledBoxState(entityBox(entity).move(entity.getPosition(partialTicks)), c.getRGB(), depth));
    }

    public static void highlightBox(Entity entity, AColor c, float partialTicks, boolean depth) {
        int rgb = RenderUtils.getColorAt(entity.getX() % 20,entity.getY() % 20,c);
        DGWorldRenderer.record(new FilledBoxState(entityBox(entity).move(entity.getPosition(partialTicks)), rgb, depth));
    }
    public static void drawTextAtWorld(String text, float x, float y, float z, int color, float scale, boolean increase, boolean renderBlackBox, float partialTicks) {
        DGWorldRenderer.record(new WorldTextState(text, new Vec3(x, y, z), color, scale, increase, renderBlackBox, false));
    }

    public static void drawTextAtWorldDepth(String text, float x, float y, float z, int color, float scale, boolean increase, boolean renderBlackBox, float partialTicks) {
        DGWorldRenderer.record(new WorldTextState(text, new Vec3(x, y, z), color, scale, increase, renderBlackBox, true));
    }

    public static Vector3f getRenderPos(float x, float y, float z, float partialTicks) {
        // 1.8.9 used the interpolated player position, 26.1 draws relative to the camera
        Vec3 camera = Minecraft.getInstance().gameRenderer.getMainCamera().position();
        return new Vector3f(
                x - (float) camera.x,
                y - (float) camera.y,
                z - (float) camera.z
        );
    }
}
