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
import kr.syeyoung.dungeonsguide.mod.guiv2.primitive.Rect;
import kr.syeyoung.dungeonsguide.mod.guiv2.primitive.Size;
import kr.syeyoung.dungeonsguide.mod.guiv2.renderer.OnlyChildrenRenderer;
import kr.syeyoung.dungeonsguide.mod.guiv2.renderer.Renderer;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.AnnotatedExportOnlyWidget;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.DomElementRegistry;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.annotations.Export;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.data.WidgetList;

import java.awt.*;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Row extends AnnotatedExportOnlyWidget implements Layouter {
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

    @Export(attributeName = "$")
    public final BindableAttribute<WidgetList> children = new BindableAttribute<>(WidgetList.class);
    
    public Row() {
        hAlign.addOnUpdate(a -> getDomElement().requestRelayout());
        vAlign.addOnUpdate(a -> getDomElement().requestRelayout());
    }

    @Override
    public List<Widget> build(DomElement buildContext) {
        return children.getValue();
    }

    @Override
    protected Renderer createRenderer() {
        return OnlyChildrenRenderer.INSTANCE;
    }

    @Override
    public Size layout(DomElement buildContext, ConstraintBox constraintBox) {
        double height = 0;
        double width = 0;
        double effwidth = constraintBox.getMaxWidth(); // max does not count for row.

        CrossAxisAlignment crossAxisAlignment = vAlign.getValue();
        MainAxisAlignment mainAxisAlignment = hAlign.getValue();
        Map<DomElement, Size> saved = new HashMap<>();

        for (DomElement child : getDomElement().getChildren()) {
            if (!(child.getWidget() instanceof Flexible)) {
                Size requiredSize = child.getLayouter().layout(child, new ConstraintBox(
                        0, Integer.MAX_VALUE,
                        crossAxisAlignment == CrossAxisAlignment.STRETCH ? constraintBox.getMaxHeight() : 0, constraintBox.getMaxHeight()
                ));
                saved.put(child, requiredSize);
                height = Math.max(height, requiredSize.getHeight());
                width += requiredSize.getWidth();
            }
        }


        boolean flexFound = false;
        int sumFlex = 0;
        for (DomElement child : getDomElement().getChildren()) {
            if (child.getWidget() instanceof Flexible) {
                sumFlex += Math.min(1, ((Flexible) child.getWidget()).flex.getValue());
                flexFound =true;
            }
        }

        if (flexFound && effwidth == Integer.MAX_VALUE) throw new IllegalStateException("Max width can not be infinite with flex elements");
        else if (effwidth == Integer.MAX_VALUE) effwidth = width;

        if (flexFound) {
            double remainingWidth = effwidth - width;
            double widthPer = remainingWidth / sumFlex;

            for (DomElement child : getDomElement().getChildren()) {
                if (child.getWidget() instanceof Flexible) {
                    Size requiredSize = child.getLayouter().layout(child, new ConstraintBox(
                            0, widthPer * ((Flexible) child.getWidget()).flex.getValue(),
                            crossAxisAlignment == CrossAxisAlignment.STRETCH ? constraintBox.getMaxHeight() : 0, constraintBox.getMaxHeight()
                    ));
                    saved.put(child, requiredSize);
                    height = Math.max(height, requiredSize.getHeight());
                    width += requiredSize.getWidth();
                }
            }
        }


        height = constraintBox.getMaxHeight() == Integer.MAX_VALUE ? height : constraintBox.getMaxHeight();



        double startx = 0;
        double widthDelta = 0;

        if (mainAxisAlignment == MainAxisAlignment.CENTER)
            startx = (effwidth - width) / 2;
        else if (mainAxisAlignment == MainAxisAlignment.END)
            startx = effwidth - width;
        else if (mainAxisAlignment == MainAxisAlignment.SPACE_BETWEEN) {
            double remaining = effwidth - width;
            if (remaining > 0) {
                startx = 0;
                widthDelta = remaining / (getDomElement().getChildren().size()-1);
            } else {
                startx = (effwidth - width) / 2;
            }
        } else if (mainAxisAlignment == MainAxisAlignment.SPACE_EVENLY) {
            double remaining = effwidth - width;
            if (remaining > 0) {
                widthDelta = remaining / (getDomElement().getChildren().size()+1);
                startx = widthDelta;
            } else {
                startx = (effwidth - width) / 2;
            }
        } else if (mainAxisAlignment == MainAxisAlignment.SPACE_AROUND) {
            double remaining = effwidth - width;
            if (remaining > 0) {
                widthDelta = 2 * remaining / getDomElement().getChildren().size();
                startx = widthDelta / 2;
            } else {
                startx = (effwidth - width / 2);
            }
        }

        for (DomElement child : getDomElement().getChildren()) {
            Size size = saved.get(child);

            child.setRelativeBound(new Rect(
                    startx,
                    crossAxisAlignment == CrossAxisAlignment.START ? 0 :
                            crossAxisAlignment == CrossAxisAlignment.CENTER ? (height-size.getHeight())/2 :
                                    crossAxisAlignment == CrossAxisAlignment.STRETCH ? (height-size.getHeight())/2 :
                                            height - size.getHeight(),size.getWidth(), size.getHeight()
            ));
            startx += size.getWidth();
            startx += widthDelta;
        }
        return new Size(
                effwidth,
                height
        );
    }
}
