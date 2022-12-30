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
import kr.syeyoung.dungeonsguide.mod.guiv2.renderer.OnlyChildrenRenderer;
import kr.syeyoung.dungeonsguide.mod.guiv2.renderer.SingleChildRenderer;

import java.awt.*;

public class SizedBox {
    public static class BLayout extends Layouter {
        BController bController;

        public BLayout(DomElement element) {
            super(element);
            this.bController = (BController) element.getController();
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

    public static class BController extends Controller {

        @Export(attributeName = "width")
        public BindableAttribute<Double> width = new BindableAttribute<>(Double.class, Double.POSITIVE_INFINITY);
        @Export(attributeName = "height")
        public BindableAttribute<Double> height = new BindableAttribute<>(Double.class, Double.POSITIVE_INFINITY);

        public BController(DomElement element) {
            super(element);

            loadDom();
            width.addOnUpdate(a -> element.requestRelayout());
            height.addOnUpdate(a -> element.requestRelayout());
        }
    }



    public static final DomElementRegistry.DomElementCreator CREATOR = new DomElementRegistry.GeneralDomElementCreator(
            BLayout::new, SingleChildRenderer::new, BController::new
    );
}
