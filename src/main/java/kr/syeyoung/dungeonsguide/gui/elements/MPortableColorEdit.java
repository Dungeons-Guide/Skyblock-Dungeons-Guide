package kr.syeyoung.dungeonsguide.gui.elements;

import kr.syeyoung.dungeonsguide.config.types.AColor;
import kr.syeyoung.dungeonsguide.gui.MPanel;
import kr.syeyoung.dungeonsguide.utils.RenderUtils;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.MathHelper;
import org.lwjgl.opengl.GL11;

import java.awt.*;

public class MPortableColorEdit extends MPanel {

    private float[] hsv = new float[3];
    private float alpha = 0;
    private float chromaSpeed = 0;

    @Getter
    private AColor color;

    public void setColor(AColor color) {
        this.color  = color;

        alpha = color.getAlpha() / 255.0f;
        chromaSpeed = color.isChroma() ? color.getChromaSpeed() : 0;
        Color.RGBtoHSB(color.getRed(), color.getBlue(), color.getGreen(), hsv);
    }

    @Override
    public void render(int absMousex, int absMousey, int relMousex0, int relMousey0, float partialTicks, Rectangle scissor) {

        Gui.drawRect(0,0,getSize().width,getSize().height, 0xff333333);
        Gui.drawRect(1,1,getSize().width-1,getSize().height-1, 0xffa1a1a1);

        int width = getBounds().height- 10;
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();
        int shademodel = GL11.glGetInteger(GL11.GL_SHADE_MODEL);
        GlStateManager.shadeModel(GL11.GL_SMOOTH);
        GlStateManager.enableBlend();
        GlStateManager.disableDepth();
        GlStateManager.disableTexture2D();
        GlStateManager.disableCull();;
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        //        worldrenderer.begin(GL11.GL_TRIANGLES, DefaultVertexFormats.POSITION_COLOR);

        int rgb = Color.HSBtoRGB(hsv[0], hsv[1], 1);
        float r = (rgb >> 16 & 255) / 255.0f;
        float g = (rgb >> 8 & 255) / 255.0f;
        float b = (rgb & 255) / 255.0f;
        GL11.glBegin(GL11.GL_TRIANGLES);
        GlStateManager.color(0,0,0,alpha);GL11.glVertex3i(15+width ,5, 0);
        GlStateManager.color(0,0,0,alpha);GL11.glVertex3i(10+width , 5, 0);
        GlStateManager.color(r,g,b,alpha);GL11.glVertex3i(15+width , 5+width, 0);

        GlStateManager.color(0,0,0,alpha); GL11.glVertex3i(10+width , 5, 0);
        GlStateManager.color(r,g,b,alpha);GL11.glVertex3i(10+width , 5 + width, 0);
        GlStateManager.color(r,g,b,alpha);GL11.glVertex3i(15+width , 5+width, 0);
        GL11.glEnd();
        rgb = Color.HSBtoRGB(hsv[0], hsv[1], hsv[2]);
        r = (rgb >> 16 & 255) / 255.0f;
        g = (rgb >> 8 & 255) / 255.0f;
        b = (rgb & 255) / 255.0f;
        GL11.glBegin(GL11.GL_TRIANGLES);
        GlStateManager.color(r,g,b,0);GL11.glVertex3i(25+width ,5, 0);
        GlStateManager.color(r,g,b,0);GL11.glVertex3i(20+width , 5, 0);
        GlStateManager.color(r,g,b,1);GL11.glVertex3i(25+width , 5+width, 0);

                GlStateManager.color(r,g,b,0); GL11.glVertex3i(20+width , 5, 0);
                GlStateManager.color(r,g,b,1);GL11.glVertex3i(20+width , 5+ width, 0);
                GlStateManager.color(r,g,b,1);GL11.glVertex3i(25+width , 5+width, 0);
        GL11.glEnd();


        GL11.glBegin(GL11.GL_TRIANGLES);
        rgb = RenderUtils.getChromaColorAt(0,0, chromaSpeed);
        r = (rgb >> 16 & 255) / 255.0f;
        g = (rgb >> 8 & 255) / 255.0f;
        b = (rgb & 255) / 255.0f;
        GlStateManager.color(r,g,b,1);GL11.glVertex3i(35+width ,5, 0);
        GlStateManager.color(r,g,b,1);GL11.glVertex3i(30+width , 5, 0);
        GlStateManager.color(r,g,b,1);GL11.glVertex3i(35+width , 5+width, 0);

        GlStateManager.color(r,g,b,1); GL11.glVertex3i(30+width , 5, 0);
        GlStateManager.color(r,g,b,1);GL11.glVertex3i(30+width , 5+ width, 0);
        GlStateManager.color(r,g,b,1);GL11.glVertex3i(35+width , 5+width, 0);
        GL11.glEnd();



        float radius = width/2f;
        float cx = 5 + radius;
        float cy = 5 + radius;

        GL11.glBegin(GL11.GL_TRIANGLE_FAN);
        GlStateManager.color(1,1,1,alpha);
        GL11.glVertex3f(cx,cy,0);
        for (int i = 0; i <= 360; i++) {
            float rad = 3.141592653f * i / 180;
            int rgb2 = Color.HSBtoRGB(i / 360f, 1, hsv[2]);
            float r2 = (rgb2 >> 16 & 255) / 255.0f;
            float g2 = (rgb2 >> 8 & 255) / 255.0f;
            float b2 = (rgb2 & 255) / 255.0f;
            GlStateManager.color(r2,g2,b2, alpha);
            GL11.glVertex3f(MathHelper.cos(rad) * radius + cx, MathHelper.sin(rad) * radius + cy, 0);
        }
        GL11.glEnd();
        GlStateManager.shadeModel(shademodel);

        GlStateManager.color(1,1,1,1);
        worldrenderer.begin(GL11.GL_LINE_LOOP, DefaultVertexFormats.POSITION);
        float rad2 = 2 * 3.141592653f * hsv[0] ;
        float x = 5 + radius + (MathHelper.cos(rad2)) * hsv[1] * radius;
        float y = 5 + radius + (MathHelper.sin(rad2))* hsv[1] * radius;
        for (int i = 0; i < 100; i++) {
            float rad = 2 * 3.141592653f * (i / 100f);
            worldrenderer.pos(MathHelper.sin(rad) * 2 + x, MathHelper.cos(rad) * 2 + y, 0).endVertex();
        }
        tessellator.draw();

        worldrenderer.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION);
        worldrenderer.pos(8+width, 5 + (hsv[2]) * width, 0.5).endVertex();
        worldrenderer.pos(17+width, 5 + (hsv[2]) * width, 0.5).endVertex();
        tessellator.draw();

