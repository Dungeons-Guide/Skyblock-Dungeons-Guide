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
import kr.syeyoung.dungeonsguide.mod.guiv2.renderer.Renderer;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class Row {
    public static class RLayout extends Layouter {
        public RLayout(DomElement element) {
            super(element);
            controller = (RController) element.getController();
        }
        RController controller;

        @Override
        public Dimension layout(ConstraintBox constraints) {
            int height = 0;
            int width = 0;
            RController.CrossAxisAlignment crossAxisAlignment = controller.vAlign.getValue();
            RController.MainAxisAlignment mainAxisAlignment = controller.hAlign.getValue();
            Map<DomElement, Dimension> saved = new HashMap<>();
            for (DomElement child : getDomElement().getChildren()) {
                Dimension requiredSize = child.getLayouter().layout(new ConstraintBox(
                        0, Integer.MAX_VALUE,
                        crossAxisAlignment == RController.CrossAxisAlignment.STRETCH ? constraints.getMaxHeight() : 0, constraints.getMaxHeight()
                ));
                saved.put(child, requiredSize);
                height = Math.max(height, requiredSize.height);
                width += requiredSize.width;
            }

            height = constraints.getMaxHeight();
            int effwidth = constraints.getMaxWidth(); // max does not count for row.



            int startx = 0;
            int widthDelta = 0;

            if (mainAxisAlignment == RController.MainAxisAlignment.CENTER)
                startx = (effwidth - width) / 2;
            else if (mainAxisAlignment == RController.MainAxisAlignment.END)
                startx = effwidth - width;
            else if (mainAxisAlignment == RController.MainAxisAlignment.SPACE_BETWEEN) {
                int remaining = effwidth - width;
                if (remaining > 0) {
                    startx = 0;
                    widthDelta = remaining / (getDomElement().getChildren().size()-1);
                } else {
                    startx = (effwidth - width) / 2;
                }
            } else if (mainAxisAlignment == RController.MainAxisAlignment.SPACE_EVENLY) {
                int remaining = effwidth - width;
                if (remaining > 0) {
                    widthDelta = remaining / (getDomElement().getChildren().size()+1);
                    startx = widthDelta;
                } else {
                    startx = (effwidth - width) / 2;
                }
            } else if (mainAxisAlignment == RController.MainAxisAlignment.SPACE_AROUND) {
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
                            crossAxisAlignment == RController.CrossAxisAlignment.START ? 0 :
                            crossAxisAlignment == RController.CrossAxisAlignment.CENTER ? (height-size.height)/2 :
                            crossAxisAlignment == RController.CrossAxisAlignment.STRETCH ? (height-size.height)/2 :
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
    public static class RController extends Controller {
        public static enum CrossAxisAlignment {
            START, CENTER, END, STRETCH
        }
        public static enum MainAxisAlignment {
            START, CENTER, END, SPACE_EVENLY, SPACE_AROUND, SPACE_BETWEEN
        }

        @Export(attributeName = "crossAlign")
        public BindableAttribute<CrossAxisAlignment> vAlign = new BindableAttribute<>(CrossAxisAlignment.class, CrossAxisAlignment.CENTER);

        @Export(attributeName = "mainAlign")
        public BindableAttribute<MainAxisAlignment> hAlign = new BindableAttribute<>(MainAxisAlignment.class, MainAxisAlignment.START);

        public RController(DomElement element) {
            super(element);
            loadDom();

            hAlign.addOnUpdate(a -> element.requestRelayout());
            vAlign.addOnUpdate(a -> element.requestRelayout());
        }
    }
}
