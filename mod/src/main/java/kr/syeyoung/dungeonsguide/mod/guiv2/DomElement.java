package kr.syeyoung.dungeonsguide.mod.guiv2;

import kr.syeyoung.dungeonsguide.mod.guiv2.renderer.Renderer;
import kr.syeyoung.dungeonsguide.mod.utils.cursor.EnumCursor;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.w3c.dom.Element;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class DomElement {
    @Getter
    @Setter(AccessLevel.PACKAGE)
    private Controller controller;
    @Getter
    @Setter(AccessLevel.PACKAGE)
    private Renderer renderer; // renders element itself.
    @Getter
    @Setter(AccessLevel.PACKAGE)
    private Layouter layouter; // layouts subelements

    @Getter
    @Setter(AccessLevel.PRIVATE)
    private DomElement parent;
    @Getter
    @Setter(AccessLevel.PACKAGE)
    private List<DomElement> children = new ArrayList<>();

    @Getter @Setter
    private Element representing;

    @Getter
    private DomElement componentParent;

    private List<String> classNames = new ArrayList<>();
    private String status;

    // search classNames
    // search classNames:status
    // search tag
    // search tag:status
    // search *

    public void setComponentParent(DomElement componentParent) {
        if (this.componentParent != null) return;
        this.componentParent = componentParent;
        for (DomElement child: children) {
            child.setComponentParent(componentParent);
        }
    }

    @Getter
    private boolean isMounted;


    public void setMounted(boolean mounted) {
        if (isMounted == mounted) {
            return;
        }
        isMounted = mounted;

        for (DomElement child : children) {
            child.setMounted(mounted);
        }

        if (mounted) controller.onMount();
        else controller.onUnmount();;
    }

    @Getter
    private Rectangle relativeBound; // relative bound from parent, unapplied transformation

    public void setRelativeBound(Rectangle relativeBound) {
        this.relativeBound = relativeBound;
    }

    @Getter @Setter
    private Rectangle absBounds; // absolute bound from screen top left




    @Getter
    private boolean isFocused;
    // event propagation

    public void requestRelayout() {
        if (parent != null)
        parent.requestRelayout();
    }

    public void addElement(DomElement element) {
        element.setParent(this);
        element.setComponentParent(componentParent);
        children.add(element);
        element.setMounted(isMounted);
        requestRelayout();
    }
    public void removeElement(DomElement element) {
        element.setParent(null);
        element.setComponentParent(null);
        children.remove(element);
        element.setMounted(isMounted);
        requestRelayout();
    }

    public void keyPressed0(char typedChar, int keyCode) {
        for (DomElement childComponent  : children) {
            childComponent.keyPressed0(typedChar, keyCode);
        }

        if (isFocused)
            controller.keyPressed(typedChar, keyCode);
    }
    public void keyHeld0(char typedChar, int keyCode) {
        for (DomElement childComponent  : children) {
            childComponent.keyHeld0(typedChar, keyCode);
        }

        if (isFocused)
            controller.keyHeld(typedChar, keyCode);
    }
    public void keyReleased0(char typedChar, int keyCode) {
        for (DomElement childComponent  : children) {
            childComponent.keyReleased0(typedChar, keyCode);
        }

        if (isFocused)
            controller.keyReleased(typedChar, keyCode);
    }


    public boolean mouseClicked0(int absMouseX, int absMouseY, int relMouseX0, int relMouseY0, int mouseButton) {
        if (!absBounds.contains(absMouseX, absMouseY)) {
            isFocused = false;
            return false;
        }
        isFocused = true;

        for (DomElement childComponent  : children) {
            Rectangle original = childComponent.getRelativeBound();
            Rectangle transformed = renderer.applyTransformation(childComponent);
            double XscaleFactor = transformed.getWidth() / original.width;
            double YscaleFactor = transformed.getHeight() / original.height;

            if (childComponent.mouseClicked0(absMouseX, absMouseY, (int) ((relMouseX0 - transformed.x) * XscaleFactor),
                    (int) ((relMouseY0 - transformed.y) * YscaleFactor), mouseButton)) {
                isFocused = false;
            }
        }

        controller.mouseClicked(absMouseX, absMouseY, relMouseX0, relMouseY0, mouseButton);
        return true;
    }


    public void mouseReleased0(int absMouseX, int absMouseY, int relMouseX0, int relMouseY0, int state) {
        if (!absBounds.contains(absMouseX, absMouseY)) return;

        for (DomElement childComponent : children) {
            Rectangle original = childComponent.getRelativeBound();
            Rectangle transformed = renderer.applyTransformation(childComponent);
            double XscaleFactor = transformed.getWidth() / original.width;
            double YscaleFactor = transformed.getHeight() / original.height;

            childComponent.mouseReleased0(absMouseX, absMouseY, (int) ((relMouseX0 - transformed.x) * XscaleFactor),
                    (int) ((relMouseY0 - transformed.y)*YscaleFactor), state);
        }
        controller.mouseReleased(absMouseX, absMouseY, relMouseX0, relMouseY0, state);
    }

    public void mouseClickMove0(int absMouseX, int absMouseY, int relMouseX0, int relMouseY0, int clickedMouseButton, long timeSinceLastClick) {
        if (!absBounds.contains(absMouseX, absMouseY)) return;

        for (DomElement childComponent  : children) {
            Rectangle original = childComponent.getRelativeBound();
            Rectangle transformed = renderer.applyTransformation(childComponent);
            double XscaleFactor = transformed.getWidth() / original.width;
            double YscaleFactor = transformed.getHeight() / original.height;
            childComponent.mouseClickMove0(absMouseX, absMouseY, (int) ((relMouseX0 - transformed.x) * XscaleFactor),
                    (int) ((relMouseY0 - transformed.y)*YscaleFactor), clickedMouseButton, timeSinceLastClick);
        }
        controller.mouseClickMove(absMouseX, absMouseY, relMouseX0, relMouseY0, clickedMouseButton, timeSinceLastClick);
    }
    public void mouseScrolled0(int absMouseX, int absMouseY, int relMouseX0, int relMouseY0, int scrollAmount) {
        if (!absBounds.contains(absMouseX, absMouseY)) return;

        for (DomElement childComponent  : children) {
            Rectangle original = childComponent.getRelativeBound();
            Rectangle transformed = renderer.applyTransformation(childComponent);
            double XscaleFactor = transformed.getWidth() / original.width;
            double YscaleFactor = transformed.getHeight() / original.height;
            childComponent.mouseScrolled0(absMouseX, absMouseY,  (int) ((relMouseX0 - transformed.x) * XscaleFactor),
                    (int) ((relMouseY0 - transformed.y)*YscaleFactor), scrollAmount);
        }
        controller.mouseScrolled(absMouseX, absMouseY, relMouseX0, relMouseY0, scrollAmount);
    }


    private boolean wasMouseIn = false;
    public void mouseMoved0(int absMouseX, int absMouseY, int relMouseX0, int relMouseY0) {
        if (!absBounds.contains(absMouseX, absMouseY)) {
            if (wasMouseIn) controller.mouseExited(absMouseX, absMouseY, relMouseX0, relMouseY0);
            wasMouseIn = false;
            return;
        }
        if (!wasMouseIn) controller.mouseEntered(absMouseX, absMouseY, relMouseX0, relMouseY0);
        wasMouseIn = true;

        controller.mouseMoved(absMouseX, absMouseY, relMouseX0, relMouseY0);
        for (DomElement childComponent  : children) {
            Rectangle original = childComponent.getRelativeBound();
            Rectangle transformed = renderer.applyTransformation(childComponent);
            double XscaleFactor = transformed.getWidth() / original.width;
            double YscaleFactor = transformed.getHeight() / original.height;
            childComponent.mouseMoved0(absMouseX, absMouseY,  (int) ((relMouseX0 - transformed.x) * XscaleFactor),
                    (int) ((relMouseY0 - transformed.y)*YscaleFactor));
        }
    }

    public void setCursor(EnumCursor enumCursor) {
        parent.setCursor(enumCursor);
    }
}
