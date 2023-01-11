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
import org.apache.logging.log4j.core.Layout;

import java.awt.*;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Column extends AnnotatedExportOnlyWidget implements Layouter {
    public static enum CrossAxisAlignment {
        START, CENTER, END, STRETCH
    }
    public static enum MainAxisAlignment {
        START, CENTER, END, SPACE_EVENLY, SPACE_AROUND, SPACE_BETWEEN
    }

    @Export(attributeName = "crossAlign")
    public final BindableAttribute<CrossAxisAlignment> hAlign = new BindableAttribute<>(CrossAxisAlignment.class, CrossAxisAlignment.CENTER);

    @Export(attributeName = "mainAlign")
    public final BindableAttribute<MainAxisAlignment> vAlign = new BindableAttribute<>(MainAxisAlignment.class, MainAxisAlignment.START);

    @Export(attributeName = "$")
    public final BindableAttribute<WidgetList> widgets = new BindableAttribute<>(WidgetList.class);
    
    public Column() {
        hAlign.addOnUpdate(a -> getDomElement().requestRelayout());
        vAlign.addOnUpdate(a -> getDomElement().requestRelayout());
    }

    @Override
    public List<Widget> build(DomElement buildContext) {
        return widgets.getValue();
    }

    @Override
    protected Renderer createRenderer() {
        return OnlyChildrenRenderer.INSTANCE;
    }

    @Override
    public Size layout(DomElement buildContext, ConstraintBox constraints) {
        double width = 0;
        double height = 0;
        double effheight = constraints.getMaxHeight(); // max does not count for column.
        CrossAxisAlignment crossAxisAlignment = hAlign.getValue();
        MainAxisAlignment mainAxisAlignment = vAlign.getValue();
        Map<DomElement, Size> saved = new HashMap<>();
        for (DomElement child : getDomElement().getChildren()) {
            if (!(child.getWidget() instanceof Flexible)) {
                Size requiredSize = child.getLayouter().layout(child, new ConstraintBox(
                        crossAxisAlignment == CrossAxisAlignment.STRETCH
                                ? constraints.getMaxWidth() : 0, constraints.getMaxWidth(), 0, Integer.MAX_VALUE
                ));
                saved.put(child, requiredSize);
                width = Math.max(width, requiredSize.getWidth());
                height += requiredSize.getHeight();
            }
        }



        boolean flexFound = false;
        int sumFlex = 0;
        for (DomElement child : getDomElement().getChildren()) {
            if (child.getWidget() instanceof Flexible) {
                sumFlex += Math.min(1, ((Flexible) child.getWidget()).flex.getValue());
                flexFound = true;
            }
        }

        if (flexFound && effheight == Integer.MAX_VALUE) throw new IllegalStateException("Max height can not be infinite with flex elements");
        else if (effheight == Integer.MAX_VALUE) effheight = height;

        if (flexFound) {
            double remainingHeight = effheight - height;
            double heightPer = remainingHeight / sumFlex;

            for (DomElement child : getDomElement().getChildren()) {
                if (child.getWidget() instanceof Flexible) {
                    Size requiredSize = child.getLayouter().layout(child, new ConstraintBox(
                            crossAxisAlignment == CrossAxisAlignment.STRETCH
                                    ? constraints.getMaxWidth() : 0, constraints.getMaxWidth(), 0, heightPer * ((Flexible) child.getWidget()).flex.getValue()
                    ));
                    saved.put(child, requiredSize);
                    width = Math.max(width, requiredSize.getWidth());
                    height += requiredSize.getHeight();
                }
            }
        }
        width = constraints.getMaxWidth() == Integer.MAX_VALUE ? width : constraints.getMaxWidth();



        double starty = 0;
        double heightDelta = 0;

        if (mainAxisAlignment == MainAxisAlignment.CENTER)
            starty = (effheight - height) / 2;
        else if (mainAxisAlignment == MainAxisAlignment.END)
            starty = effheight - height;
        else if (mainAxisAlignment == MainAxisAlignment.SPACE_BETWEEN) {
            double remaining = effheight - height;
            if (remaining > 0) {
                starty = 0;
                heightDelta = remaining / (getDomElement().getChildren().size()-1);
            } else {
                starty = (effheight - height) / 2;
            }
        } else if (mainAxisAlignment == MainAxisAlignment.SPACE_EVENLY) {
            double remaining = effheight - height;
            if (remaining > 0) {
                heightDelta = remaining / (getDomElement().getChildren().size()+1);
                starty = heightDelta;
            } else {
                starty= (effheight - height) / 2;
            }
        } else if (mainAxisAlignment == MainAxisAlignment.SPACE_AROUND) {
            double remaining = effheight - height;
            if (remaining > 0) {
                heightDelta = 2 * remaining / getDomElement().getChildren().size();
                starty = heightDelta / 2;
            } else {
                starty = (effheight - height / 2);
            }
        }

        for (DomElement child : getDomElement().getChildren()) {
            Size size = saved.get(child);

            child.setRelativeBound(new Rect(
                    crossAxisAlignment == CrossAxisAlignment.START ? 0 :
                            crossAxisAlignment == CrossAxisAlignment.CENTER ? (width-size.getWidth())/2 :
                                    crossAxisAlignment == CrossAxisAlignment.STRETCH ? (width - size.getWidth()) /2 :
                                            width - size.getWidth(), starty
                    ,size.getWidth(), size.getHeight()
            ));
            starty += size.getHeight();
            starty += heightDelta;
        }
        return new Size(
                width,
                effheight
        );
    }
}
