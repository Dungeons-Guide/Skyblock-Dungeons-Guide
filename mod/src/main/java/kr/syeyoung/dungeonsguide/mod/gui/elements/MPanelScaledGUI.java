/*
 * Dungeons Guide - The most intelligent Hypixel Skyblock Dungeons Mod
 * Copyright (C) 2021  cyoung06
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

package kr.syeyoung.dungeonsguide.mod.gui.elements;

import kr.syeyoung.dungeonsguide.mod.gui.MPanel;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.opengl.GL11;

import java.awt.*;

public class MPanelScaledGUI extends MPanel {
    @Getter
    protected double scale = 1.0;
    @Getter
    protected double relativeScale;

    public void setScale(double scale) {
        this.scale = scale;
        for (MPanel childComponent : childComponents) {
            childComponent.resize0((int) (getBounds().width/scale), (int) (getBounds().height/scale));
        }
        onBoundsUpdate();
    }

    @Override
    public void setBounds(Rectangle bounds) {
        if (bounds == null) return;
        this.bounds.x = bounds.x;
        this.bounds.y = bounds.y;
        this.bounds.width = bounds.width;
        this.bounds.height = bounds.height;

        for (MPanel childComponent : childComponents) {
            childComponent.resize0((int) (getBounds().width/scale), (int) (getBounds().height/scale));
        }
        onBoundsUpdate();
    }

    public Dimension getEffectiveDimension() {
        return new Dimension((int)(getBounds().width / scale), (int)(getBounds().height / scale));
    }

    @Override
    public void render0(double parentScale, Point parentPoint, Rectangle parentClip, int absMousex0, int absMousey0, int relMousex0, int relMousey0, float partialTicks) {
        lastParentPoint = parentPoint;

        GlStateManager.translate(getBounds().x, getBounds().y, 5);
        GlStateManager.color(1,1,1,1);

        Rectangle absBound = getBounds().getBounds();
        absBound.setLocation(absBound.x + parentPoint.x, absBound.y + parentPoint.y);

        Rectangle clip;
        if (isIgnoreBoundOnClip()) clip = parentClip;
        else clip = determineClip(parentClip, absBound);
        lastAbsClip = clip;

        if (clip.getSize().height * clip.getSize().width == 0) return;

        int absMousex = (int) (absMousex0 / scale), absMousey = (int) (absMousey0 / scale);
        int relMousex = (int) ((relMousex0 - getBounds().x) / scale);
        int relMousey = (int) ((relMousey0 - getBounds().y) /scale);

        // FROM HERE, IT IS SCALED

        GlStateManager.scale(this.scale, this.scale, 1);
        clip = new Rectangle((int) (clip.x / scale), (int) (clip.y / scale), (int) (clip.width / scale), (int) (clip.height / scale));
        lastAbsClip = clip;


        this.relativeScale = parentScale * this.scale;
        clip(clip.x, clip.y, clip.width, clip.height);



        GL11.glEnable(GL11.GL_SCISSOR_TEST);


        GuiScreen.drawRect(0,0, (int) (getBounds().width / scale), (int) (getBounds().height / scale),  backgroundColor.getRGB());
        GlStateManager.enableBlend();


        GlStateManager.pushMatrix();


        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.enableBlend();
        GlStateManager.enableAlpha();
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0);

        render(absMousex, absMousey, relMousex, relMousey, partialTicks, clip);

        GlStateManager.popMatrix();

        GL11.glDisable(GL11.GL_SCISSOR_TEST);



        Point newPt = new Point((int) ((parentPoint.x + getBounds().x) / scale), (int) ((parentPoint.y + getBounds().y) / scale));

        for (MPanel mPanel : getChildComponents()){
            GlStateManager.pushMatrix();

            mPanel.render0(relativeScale, newPt,clip,absMousex, absMousey, relMousex, relMousey, partialTicks);

            GlStateManager.popMatrix();
        }
    }

    @Override
    public void clip(int x, int y, int width, int height) {
        if (width < 0 || height < 0) return;

        GL11.glScissor((int) (x  * relativeScale), Minecraft.getMinecraft().displayHeight - (int) ((y + height+1) * relativeScale), (int)((width+1)* relativeScale), (int) ((height+1) * relativeScale));
    }

    @Override
    public void resize0(int parentWidth, int parentHeight) {
        super.resize0(parentWidth, parentHeight);
    }


    public boolean mouseClicked0(int absMouseX, int absMouseY, int relMouseX0, int relMouseY0, int mouseButton) {
        int relMousex = (int) ((relMouseX0 - getBounds().x)  / scale);
        int relMousey = (int) ((relMouseY0 - getBounds().y) / scale);
        absMouseX = (int) (absMouseX / scale);
        absMouseY = (int) (absMouseY / scale);

        boolean noClip = true;
        boolean focusedOverall = false;
        for (MPanel childComponent : getChildComponents()) {
            if (childComponent.mouseClicked0(absMouseX, absMouseY,relMousex, relMousey, mouseButton)) {
                noClip = false;
                focusedOverall = true;
            }
        }

        if (getBounds().contains(relMouseX0, relMouseY0) && noClip) {
            isFocused = true;
            focusedOverall = true;
        } else {
            isFocused = false;
        }
        mouseClicked(absMouseX, absMouseY, relMousex, relMousey, mouseButton);

        return focusedOverall;
    }
    public void mouseReleased0(int absMouseX, int absMouseY, int relMouseX0, int relMouseY0, int state) {
        int relMousex = (int) ((relMouseX0 - getBounds().x)  / scale);
        int relMousey = (int) ((relMouseY0 - getBounds().y) / scale);
        absMouseX = (int) (absMouseX / scale);
        absMouseY = (int) (absMouseY / scale);

        for (MPanel childComponent : getChildComponents()) {
            childComponent.mouseReleased0(absMouseX, absMouseY, relMousex, relMousey, state);
        }
        mouseReleased(absMouseX, absMouseY, relMousex, relMousey, state);
    }
    public void mouseClickMove0(int absMouseX, int absMouseY, int relMouseX0, int relMouseY0, int clickedMouseButton, long timeSinceLastClick) {
        int relMousex = (int) ((relMouseX0 - getBounds().x)  / scale);
        int relMousey = (int) ((relMouseY0 - getBounds().y) / scale);
        absMouseX = (int) (absMouseX / scale);
        absMouseY = (int) (absMouseY / scale);

        for (MPanel childComponent  : getChildComponents()) {
            childComponent.mouseClickMove0(absMouseX, absMouseY, relMousex, relMousey, clickedMouseButton, timeSinceLastClick);
        }
        mouseClickMove(absMouseX, absMouseY, relMousex, relMousey, clickedMouseButton, timeSinceLastClick);
    }
    public void mouseScrolled0(int absMouseX, int absMouseY, int relMouseX0, int relMouseY0, int scrollAmount) {
        int relMousex = (int) ((relMouseX0 - getBounds().x)  / scale);
        int relMousey = (int) ((relMouseY0 - getBounds().y) / scale);
        absMouseX = (int) (absMouseX / scale);
        absMouseY = (int) (absMouseY / scale);

        for (MPanel childComponent  : getChildComponents()) {
            childComponent.mouseScrolled0(absMouseX, absMouseY, relMousex, relMousey, scrollAmount);
        }
        mouseScrolled(absMouseX, absMouseY, relMousex, relMousey, scrollAmount);
    }

    @Override
    public void mouseMoved0(int absMouseX, int absMouseY, int relMouseX0, int relMouseY0) {
        int relMousex = (int) ((relMouseX0 - getBounds().x)  / scale);
        int relMousey = (int) ((relMouseY0 - getBounds().y) / scale);
        absMouseX = (int) (absMouseX / scale);
        absMouseY = (int) (absMouseY / scale);

        mouseMoved(absMouseX, absMouseY, relMousex, relMousey);
        for (MPanel childComponent  : getChildComponents()) {
            childComponent.mouseMoved0(absMouseX, absMouseY, relMousex, relMousey);
        }
    }
}
