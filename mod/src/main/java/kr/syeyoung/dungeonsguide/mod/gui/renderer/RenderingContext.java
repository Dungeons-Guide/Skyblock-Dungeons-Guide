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
import kr.syeyoung.dungeonsguide.mod.gui.primitive.Rect;
import kr.syeyoung.dungeonsguide.mod.gui.primitive.Size;
import kr.syeyoung.modapi.data.ResourceIdentifier;
import kr.syeyoung.modapi.entity.UEntityLiving;
import kr.syeyoung.modapi.item.UItemStack;
import kr.syeyoung.modapi.rendering.UGuiRenderContext;
import kr.syeyoung.modapi.rendering.URenderContext;

import java.awt.*;
import java.util.List;

/**
 * Why are render methods here not static?
 * Well, might put them all into one gigantic array and only do 1 call.
 */
public class RenderingContext {
    public final UGuiRenderContext uGuiRenderContext;
    private URenderContext renderContext;
    public RenderingContext(URenderContext context) {
        this.renderContext = context;
        uGuiRenderContext = context.getContext();
    }

    public URenderContext ctx() {
        return renderContext;
    }

    public void drawRect(double left, double top, double right, double bottom, int color) {

        uGuiRenderContext.drawRect(left, top, right, bottom, color);
    }
    public void drawUnfilledBox(int left, int top, int right, int bottom, AColor color, float width)
    {
        uGuiRenderContext.drawUnfilledBox(left, top, right, bottom, color.getRGB(), color.isChroma(), color.getChromaSpeed(), width);
    }
    public void drawUnfilledBox(int left, int top, int right, int bottom, int color, boolean isChroma, float width)
    {

        uGuiRenderContext.drawUnfilledBox(left, top, right, bottom, color, isChroma, width);
    }

    public void drawScaledCustomSizeModalRect(ResourceIdentifier resourceIdentifier, double x, double y, float u, float v, int uWidth, int vHeight, double width, double height, float tileWidth, float tileHeight) {

        uGuiRenderContext.drawScaledCustomSizeModalRect(resourceIdentifier, x, y, u, v, uWidth, vHeight, width, height, tileWidth, tileHeight);
    }

    public Rectangle currentClip() {
        return uGuiRenderContext.currentClip();
    }


    public boolean canDraw(Rect bounds) {

        return uGuiRenderContext.canDraw(bounds);
    }

    public void pushClip(Rect absBounds, Size size, double x, double y, double width, double height) {


        uGuiRenderContext.pushClip(absBounds, size, x, y, width, height);
    }

    public void popClip() {

        uGuiRenderContext.popClip();
    }

    public void drawPassthrough(int scaledX, int scaledY, double xInScreen, double yInScreen, double widthInScreen, double heightInScreen, double scaledWidth, double scaledHeight) {

        uGuiRenderContext.drawPassthrough(scaledX, scaledY, xInScreen, yInScreen, widthInScreen, heightInScreen, scaledWidth, scaledHeight);
    }

    public void drawLineStipple(double x1, double y1, double x2, double y2, float lineWidth, int color, Integer factor, Short pattern) {


        uGuiRenderContext.drawLineStipple(x1, y1, x2, y2, lineWidth, color, factor, pattern);
    }

    public void drawLine(double x1, double y1, double x2, double y2, int color, float lineWidth) {

        uGuiRenderContext.drawLine(x1, y1, x2, y2, color, lineWidth);
    }

    public void drawString(String value, int x, int y, int color) {
        uGuiRenderContext.drawString(value, x, y, color);
    }

    public void drawStringWithShadow(String value, int x, int y, int color) {
        uGuiRenderContext.drawStringWithShadow(value, x, y, color);
    }

    public void drawSplitString(String value, int x, int y, int maxWidth, int color) {

        uGuiRenderContext.drawSplitString(value, x, y, maxWidth, color);
    }

    public void drawItemStackAndEffect(UItemStack itemStack, int x, int y) {

        uGuiRenderContext.drawItemStackAndEffect(itemStack, x, y);
    }

    public void drawHoveringText(List<String> tooltip, int x, int y, int width, int height, int maxTextWidth) {
        //        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

        uGuiRenderContext.drawHoveringText(tooltip, x, y, width, height, maxTextWidth);
    }


    public void drawGradientRoundRect(float radius, float halfWidth, float halfHeight, float centerX, float centerY, float smoothness, int x, int y, double width, double height, int startColor, int endColor) {


        uGuiRenderContext.drawGradientRoundRect(radius, halfWidth, halfHeight, centerX, centerY, smoothness, x, y, width, height, startColor, endColor);
    }

    public void drawRoundRect(float radius, float halfWidth, float halfHeight, float centerX, float centerY, float smoothness, int x, int y, double width, double height, int color) {


        uGuiRenderContext.drawRoundRect(radius, halfWidth, halfHeight, centerX, centerY, smoothness, x, y, width, height, color);
    }

    public void drawChromaGradientRoundRect(float radius, float halfWidth, float halfHeight, float centerX, float centerY, float smoothness, int x, int y, double width, double height, int startColor, int endColor) {


        uGuiRenderContext.drawChromaGradientRoundRect(radius, halfWidth, halfHeight, centerX, centerY, smoothness, x, y, width, height, startColor, endColor);
    }
    public void drawChromaRoundRect(float radius, float halfWidth, float halfHeight, float centerX, float centerY, float smoothness, int x, int y, double width, double height, int color) {


        uGuiRenderContext.drawChromaRoundRect(radius, halfWidth, halfHeight, centerX, centerY, smoothness, x, y, width, height, color);
    }

    public void drawEntityOnScreen(int x, int y, int scale, float mouseX, int mouseY, UEntityLiving fakePlayer) {

        uGuiRenderContext.drawEntityOnScreen(x, y, scale, mouseX, mouseY, fakePlayer);
    }

    public void drawChromaCircle(int x, int y, double width, double height, double rad, float value, float centerX, float centerY, float smoothness) {
        uGuiRenderContext.drawChromaCircle(x, y, width, height, rad, value, centerX, centerY, smoothness);
    }

    public void drawDonut(int x, int y, double width, double height, int color, float rad, float thickness, float centerX, float centerY, float smoothness) {
        uGuiRenderContext.drawDonut(x, y, width, height, color, rad, thickness, centerX, centerY, smoothness);
    }
}
