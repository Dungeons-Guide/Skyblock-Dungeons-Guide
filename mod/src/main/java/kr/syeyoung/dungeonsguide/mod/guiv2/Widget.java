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
import kr.syeyoung.dungeonsguide.mod.guiv2.renderer.RenderBuilder;
import kr.syeyoung.dungeonsguide.mod.guiv2.renderer.Renderer;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;

public abstract class Widget {

    @Getter
    private DomElement domElement;

    public DomElement createDomElement(DomElement parent) {
        if (domElement != null) throw new IllegalStateException("Controller already has corresponding DomElement!");
        domElement = new DomElement();
        domElement.setWidget(this);
        parent.addElement(domElement);

        if (this instanceof Layouter) domElement.setLayouter((Layouter) this);
        if (this instanceof RenderBuilder) domElement.setRenderer((RenderBuilder) this);

        // build
        List<Widget> widgets = build(domElement);
        for (Widget widget : widgets) {
            widget.build(domElement);
        }
        return domElement;
    }

    public abstract List<Widget> build(DomElement buildContext);

    public Widget() {
        // parameters shall be passed through constructor
        // or maybe a "builder"
        // set bindable props in the builder and yeah
        // or maybe set raw props
    }


    public void mouseScrolled(int absMouseX, int absMouseY, int relMouseX0, int relMouseY0, int scrollAmount) {}
    public void mouseMoved(int absMouseX, int absMouseY, int relMouseX0, int relMouseY0) {}
    public void mouseClickMove(int absMouseX, int absMouseY, int relMouseX, int relMouseY, int clickedMouseButton, long timeSinceLastClick) {}
    public void mouseReleased(int absMouseX, int absMouseY, int relMouseX, int relMouseY, int state) {}
    public boolean mouseClicked(int absMouseX, int absMouseY, int relMouseX, int relMouseY, int mouseButton) {
        return false;
    }
    public void keyReleased(char typedChar, int keyCode) {}
    public void keyHeld(char typedChar, int keyCode) {}
    public void keyPressed(char typedChar, int keyCode) {}
    public void mouseExited(int absMouseX, int absMouseY, int relMouseX, int relMouseY) {}
    public void mouseEntered(int absMouseX, int absMouseY, int relMouseX, int relMouseY) {}

    public void onMount() {
    }
    public void onUnmount() {
    }
}
