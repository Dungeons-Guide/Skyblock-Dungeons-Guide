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

import kr.syeyoung.dungeonsguide.mod.guiv2.BindableAttribute;
import kr.syeyoung.dungeonsguide.mod.guiv2.DomElement;
import kr.syeyoung.dungeonsguide.mod.guiv2.Widget;
import kr.syeyoung.dungeonsguide.mod.guiv2.layouter.Layouter;
import kr.syeyoung.dungeonsguide.mod.guiv2.primitive.ConstraintBox;
import kr.syeyoung.dungeonsguide.mod.guiv2.primitive.Rect;
import kr.syeyoung.dungeonsguide.mod.guiv2.primitive.Size;
import kr.syeyoung.dungeonsguide.mod.guiv2.renderer.OnlyChildrenRenderer;
import kr.syeyoung.dungeonsguide.mod.guiv2.renderer.Renderer;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.AnnotatedExportOnlyWidget;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.annotations.Export;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.data.WidgetList;

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

    @Export(attributeName = "_")
    public final BindableAttribute<WidgetList> widgets = new BindableAttribute<>(WidgetList.class);

    @Export(attributeName = "api")
    public final BindableAttribute<Column> api = new BindableAttribute<>(Column.class, this);

    public Column() {
        hAlign.addOnUpdate((a,b) -> getDomElement().requestRelayout());
        vAlign.addOnUpdate((a,b) -> getDomElement().requestRelayout());
    }

    @Override
    public List<Widget> build(DomElement buildContext) {
        return widgets.getValue();
    }

    @Override
    protected Renderer createRenderer() {
        return OnlyChildrenRenderer.INSTANCE;
    }

    public void addWidget(Widget widget) {
        if (getDomElement().getWidget() == null) {
            widgets.getValue().add(widget);
        } else {
            DomElement domElement = widget.createDomElement(getDomElement());
            getDomElement().addElement(domElement);
        }
    }

    public void removeWidget(Widget widget) {
        getDomElement().removeElement(widget.getDomElement());
    }

    @Override
    public Size layout(DomElement buildContext, ConstraintBox constraints) {
        double width = 0;
        double height = 0;
        double effheight = constraints.getMaxHeight(); // max does not count for column.
        CrossAxisAlignment crossAxisAlignment = hAlign.getValue();
        MainAxisAlignment mainAxisAlignment = vAlign.getValue();
        Map<DomElement, Size> saved = new HashMap<>();
        for (DomElement child : buildContext.getChildren()) {
            if (!(child.getWidget() instanceof Flexible)) {
                Size requiredSize = child.getLayouter().layout(child, new ConstraintBox(
                        crossAxisAlignment == CrossAxisAlignment.STRETCH
                                ? constraints.getMaxWidth() : 0, constraints.getMaxWidth(), 0, Double.POSITIVE_INFINITY
                ));
                saved.put(child, requiredSize);
                width = Math.max(width, requiredSize.getWidth());
                height += requiredSize.getHeight();
            }
        }



        boolean flexFound = false;
        int sumFlex = 0;
        for (DomElement child : buildContext.getChildren()) {
            if (child.getWidget() instanceof Flexible) {
                sumFlex += Math.max(1, ((Flexible) child.getWidget()).flex.getValue());
                flexFound = true;
            }
        }

        if (flexFound && effheight == Double.POSITIVE_INFINITY) throw new IllegalStateException("Max height can not be infinite with flex elements");
        else if (effheight == Double.POSITIVE_INFINITY) effheight = height;

        if (flexFound) {
            double remainingHeight = effheight - height;
            double heightPer = remainingHeight / sumFlex;

            for (DomElement child : buildContext.getChildren()) {
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
        width = constraints.getMaxWidth() == Double.POSITIVE_INFINITY ? width : constraints.getMaxWidth();



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
                heightDelta = remaining / (buildContext.getChildren().size()-1);
            } else {
                starty = (effheight - height) / 2;
            }
        } else if (mainAxisAlignment == MainAxisAlignment.SPACE_EVENLY) {
            double remaining = effheight - height;
            if (remaining > 0) {
                heightDelta = remaining / (buildContext.getChildren().size()+1);
                starty = heightDelta;
            } else {
                starty= (effheight - height) / 2;
            }
        } else if (mainAxisAlignment == MainAxisAlignment.SPACE_AROUND) {
            double remaining = effheight - height;
            if (remaining > 0) {
                heightDelta = 2 * remaining / buildContext.getChildren().size();
                starty = heightDelta / 2;
            } else {
                starty = (effheight - height / 2);
            }
        }

        for (DomElement child : buildContext.getChildren()) {
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
    @Override
    public double getMaxIntrinsicHeight(DomElement buildContext, double width) {
        double height = 0;
        double flex = 0;
        double maxPer = 0;
        for (DomElement child : buildContext.getChildren()) {
            if (child.getWidget() instanceof Flexible) {
                flex += ((Flexible) child.getWidget()).flex.getValue();
                maxPer = Double.max(maxPer, child.getLayouter().getMaxIntrinsicHeight(child, width) /
                        ((Flexible) child.getWidget()).flex.getValue());
            } else {
                height += child.getLayouter().getMaxIntrinsicHeight(child, width);
            }
        }
        return height + maxPer * flex;
    }

    @Override
    public double getMaxIntrinsicWidth(DomElement buildContext, double height) {
        double maxWidth = 0;
        double heightTaken = 0;
        int sumFlex = 0;
        for (DomElement child : buildContext.getChildren()) {
            if (!(child.getWidget() instanceof Flexible)) {
                heightTaken += child.getLayouter().getMaxIntrinsicHeight(child, 0);
                maxWidth = Double.max(maxWidth, child.getLayouter().getMaxIntrinsicWidth(child, 0));
            } else {
                sumFlex += ((Flexible) child.getWidget()).flex.getValue();
            }
        }
        double leftOver = height - heightTaken;
        if (sumFlex > 0) {
            double per = leftOver / sumFlex;
            if (height == 0) per = 0;
            for (DomElement child : buildContext.getChildren()) {
                if (child.getWidget() instanceof Flexible) {
                    maxWidth = Double.max(maxWidth, child.getLayouter().getMaxIntrinsicWidth(child, per * ((Flexible) child.getWidget()).flex.getValue()));
                }
            }
        }
        return maxWidth;
    }
}