        worldrenderer.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION);
        worldrenderer.pos(18+width, 5 + (alpha) * width, 0.5).endVertex();
        worldrenderer.pos(27+width, 5 + (alpha) * width, 0.5).endVertex();
        tessellator.draw();

        worldrenderer.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION);
        worldrenderer.pos(28+width, 5 + (chromaSpeed) * width, 0.5).endVertex();
        worldrenderer.pos(37+width, 5 + (chromaSpeed) * width, 0.5).endVertex();
        tessellator.draw();

        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
        GlStateManager.color(1,1,1,1);
        GlStateManager.color(1,1,1,1);
    }

    @Override
    public void render0(ScaledResolution resolution, Point parentPoint, Rectangle parentClip, int absMousex, int absMousey, int relMousex0, int relMousey0, float partialTicks) {
        int relMousex = relMousex0 - getBounds().x;
        int relMousey = relMousey0 - getBounds().y;

        GlStateManager.translate(getBounds().x, getBounds().y, 0);
        GlStateManager.color(1,1,1,0);


        Rectangle absBound = getBounds().getBounds();
        absBound.setLocation(absBound.x + parentPoint.x, absBound.y + parentPoint.y);
        Rectangle clip = determineClip(parentClip, absBound);
        lastAbsClip = clip;

        clip(resolution, clip.x, clip.y, clip.width, clip.height);
        GlStateManager.pushAttrib();
        GL11.glDisable(GL11.GL_SCISSOR_TEST);

        GlStateManager.pushAttrib();
        GuiScreen.drawRect(0,0, getBounds().width, getBounds().height, backgroundColor.getRGB());
        GlStateManager.popAttrib();

        GlStateManager.pushMatrix();
        GlStateManager.pushAttrib();
        render(absMousex, absMousey, relMousex, relMousey, partialTicks, clip);
        GlStateManager.popAttrib();
        GlStateManager.popMatrix();

        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        GlStateManager.popAttrib();


        Point newPt = new Point(parentPoint.x + getBounds().x, parentPoint.y + getBounds().y);

        for (MPanel mPanel : getChildComponents()){
            GlStateManager.pushMatrix();
            GlStateManager.pushAttrib();
            mPanel.render0(resolution, newPt, clip, absMousex, absMousey, relMousex, relMousey, partialTicks);
            GlStateManager.popAttrib();
            GlStateManager.popMatrix();
        }
    }

    private int selected = 0;

    @Override
    public void mouseClicked(int absMouseX, int absMouseY, int relMouseX, int relMouseY, int mouseButton) {
        int width = getBounds().height- 10;
        float radius = width / 2f;
        float circleX = 5 + radius;
        float circleY = 5 + radius;

        selected = 0;
        if (!getBounds().contains(relMouseX, relMouseY)) return;

        {
            // check circle
            float dx = relMouseX - circleX;
            float dy = circleY - relMouseY;
            if (dx * dx + dy * dy <= radius * radius) {
                double theta = (MathHelper.atan2(dx, dy) / Math.PI * 180 + 270) % 360;
                hsv[0] = (float) theta / 360f;
                hsv[1] = MathHelper.sqrt_float(dx * dx + dy * dy) / radius;
                selected = 1;
            }
        }
        {
            if (10+width <= relMouseX && relMouseX <= 15 + width &&
                    5 <= relMouseY && relMouseY <= 5 + width) {
                hsv[2] = (relMouseY - 5) / (float)width;
                selected = 2;
            }
        }
        {
            if (20+width <= relMouseX && relMouseX <= 25 + width &&
                    5 <= relMouseY && relMouseY <= 5 + width) {
                alpha = (relMouseY - 5) / (float)width;
                selected = 3;
            }
        }
        {
            if (30+width <= relMouseX && relMouseX <= 35 + width &&
                    5 <= relMouseY && relMouseY <= 5 + width) {
                chromaSpeed = (relMouseY - 5) / (float)width;
                selected = 4;
            }
        }
        update();
    }

    @Override
    public void mouseClickMove(int absMouseX, int absMouseY, int relMouseX, int relMouseY, int clickedMouseButton, long timeSinceLastClick) {
        int width = getBounds().height- 10;
        float radius = width / 2f;
        float circleX = 5 + radius;
        float circleY = 5 + radius;
        {
            // check circle
            float dx = relMouseX - circleX;
            float dy = circleY - relMouseY;
            if (selected == 1) {
                double theta = (MathHelper.atan2(dx, dy) / Math.PI * 180 + 270) % 360;
                hsv[0] = (float) theta / 360f;
                hsv[1] = MathHelper.clamp_float(MathHelper.sqrt_float(dx * dx + dy * dy) / radius, 0, 1);
            }
        }
        {
            if (selected == 2) {
                hsv[2] = MathHelper.clamp_float((relMouseY - 5) / (float)width, 0, 1);
            }
            if (selected == 3) {
                alpha = MathHelper.clamp_float((relMouseY - 5) / (float)width, 0, 1);
            }
            if (selected == 4) {
                chromaSpeed = MathHelper.clamp_float((relMouseY - 5) / (float)width, 0, 1);
            }
        }
        update();
    }


    public void update() {
        color = new AColor(Color.HSBtoRGB(hsv[0], hsv[1], hsv[2]) & 0xffffff | (MathHelper.clamp_int((int)(alpha * 255), 0, 255) << 24), true);
        color.setChromaSpeed(chromaSpeed);
        color.setChroma(chromaSpeed != 0);
        update2();
    }

    public void update2() {

    }
}
