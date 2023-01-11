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

public class Border {
    public static class BLayouter extends Layouter {
        private BWidget controller;
        public BLayouter(DomElement element) {
            super(element);
            this.controller = (BWidget) element.getWidget();
        }

        @Override
        public Dimension layout(ConstraintBox constraintBox) {
            // layout borders, ask them about their constraints
            // then layout content with space less than blahblah
            // then relayout borders.
            int th = 0, bh = 0;
            {
                if (controller.top != null)
                    th= controller.top.getLayouter().layout(new ConstraintBox(constraintBox.getMaxWidth(), constraintBox.getMaxWidth(), 0, constraintBox.getMaxHeight())).height;
                if (controller.bottom != null)
                    bh = controller.bottom.getLayouter().layout(new ConstraintBox(constraintBox.getMaxWidth(), constraintBox.getMaxWidth(), 0, constraintBox.getMaxHeight())).height;
            }

            int lw = 0, rw = 0;
            {
                if (controller.left != null)
                    lw= controller.left.getLayouter().layout(new ConstraintBox(0, constraintBox.getMaxWidth(), constraintBox.getMaxHeight(), constraintBox.getMaxHeight())).width;
                if (controller.right != null)
                    rw= controller.right.getLayouter().layout(new ConstraintBox(0, constraintBox.getMaxWidth(), constraintBox.getMaxHeight(), constraintBox.getMaxHeight())).width;
            }

            Dimension dimension = controller.content.getLayouter().layout(new ConstraintBox(
                    0, constraintBox.getMaxWidth() - lw - rw,
                    0, constraintBox.getMaxHeight() - th - bh
            ));


            {
                if (controller.left != null)
                    lw= controller.left.getLayouter().layout(new ConstraintBox(lw, lw, dimension.height, dimension.height)).width;
                if (controller.right != null)
                    rw= controller.right.getLayouter().layout(new ConstraintBox(rw, rw, dimension.height, dimension.height)).width;
            }
            {
                if (controller.top != null)
                    th= controller.top.getLayouter().layout(new ConstraintBox(dimension.width + lw + rw, dimension.width + lw + rw,  th, th)).height;
                if (controller.bottom != null)
                    bh = controller.bottom.getLayouter().layout(new ConstraintBox(dimension.width + lw + rw, dimension.width + lw + rw,  bh,bh)).height;
            }

            if (controller.top != null)
                controller.top.setRelativeBound(new Rectangle(0,0, dimension.width + lw + rw, th));
            if (controller.bottom != null)
                controller.bottom.setRelativeBound(new Rectangle(0, dimension.height + th, dimension.width + lw + rw, bh));
            if (controller.left != null)
                controller.left.setRelativeBound(new Rectangle(0, th, lw, dimension.height));
            if (controller.right != null)
                controller.right.setRelativeBound(new Rectangle(lw + dimension.width, th, rw, dimension.height));

            controller.content.setRelativeBound(new Rectangle(
                    th, lw, dimension.width, dimension.height
            ));



            return new Dimension(dimension.width + lw + rw, dimension.height + th + bh);
        }
    }

    public static class BWidget extends Widget {
        DomElement left, right, top, bottom, content;
        public BWidget(DomElement element) {
            super(element);
            loadAttributes();

            if (!getSlots().containsKey("content")) throw new IllegalArgumentException("No content for border?");

            if (getSlots().containsKey("left"))
                element.addElement(left =getSlots().get("left"));
            if (getSlots().containsKey("right"))
                element.addElement(right = getSlots().get("right"));
            if (getSlots().containsKey("top"))
                element.addElement(top = getSlots().get("top"));
            if (getSlots().containsKey("bottom"))
                element.addElement(bottom = getSlots().get("bottom"));
            element.addElement(content = getSlots().get("content"));
        }
    }

    public static final DomElementRegistry.DomElementCreator CREATOR = new DomElementRegistry.GeneralDomElementCreator(
            BLayouter::new, OnlyChildrenRenderer::new, BWidget::new
    );
}
