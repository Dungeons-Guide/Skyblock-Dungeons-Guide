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

public class Column {
    public static class CLayout extends Layouter {
        public CLayout(DomElement element) {
            super(element);
            controller = (CController) element.getController();
        }
        CController controller;

        @Override
        public Dimension layout(ConstraintBox constraints) {
            int width = 0;
            int height = 0;
            CController.CrossAxisAlignment crossAxisAlignment = controller.hAlign.getValue();
            CController.MainAxisAlignment mainAxisAlignment = controller.vAlign.getValue();
            Map<DomElement, Dimension> saved = new HashMap<>();
            for (DomElement child : getDomElement().getChildren()) {
                Dimension requiredSize = child.getLayouter().layout(new ConstraintBox(
                        crossAxisAlignment == CController.CrossAxisAlignment.STRETCH 
                                ? constraints.getMaxWidth() : 0, constraints.getMaxWidth(), 0, Integer.MAX_VALUE
                ));
                saved.put(child, requiredSize);
                width = Math.max(width, requiredSize.width);
                height += requiredSize.height;
            }

            width = constraints.getMaxWidth();
            int effheight = constraints.getMaxHeight(); // max does not count for column.



            int starty = 0;
            int heightDelta = 0;

            if (mainAxisAlignment == CController.MainAxisAlignment.CENTER)
                starty = (effheight - height) / 2;
            else if (mainAxisAlignment == CController.MainAxisAlignment.END)
                starty = effheight - height;
            else if (mainAxisAlignment == CController.MainAxisAlignment.SPACE_BETWEEN) {
                int remaining = effheight - height;
                if (remaining > 0) {
                    starty = 0;
                    heightDelta = remaining / (getDomElement().getChildren().size()-1);
                } else {
                    starty = (effheight - height) / 2;
                }
            } else if (mainAxisAlignment == CController.MainAxisAlignment.SPACE_EVENLY) {
                int remaining = effheight - height;
                if (remaining > 0) {
                    heightDelta = remaining / (getDomElement().getChildren().size()+1);
                    starty = heightDelta;
                } else {
                    starty= (effheight - height) / 2;
                }
            } else if (mainAxisAlignment == CController.MainAxisAlignment.SPACE_AROUND) {
                int remaining = effheight - height;
                if (remaining > 0) {
                    heightDelta = 2 * remaining / getDomElement().getChildren().size();
                    starty = heightDelta / 2;
                } else {
                    starty = (effheight - height / 2);
                }
            }

            for (DomElement child : getDomElement().getChildren()) {
                Dimension size = saved.get(child);

                child.setRelativeBound(new Rectangle(
                        crossAxisAlignment == CController.CrossAxisAlignment.START ? 0 : 
                        crossAxisAlignment == CController.CrossAxisAlignment.CENTER ? (width-size.width)/2 : 
                        crossAxisAlignment == CController.CrossAxisAlignment.STRETCH ? (width - size.width) /2 :
                                        width - size.width, starty
                            ,size.height, size.width
                ));
                starty += size.height;
                starty += heightDelta;
            }
            return new Dimension(
                    width,
                    effheight
            );
        }
    }

    public static class CController extends Controller {
        public static enum CrossAxisAlignment {
            START, CENTER, END, STRETCH
        }
        public static enum MainAxisAlignment {
            START, CENTER, END, SPACE_EVENLY, SPACE_AROUND, SPACE_BETWEEN
        }

        @Export(attributeName = "crossAlign")
        public BindableAttribute<CrossAxisAlignment> hAlign = new BindableAttribute<>(CrossAxisAlignment.class, CrossAxisAlignment.CENTER);

        @Export(attributeName = "mainAlign")
        public BindableAttribute<MainAxisAlignment> vAlign = new BindableAttribute<>(MainAxisAlignment.class, MainAxisAlignment.START);

        public CController(DomElement element) {
            super(element);
            loadDom();

            hAlign.addOnUpdate(a -> element.requestRelayout());
            vAlign.addOnUpdate(a -> element.requestRelayout());
        }
    }
}
