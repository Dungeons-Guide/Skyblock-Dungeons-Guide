/*
 * Dungeons Guide - The most intelligent Hypixel Skyblock Dungeons Mod
 * Copyright (C) 2023  cyoung06 (syeyoung)
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

package kr.syeyoung.dungeonsguide.mod.guiv2.renderer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import org.lwjgl.opengl.GL11;

import java.awt.*;

/**
 * Why are render methods here not static?
 * Well, might put them all into one gigantic array and only do 1 call.
 */
public class RenderingContext {
//    public void clip(int x, int y, int width, int height) {
//        if (width < 0 || height < 0) return;
//
//        // transform the values to well... the thing
//        Rectangle valueinsystem = domElement.getRelativeBound();
//        Rectangle valueoutofsystem = domElement.getAbsBounds();
//
//        double xScale = valueoutofsystem.width / valueinsystem.getWidth();
//        double yScale = valueoutofsystem.height / valueinsystem.getHeight();
//
//        int resWidth = (int) Math.ceil(width * xScale);
//        int resHeight = (int) Math.ceil(height * yScale);
//        int resX = (int) (valueoutofsystem.x + x * xScale);
//        int resY = (int) (valueoutofsystem.y + y * yScale);
//
//        GL11.glScissor(resX, Minecraft.getMinecraft().displayHeight - (resY+resHeight), resWidth, resHeight);
//    }
    public void drawRect(double left, double top, double right, double bottom, int color) {
        double i;
        if (left < right) {
            i = left;
            left = right;
            right = i;
        }

        if (top < bottom) {
            i = top;
            top = bottom;
            bottom = i;
        }

        float f = (float)(color >> 24 & 255) / 255.0F;
        float g = (float)(color >> 16 & 255) / 255.0F;
        float h = (float)(color >> 8 & 255) / 255.0F;
        float j = (float)(color & 255) / 255.0F;
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldRenderer = tessellator.getWorldRenderer();
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.color(g, h, j, f);
        worldRenderer.begin(7, DefaultVertexFormats.POSITION);
        worldRenderer.pos(left, bottom, 0.0).endVertex();
        worldRenderer.pos(right, bottom, 0.0).endVertex();
        worldRenderer.pos(right, top, 0.0).endVertex();
        worldRenderer.pos(left, top, 0.0).endVertex();
        tessellator.draw();
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
    }

    public void clip(int i, int i1, double v, double v1) {
    }
}
