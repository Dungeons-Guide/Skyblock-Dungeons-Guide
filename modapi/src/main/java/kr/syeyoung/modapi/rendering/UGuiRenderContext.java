package kr.syeyoung.modapi.rendering;

import kr.syeyoung.modapi.data.ResourceIdentifier;
import kr.syeyoung.modapi.data.URect;
import kr.syeyoung.modapi.data.USize;
import kr.syeyoung.modapi.entity.UEntityLiving;
import kr.syeyoung.modapi.item.UItemStack;
import java.awt.*;
import java.util.List;

public interface UGuiRenderContext extends URenderContext {
    void drawRect(double left, double top, double right, double bottom, int color);

    void drawUnfilledBox(int left, int top, int right, int bottom, int color, boolean isChroma, float chromaSpeed, float width);

    void drawUnfilledBox(int left, int top, int right, int bottom, int color, boolean isChroma, float width);

    void drawScaledCustomSizeModalRect(ResourceIdentifier resourceIdentifier, double x, double y, float u, float v, int uWidth, int vHeight, double width, double height, float tileWidth, float tileHeight);

    Rectangle currentClip();

    boolean canDraw(URect bounds);

    void pushClip(URect absBounds, USize size, double x, double y, double width, double height);

    void popClip();

    void drawPassthrough(int scaledX, int scaledY, double xInScreen, double yInScreen, double widthInScreen, double heightInScreen, double scaledWidth, double scaledHeight);

    void drawLineStipple(double x1, double y1, double x2, double y2, float lineWidth, int color, Integer factor, Short pattern);

    void drawLine(double x1, double y1, double x2, double y2, int color, float lineWidth);

    void drawString(String value, int x, int y, int color);

    void drawStringWithShadow(String value, int x, int y, int color);

    void drawSplitString(String value, int x, int y, int maxWidth, int color);

    void drawItemStackAndEffect(UItemStack itemStack, int x, int y);

    void drawHoveringText(List<String> tooltip, int x, int y, int width, int height, int maxTextWidth);

    void drawGradientRoundRect(float radius, float halfWidth, float halfHeight, float centerX, float centerY, float smoothness, int x, int y, double width, double height, int startColor, int endColor);

    void drawRoundRect(float radius, float halfWidth, float halfHeight, float centerX, float centerY, float smoothness, int x, int y, double width, double height, int color);

    void drawChromaGradientRoundRect(float radius, float halfWidth, float halfHeight, float centerX, float centerY, float smoothness, int x, int y, double width, double height, int startColor, int endColor);

    void drawChromaRoundRect(float radius, float halfWidth, float halfHeight, float centerX, float centerY, float smoothness, int x, int y, double width, double height, int color);

    void drawEntityOnScreen(int x, int y, int scale, float mouseX, int mouseY, UEntityLiving fakePlayer);

    void drawChromaCircle(int x, int y, double width, double height, double rad, float value, float centerX, float centerY, float smoothness);

    void drawDonut(int x, int y, double width, double height, int color, float rad, float thickness, float centerX, float centerY, float smoothness);

    void drawEtherwarpPreviewBackground(double halfWidth, double offset, double leeway, double radius, double centerX, double centerY);
}
