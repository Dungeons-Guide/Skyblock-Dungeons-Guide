package kr.syeyoung.modapi.v1_21_9.render;

import kr.syeyoung.modapi.ModAPI;
import kr.syeyoung.modapi.data.ResourceIdentifier;
import kr.syeyoung.modapi.data.URect;
import kr.syeyoung.modapi.data.USize;
import kr.syeyoung.modapi.entity.UEntityLiving;
import kr.syeyoung.modapi.item.UItemStack;
import kr.syeyoung.modapi.rendering.UGuiRenderContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.StringVisitable;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.joml.Matrix3x2fStack;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;

import java.awt.*;
import java.util.List;
import java.util.Stack;

public class UGuiRenderContextImpl implements UGuiRenderContext {
    public Stack<Rectangle> clips = new Stack<Rectangle>();

    private DrawContext context;
    private Matrix3x2fStack stack;
    public UGuiRenderContextImpl(DrawContext context) {
        this.context = context;
        this.stack = context.getMatrices();
    }


    @Override
    public void pushMatrix() {
        stack = stack.pushMatrix();
    }

    @Override
    public void popMatrix() {
        stack = stack.popMatrix();
    }

    @Override
    public void translate(double x, double y, double z) {
        stack.translate((float) x, (float) y);
    }

    @Override
    public void scale(double x, double y, double z) {
        stack.scale((float)x, (float)y);
    }

    @Override
    public void rotate(float angle, float x, float y, float z) {
        stack.rotate(angle);
    }


    public static int getColorAt(double x, double y, int color, boolean chroma, float chromaSpeed) {
        if (!chroma)
            return color;

        double blah = ((double)(chromaSpeed) * (System.currentTimeMillis() / 2)) % 360;
        float[] hsv = new float[3];
        Color.RGBtoHSB((color >> 16) & 0xFF, (color >> 8) & 0xFF, (color) & 0xFF, hsv);


        return (Color.HSBtoRGB((float) (((blah - (x + y) / 2.0f) % 360) / 360.0f), hsv[1],hsv[2]) & 0xffffff)
                | ((color) & 0xff000000);
    }

//    public static WorldRenderer color(WorldRenderer worldRenderer, int color ){
//        return worldRenderer.color(((color >> 16) & 0xFF) / 255.0f, ((color >> 8) & 0xFF) / 255.0f, (color &0xFF) / 255.0f, ((color >> 24) & 0xFF) / 255.0f);
//    }

    @Override
    public void drawRect(double left, double top, double right, double bottom, int color) {
        context.fill((int)left, (int)top, (int)right, (int)bottom, color);
    }

    @Override
    public void drawUnfilledBox(int left, int top, int right, int bottom, int color, boolean chroma, float chromaSpeed, float width) {
        if (left < right) {
            int i = left;
            left = right;
            right = i;
        }

        if (top < bottom) {
            int j = top;
            top = bottom;
            bottom = j;
        }
        context.drawStrokedRectangle(left, top, right-left, bottom-top, color);
        // TODO: Chroma $$
        if (!chroma) {
        } else {
//            worldrenderer.begin(GL11.GL_LINE_LOOP, DefaultVertexFormats.POSITION_COLOR);
//            GlStateManager.shadeModel(GL11.GL_SMOOTH);
//            color(worldrenderer.pos(left, bottom, 0.0D), getColorAt(left, bottom, color, chroma, chromaSpeed)).endVertex();
//            color(worldrenderer.pos(right, bottom, 0.0D),getColorAt(right, bottom, color, chroma, chromaSpeed)).endVertex();
//            color(worldrenderer.pos(right, top, 0.0D), getColorAt(right, top, color, chroma, chromaSpeed)).endVertex();
//            color(worldrenderer.pos(left, top, 0.0D), getColorAt(left, top, color, chroma, chromaSpeed)).endVertex();
        }
    }

    @Override
    public void drawUnfilledBox(int left, int top, int right, int bottom, int color, boolean isChroma, float width) {
        if (left < right) {
            int i = left;
            left = right;
            right = i;
        }

        if (top < bottom) {
            int j = top;
            top = bottom;
            bottom = j;
        }
        context.drawStrokedRectangle(left, top, right-left, bottom-top, color);
        // TODO: Chroma $$
    }

    @Override
    public void drawScaledCustomSizeModalRect(ResourceIdentifier resourceIdentifier, double x, double y, float u, float v, int uWidth, int vHeight, double width, double height, float tileWidth, float tileHeight) {
        context.drawTexture(RenderPipelines.GUI_TEXTURED, Identifier.of(resourceIdentifier.getMod(), resourceIdentifier.getLocation()),
                (int) x, (int) y, u, v, (int) width, (int) height, uWidth, vHeight, (int) tileWidth, (int) tileHeight);
    }

