/*
 *     Dungeons Guide - The most intelligent Hypixel Skyblock Dungeons Mod
 *     Copyright (C) 2021  cyoung06
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Affero General Public License as published
 *     by the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Affero General Public License for more details.
 *
 *     You should have received a copy of the GNU Affero General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

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
import org.apache.commons.lang3.StringUtils;
import org.lwjgl.opengl.GL11;

import java.awt.*;

public class MPortableColorEdit extends MTooltip {

    private final float[] hsv = new float[3];
    private float alpha = 0;
    private float chromaSpeed = 0;

    @Getter
    private AColor color;

    private final MTextField textField;

    public MPortableColorEdit() {
        textField = new MTextField() {
            @Override
            public void edit(String str) {
                if (str.length() >= 7 && str.startsWith("#")) {
                    String color = str.substring(1);
                    try {
                        long colorInt = Long.parseLong(color, 16);

                        Color.RGBtoHSB((int) (colorInt >> 16) & 0xFF, (int) (colorInt >> 8) & 0xFF, (int) colorInt & 0xFF, hsv);
                        if (color.length() >= 8)
                            alpha = ((int) ((colorInt >> 24) & 0xFF)) / 255.0f;
                        update2();
                    } catch (Exception e) {}
                }
            }
        };
        add(textField);
    }

    @Override
    public void onBoundsUpdate() {
        super.onBoundsUpdate();

        textField.setBounds(new Rectangle(5, getEffectiveDimension().height - 25, getEffectiveDimension().width - 10, 20));
    }

    public void setColor(AColor color) {
        this.color  = color;

        alpha = color.getAlpha() / 255.0f;
        chromaSpeed = color.isChroma() ? color.getChromaSpeed() : 0;
        Color.RGBtoHSB(color.getRed(), color.getBlue(), color.getGreen(), hsv);

        int rgb = Color.HSBtoRGB(hsv[0], hsv[1], hsv[2]);
        rgb = (rgb & 0xFFFFFF) | ((int)(alpha * 255) << 24);
        textField.setText("#" + StringUtils.leftPad(Integer.toHexString(rgb).toUpperCase(), 8, '0'));
    }

    @Override
    public void render(int absMousex, int absMousey, int relMousex0, int relMousey0, float partialTicks, Rectangle scissor) {
        Dimension size = getEffectiveDimension();

        Gui.drawRect(0,0,size.width,size.height, 0xff333333);
        Gui.drawRect(1,1,size.width-1,size.height-1, 0xffa1a1a1);

        int width = size.height- 35;
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();
        int shademodel = GL11.glGetInteger(GL11.GL_SHADE_MODEL);
        GlStateManager.shadeModel(GL11.GL_SMOOTH);
        GlStateManager.enableBlend();
        GlStateManager.disableDepth();
        GlStateManager.disableTexture2D();
        GlStateManager.disableCull();
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
        rgb = RenderUtils.getChromaColorAt(0,0, chromaSpeed, hsv[1], hsv[2], alpha);
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

    private int selected = 0;

    @Override
    public void mouseClicked(int absMouseX, int absMouseY, int relMouseX, int relMouseY, int mouseButton) {
        int width = getEffectiveDimension().height- 35;
        float radius = width / 2f;
        float circleX = 5 + radius;
        float circleY = 5 + radius;

        selected = 0;
        if (!lastAbsClip.contains(absMouseX, absMouseY)) {
            close();
            return;
        }

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
        int width = getEffectiveDimension().height- 35;
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
        int rgb = Color.HSBtoRGB(hsv[0], hsv[1], hsv[2]);
        rgb = (rgb & 0xFFFFFF) | ((int)(alpha * 255) << 24);
        textField.setText("#" + StringUtils.leftPad(Integer.toHexString(rgb).toUpperCase(), 8, '0'));
        update2();
    }

    public void update2() {
        color = new AColor(Color.HSBtoRGB(hsv[0], hsv[1], hsv[2]) & 0xffffff | (MathHelper.clamp_int((int)(alpha * 255), 0, 255) << 24), true);
        color.setChromaSpeed(chromaSpeed);
        color.setChroma(chromaSpeed != 0);
    }
}
