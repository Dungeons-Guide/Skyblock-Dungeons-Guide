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

import kr.syeyoung.dungeonsguide.mod.guiv2.primitive.ConstraintBox;
import kr.syeyoung.dungeonsguide.mod.guiv2.Widget;
import kr.syeyoung.dungeonsguide.mod.guiv2.DomElement;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.DomElementRegistry;
import kr.syeyoung.dungeonsguide.mod.guiv2.layouter.Layouter;
import kr.syeyoung.dungeonsguide.mod.guiv2.renderer.OnlyChildrenRenderer;

import java.awt.*;

public class Stack {
    public static class SLayouter extends Layouter {

        public SLayouter(DomElement element) {
            super(element);
        }

        @Override
        public Dimension layout(ConstraintBox constraintBox) {
            Dimension max = new Dimension();
            for (DomElement child : getDomElement().getChildren()) {
                Dimension dim = child.getLayouter().layout(constraintBox);
                if (max.width < dim.width) max.width = dim.width;
                if (max.height < dim.height) max.height = dim.height;
                child.setRelativeBound(new Rectangle(0,0,dim.width, dim.height));
            }
            return max;
        }
    }

    public static class SWidget extends Widget {

        public SWidget(DomElement element) {
            super(element);
            loadAttributes();
            loadDom();
        }
    }



    public static final DomElementRegistry.DomElementCreator CREATOR = new DomElementRegistry.GeneralDomElementCreator(
            SLayouter::new, OnlyChildrenRenderer::new, SWidget::new
    );
}
