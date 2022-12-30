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

public class Padding {
    public static class PLayouter extends Layouter {
        PController controller;
        public PLayouter(DomElement element) {
            super(element);
            this.controller = (PController) element.getController();
        }

        @Override
        public Dimension layout(ConstraintBox constraintBox) {
            DomElement domElement = getDomElement().getChildren().get(0);

            int width = (int) (controller.left.getValue() + controller.right.getValue());
            int height = (int) (controller.top.getValue() + controller.bottom.getValue());
            Dimension dim = domElement.getLayouter().layout(new ConstraintBox(
                    constraintBox.getMinWidth() - width,
                    constraintBox.getMaxWidth() - width,
                    constraintBox.getMinHeight() - height,
                    constraintBox.getMaxHeight() - height
            ));

            domElement.setRelativeBound(new Rectangle(
                    controller.left.getValue().intValue(),
                    controller.top.getValue().intValue(),
                    dim.width,
                    dim.height
            ));


            return new Dimension(dim.width + width, dim.height + height);
        }
    }

    public static class PController extends Controller {
        @Export(attributeName = "left")
        public BindableAttribute<Double> left = new BindableAttribute<>(Double.class, 0.0);
        @Export(attributeName = "right")
        public BindableAttribute<Double> right = new BindableAttribute<>(Double.class, 0.0);
        @Export(attributeName = "top")
        public BindableAttribute<Double> top = new BindableAttribute<>(Double.class, 0.0);
        @Export(attributeName = "bottom")
        public BindableAttribute<Double> bottom = new BindableAttribute<>(Double.class, 0.0);

        public PController(DomElement element) {
            super(element);

            loadDom();

            left.addOnUpdate(a -> element.requestRelayout());
            right.addOnUpdate(a -> element.requestRelayout());
            top.addOnUpdate(a -> element.requestRelayout());
            bottom.addOnUpdate(a -> element.requestRelayout());
        }
    }

    public static final DomElementRegistry.DomElementCreator CREATOR = new DomElementRegistry.GeneralDomElementCreator(
            PLayouter::new, SingleChildRenderer::new, PController::new
    );
}
