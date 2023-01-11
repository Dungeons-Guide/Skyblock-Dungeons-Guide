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

package kr.syeyoung.dungeonsguide.mod.guiv2.elements;

import kr.syeyoung.dungeonsguide.mod.guiv2.*;
import kr.syeyoung.dungeonsguide.mod.guiv2.layouter.Layouter;
import kr.syeyoung.dungeonsguide.mod.guiv2.primitive.ConstraintBox;
import kr.syeyoung.dungeonsguide.mod.guiv2.renderer.SingleChildRenderer;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.DomElementRegistry;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.annotations.Export;

import java.awt.*;

public class SizedBox {
    public static class BLayout extends Layouter {
        BWidget bController;

        public BLayout(DomElement element) {
            super(element);
            this.bController = (BWidget) element.getWidget();
        }

        @Override
        public Dimension layout(ConstraintBox constraintBox) {

            int width = (int) Math.min(bController.width.getValue(), constraintBox.getMaxWidth());
            int height = (int) Math.min(bController.height.getValue(), constraintBox.getMaxHeight());

            if (getDomElement().getChildren().isEmpty()) {
                return new Dimension(width, height);
            }

            DomElement child = getDomElement().getChildren().get(0);
            Dimension dim = child.getLayouter().layout(new ConstraintBox(
                    width, width, height, height
            )); // force size heh.
            child.setRelativeBound(new Rectangle(0,0,dim.width,dim.height));
            return dim;
        }

        @Override
        public boolean shouldRelayout() {
            return false;
        }
    }

    public static class BWidget extends Widget {

        @Export(attributeName = "width")
        public final BindableAttribute<Double> width = new BindableAttribute<>(Double.class, Double.POSITIVE_INFINITY);
        @Export(attributeName = "height")
        public final BindableAttribute<Double> height = new BindableAttribute<>(Double.class, Double.POSITIVE_INFINITY);

        public BWidget(DomElement element) {
            super(element);
            loadAttributes();
            loadDom();
            width.addOnUpdate(a -> element.requestRelayout());
            height.addOnUpdate(a -> element.requestRelayout());
        }
    }



    public static final DomElementRegistry.DomElementCreator CREATOR = new DomElementRegistry.GeneralDomElementCreator(
            BLayout::new, SingleChildRenderer::new, BWidget::new
    );
}
