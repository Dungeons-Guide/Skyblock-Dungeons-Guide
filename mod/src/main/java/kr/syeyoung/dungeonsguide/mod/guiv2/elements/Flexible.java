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

// yes it's that flexible from flutter
public class Flexible {
    public static class FLayout extends Layouter {
        private FWidget controller;
        public FLayout(DomElement element) {
            super(element);
            this.controller = (FWidget) element.getWidget();
        }

        @Override
        public Dimension layout(ConstraintBox constraintBox) {
            FWidget.FlexFit fit = controller.fit.getValue();
            ConstraintBox box =
                    fit == FWidget.FlexFit.TIGHT ?
                            new ConstraintBox(constraintBox.getMaxWidth() == Integer.MAX_VALUE ? 0 : constraintBox.getMaxWidth(), constraintBox.getMaxWidth()
                                    , constraintBox.getMaxHeight() == Integer.MAX_VALUE ? 0 : constraintBox.getMaxHeight(), constraintBox.getMaxHeight())
                            : ConstraintBox.loose(constraintBox.getMaxWidth(), constraintBox.getMaxHeight());
            Dimension dim = getDomElement().getChildren().get(0).getLayouter().layout(box);

            getDomElement().getChildren().get(0).setRelativeBound(new Rectangle(0,0, dim.width, dim.height));
            return dim;
        }
    }

    public static class FWidget extends Widget {

        @Export(attributeName = "flex")
        public final BindableAttribute<Integer> flex = new BindableAttribute<>(Integer.class, 1);

        @Export(attributeName = "fit")
        public final BindableAttribute<FlexFit> fit = new BindableAttribute<>(FlexFit.class, FlexFit.TIGHT);

        public static enum FlexFit {
            TIGHT, LOOSE
        }

        public FWidget(DomElement element) {
            super(element);
            loadAttributes();
            loadDom();

            flex.addOnUpdate(a -> element.requestRelayout());
            fit.addOnUpdate(a -> element.requestRelayout());
        }
    }

    public static final DomElementRegistry.DomElementCreator CREATOR = new DomElementRegistry.GeneralDomElementCreator(
            FLayout::new, SingleChildRenderer::new, FWidget::new
    );
}
