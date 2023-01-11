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
import kr.syeyoung.dungeonsguide.mod.guiv2.renderer.Renderer;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.DomElementRegistry;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.annotations.Export;

import java.awt.*;

public class Scaler {
    public static class SLayouter extends Layouter {
        private SWidget controller;

        public SLayouter(DomElement element) {
            super(element);
            this.controller = (SWidget) element.getWidget();
        }

        @Override
        public Dimension layout(ConstraintBox constraintBox) {
            Dimension dims = getDomElement().getChildren().get(0).getLayouter().layout(new ConstraintBox(
                    (int) (constraintBox.getMinWidth() / controller.scale.getValue()),
                    (int) (constraintBox.getMaxWidth() / controller.scale.getValue()),
                    (int) (constraintBox.getMinHeight() / controller.scale.getValue()),
                    (int) (constraintBox.getMaxHeight() / controller.scale.getValue())
            ));

            getDomElement().getChildren().get(0).setRelativeBound(new Rectangle(0,0,
                    (int) (dims.width),
                    (int) (dims.height)));

            return new Dimension((int) (dims.width * controller.scale.getValue()), (int) (dims.height * controller.scale.getValue()));
        }
    }

    public static class SRenderer extends Renderer {
        private SWidget controller;

        public SRenderer(DomElement element) {
            super(element);
            this.controller = (SWidget) element.getWidget();
        }

        @Override
        public Rectangle applyTransformation(DomElement target) {
            Rectangle bound =
                    target.getRelativeBound();
            return new Rectangle(
                    bound.x,
                    bound.y,
                    (int) (bound.width * controller.scale.getValue()),
                    (int) (bound.height * controller.scale.getValue())
            );
        }
    }

    public static class SWidget extends Widget {
        @Export(attributeName = "scale")
        public final BindableAttribute<Double> scale = new BindableAttribute<>(Double.class, 1.0);


        public SWidget(DomElement element) {
            super(element);
            loadAttributes();
            loadDom();
        }
    }



    public static final DomElementRegistry.DomElementCreator CREATOR = new DomElementRegistry.GeneralDomElementCreator(
            SLayouter::new, SRenderer::new, SWidget::new
    );
}
