/*
 * Dungeons Guide - The most intelligent Hypixel Skyblock Dungeons Mod
 * Copyright (C) 2022  cyoung06 (syeyoung)
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

package kr.syeyoung.dungeonsguide.mod.guiv2;

import kr.syeyoung.dungeonsguide.mod.guiv2.layouter.Layouter;
import kr.syeyoung.dungeonsguide.mod.guiv2.layouter.NullLayouter;
import kr.syeyoung.dungeonsguide.mod.guiv2.primitive.Position;
import kr.syeyoung.dungeonsguide.mod.guiv2.primitive.Rect;
import kr.syeyoung.dungeonsguide.mod.guiv2.primitive.Size;
import kr.syeyoung.dungeonsguide.mod.guiv2.renderer.DrawNothingRenderer;
import kr.syeyoung.dungeonsguide.mod.guiv2.renderer.Renderer;
import kr.syeyoung.dungeonsguide.mod.utils.cursor.EnumCursor;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.util.LinkedList;
import java.util.List;

public class DomElement {
    @Getter
    @Setter(AccessLevel.PACKAGE)
    private Widget widget;
    @Getter
    @Setter(AccessLevel.PACKAGE)
    private Renderer renderer = DrawNothingRenderer.INSTANCE; // renders element itself.
    @Getter
    @Setter(AccessLevel.PACKAGE)
    private Layouter layouter = NullLayouter.INSTANCE; // layouts subelements

    @Getter
    @Setter(AccessLevel.PRIVATE)
    private DomElement parent;
    @Getter
    private List<DomElement> children = new LinkedList<>();

    @Getter
    Context context;

    public DomElement() {
    }

    @Getter
    private boolean isMounted;


    public void setMounted(boolean mounted) {
        if (isMounted == mounted) {
            return;
        }
        isMounted = mounted;

        for (DomElement child : children) {
            child.context = context;
            child.setMounted(mounted);
        }

        if (mounted) widget.onMount();
        else {
            if (isFocused())
                context.CONTEXT.put("focus", null);
            widget.onUnmount();
        };
    }

    @Getter
    private Rect relativeBound; // relative bound from parent, unapplied transformation

    public void setRelativeBound(Rect relativeBound) {
        this.relativeBound = relativeBound;
        this.size = new Size(relativeBound.getWidth(), relativeBound.getHeight());
    }

    @Getter @Setter
    private Size size;

    @Getter @Setter
    private Rect absBounds; // absolute bound from screen top left



    // event propagation

    public void requestRelayout() {
        if (parent != null)
            parent.requestRelayout();
    }

    public void addElementFirst(DomElement element) {
        element.setParent(this);
        children.add(0, element);
        element.context = context;
        element.setMounted(isMounted);
        requestRelayout();
    }
    public void addElement(DomElement element) {
        element.setParent(this);
        children.add(element);
        element.context = context;
        element.setMounted(isMounted);
        requestRelayout();
    }
    public void removeElement(DomElement element) {
        element.setParent(null);
        children.remove(element);
        element.setMounted(false);
        requestRelayout();
    }

    public void keyPressed0(char typedChar, int keyCode) {
        for (DomElement childComponent  : children) {
            childComponent.keyPressed0(typedChar, keyCode);
        }

        if (isFocused())
            widget.keyPressed(typedChar, keyCode);
    }
    public void keyHeld0(char typedChar, int keyCode) {
        for (DomElement childComponent  : children) {
            childComponent.keyHeld0(typedChar, keyCode);
        }

        if (isFocused())
            widget.keyHeld(typedChar, keyCode);
    }
    public void keyReleased0(char typedChar, int keyCode) {
        for (DomElement childComponent  : children) {
            childComponent.keyReleased0(typedChar, keyCode);
        }

        if (isFocused())
            widget.keyReleased(typedChar, keyCode);
    }

    public void obtainFocus() {
        context.CONTEXT.put("focus", this);
    }

    public boolean isFocused() {
        return context.getValue(DomElement.class, "focus") == this;
    }

    public boolean mouseClicked0(int absMouseX, int absMouseY, double relMouseX0, double relMouseY0, int mouseButton) {
        if (absBounds == null) return false;
        if (!absBounds.contains(absMouseX, absMouseY)) {
            return false;
        }

        for (DomElement childComponent  : children) {
            Position transformed = renderer.transformPoint(childComponent, new Position(relMouseX0, relMouseY0));

            if (childComponent.mouseClicked0(absMouseX, absMouseY, transformed.x, transformed.y, mouseButton)) {
                return true;
            }
        }

        return widget.mouseClicked(absMouseX, absMouseY, relMouseX0, relMouseY0, mouseButton);
    }


    public void mouseReleased0(int absMouseX, int absMouseY, double relMouseX0, double relMouseY0, int state) {
        if (absBounds == null) return;

        for (DomElement childComponent : children) {
            Position transformed = renderer.transformPoint(childComponent, new Position(relMouseX0, relMouseY0));

            childComponent.mouseReleased0(absMouseX, absMouseY, transformed.x, transformed.y, state);
        }
        widget.mouseReleased(absMouseX, absMouseY, relMouseX0, relMouseY0, state);
    }

    public void mouseClickMove0(int absMouseX, int absMouseY, double relMouseX0, double relMouseY0, int clickedMouseButton, long timeSinceLastClick) {
        if (absBounds == null) return;

        for (DomElement childComponent  : children) {
            Position transformed = renderer.transformPoint(childComponent, new Position(relMouseX0, relMouseY0));

            childComponent.mouseClickMove0(absMouseX, absMouseY, transformed.x, transformed.y, clickedMouseButton, timeSinceLastClick);
        }
        if (isFocused())
            widget.mouseClickMove(absMouseX, absMouseY, relMouseX0, relMouseY0, clickedMouseButton, timeSinceLastClick);
    }
    public void mouseScrolled0(int absMouseX, int absMouseY, double relMouseX0, double relMouseY0, int scrollAmount) {
        if (absBounds == null) return;
        if (!absBounds.contains(absMouseX, absMouseY)) return;

        for (DomElement childComponent  : children) {
            Position transformed = renderer.transformPoint(childComponent, new Position(relMouseX0, relMouseY0));

            childComponent.mouseScrolled0(absMouseX, absMouseY, transformed.x, transformed.y, scrollAmount);
        }
        widget.mouseScrolled(absMouseX, absMouseY, relMouseX0, relMouseY0, scrollAmount);
    }


    private boolean wasMouseIn = false;
    public void mouseMoved0(int absMouseX, int absMouseY, double relMouseX0, double relMouseY0) {
        if (absBounds == null) return;
        if (!absBounds.contains(absMouseX, absMouseY)) {
            if (wasMouseIn) widget.mouseExited(absMouseX, absMouseY, relMouseX0, relMouseY0);
            wasMouseIn = false;
            return;
        }
        if (!wasMouseIn) widget.mouseEntered(absMouseX, absMouseY, relMouseX0, relMouseY0);
        wasMouseIn = true;

        for (DomElement childComponent  : children) {
            Position transformed = renderer.transformPoint(childComponent, new Position(relMouseX0, relMouseY0));

            childComponent.mouseMoved0(absMouseX, absMouseY,  transformed.x, transformed.getY());
        }
        widget.mouseMoved(absMouseX, absMouseY, relMouseX0, relMouseY0);
    }

    public void setCursor(EnumCursor enumCursor) {
        parent.setCursor(enumCursor);
    }
}
