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

package kr.syeyoung.dungeonsguide.config.guiconfig;

import kr.syeyoung.dungeonsguide.config.types.GUIRectangle;
import kr.syeyoung.dungeonsguide.features.GuiFeature;
import kr.syeyoung.dungeonsguide.gui.MPanel;
import kr.syeyoung.dungeonsguide.utils.RenderUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;

import java.awt.*;

public class PanelDelegate extends MPanel {
    private final GuiFeature guiFeature;
    private boolean draggable = false;
    public PanelDelegate(GuiFeature guiFeature, boolean draggable) {
        this.guiFeature = guiFeature;
        this.draggable = draggable;
    }

    @Override
    public Rectangle getBounds() {
        Rectangle rectangle = guiFeature.getFeatureRect().getRectangle();
        return new Rectangle(rectangle.x, rectangle.y, rectangle.width, rectangle.height);
    }

    @Override
    public void render(int absMousex, int absMousey, int relMouseX, int relMouseY, float partialTicks, Rectangle scissor) {

        GlStateManager.pushMatrix();
        guiFeature.drawDemo(partialTicks);
        GlStateManager.popMatrix();
        if (!draggable) return;
        Gui.drawRect(0,0, 4, 4, 0xFFBBBBBB);
        Gui.drawRect(0, getBounds().height - 4, 4, getBounds().height, 0xFFBBBBBB);
        Gui.drawRect(getBounds().width - 4,0, getBounds().width, 4, 0xFFBBBBBB);
        Gui.drawRect(getBounds().width - 4,getBounds().height - 4, getBounds().width, getBounds().height, 0xFFBBBBBB);
        if (lastAbsClip.contains(absMousex, absMousey)) {
            if (relMouseX < 4 && relMouseY < 4) {
                Gui.drawRect(0,0, 4, 4, 0x55FFFFFF);
            } else if (relMouseX < 4 && relMouseY > getBounds().height - 4) {
                Gui.drawRect(0, getBounds().height - 4, 4, getBounds().height, 0x55FFFFFF);
            } else if (relMouseX > getBounds().width - 4 && relMouseY > getBounds().height - 4) {
                Gui.drawRect(getBounds().width - 4,getBounds().height - 4, getBounds().width, getBounds().height, 0x55FFFFFF);
            } else if (relMouseX > getBounds().width - 4 && relMouseY < 4) {
                Gui.drawRect(getBounds().width - 4,0, getBounds().width, 4, 0x55FFFFFF);
            } else if (selectedPart == -2){
                Gui.drawRect(0,0, getBounds().width, getBounds().height, 0x55FFFFFF);
            }
        }
        GlStateManager.enableBlend();
    }

    private int selectedPart = -2;

    private int lastX = 0;
    private int lastY = 0;

    private Rectangle internallyThinking;
    private Rectangle constraintApplied;

    public void applyConstraint() {
        Rectangle rectangle = internallyThinking.getBounds();

        int minWidth;
        int minHeight;
        if (guiFeature.isKeepRatio()) {
            minHeight = (int) Math.max(8, 8 / guiFeature.getDefaultRatio());
            minWidth = (int) (guiFeature.getDefaultRatio() * minHeight);
        } else {
            minWidth = 8;
            minHeight = 8;
        }

        if (Math.abs(rectangle.width) < minWidth) rectangle.width = rectangle.width < 0 ? -minWidth : minWidth;
        if (Math.abs(rectangle.height) < minHeight) rectangle.height = rectangle.height < 0 ? -minHeight : minHeight;

        if (guiFeature.isKeepRatio()) {
            double ratio = guiFeature.getDefaultRatio();

            if (ratio >= 1) {
                int width1 = Math.abs(rectangle.width);
                int height1 = (int) (width1 / ratio);
                rectangle.width = rectangle.width < 0 ? -width1 : width1;
                rectangle.height = rectangle.height < 0 ? -height1 : height1;
            } else {
                int width2 = (int) Math.abs(rectangle.height * ratio);
                int height2 = Math.abs(rectangle.height);
                rectangle.width = rectangle.width < 0 ? -width2 : width2;
                rectangle.height = rectangle.height < 0 ? -height2 : height2;
            }
        }

        if (rectangle.height < 0) {
            rectangle.height = -rectangle.height;
            rectangle.y -= rectangle.height;
        }

        if (rectangle.width < 0) {
            rectangle.width = -rectangle.width;
            rectangle.x -= rectangle.width;
        }

        if (rectangle.x < 0) rectangle.x = 0;
        if (rectangle.y < 0) rectangle.y = 0;
        if (rectangle.x + rectangle.width + 1 >=Minecraft.getMinecraft().displayWidth) rectangle.x = Minecraft.getMinecraft().displayWidth - rectangle.width - 1;
        if (rectangle.y + rectangle.height  + 1>= Minecraft.getMinecraft().displayHeight) rectangle.y = Minecraft.getMinecraft().displayHeight - rectangle.height - 1;


        constraintApplied = rectangle;
    }

