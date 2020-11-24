package kr.syeyoung.dungeonsguide.roomedit;

import lombok.AccessLevel;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import org.lwjgl.opengl.GL11;
import org.w3c.dom.css.Rect;

import java.awt.*;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;

@Getter
public class MPanel {
    protected Rectangle bounds = new Rectangle(0,0,0,0); // relative to parent

    protected List<MPanel> childComponents = new CopyOnWriteArrayList<MPanel>();

    protected Color backgroundColor = new Color(0,0,0,0);

    @Getter(AccessLevel.PUBLIC)
    protected boolean isFocused;

    public void setBackgroundColor(Color c) {
        if (c == null) return;
        this.backgroundColor = c;
    }

    public void setPosition(Point pt) {
        this.bounds.x = pt.x;
        this.bounds.y = pt.y;
    }

    public void setSize(Dimension dim) {
        this.bounds.width = dim.width;
        this.bounds.height = dim.height;
    }

    public Dimension getSize() {
        return bounds.getSize();
    }

    public void setBounds(Rectangle bounds) {
        if (bounds == null) return;
        this.bounds.x = bounds.x;
        this.bounds.y = bounds.y;
        this.bounds.width = bounds.width;
        this.bounds.height = bounds.height;

        for (MPanel childComponent : childComponents) {
            childComponent.resize0(bounds.width, bounds.height);
        }
    }

    public void add(MPanel child) {
        this.childComponents.add(child);
    }

    public void remove(MPanel panel) {
        this.childComponents.remove(panel);
    }

    public void render0(ScaledResolution resolution, Point parentPoint, Rectangle parentClip, int absMousex, int absMousey, int relMousex0, int relMousey0, float partialTicks) { // 0,0 - a a


        int relMousex = relMousex0 - bounds.x;
        int relMousey = relMousey0 - bounds.y;

        GL11.glTranslated(bounds.x, bounds.y, 0);

        Rectangle absBound = bounds.getBounds();
        absBound.setLocation(absBound.x + parentPoint.x, absBound.y + parentPoint.y);
        Rectangle clip = determineClip(parentClip, absBound);

        clip(resolution, clip.x, clip.y, clip.width, clip.height);
        GL11.glPushAttrib(GL11.GL_SCISSOR_BIT);
        GL11.glEnable(GL11.GL_SCISSOR_TEST);

        GL11.glEnable(GL11.GL_BLEND);
        OpenGlHelper.glBlendFunc(770, 771, 1, 0);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        GuiScreen.drawRect(0,0, bounds.width, bounds.height, backgroundColor.getRGB());

        GL11.glPushMatrix();
        render(absMousex, absMousey, relMousex, relMousey, partialTicks, clip);
        GL11.glPopMatrix();

        GL11.glDisable(GL11.GL_SCISSOR_TEST);
        GL11.glPopAttrib();


        Point newPt = new Point(parentPoint.x + bounds.x, parentPoint.y + bounds.y);

        for (MPanel mPanel : getChildComponents()){
            GL11.glPushMatrix();
            mPanel.render0(resolution, newPt, clip, absMousex, absMousey, relMousex, relMousey, partialTicks);
            GL11.glPopMatrix();
        }
    }

    public void clip(ScaledResolution resolution, int x, int y, int width, int height) {
        int scale = resolution.getScaleFactor();
        GL11.glScissor((x ) * scale, Minecraft.getMinecraft().displayHeight - (y + height) * scale, (width) * scale, height * scale);
    }

    private Rectangle determineClip(Rectangle rect1, Rectangle rect2) {
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


    protected void keyTyped0(char typedChar, int keyCode) {
        for (MPanel childComponent  : getChildComponents()) {
            childComponent.keyTyped0(typedChar, keyCode);
        }

        if (isFocused)
            keyTyped(typedChar, keyCode);
    }
    protected void keyTyped(char typedChar, int keyCode) {}

    protected boolean mouseClicked0(int absMouseX, int absMouseY, int relMouseX0, int relMouseY0, int mouseButton) {
        int relMousex = relMouseX0 - bounds.x;
        int relMousey = relMouseY0 - bounds.y;

        boolean noClip = true;
        boolean focusedOverall = false;
        for (MPanel childComponent  : getChildComponents()) {
            if (childComponent.mouseClicked0(absMouseX, absMouseY, relMousex, relMousey, mouseButton)) {
                noClip = false;
                focusedOverall = true;
            }
        }

        if (bounds.contains(relMouseX0, relMouseY0) && noClip) {
            isFocused = true;
            focusedOverall = true;
        } else {
            isFocused = false;
        }

        mouseClicked(absMouseX, absMouseY, relMousex, relMousey, mouseButton);
        return focusedOverall;
    }

    protected void mouseClicked(int absMouseX, int absMouseY, int relMouseX, int relMouseY, int mouseButton) {}

    protected void mouseReleased0(int absMouseX, int absMouseY, int relMouseX0, int relMouseY0, int state) {
        int relMousex = relMouseX0 - bounds.x;
        int relMousey = relMouseY0 - bounds.y;

        for (MPanel childComponent : getChildComponents()) {
            childComponent.mouseReleased0(absMouseX, absMouseY, relMousex, relMousey, state);
        }
        mouseReleased(absMouseX, absMouseY, relMousex, relMousey, state);
    }
    protected void mouseReleased(int absMouseX, int absMouseY, int relMouseX, int relMouseY, int state) {}

    protected void mouseClickMove0(int absMouseX, int absMouseY, int relMouseX0, int relMouseY0, int clickedMouseButton, long timeSinceLastClick) {
        int relMousex = relMouseX0 - bounds.x;
        int relMousey = relMouseY0 - bounds.y;

        for (MPanel childComponent  : getChildComponents()) {
            childComponent.mouseClickMove0(absMouseX, absMouseY, relMousex, relMousey, clickedMouseButton, timeSinceLastClick);
        }
        mouseClickMove(absMouseX, absMouseY, relMousex, relMousey, clickedMouseButton, timeSinceLastClick);
    }
    protected void mouseClickMove(int absMouseX, int absMouseY, int relMouseX, int relMouseY, int clickedMouseButton, long timeSinceLastClick) {}

    protected void mouseScrolled0(int absMouseX, int absMouseY, int relMouseX0, int relMouseY0, int scrollAmount) {
        int relMousex = relMouseX0 - bounds.x;
        int relMousey = relMouseY0 - bounds.y;

        for (MPanel childComponent  : getChildComponents()) {
            childComponent.mouseScrolled0(absMouseX, absMouseY, relMousex, relMousey, scrollAmount);
        }
        mouseScrolled(absMouseX, absMouseY, relMousex, relMousey, scrollAmount);
    }
    protected void mouseScrolled(int absMouseX, int absMouseY, int relMouseX0, int relMouseY0, int scrollAmount) {}
}
