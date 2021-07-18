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

import kr.syeyoung.dungeonsguide.gui.elements.MTooltip;
import lombok.AccessLevel;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Getter
public class MPanel {
    protected Rectangle bounds = new Rectangle(0,0,0,0); // relative to parent

    protected List<MPanel> childComponents = new CopyOnWriteArrayList<MPanel>();

    protected Color backgroundColor = new Color(0,0,0,0);

    protected Rectangle lastAbsClip = new Rectangle(0,0,0,0);

    @Getter(AccessLevel.PUBLIC)
    protected boolean isFocused;

    @Getter
    protected MPanel parent;

    public void setBackgroundColor(Color c) {
        if (c == null) return;
        this.backgroundColor = c;
    }

    public void setPosition(Point pt) {
        this.setBounds(new Rectangle(pt.x, pt.y, getBounds().width, getBounds().height));
    }

    public void setSize(Dimension dim) {
        this.setBounds(new Rectangle(getBounds().x, getBounds().y, dim.width, dim.height));
    }

    public Dimension getSize() {
        return getBounds().getSize();
    }

    public Dimension getPreferredSize() { return getSize(); }

    public void setBounds(Rectangle bounds) {
        if (bounds == null) return;
        this.bounds.x = bounds.x;
        this.bounds.y = bounds.y;
        this.bounds.width = bounds.width;
        this.bounds.height = bounds.height;

        for (MPanel childComponent : childComponents) {
            childComponent.resize0(getBounds().width, getBounds().height);
        }
        onBoundsUpdate();
    }

    public void onBoundsUpdate() {

    }

    public void add(MPanel child) {
        if (child.parent != null) throw new IllegalArgumentException("What have you done");
        this.childComponents.add(child);
        child.parent = this;
    }

    public void openTooltip(MTooltip mPanel) {
        parent.openTooltip(mPanel);
    }
    public int getTooltipsOpen() {
        return parent.getTooltipsOpen();
    }

    public void remove(MPanel panel) {
        panel.parent = null;
        this.childComponents.remove(panel);
    }

    protected Point lastParentPoint;
    public void render0(ScaledResolution resolution, Point parentPoint, Rectangle parentClip, int absMousex, int absMousey, int relMousex0, int relMousey0, float partialTicks) { // 0,0 - a a

        lastParentPoint = parentPoint;
        int relMousex = relMousex0 - getBounds().x;
        int relMousey = relMousey0 - getBounds().y;

        GlStateManager.translate(getBounds().x, getBounds().y, 5);
        GlStateManager.color(1,1,1,0);


        Rectangle absBound = getBounds().getBounds();
        absBound.setLocation(absBound.x + parentPoint.x, absBound.y + parentPoint.y);
        Rectangle clip = determineClip(parentClip, absBound);
        lastAbsClip = clip;
        if (clip.getSize().height * clip.getSize().width == 0) return;

        clip(resolution, clip.x, clip.y, clip.width, clip.height);
        GlStateManager.pushAttrib();
        GL11.glEnable(GL11.GL_SCISSOR_TEST);

        GlStateManager.pushAttrib();
        GuiScreen.drawRect(0,0, getBounds().width, getBounds().height,  backgroundColor.getRGB());
        GlStateManager.enableBlend();
        GlStateManager.popAttrib();

        GlStateManager.pushMatrix();
        GlStateManager.pushAttrib();

        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.enableBlend();
        GlStateManager.enableAlpha();
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0);

        render(absMousex, absMousey, relMousex, relMousey, partialTicks, clip);
        GlStateManager.popAttrib();
        GlStateManager.popMatrix();

        GL11.glDisable(GL11.GL_SCISSOR_TEST);
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

    public static void clip(ScaledResolution resolution, int x, int y, int width, int height) {
        if (width < 0 || height < 0) return;

//        int scale = resolution.getScaleFactor();
        int scale = 1;
        GL11.glScissor((x ) * scale, Minecraft.getMinecraft().displayHeight - (y + height) * scale, (width) * scale, height * scale);
    }

