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
import kr.syeyoung.dungeonsguide.mod.guiv2.renderer.OnlyChildrenRenderer;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.DomElementRegistry;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.annotations.Export;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class Row {
    public static class RLayout extends Layouter {
        public RLayout(DomElement element) {
            super(element);
            controller = (RWidget) element.getWidget();
        }
        RWidget controller;

        @Override
        public Dimension layout(ConstraintBox constraints) {
            int height = 0;
            int width = 0;
            int effwidth = constraints.getMaxWidth(); // max does not count for row.

            RWidget.CrossAxisAlignment crossAxisAlignment = controller.vAlign.getValue();
            RWidget.MainAxisAlignment mainAxisAlignment = controller.hAlign.getValue();
            Map<DomElement, Dimension> saved = new HashMap<>();

            for (DomElement child : getDomElement().getChildren()) {
                if (!(child.getWidget() instanceof Flexible.FWidget)) {
                    Dimension requiredSize = child.getLayouter().layout(new ConstraintBox(
                            0, Integer.MAX_VALUE,
                            crossAxisAlignment == RWidget.CrossAxisAlignment.STRETCH ? constraints.getMaxHeight() : 0, constraints.getMaxHeight()
                    ));
                    saved.put(child, requiredSize);
                    height = Math.max(height, requiredSize.height);
                    width += requiredSize.width;
                }
            }


            boolean flexFound = false;
            int sumFlex = 0;
            for (DomElement child : getDomElement().getChildren()) {
                if (child.getWidget() instanceof Flexible.FWidget) {
                    sumFlex += Math.min(1, ((Flexible.FWidget) child.getWidget()).flex.getValue());
                    flexFound =true;
                }
            }

            if (flexFound && effwidth == Integer.MAX_VALUE) throw new IllegalStateException("Max width can not be infinite with flex elements");
            else if (effwidth == Integer.MAX_VALUE) effwidth = width;

            if (flexFound) {
                int remainingWidth = effwidth - width;
                int widthPer = remainingWidth / sumFlex;

                for (DomElement child : getDomElement().getChildren()) {
                    if (child.getWidget() instanceof Flexible.FWidget) {
                        Dimension requiredSize = child.getLayouter().layout(new ConstraintBox(
                                0, widthPer * ((Flexible.FWidget) child.getWidget()).flex.getValue(),
                                crossAxisAlignment == RWidget.CrossAxisAlignment.STRETCH ? constraints.getMaxHeight() : 0, constraints.getMaxHeight()
                        ));
                        saved.put(child, requiredSize);
                        height = Math.max(height, requiredSize.height);
                        width += requiredSize.width;
                    }
                }
            }


            height = constraints.getMaxHeight() == Integer.MAX_VALUE ? height : constraints.getMaxHeight();



            int startx = 0;
            int widthDelta = 0;

            if (mainAxisAlignment == RWidget.MainAxisAlignment.CENTER)
                startx = (effwidth - width) / 2;
            else if (mainAxisAlignment == RWidget.MainAxisAlignment.END)
                startx = effwidth - width;
            else if (mainAxisAlignment == RWidget.MainAxisAlignment.SPACE_BETWEEN) {
                int remaining = effwidth - width;
                if (remaining > 0) {
                    startx = 0;
                    widthDelta = remaining / (getDomElement().getChildren().size()-1);
                } else {
                    startx = (effwidth - width) / 2;
                }
            } else if (mainAxisAlignment == RWidget.MainAxisAlignment.SPACE_EVENLY) {
                int remaining = effwidth - width;
                if (remaining > 0) {
                    widthDelta = remaining / (getDomElement().getChildren().size()+1);
                    startx = widthDelta;
                } else {
                    startx = (effwidth - width) / 2;
                }
            } else if (mainAxisAlignment == RWidget.MainAxisAlignment.SPACE_AROUND) {
                int remaining = effwidth - width;
                if (remaining > 0) {
                    widthDelta = 2 * remaining / getDomElement().getChildren().size();
                    startx = widthDelta / 2;
                } else {
                    startx = (effwidth - width / 2);
                }
            }

            for (DomElement child : getDomElement().getChildren()) {
                Dimension size = saved.get(child);

                child.setRelativeBound(new Rectangle(
                        startx,
                            crossAxisAlignment == RWidget.CrossAxisAlignment.START ? 0 :
                            crossAxisAlignment == RWidget.CrossAxisAlignment.CENTER ? (height-size.height)/2 :
                            crossAxisAlignment == RWidget.CrossAxisAlignment.STRETCH ? (height-size.height)/2 :
                                            height - size.height,size.width, size.height
                ));
                startx += size.width;
                startx += widthDelta;
            }
            return new Dimension(
                    effwidth,
                    height
            );
        }
    }
    public static class RWidget extends Widget {
        public static enum CrossAxisAlignment {
            START, CENTER, END, STRETCH
        }
        public static enum MainAxisAlignment {
            START, CENTER, END, SPACE_EVENLY, SPACE_AROUND, SPACE_BETWEEN
        }

        @Export(attributeName = "crossAlign")
        public final BindableAttribute<CrossAxisAlignment> vAlign = new BindableAttribute<>(CrossAxisAlignment.class, CrossAxisAlignment.CENTER);

        @Export(attributeName = "mainAlign")
        public final BindableAttribute<MainAxisAlignment> hAlign = new BindableAttribute<>(MainAxisAlignment.class, MainAxisAlignment.START);

        public RWidget(DomElement element) {
            super(element);
            loadAttributes();
            loadDom();

            hAlign.addOnUpdate(a -> element.requestRelayout());
            vAlign.addOnUpdate(a -> element.requestRelayout());
        }
    }



    public static final DomElementRegistry.DomElementCreator CREATOR = new DomElementRegistry.GeneralDomElementCreator(
            RLayout::new, OnlyChildrenRenderer::new, RWidget::new
    );
}
