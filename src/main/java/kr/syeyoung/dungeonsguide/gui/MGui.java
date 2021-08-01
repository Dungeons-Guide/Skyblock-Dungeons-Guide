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

package kr.syeyoung.dungeonsguide.gui;

import kr.syeyoung.dungeonsguide.gui.elements.MRootPanel;
import kr.syeyoung.dungeonsguide.utils.cursor.EnumCursor;
import kr.syeyoung.dungeonsguide.utils.cursor.GLCursors;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.LWJGLException;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.io.IOException;

public class MGui extends GuiScreen {

    @Getter
    private final MRootPanel mainPanel = new MRootPanel();

    public MGui(){
        try {
            Mouse.setNativeCursor(GLCursors.getCursor(EnumCursor.DEFAULT));
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    @Override
    public void initGui() {
        super.initGui();
        ScaledResolution scaledResolution = new ScaledResolution(Minecraft.getMinecraft());
        mainPanel.setBounds(new Rectangle(0,0,Minecraft.getMinecraft().displayWidth,Minecraft.getMinecraft().displayHeight));
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        try {

            int i = Mouse.getEventX();
            int j = this.mc.displayHeight - Mouse.getEventY();
            ScaledResolution scaledResolution = new ScaledResolution(Minecraft.getMinecraft());
            GlStateManager.pushMatrix();
            GlStateManager.pushAttrib();
            GlStateManager.disableLighting();
            GlStateManager.disableFog();
            GlStateManager.disableDepth();
            GL11.glDisable(GL11.GL_FOG);
            GlStateManager.enableBlend();
            GlStateManager.enableAlpha();
            GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0);
            GlStateManager.color(1, 1, 1, 1);
            GlStateManager.scale(1.0/scaledResolution.getScaleFactor(), 1.0/scaledResolution.getScaleFactor(), 1.0d);
            mainPanel.render0(1, new Point(0,0), new Rectangle(0,0,Minecraft.getMinecraft().displayWidth, Minecraft.getMinecraft().displayHeight), i, j, i, j, partialTicks);
            GlStateManager.popAttrib();
            GlStateManager.popMatrix();
            GlStateManager.enableBlend();
            GlStateManager.enableLighting();
        } catch (Throwable e) {
            e.printStackTrace();
        }
        GL11.glDisable(GL11.GL_SCISSOR_TEST);
    }

    @Override
    public void keyTyped(char typedChar, int keyCode) throws IOException {
        try {
            mainPanel.keyTyped0(typedChar, keyCode);
            super.keyTyped(typedChar, keyCode);
        } catch (Throwable e) {
            if (e.getMessage() == null || !e.getMessage().contains("hack to stop"))
                e.printStackTrace();
        }
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        try {
            super.mouseClicked(mouseX, mouseY, mouseButton);
            mainPanel.mouseClicked0(mouseX, mouseY
                    ,mouseX, mouseY, mouseButton);
        } catch (Throwable e) {
            if (e.getMessage() == null || !e.getMessage().contains("hack to stop"))
            e.printStackTrace();
        }
    }

    @Override
    public void onGuiClosed() {
        super.onGuiClosed();

        try {
            Mouse.setNativeCursor(null);
            mainPanel.setCursor(EnumCursor.DEFAULT);
        } catch (LWJGLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int state) {
        try {
            mainPanel.mouseReleased0(mouseX, mouseY
                    ,mouseX,mouseY , state);
        } catch (Throwable e) {
            if (e.getMessage() == null || !e.getMessage().contains("hack to stop"))
            e.printStackTrace();
        }
    }

    @Override
    public void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
        try {
            mainPanel.mouseClickMove0(mouseX, mouseY
                    ,mouseX ,mouseY, clickedMouseButton, timeSinceLastClick);
        } catch (Throwable e) {
            if (e.getMessage() == null || !e.getMessage().contains("hack to stop"))
            e.printStackTrace();
        }
    }

    public void mouseMove(int mouseX, int mouseY) {
        try {
            mainPanel.mouseMoved0(mouseX, mouseY
                    ,mouseX ,mouseY);
        } catch (Throwable e) {
            if (e.getMessage() == null || !e.getMessage().contains("hack to stop"))
                e.printStackTrace();
        }
    }


    private int touchValue;
    private int eventButton;
    private long lastMouseEvent;


    private int lastX, lastY;
    @Override
    public void handleMouseInput() throws IOException {
        try {
            int i = Mouse.getEventX();
            int j = this.mc.displayHeight - Mouse.getEventY();
            int k = Mouse.getEventButton();

            if (Mouse.getEventButtonState())
            {
                if (this.mc.gameSettings.touchscreen && this.touchValue++ > 0)
                {
                    return;
                }

                this.eventButton = k;
                this.lastMouseEvent = Minecraft.getSystemTime();
                this.mouseClicked(i, j, this.eventButton);
            }
            else if (k != -1)
            {
                if (this.mc.gameSettings.touchscreen && --this.touchValue > 0)
                {
                    return;
                }

                this.eventButton = -1;
                this.mouseReleased(i, j, k);
            }
            else if (this.eventButton != -1 && this.lastMouseEvent > 0L)
            {
                long l = Minecraft.getSystemTime() - this.lastMouseEvent;
                this.mouseClickMove(i, j, this.eventButton, l);
            }
            if (lastX != i || lastY != j) {
                try {
                    EnumCursor prevCursor = mainPanel.getCurrentCursor();
                    mainPanel.setCursor(EnumCursor.DEFAULT);
                    this.mouseMove(i, j);
                    EnumCursor newCursor = mainPanel.getCurrentCursor();
                    if (prevCursor != newCursor) Mouse.setNativeCursor(GLCursors.getCursor(newCursor));
                } catch (Throwable e) {
                    if (e.getMessage() == null || !e.getMessage().contains("hack to stop"))
                        e.printStackTrace();
                }
            }


            int wheel = Mouse.getDWheel();
            if (wheel != 0) {
                mainPanel.mouseScrolled0(i, j,i,j, wheel);
            }
            lastX = i;
            lastY = j;
        } catch (Throwable e) {
                e.printStackTrace();
        }
    }


}
