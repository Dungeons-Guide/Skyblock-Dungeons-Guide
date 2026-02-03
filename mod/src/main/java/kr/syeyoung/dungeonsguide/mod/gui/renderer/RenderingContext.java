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

package kr.syeyoung.dungeonsguide.mod.gui.renderer;

import kr.syeyoung.dungeonsguide.mod.config.types.AColor;
import kr.syeyoung.dungeonsguide.mod.gui.PassthroughManager;
import kr.syeyoung.dungeonsguide.mod.gui.primitive.Rect;
import kr.syeyoung.dungeonsguide.mod.gui.primitive.Size;
import kr.syeyoung.dungeonsguide.mod.shader.ShaderManager;
import kr.syeyoung.dungeonsguide.mod.shader.ShaderProgram;
import kr.syeyoung.dungeonsguide.mod.utils.RenderUtils;
import kr.syeyoung.modapi.ModAPI;
import kr.syeyoung.modapi.data.ResourceIdentifier;
import kr.syeyoung.modapi.entity.UEntityLiving;
import kr.syeyoung.modapi.item.UItemStack;
import kr.syeyoung.modapi.rendering.URenderContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.config.GuiUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;

import java.awt.*;
import java.util.List;
import java.util.Stack;

/**
 * Why are render methods here not static?
 * Well, might put them all into one gigantic array and only do 1 call.
 */
public class RenderingContext {
    private URenderContext renderContext;
    public RenderingContext(URenderContext context) {
        this.renderContext = context;
    }