    protected Rectangle determineClip(Rectangle rect1, Rectangle rect2) {
        int minX = Math.max(rect1.x, rect2.x);
        int minY = Math.max(rect1.y, rect2.y);
        int maxX = Math.min(rect1.x + rect1.width, rect2.x + rect2.width);
        int maxY = Math.min(rect1.y + rect1.height, rect2.y +rect2.height);
        if (minX > maxX) return new Rectangle(0,0,0,0);
        if (minY > maxY) return new Rectangle(0,0,0,0);
        return new Rectangle(minX, minY, maxX - minX, maxY - minY);
    }

    public void render(int absMousex, int absMousey, int relMousex0, int relMousey0, float partialTicks, Rectangle scissor) {}

    public void resize0(int parentWidth, int parentHeight) {
        resize(parentWidth, parentHeight);
    }

    public void resize(int parentWidth, int parentHeight) {}


    public void keyTyped0(char typedChar, int keyCode) {
        for (MPanel childComponent  : getChildComponents()) {
            childComponent.keyTyped0(typedChar, keyCode);
        }

        if (isFocused)
            keyTyped(typedChar, keyCode);
    }
    public void keyTyped(char typedChar, int keyCode) {}

    public boolean mouseClicked0(int absMouseX, int absMouseY, int relMouseX0, int relMouseY0, int mouseButton) {
        int relMousex = relMouseX0 - getBounds().x;
        int relMousey = relMouseY0 - getBounds().y;

        boolean noClip = true;
        boolean focusedOverall = false;
        for (MPanel childComponent  : getChildComponents()) {
            if (childComponent.mouseClicked0(absMouseX, absMouseY, relMousex, relMousey, mouseButton)) {
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

    public void mouseClicked(int absMouseX, int absMouseY, int relMouseX, int relMouseY, int mouseButton) {}

    public void mouseReleased0(int absMouseX, int absMouseY, int relMouseX0, int relMouseY0, int state) {
        int relMousex = relMouseX0 - getBounds().x;
        int relMousey = relMouseY0 - getBounds().y;

        for (MPanel childComponent : getChildComponents()) {
            childComponent.mouseReleased0(absMouseX, absMouseY, relMousex, relMousey, state);
        }
        mouseReleased(absMouseX, absMouseY, relMousex, relMousey, state);
    }
    public void mouseReleased(int absMouseX, int absMouseY, int relMouseX, int relMouseY, int state) {}

    public void mouseClickMove0(int absMouseX, int absMouseY, int relMouseX0, int relMouseY0, int clickedMouseButton, long timeSinceLastClick) {
        int relMousex = relMouseX0 - getBounds().x;
        int relMousey = relMouseY0 - getBounds().y;

        for (MPanel childComponent  : getChildComponents()) {
            childComponent.mouseClickMove0(absMouseX, absMouseY, relMousex, relMousey, clickedMouseButton, timeSinceLastClick);
        }
        mouseClickMove(absMouseX, absMouseY, relMousex, relMousey, clickedMouseButton, timeSinceLastClick);
    }
    public void mouseClickMove(int absMouseX, int absMouseY, int relMouseX, int relMouseY, int clickedMouseButton, long timeSinceLastClick) {}

    public void mouseScrolled0(int absMouseX, int absMouseY, int relMouseX0, int relMouseY0, int scrollAmount) {
        int relMousex = relMouseX0 - getBounds().x;
        int relMousey = relMouseY0 - getBounds().y;

        for (MPanel childComponent  : getChildComponents()) {
            childComponent.mouseScrolled0(absMouseX, absMouseY, relMousex, relMousey, scrollAmount);
        }
        mouseScrolled(absMouseX, absMouseY, relMousex, relMousey, scrollAmount);
    }
    public void mouseScrolled(int absMouseX, int absMouseY, int relMouseX0, int relMouseY0, int scrollAmount) {}
}