    @Override
    public Rectangle currentClip() {
        return clips.empty() ? null : clips.peek();
    }

    @Override
    public boolean canDraw(URect bounds) {
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

    @Override
    public void pushClip(URect absBounds, USize size, double x, double y, double width, double height) {
        if (width < 0 || height < 0) {
            width = 0;
            height = 0;
        }

        Rectangle previousClip;
        if (clips.size() == 0)
            previousClip = new Rectangle(0, 0, Integer.MAX_VALUE, Integer.MAX_VALUE);
        else
            previousClip = clips.peek();

        double xScale = absBounds.getWidth() / size.getWidth();
        double yScale = absBounds.getHeight() / size.getHeight();

        int resWidth = (int) Math.ceil(width * xScale);
        int resHeight = (int) Math.ceil(height * yScale);
        int resX = (int) (absBounds.getX() + x * xScale);
        int resY = (int) (absBounds.getY() + y * yScale);


        Rectangle newClip = new Rectangle(resX, ModAPI.getAPI().getDisplayHeight() - (resY + resHeight), resWidth, resHeight);
        newClip = previousClip.intersection(newClip);

        if (clips.size() == 0)
            GL11.glEnable(GL11.GL_SCISSOR_TEST);

        clips.push(newClip);

        if (newClip.width <= 0 || newClip.height <= 0)
            GL11.glColorMask(false, false, false, false);
        else
            GL11.glScissor(newClip.x, newClip.y, newClip.width, newClip.height);
    }

    @Override
    public void popClip() {
        Rectangle currentClip = clips.pop();

        GL11.glColorMask(true, true, true, true);
        if (clips.size() == 0)
            GL11.glDisable(GL11.GL_SCISSOR_TEST);
        else {
            Rectangle newClip = clips.peek();
            if (newClip.width <= 0 || newClip.height <= 0)
                GL11.glColorMask(false, false, false, false);
            else
                GL11.glScissor(newClip.x, newClip.y, newClip.width, newClip.height);
        }
    }

    @Override
    public void drawPassthrough(int scaledX, int scaledY, double xInScreen, double yInScreen, double widthInScreen, double heightInScreen, double scaledWidth, double scaledHeight) {

        // TODO: passthrough
        context.fill(scaledX, scaledY, (int)scaledWidth, (int) scaledHeight, 0xFFFFFFFF);
//        Framebuffer framebuffer = PassthroughManager.INSTANCE.getFramebuffer();
//
//        framebuffer.bindFramebufferTexture();
//        drawRect(scaledX, scaledY, widthInScreen, heightInScreen, PassthroughManager.INSTANCE.getFogColor());
//
//        drawScaledCustomSizeModalRect(null, scaledX, scaledY,
//                (float) xInScreen,
//                (float) yInScreen,
//                (int) widthInScreen,
//                (int) heightInScreen,
//                scaledWidth,
//                scaledHeight,
//                ModAPI.getAPI().getDisplayWidth(),
//                ModAPI.getAPI().getDisplayHeight());
//
//        framebuffer.unbindFramebufferTexture();
    }

    @Override
    public void drawLineStipple(double x1, double y1, double x2, double y2, float lineWidth, int color, Integer factor, Short pattern) {
//        GL11.glLineStipple(factor, pattern);
//        GL11.glLineWidth(lineWidth);
//        GL11.glEnable(GL11.GL_LINE_STIPPLE);
//
//        GlStateManager.color((color >> 16) & 0xFF, (color >> 8) & 0xFF, color & 0xFF, (color >> 24) & 0xFF);
//        GlStateManager.disableTexture2D();
//
//
//        GL11.glBegin(GL11.GL_LINES);
//
//        GL11.glVertex2d(x1, y1);
//        GL11.glVertex2d(x2, y2);
//        GL11.glEnd();
//        GlStateManager.enableTexture2D();
//
//        GL11.glDisable(GL11.GL_LINE_STIPPLE);
        // TODO: lines $$
    }

    @Override
    public void drawLine(double x1, double y1, double x2, double y2, int color, float lineWidth) {
        // TODO: lines $$;
    }

    @Override
    public void drawString(String value, int x, int y, int color) {
        context.drawText(MinecraftClient.getInstance().textRenderer, value, x, y, color, false);
    }

    @Override
    public void drawStringWithShadow(String value, int x, int y, int color) {
        context.drawText(MinecraftClient.getInstance().textRenderer, value, x, y, color, true);
    }

    @Override
    public void drawSplitString(String value, int x, int y, int maxWidth, int color) {
        context.drawWrappedText(MinecraftClient.getInstance().textRenderer, StringVisitable.plain(value), x, y, maxWidth, color, false);
    }

    @Override
    public void drawItemStackAndEffect(UItemStack itemStack, int x, int y) {
        context.drawItem((ItemStack) itemStack.getItemStack(), x, y);
    }

    @Override
    public void drawHoveringText(List<String> tooltip, int x, int y, int width, int height, int maxTextWidth) {
        context.drawTooltip(Text.of(String.join("\n", tooltip)), x, y);
    }

    @Override
    public void drawGradientRoundRect(float radius, float halfWidth, float halfHeight, float centerX, float centerY, float smoothness, int x, int y, double width, double height, int startColor, int endColor) {

        context.fillGradient(x,y, (int) width, (int) height, startColor, endColor);
        // TODO: ROND RECT $$
    }

    @Override
    public void drawRoundRect(float radius, float halfWidth, float halfHeight, float centerX, float centerY, float smoothness, int x, int y, double width, double height, int color) {
        context.fill(x,y, (int) width, (int) height, color);
        // TODO: ROND RECT $$

//        shaderProgram.useShader();
//        shaderProgram.uploadUniform("radius", radius);
//        shaderProgram.uploadUniform("halfSize", halfWidth, halfHeight);
//        shaderProgram.uploadUniform("centerPos", centerX, centerY);
//        shaderProgram.uploadUniform("smoothness", smoothness);
//
//        Tessellator tessellator = Tessellator.getInstance();
//        WorldRenderer worldRenderer = tessellator.getWorldRenderer();
//        GlStateManager.enableBlend();
//        GlStateManager.disableTexture2D();
//        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
//        worldRenderer.begin(7, DefaultVertexFormats.POSITION_COLOR);
//
//
//        int r1, g1, b1, a1;
//        r1 = (color >> 16) & 0xFF;
//        g1 = (color >> 8) & 0xFF;
//        b1 = (color) & 0xFF;
//        a1 = (color >> 24) & 0xFF;
//
//        worldRenderer.pos(x, y + height, 0.0).color(r1, g1, b1, a1).endVertex();
//        worldRenderer.pos(x + width, y + height, 0.0).color(r1, g1, b1, a1).endVertex();
//        worldRenderer.pos(x + width, y, 0.0).color(r1, g1, b1, a1).endVertex();
//        worldRenderer.pos(x, y, 0.0).color(r1, g1, b1, a1).endVertex();
//        tessellator.draw();
//        GlStateManager.enableTexture2D();
//        GL20.glUseProgram(0);
    }

    @Override
    public void drawChromaGradientRoundRect(float radius, float halfWidth, float halfHeight, float centerX, float centerY, float smoothness, int x, int y, double width, double height, int startColor, int endColor) {
        context.fillGradient(x,y, (int) width, (int) height, startColor, endColor);

    }

    @Override
    public void drawChromaRoundRect(float radius, float halfWidth, float halfHeight, float centerX, float centerY, float smoothness, int x, int y, double width, double height, int color) {
        context.fill(x,y, (int) width, (int) height, color);

    }

    @Override
    public void drawEntityOnScreen(int x, int y, int scale, float mouseX, int mouseY, UEntityLiving fakePlayer) {
//        context.draw
        InventoryScreen.drawEntity(context, x-100, y-100, x+100, y+100, 1, scale, mouseX, mouseY, (LivingEntity) fakePlayer.getHandle());
    }

    @Override
    public void drawChromaCircle(int x, int y, double width, double height, double rad, float value, float centerX, float centerY, float smoothness) {
//        ShaderProgram shaderProgram = ShaderManager.getShader("shaders/chromacircle");
//        shaderProgram.useShader();
//        shaderProgram.uploadUniform("radius", (float) rad);
//        shaderProgram.uploadUniform("value", value);
//        shaderProgram.uploadUniform("centerPos", centerX, centerY);
//        shaderProgram.uploadUniform("smoothness", smoothness);
//        drawRect(x, y, width, height, 0xFFFFFFFF);
//        GL20.glUseProgram(0);
        // TODO: chroma circle $$
    }

    @Override
    public void drawDonut(int x, int y, double width, double height, int color, float rad, float thickness, float centerX, float centerY, float smoothness) {
//        ShaderProgram shaderProgram = ShaderManager.getShader("shaders/donut");
//        shaderProgram.useShader();
//        shaderProgram.uploadUniform("radius", rad);
//        shaderProgram.uploadUniform("thickness", thickness);
//        shaderProgram.uploadUniform("centerPos", centerX, centerY);
//        shaderProgram.uploadUniform("smoothness", smoothness);
//        drawRect(x, y, width, height, color);
//        GL20.glUseProgram(0);
        // TODO: donut $$
    }

    @Override
    public void drawEtherwarpPreviewBackground(double halfWidth, double offset, double leeway, double radius, double centerX, double centerY) {
//        ShaderProgram shaderProgram = ShaderManager.getShader("shaders/etherwarppreview");
//        shaderProgram.useShader();
//        shaderProgram.uploadUniform("radius", (float) radius);
//        shaderProgram.uploadUniform("centerPos", (float) centerX, (float) centerY);
//        shaderProgram.uploadUniform("smoothness", 0.0f);
//
//        GlStateManager.color(1.0f, 0f, 0f, 0.3f);
//        GlStateManager.disableTexture2D();
//        GlStateManager.disableCull();
//
//        Tessellator tessellator = Tessellator.getInstance();
//        WorldRenderer worldRenderer = tessellator.getWorldRenderer();
//        worldRenderer.begin(GL11.GL_TRIANGLE_FAN, DefaultVertexFormats.POSITION);
//        worldRenderer.pos(0,offset * 16, 0).endVertex();
//        worldRenderer.pos(-halfWidth, -offset/0.5 * halfWidth + offset* 16, 0).endVertex();
//        worldRenderer.pos(-halfWidth, offset * 16, 0).endVertex();
//        tessellator.draw();
//        worldRenderer.begin(GL11.GL_TRIANGLE_FAN, DefaultVertexFormats.POSITION);
//        worldRenderer.pos(0,offset * 16, 0).endVertex();
//        worldRenderer.pos(halfWidth, -offset/0.5 * halfWidth + offset* 16, 0).endVertex();
//        worldRenderer.pos(halfWidth, offset * 16, 0).endVertex();
//        tessellator.draw();
//        drawRect(-halfWidth, offset* 16, halfWidth, 40, 0x4DFF0000);
//
//
//        // leeway...
//        // sample block is 2 right, 5 up
//        // top left: 1.5, -5 =>
//        // bottom right: 2.5, -4
//
//        {
//            double slope1 = (5+offset) / (1.5 - leeway);
//            double slope2 = (4+offset) / (2.5 + leeway);
//
//
//            GlStateManager.color(0.0f, 1f, 0f, 0.3f);
//            worldRenderer.begin(GL11.GL_TRIANGLE_FAN, DefaultVertexFormats.POSITION);
//            worldRenderer.pos(0,offset * 16, 0).endVertex();
//            worldRenderer.pos(-halfWidth, -offset/0.5 * halfWidth + offset* 16, 0).endVertex();
//            worldRenderer.pos(halfWidth, -slope1 * halfWidth + offset* 16, 0).endVertex();
//            worldRenderer.pos(24-leeway*16, -80, 0).endVertex();
//            worldRenderer.pos(24, -80, 0).endVertex();
//            worldRenderer.pos(24, -64, 0).endVertex();
//            worldRenderer.pos(40+leeway*16, -64, 0).endVertex();
//            worldRenderer.pos(halfWidth, -slope2 * halfWidth + offset* 16, 0).endVertex();
//            worldRenderer.pos(halfWidth, -offset/0.5 * halfWidth + offset* 16, 0).endVertex();
//
//            tessellator.draw();
//
//            GlStateManager.color(1.0f, 0f, 0f, 0.3f);
//            worldRenderer.begin(GL11.GL_TRIANGLE_STRIP, DefaultVertexFormats.POSITION);
//            worldRenderer.pos(24-leeway*16, -80, 0).endVertex();
//            worldRenderer.pos(40, -80, 0).endVertex();
//            worldRenderer.pos(halfWidth, -slope1 * halfWidth + offset* 16, 0).endVertex();
//            worldRenderer.pos(40, -64, 0).endVertex();
//            worldRenderer.pos(halfWidth, -slope2 * halfWidth + offset* 16, 0).endVertex();
//            worldRenderer.pos(40+leeway*16, -64, 0).endVertex();
////            worldRenderer.pos(halfWidth, -slope1 * halfWidth + offset* 16, 0).endVertex();
////            worldRenderer.pos(halfWidth, -slope2 * halfWidth + offset* 16, 0).endVertex();
//
//            tessellator.draw();
//        }
        // TODO: etherwarp $$

        GL20.glUseProgram(0);
    }

    @Override
    public void drawStringWithStyle(String text, double x, double y, kr.syeyoung.modapi.rendering.TextStyleConfig style) {
        if (text == null || text.isEmpty()) return;
        context.drawText(MinecraftClient.getInstance().textRenderer, text, (int)x,  (int)y, style.getTextColor(), style.isShadow());
//        DefaultFontRendererImpl.getInstance().renderString(text, x, y, style);
        // TODO: proper styled drawing $$
    }
}
