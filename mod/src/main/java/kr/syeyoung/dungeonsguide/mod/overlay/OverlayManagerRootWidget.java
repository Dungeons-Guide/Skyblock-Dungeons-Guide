/*
 * Dungeons Guide - The most intelligent Hypixel Skyblock Dungeons Mod
 * Copyright (C) 2023  cyoung06 (syeyoung)
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

package kr.syeyoung.dungeonsguide.mod.overlay;

import kr.syeyoung.dungeonsguide.mod.guiv2.DomElement;
import kr.syeyoung.dungeonsguide.mod.guiv2.Widget;
import kr.syeyoung.dungeonsguide.mod.guiv2.layouter.Layouter;
import kr.syeyoung.dungeonsguide.mod.guiv2.primitive.ConstraintBox;
import kr.syeyoung.dungeonsguide.mod.guiv2.primitive.Rect;
import kr.syeyoung.dungeonsguide.mod.guiv2.primitive.Size;
import kr.syeyoung.dungeonsguide.mod.guiv2.renderer.OnlyChildrenRenderer;
import kr.syeyoung.dungeonsguide.mod.guiv2.renderer.Renderer;

import java.util.Collections;
import java.util.List;

public class OverlayManagerRootWidget extends Widget implements Layouter {
    @Override
    public List<Widget> build(DomElement buildContext) {
        return Collections.emptyList();
    }

    public void addOverlay(OverlayWidget overlayWidget) {
        if (getDomElement().getChildren().contains(overlayWidget.getDomElement())) {
            System.out.println("ono");
            removeOverlay(overlayWidget);
        }
        DomElement domElement = overlayWidget.createDomElement(getDomElement());
        getDomElement().addElement(domElement);

        updateOverlayPosition(overlayWidget);
    }

    public void removeOverlay(OverlayWidget widget) {
        getDomElement().removeElement(widget.getDomElement());
    }

    public void updateOverlayPosition(OverlayWidget overlayWidget) {
        DomElement domElement = overlayWidget.getDomElement();
        Positioner positioner = overlayWidget.positionSize;
        domElement.setRelativeBound(positioner.position(domElement, lastWidth, lastHeight));
    }

    private double lastWidth, lastHeight;

    @Override
    public Size layout(DomElement buildContext, ConstraintBox constraintBox) {
        lastWidth = constraintBox.getMaxWidth();
        lastHeight = constraintBox.getMaxHeight();
        for (DomElement child : buildContext.getChildren()) {
            if (!(child.getWidget() instanceof OverlayWidget)) continue;
            updateOverlayPosition((OverlayWidget) child.getWidget());
        }

        return new Size(constraintBox.getMaxWidth(), constraintBox.getMaxHeight());
    }

    @Override
    protected Renderer createRenderer() {
        return OnlyChildrenRenderer.INSTANCE;
    }
}
