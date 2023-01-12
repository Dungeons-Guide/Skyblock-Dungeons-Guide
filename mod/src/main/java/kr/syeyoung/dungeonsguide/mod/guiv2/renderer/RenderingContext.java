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

import kr.syeyoung.dungeonsguide.mod.guiv2.primitive.Position;
import kr.syeyoung.dungeonsguide.mod.guiv2.primitive.Rect;
import kr.syeyoung.dungeonsguide.mod.guiv2.primitive.Size;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.Stack;

/**
 * Why are render methods here not static?
 * Well, might put them all into one gigantic array and only do 1 call.
 */
public class RenderingContext {
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

    public Stack<Rectangle> clips = new Stack<>();

    public void pushClip(Rect absBounds, Size size, double x, double y, double width, double height) {
        if (width < 0 || height < 0) return;

        Rectangle previousClip;
        if (clips.size() == 0)
            previousClip = new Rectangle(0,0,Integer.MAX_VALUE, Integer.MAX_VALUE);
        else
            previousClip = clips.peek();

        double xScale = absBounds.getWidth() / size.getWidth();
        double yScale = absBounds.getHeight() / size.getHeight();

        int resWidth = (int) Math.ceil(width * xScale);
        int resHeight = (int) Math.ceil(height * yScale);
        int resX = (int) (absBounds.getX()+ x * xScale);
        int resY = (int) (absBounds.getY() + y * yScale);

        Rectangle newClip = new Rectangle(resX, Minecraft.getMinecraft().displayHeight - (resY+resHeight), resWidth, resHeight);
        newClip = previousClip.intersection(newClip);

        if (clips.size() == 0)
            GL11.glEnable(GL11.GL_SCISSOR_TEST);

        clips.push(newClip);

        GL11.glScissor(newClip.x, newClip.y, newClip.width, newClip.height);
    }

    public void popClip() {
        Rectangle currentClip = clips.pop();


        if (clips.size() == 0)
            GL11.glDisable(GL11.GL_SCISSOR_TEST);
        else {
            Rectangle newClip = clips.peek();
            GL11.glScissor(newClip.x, newClip.y, newClip.width, newClip.height);
        }
    }
}
