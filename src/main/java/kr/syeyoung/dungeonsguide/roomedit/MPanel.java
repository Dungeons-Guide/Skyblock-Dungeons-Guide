package kr.syeyoung.dungeonsguide.roomedit;

import lombok.AccessLevel;
import lombok.Getter;
import org.lwjgl.opengl.GL11;
import org.w3c.dom.css.Rect;

import java.awt.*;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;

@Getter
public class MPanel {
    protected Rectangle bounds = new Rectangle(0,0,0,0); // relative to parent

    protected List<MPanel> childComponents = new ArrayList<MPanel>();

    protected Color backgroundColor;

    @Getter(AccessLevel.PUBLIC)
    protected boolean isFocused;

    public void setBackgroundColor(Color c) {
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
        this.bounds.x = bounds.x;
        this.bounds.y = bounds.y;
        this.bounds.width = bounds.width;
        this.bounds.height = bounds.height;
    }

    public void add(MPanel child) {
        this.childComponents.add(child);
    }

    public void remove(MPanel panel) {
        this.childComponents.remove(panel);
    }

    public void render0(Rectangle parentClip, int absMousex, int absMousey, int relMousex0, int relMousey0, float partialTicks) { // 0,0 - a a
        int relMousex = relMousex0 - bounds.x;
        int relMousey = relMousey0 - bounds.y;

        Rectangle absParent = parentClip.getBounds();

        GL11.glPushAttrib(GL11.GL_SCISSOR_BIT);
        GL11.glTranslated(bounds.x, bounds.y, 0);
        absParent.add(-bounds.x, -bounds.y);

        Rectangle absBound = bounds.getBounds(); // 0,0 - a a
        Rectangle clip = determineClip(parentClip, absBound);

        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        GL11.glScissor(clip.x, clip.y, clip.width, clip.height);

        GL11.glClearColor(backgroundColor.getRed(), backgroundColor.getGreen(), backgroundColor.getBlue(), backgroundColor.getAlpha());

        GL11.glPushMatrix();
        render(absMousex, absMousey, relMousex, relMousey, partialTicks);
        GL11.glPopMatrix();

        GL11.glDisable(GL11.GL_SCISSOR_TEST);

        GL11.glPopAttrib();

        for (MPanel mPanel : childComponents){
            GL11.glPushMatrix();
            mPanel.render0(clip, absMousex, absMousey, relMousex, relMousey, partialTicks);
            GL11.glPopMatrix();
        }
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

    public void render(int absMousex, int absMousey, int relMousex0, int relMousey0, float partialTicks) {}

    protected void keyTyped0(char typedChar, int keyCode) {
        for (MPanel childComponent : childComponents) {
            childComponent.keyTyped0(typedChar, keyCode);
        }

        if (isFocused)
            keyTyped(typedChar, keyCode);
    }
    protected void keyTyped(char typedChar, int keyCode) {};

    protected boolean mouseClicked0(int absMouseX, int absMouseY, int relMouseX0, int relMouseY0, int mouseButton) {
        int relMousex = relMouseX0 - bounds.x;
        int relMousey = relMouseY0 - bounds.y;

        boolean noClip = true;
        boolean focusedOverall = false;
        for (MPanel childComponent : childComponents) {
            if (childComponent.mouseClicked0(absMouseX, absMouseY, relMouseX0, relMouseY0, mouseButton)) {
                noClip = false;
                focusedOverall = true;
            }
        }

        if (bounds.contains(relMousex, relMousey) && noClip) {
            isFocused = true;
            focusedOverall = true;
        }

        mouseClicked(absMouseX, absMouseY, relMousex, relMousey, mouseButton);
        return focusedOverall;
    }

    protected void mouseClicked(int absMouseX, int absMouseY, int relMouseX, int relMouseY, int mouseButton) {}

    protected void mouseReleased0(int absMouseX, int absMouseY, int relMouseX0, int relMouseY0, int state) {
        int relMousex = relMouseX0 - bounds.x;
        int relMousey = relMouseY0 - bounds.y;

        for (MPanel childComponent : childComponents) {
            childComponent.mouseReleased0(absMouseX, absMouseY, relMousex, relMousey, state);
        }
        mouseReleased(absMouseX, absMouseY, relMousex, relMousey, state);
    }
    protected void mouseReleased(int absMouseX, int absMouseY, int relMouseX, int relMouseY, int state) {}

    protected void mouseClickMove0(int absMouseX, int absMouseY, int relMouseX0, int relMouseY0, int clickedMouseButton, long timeSinceLastClick) {
        int relMousex = relMouseX0 - bounds.x;
        int relMousey = relMouseY0 - bounds.y;

        for (MPanel childComponent : childComponents) {
            childComponent.mouseClickMove0(absMouseX, absMouseY, relMousex, relMousey, clickedMouseButton, timeSinceLastClick);
        }
        mouseClickMove(absMouseX, absMouseY, relMousex, relMousey, clickedMouseButton, timeSinceLastClick);
    }
    protected void mouseClickMove(int absMouseX, int absMouseY, int relMouseX, int relMouseY, int clickedMouseButton, long timeSinceLastClick) {}

    protected void mouseScrolled0(int absMouseX, int absMouseY, int relMouseX0, int relMouseY0, int scrollAmount) {
        int relMousex = relMouseX0 - bounds.x;
        int relMousey = relMouseY0 - bounds.y;

        for (MPanel childComponent : childComponents) {
            childComponent.mouseScrolled0(absMouseX, absMouseY, relMousex, relMousey, scrollAmount);
        }
        mouseScrolled(absMouseX, absMouseY, relMousex, relMousey, scrollAmount);
    }
    protected void mouseScrolled(int absMouseX, int absMouseY, int relMouseX0, int relMouseY0, int scrollAmount) {}
}
