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

import kr.syeyoung.dungeonsguide.mod.guiv2.ConstraintBox;
import kr.syeyoung.dungeonsguide.mod.guiv2.DomElement;
import kr.syeyoung.dungeonsguide.mod.guiv2.RootDom;
import kr.syeyoung.dungeonsguide.mod.guiv2.layouter.Layouter;

import java.awt.*;

// this element is very important, because it has no parent, and it fills to screen.
public class View {

    public static class VLayout extends Layouter {
        public VLayout(DomElement element) {
            super(element);
        }

        @Override
        public Dimension layout(ConstraintBox constraintBox) {
            if (getDomElement().getChildren().isEmpty()) {
                return new Dimension(constraintBox.getMaxWidth(), constraintBox.getMaxHeight());
            }

            Dimension dim = getDomElement().getChildren().get(0).getLayouter().layout(constraintBox);
            getDomElement().getChildren().get(0).setRelativeBound(new Rectangle(0,0, dim.width, dim.height));
            return new Dimension(constraintBox.getMaxWidth(), constraintBox.getMaxHeight());
        }
    }

}
