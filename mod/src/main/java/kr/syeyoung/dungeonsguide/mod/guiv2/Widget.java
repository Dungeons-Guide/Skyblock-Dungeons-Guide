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
import kr.syeyoung.dungeonsguide.mod.guiv2.layouter.SingleChildPassingLayouter;
import kr.syeyoung.dungeonsguide.mod.guiv2.renderer.DrawNothingRenderer;
import kr.syeyoung.dungeonsguide.mod.guiv2.renderer.OnlyChildrenRenderer;
import kr.syeyoung.dungeonsguide.mod.guiv2.renderer.Renderer;
import kr.syeyoung.dungeonsguide.mod.guiv2.renderer.SingleChildRenderer;
import lombok.Getter;

import java.util.*;

public abstract class Widget {

    @Getter
    private DomElement domElement;

    public DomElement createDomElement(DomElement parent) {
        if (domElement != null) throw new IllegalStateException("Controller already has corresponding DomElement!");
        domElement = new DomElement();
        domElement.setWidget(this);
        parent.addElement(domElement);

        domElement.setLayouter(createLayouter());
        domElement.setRenderer(createRenderer());

        // build
        List<Widget> widgets = build(domElement);
        for (Widget widget : widgets) {
            widget.build(domElement);
        }
        return domElement;
    }

    protected Layouter createLayouter() {
        if (this instanceof Layouter) return (Layouter) this;
        return SingleChildPassingLayouter.INSTANCE;
    }
    protected Renderer createRenderer() {
        if (this instanceof Renderer) return (Renderer) this;
        return SingleChildRenderer.INSTANCE;
    }

    public abstract List<Widget> build(DomElement buildContext);

    public Widget() {
        // parameters shall be passed through constructor
        // or maybe a "builder"
        // set bindable props in the builder and yeah
        // or maybe set raw props
    }


    public void mouseScrolled(int absMouseX, int absMouseY, double relMouseX0, double relMouseY0, int scrollAmount) {}
    public void mouseMoved(int absMouseX, int absMouseY, double relMouseX0, double relMouseY0) {}
    public void mouseClickMove(int absMouseX, int absMouseY, double relMouseX, double relMouseY, int clickedMouseButton, long timeSinceLastClick) {}
    public void mouseReleased(int absMouseX, int absMouseY, double relMouseX, double relMouseY, int state) {}
    public boolean mouseClicked(int absMouseX, int absMouseY, double relMouseX, double relMouseY, int mouseButton) {
        return false;
    }
    public void keyReleased(char typedChar, int keyCode) {}
    public void keyHeld(char typedChar, int keyCode) {}
    public void keyPressed(char typedChar, int keyCode) {}
    public void mouseExited(int absMouseX, int absMouseY, double relMouseX, double relMouseY) {}
    public void mouseEntered(int absMouseX, int absMouseY, double relMouseX, double relMouseY) {}

    public void onMount() {
    }
    public void onUnmount() {
    }
}
