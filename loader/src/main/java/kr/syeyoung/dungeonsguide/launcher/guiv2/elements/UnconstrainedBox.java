/*
 * Dungeons Guide - The most intelligent Hypixel Skyblock Dungeons Mod
 * Copyright (C) 2023  cyoung06 (syeyoung)
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

package kr.syeyoung.dungeonsguide.launcher.guiv2.elements;

import kr.syeyoung.dungeonsguide.launcher.guiv2.BindableAttribute;
import kr.syeyoung.dungeonsguide.launcher.guiv2.DomElement;
import kr.syeyoung.dungeonsguide.launcher.guiv2.Widget;
import kr.syeyoung.dungeonsguide.launcher.guiv2.layouter.Layouter;
import kr.syeyoung.dungeonsguide.launcher.guiv2.primitive.ConstraintBox;
import kr.syeyoung.dungeonsguide.launcher.guiv2.primitive.Rect;
import kr.syeyoung.dungeonsguide.launcher.guiv2.primitive.Size;
import kr.syeyoung.dungeonsguide.launcher.guiv2.xml.AnnotatedExportOnlyWidget;
import kr.syeyoung.dungeonsguide.launcher.guiv2.xml.annotations.Export;

import java.util.Collections;
import java.util.List;

public class UnconstrainedBox extends AnnotatedExportOnlyWidget implements Layouter {


    @Export(attributeName = "_")
    public final BindableAttribute<Widget> widget = new BindableAttribute<>(Widget.class);
    @Export(attributeName = "direction")
    public final BindableAttribute<ScrollablePanel.Direction> direction = new BindableAttribute<>(ScrollablePanel.Direction.class, ScrollablePanel.Direction.BOTH);

    @Override
    public List<Widget> build(DomElement buildContext) {
        return Collections.singletonList(widget.getValue());
    }

    @Override
    public Size layout(DomElement buildContext, ConstraintBox constraintBox) {
        if (buildContext.getChildren().isEmpty()) {
            return new Size(constraintBox.getMaxWidth(), constraintBox.getMaxHeight());
        }
        DomElement childCtx = buildContext.getChildren().get(0);

        Size dim = childCtx.getLayouter().layout(childCtx, new ConstraintBox(0,
                direction.getValue().isHorizontal() ? Double.POSITIVE_INFINITY : constraintBox.getMaxWidth(),
                0, direction.getValue().isVertical() ? Double.POSITIVE_INFINITY : constraintBox.getMaxHeight()));
        childCtx.setRelativeBound(new Rect(0,0, dim.getWidth(), dim.getHeight()));
        return new Size(dim.getWidth(), dim.getHeight());
    }


    @Override
    public double getMaxIntrinsicWidth(DomElement buildContext, double height) {
        return buildContext.getChildren().isEmpty() ? 0 : buildContext.getChildren().get(0).getLayouter().getMaxIntrinsicWidth(buildContext.getChildren().get(0), height);
    }

    @Override
    public double getMaxIntrinsicHeight(DomElement buildContext, double width) {
        return buildContext.getChildren().isEmpty() ? 0 : buildContext.getChildren().get(0).getLayouter().getMaxIntrinsicHeight(buildContext.getChildren().get(0), width);
    }
}