    @Override
    public void mouseClicked(int absMouseX, int absMouseY, int relMouseX, int relMouseY, int mouseButton) {
        if (!draggable) return;
        if (!lastAbsClip.contains(absMouseX, absMouseY)) return;
        if (relMouseX < 4 && relMouseY < 4) {
            selectedPart = 0;
        } else if (relMouseX < 4 && relMouseY > getBounds().height - 4) {
            selectedPart = 2;
        } else if (relMouseX > getBounds().width - 4 && relMouseY > getBounds().height - 4) {
            selectedPart = 3;
        } else if (relMouseX > getBounds().width - 4 && relMouseY < 4) {
            selectedPart = 1;
        } else {
            selectedPart = -1;
        }
        lastX = absMouseX;
        lastY = absMouseY;
        internallyThinking = guiFeature.getFeatureRect().getRectangleNoScale();
        applyConstraint();

        throw new IllegalArgumentException("bruh, a hack to stop event progress");
    }

    @Override
    public void mouseReleased(int absMouseX, int absMouseY, int relMouseX, int relMouseY, int state) {
        if (!draggable) return;
        if (selectedPart >= -1) {
            guiFeature.setFeatureRect(new GUIRectangle(constraintApplied));
        }

        selectedPart = -2;
    }

    @Override
    public void mouseClickMove(int absMouseX, int absMouseY, int relMouseX, int relMouseY, int clickedMouseButton, long timeSinceLastClick) {
        if (!draggable) return;
        int dx = (absMouseX - lastX);
        int dy = (absMouseY - lastY);
        if (selectedPart >= 0) {
            Rectangle rectangle = internallyThinking;

            boolean revChangeX = (selectedPart & 0x1) == 0;
            boolean revChangeY = (selectedPart & 0x2) == 0;
            int prevWidth = rectangle.width;
            int prevHeight= rectangle.height;

            rectangle.width = prevWidth + (revChangeX ? -1 : 1) * dx;
            rectangle.height = prevHeight + (revChangeY ? -1 : 1 ) * dy;

            if (rectangle.height * prevHeight <= 0 && prevHeight != rectangle.height) {
                System.out.println("Flip!");
                rectangle.height += prevHeight < 0 ? 4 : -4;
            }
            if (rectangle.width * prevWidth <= 0 && prevWidth != rectangle.width) {
                System.out.println("Flip!");
                rectangle.width += prevWidth < 0 ? 4 : -4;
            }

            if (revChangeX) rectangle.x -= (rectangle.width - prevWidth );
            if (revChangeY) rectangle.y -= (rectangle.height - prevHeight);

            applyConstraint();
            guiFeature.setFeatureRect(new GUIRectangle(constraintApplied));
            lastX = absMouseX;
            lastY = absMouseY;
            throw new IllegalArgumentException("bruh, a hack to stop event progress");
        } else if (selectedPart == -1){
            Rectangle rectangle = internallyThinking;
            rectangle.translate(dx, dy);
            applyConstraint();
            guiFeature.setFeatureRect(new GUIRectangle(constraintApplied));
            lastX = absMouseX;
            lastY = absMouseY;
        }
    }
}