    public URenderContext ctx() {
        return renderContext;
    }

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
    }
    public void drawUnfilledBox(int left, int top, int right, int bottom, AColor color, float width)
    {
        GL11.glLineWidth(width);
        if (left < right)
        {
            int i = left;
            left = right;
            right = i;
        }

        if (top < bottom)
        {
            int j = top;
            top = bottom;
            bottom = j;
        }

        float f3 = (float)(color.getRGB() >> 24 & 255) / 255.0F;
        float f = (float)(color.getRGB() >> 16 & 255) / 255.0F;
        float f1 = (float)(color.getRGB() >> 8 & 255) / 255.0F;
        float f2 = (float)(color.getRGB() & 255) / 255.0F;
        if (!color.isChroma() && f3 == 0) return;
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        if (!color.isChroma()) {
            GlStateManager.color(f, f1, f2, f3);
            worldrenderer.begin(GL11.GL_LINE_LOOP, DefaultVertexFormats.POSITION);
            worldrenderer.pos(left, bottom, 0.0D).endVertex();
            worldrenderer.pos(right, bottom, 0.0D).endVertex();
            worldrenderer.pos(right, top, 0.0D).endVertex();
            worldrenderer.pos(left, top, 0.0D).endVertex();
        } else {
            worldrenderer.begin(GL11.GL_LINE_LOOP, DefaultVertexFormats.POSITION_COLOR);
            GlStateManager.shadeModel(GL11.GL_SMOOTH);
            RenderUtils.color(worldrenderer.pos(left, bottom, 0.0D), RenderUtils.getColorAt(left, bottom, color)).endVertex();
            RenderUtils.color(worldrenderer.pos(right, bottom, 0.0D), RenderUtils.getColorAt(right, bottom, color)).endVertex();
            RenderUtils.color(worldrenderer.pos(right, top, 0.0D), RenderUtils.getColorAt(right, top, color)).endVertex();
            RenderUtils.color(worldrenderer.pos(left, top, 0.0D), RenderUtils.getColorAt(left, top, color)).endVertex();
        }
        tessellator.draw();
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
    }
    public void drawUnfilledBox(int left, int top, int right, int bottom, int color, boolean isChroma, float width)
    {
        GL11.glLineWidth(width);
        if (left < right)
        {
            int i = left;
            left = right;
            right = i;
        }

        if (top < bottom)
        {
            int j = top;
            top = bottom;
            bottom = j;
        }

        float f3 = (float)(color >> 24 & 255) / 255.0F;
        float f = (float)(color >> 16 & 255) / 255.0F;
        float f1 = (float)(color >> 8 & 255) / 255.0F;
        float f2 = (float)(color & 255) / 255.0F;
        if (!isChroma && f3 == 0) return;
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        if (!isChroma) {
            GlStateManager.color(f, f1, f2, f3);
            worldrenderer.begin(GL11.GL_LINE_LOOP, DefaultVertexFormats.POSITION);
            worldrenderer.pos(left, bottom, 0.0D).endVertex();
            worldrenderer.pos(right, bottom, 0.0D).endVertex();
            worldrenderer.pos(right, top, 0.0D).endVertex();
            worldrenderer.pos(left, top, 0.0D).endVertex();
        } else {
            worldrenderer.begin(GL11.GL_LINE_LOOP, DefaultVertexFormats.POSITION_COLOR);
            float blah = (System.currentTimeMillis()  / 10) % 360;
            GlStateManager.shadeModel(GL11.GL_SMOOTH);
            RenderUtils.color(worldrenderer.pos(left, bottom, 0.0D), Color.HSBtoRGB((((blah + 20) % 360) / 360.0f), 1, 1)).endVertex();
            RenderUtils.color(worldrenderer.pos(right, bottom, 0.0D), Color.HSBtoRGB((((blah + 40) % 360)  / 360.0f), 1, 1)).endVertex();
            RenderUtils.color(worldrenderer.pos(right, top, 0.0D), Color.HSBtoRGB((((blah + 20) % 360) / 360.0f), 1, 1)).endVertex();
            RenderUtils.color(worldrenderer.pos(left, top, 0.0D), Color.HSBtoRGB(blah / 360.0f, 1, 1)).endVertex();
        }
        tessellator.draw();
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
    }

    public void drawScaledCustomSizeModalRect(ResourceIdentifier resourceIdentifier, double x, double y, float u, float v, int uWidth, int vHeight, double width, double height, float tileWidth, float tileHeight) {
        double f = 1.0F / tileWidth;
        double g = 1.0F / tileHeight;

        if (resourceIdentifier != null) {
            Minecraft.getMinecraft().getTextureManager().bindTexture(new ResourceLocation(resourceIdentifier.getMod(), resourceIdentifier.getLocation()));
        }

        GlStateManager.enableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.color(1,1,1,1);
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldRenderer = tessellator.getWorldRenderer();
        worldRenderer.begin(7, DefaultVertexFormats.POSITION_TEX);
        worldRenderer.pos(x, (y + height), 0.0).tex((u * f), ((v + vHeight) * g)).endVertex();
        worldRenderer.pos((x + width), (y + height), 0.0).tex(((u + uWidth) * f), ((v + vHeight) * g)).endVertex();
        worldRenderer.pos((x + width), y, 0.0).tex(((u + uWidth) * f), (v * g)).endVertex();
        worldRenderer.pos(x, y, 0.0).tex((u * f), (v * g)).endVertex();
        tessellator.draw();
    }

    public Stack<Rectangle> clips = new Stack<>();

    public Rectangle currentClip() {
        return clips.empty() ? null : clips.peek();
    }


    public boolean canDraw(Rect bounds) {
        Rectangle clip = currentClip();
        if (clip == null) return true;

        if (clip.isEmpty() || bounds.getWidth() <= 0 || bounds.getHeight() <= 0) {
            return false;
        }
        double x0 = clip.x;
        double y0 = ModAPI.getAPI().getDisplayHeight() - clip.y - clip.height;
        return (bounds.getX() + bounds.getWidth() > x0 &&
                bounds.getY() + bounds.getHeight() > y0 &&
                bounds.getX() < x0 + clip.width &&
                bounds.getY() < y0 + clip.height);
    }

    public void pushClip(Rect absBounds, Size size, double x, double y, double width, double height) {
        if (width < 0 || height < 0) {
            width = 0;
            height = 0;
        }

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


        Rectangle newClip = new Rectangle(resX, ModAPI.getAPI().getDisplayHeight() - (resY+resHeight), resWidth, resHeight);
        newClip = previousClip.intersection(newClip);

        if (clips.size() == 0)
            GL11.glEnable(GL11.GL_SCISSOR_TEST);

        clips.push(newClip);

        if (newClip.width <= 0 || newClip.height <= 0)
            GL11.glColorMask(false, false ,false ,false);
        else
            GL11.glScissor(newClip.x, newClip.y, newClip.width, newClip.height);
    }

    public void popClip() {
        Rectangle currentClip = clips.pop();

        GL11.glColorMask(true, true ,true ,true);
        if (clips.size() == 0)
            GL11.glDisable(GL11.GL_SCISSOR_TEST);
        else {
            Rectangle newClip = clips.peek();
            if (newClip.width <= 0 || newClip.height <= 0)
                GL11.glColorMask(false, false ,false ,false);
            else
                GL11.glScissor(newClip.x, newClip.y, newClip.width, newClip.height);
        }
    }

    public void drawPassthrough(int scaledX, int scaledY, double xInScreen, double yInScreen, double widthInScreen, double heightInScreen, double scaledWidth, double scaledHeight) {

        Framebuffer framebuffer = PassthroughManager.INSTANCE.getFramebuffer();

        framebuffer.bindFramebufferTexture();
        drawRect(scaledX,scaledY,widthInScreen,heightInScreen, PassthroughManager.INSTANCE.getFogColor());

        drawScaledCustomSizeModalRect(null, scaledX, scaledY,
                (float) xInScreen,
                (float) yInScreen,
                (int) widthInScreen,
                (int) heightInScreen,
                scaledWidth,
                scaledHeight,
                ModAPI.getAPI().getDisplayWidth(),
                ModAPI.getAPI().getDisplayHeight());

        framebuffer.unbindFramebufferTexture();
    }

    public void drawLineStipple(double x1, double y1, double x2, double y2, float lineWidth, int color, Integer factor, Short pattern) {
        GL11.glLineStipple(factor, pattern);
        GL11.glLineWidth(lineWidth);
        GL11.glEnable(GL11.GL_LINE_STIPPLE);

        GlStateManager.color((color >> 16) & 0xFF, (color >> 8) & 0xFF, color & 0xFF,(color >> 24) & 0xFF);
        GlStateManager.disableTexture2D();


        GL11.glBegin(GL11.GL_LINES);

        GL11.glVertex2d(x1, y1);
        GL11.glVertex2d(x2, y2);
        GL11.glEnd();
        GlStateManager.enableTexture2D();

        GL11.glDisable(GL11.GL_LINE_STIPPLE);
    }

    public void drawLine(double x1, double y1, double x2, double y2, int color, float lineWidth) {
        GL11.glLineWidth(lineWidth);

        GlStateManager.color((color >> 16) & 0xFF, (color >> 8) & 0xFF, color & 0xFF,(color >> 24) & 0xFF);
        GlStateManager.disableTexture2D();

        GL11.glBegin(GL11.GL_LINES);

        GL11.glVertex2d(x1, y1);
        GL11.glVertex2d(x2, y2);
        GL11.glEnd();
        GlStateManager.enableTexture2D();
    }

    public void drawString(String value, int x, int y, int color) {
        GlStateManager.enableTexture2D();
        Minecraft.getMinecraft().fontRendererObj.drawString(value, x, y, color);
    }

    public void drawStringWithShadow(String value, int x, int y, int color) {
        GlStateManager.enableTexture2D();
        Minecraft.getMinecraft().fontRendererObj.drawStringWithShadow(value, x, y, color);
    }

    public void drawSplitString(String value, int x, int y, int maxWidth, int color) {
        GlStateManager.enableTexture2D();
        Minecraft.getMinecraft().fontRendererObj.drawSplitString(value, x, y, maxWidth, color);

    }

    public void drawItemStackAndEffect(UItemStack itemStack, int x, int y) {
        RenderItem renderItem=  Minecraft.getMinecraft().getRenderItem();
        GlStateManager.disableRescaleNormal();
        RenderHelper.enableGUIStandardItemLighting();
        GlStateManager.disableLighting();
        RenderHelper.disableStandardItemLighting();
        RenderHelper.enableGUIStandardItemLighting();
        GlStateManager.enableDepth();
        renderItem.renderItemAndEffectIntoGUI((ItemStack) itemStack.getItemStack(), x,y);
        GlStateManager.disableDepth();

    }

    public void drawHoveringText(List<String> tooltip, int x, int y, int width, int height, int maxTextWidth) {
        GlStateManager.disableBlend();
//        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.disableAlpha();
        GlStateManager.disableTexture2D();
        GlStateManager.enableCull();
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0);

        GuiUtils.drawHoveringText(tooltip, x, y, width, height, maxTextWidth, Minecraft.getMinecraft().fontRendererObj);
    }


    public void drawGradientRoundRect(float radius, float halfWidth, float halfHeight, float centerX, float centerY, float smoothness, int x, int y, double width, double height, int startColor, int endColor) {
        ShaderProgram shaderProgram = ShaderManager.getShader("shaders/roundrect");
        shaderProgram.useShader();
        shaderProgram.uploadUniform("radius", radius);
        shaderProgram.uploadUniform("halfSize", halfWidth, halfHeight);
        shaderProgram.uploadUniform("centerPos", centerX, centerY);
        shaderProgram.uploadUniform("smoothness", smoothness);

        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldRenderer = tessellator.getWorldRenderer();
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        worldRenderer.begin(7, DefaultVertexFormats.POSITION_COLOR);


        int r1, g1, b1, a1, r2, g2, b2, a2;
        r1 = (startColor >> 16) & 0xFF;
        g1 = (startColor >> 8) & 0xFF;
        b1 = (startColor) & 0xFF;
        a1 = (startColor >> 24) & 0xFF;
        r2 = (endColor >> 16) & 0xFF;
        g2 = (endColor >> 8) & 0xFF;
        b2 = (endColor) & 0xFF;
        a2 = (endColor >> 24) & 0xFF;

        worldRenderer.pos(x, y+height, 0.0).color(r2,g2,b2,a2).endVertex();
        worldRenderer.pos(x+width, y+height, 0.0).color(r2,g2,b2,a2).endVertex();
        worldRenderer.pos(x+width, y, 0.0).color(r1,g1,b1,a1).endVertex();
        worldRenderer.pos(x, y, 0.0).color(r1,g1,b1,a1).endVertex();
        tessellator.draw();
        GlStateManager.enableTexture2D();
        GL20.glUseProgram(0);
    }

    public void drawRoundRect(float radius, float halfWidth, float halfHeight, float centerX, float centerY, float smoothness, int x, int y, double width, double height, int color) {
        ShaderProgram shaderProgram = ShaderManager.getShader("shaders/roundrect");
        shaderProgram.useShader();
        shaderProgram.uploadUniform("radius", radius);
        shaderProgram.uploadUniform("halfSize", halfWidth, halfHeight);
        shaderProgram.uploadUniform("centerPos", centerX, centerY);
        shaderProgram.uploadUniform("smoothness", smoothness);

        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldRenderer = tessellator.getWorldRenderer();
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        worldRenderer.begin(7, DefaultVertexFormats.POSITION_COLOR);


        int r1, g1, b1, a1;
        r1 = (color >> 16) & 0xFF;
        g1 = (color >> 8) & 0xFF;
        b1 = (color) & 0xFF;
        a1 = (color >> 24) & 0xFF;

        worldRenderer.pos(x, y+height, 0.0).color(r1,g1,b1,a1).endVertex();
        worldRenderer.pos(x+width, y+height, 0.0).color(r1,g1,b1,a1).endVertex();
        worldRenderer.pos(x+width, y, 0.0).color(r1,g1,b1,a1).endVertex();
        worldRenderer.pos(x, y, 0.0).color(r1,g1,b1,a1).endVertex();
        tessellator.draw();
        GlStateManager.enableTexture2D();
        GL20.glUseProgram(0);
    }

    public void drawChromaGradientRoundRect(float radius, float halfWidth, float halfHeight, float centerX, float centerY, float smoothness, int x, int y, double width, double height, int startColor, int endColor) {
        ShaderProgram shaderProgram = ShaderManager.getShader("shaders/chromaroundrect");
        shaderProgram.useShader();
        shaderProgram.uploadUniform("radius", radius);
        shaderProgram.uploadUniform("halfSize", halfWidth, halfHeight);
        shaderProgram.uploadUniform("centerPos", centerX, centerY);
        shaderProgram.uploadUniform("smoothness", smoothness);

        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldRenderer = tessellator.getWorldRenderer();
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        worldRenderer.begin(7, DefaultVertexFormats.POSITION_COLOR);


        int r1, g1, b1, a1, r2, g2, b2, a2;
        r1 = (startColor >> 16) & 0xFF;
        g1 = (startColor >> 8) & 0xFF;
        b1 = (startColor) & 0xFF;
        a1 = (startColor >> 24) & 0xFF;
        r2 = (endColor >> 16) & 0xFF;
        g2 = (endColor >> 8) & 0xFF;
        b2 = (endColor) & 0xFF;
        a2 = (endColor >> 24) & 0xFF;

        worldRenderer.pos(x, y+height, 0.0).color(r2,g2,b2,a2).endVertex();
        worldRenderer.pos(x+width, y+height, 0.0).color(r2,g2,b2,a2).endVertex();
        worldRenderer.pos(x+width, y, 0.0).color(r1,g1,b1,a1).endVertex();
        worldRenderer.pos(x, y, 0.0).color(r1,g1,b1,a1).endVertex();
        tessellator.draw();
        GlStateManager.enableTexture2D();
        GL20.glUseProgram(0);
    }
    public void drawChromaRoundRect(float radius, float halfWidth, float halfHeight, float centerX, float centerY, float smoothness, int x, int y, double width, double height, int color) {
        ShaderProgram shaderProgram = ShaderManager.getShader("shaders/chromaroundrect");
        shaderProgram.useShader();
        shaderProgram.uploadUniform("radius", radius);
        shaderProgram.uploadUniform("halfSize", halfWidth, halfHeight);
        shaderProgram.uploadUniform("centerPos", centerX, centerY);
        shaderProgram.uploadUniform("smoothness", smoothness);

        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldRenderer = tessellator.getWorldRenderer();
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        worldRenderer.begin(7, DefaultVertexFormats.POSITION_COLOR);


        int r1, g1, b1, a1;
        r1 = (color >> 16) & 0xFF;
        g1 = (color >> 8) & 0xFF;
        b1 = (color) & 0xFF;
        a1 = (color >> 24) & 0xFF;

        worldRenderer.pos(x, y+height, 0.0).color(r1,g1,b1,a1).endVertex();
        worldRenderer.pos(x+width, y+height, 0.0).color(r1,g1,b1,a1).endVertex();
        worldRenderer.pos(x+width, y, 0.0).color(r1,g1,b1,a1).endVertex();
        worldRenderer.pos(x, y, 0.0).color(r1,g1,b1,a1).endVertex();
        tessellator.draw();
        GlStateManager.enableTexture2D();
        GL20.glUseProgram(0);
    }

    public void drawEntityOnScreen(int x, int y, int scale, float mouseX, int mouseY, UEntityLiving fakePlayer) {
        GlStateManager.enableDepth();
        GlStateManager.color(1, 1, 1, 1.0F);
        GuiInventory.drawEntityOnScreen(x,y,scale, mouseX, mouseY, (EntityLivingBase) fakePlayer.getHandle());
        GlStateManager.disableDepth();

    }

    public void drawChromaCircle(int x, int y, double width, double height, double rad, float value, float centerX, float centerY, float smoothness) {
        ShaderProgram shaderProgram = ShaderManager.getShader("shaders/chromacircle");
        shaderProgram.useShader();
        shaderProgram.uploadUniform("radius", (float) rad);
        shaderProgram.uploadUniform("value", value);
        shaderProgram.uploadUniform("centerPos", centerX, centerY);
        shaderProgram.uploadUniform("smoothness", smoothness);
        drawRect(x,y,width, height, 0xFFFFFFFF);
        GL20.glUseProgram(0);
    }

    public void drawDonut(int x, int y, double width, double height, int color, float rad, float thickness, float centerX, float centerY, float smoothness) {
        ShaderProgram shaderProgram = ShaderManager.getShader("shaders/donut");
        shaderProgram.useShader();
        shaderProgram.uploadUniform("radius", rad);
        shaderProgram.uploadUniform("thickness", thickness);
        shaderProgram.uploadUniform("centerPos", centerX, centerY);
        shaderProgram.uploadUniform("smoothness", smoothness);
        drawRect(x,y,width,height,color);
        GL20.glUseProgram(0);
    }
}
