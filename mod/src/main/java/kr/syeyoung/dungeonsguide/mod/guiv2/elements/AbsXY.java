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

package kr.syeyoung.dungeonsguide.mod.guiv2.elements;

import kr.syeyoung.dungeonsguide.mod.guiv2.BindableAttribute;
import kr.syeyoung.dungeonsguide.mod.guiv2.DomElement;
import kr.syeyoung.dungeonsguide.mod.guiv2.Widget;
import kr.syeyoung.dungeonsguide.mod.guiv2.layouter.Layouter;
import kr.syeyoung.dungeonsguide.mod.guiv2.primitive.ConstraintBox;
import kr.syeyoung.dungeonsguide.mod.guiv2.primitive.Rect;
import kr.syeyoung.dungeonsguide.mod.guiv2.primitive.Size;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.AnnotatedExportOnlyWidget;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.annotations.Export;

import java.util.Collections;
import java.util.List;

public class AbsXY extends AnnotatedExportOnlyWidget implements Layouter {
    @Export(attributeName = "_")
    public final BindableAttribute<Widget> child = new BindableAttribute<>(Widget.class);

    @Export(attributeName = "x")
    public final BindableAttribute<Double> x = new BindableAttribute<>(Double.class, 0.0);

    @Export(attributeName = "y")
    public final BindableAttribute<Double> y = new BindableAttribute<>(Double.class, 0.0);

    public AbsXY() {
        x.addOnUpdate(this::setLocations);
        y.addOnUpdate(this::setLocations);
    }

    private void setLocations(double old, double neu) {
        if (getDomElement().getChildren().size() == 0) return;
        DomElement child = getDomElement().getChildren().get(0);
        if (child.getSize() == null) return;
        child.setRelativeBound(new Rect(x.getValue(), y.getValue(), child.getSize().getWidth(), child.getSize().getHeight()));
    }

    @Override
    public Size layout(DomElement buildContext, ConstraintBox constraintBox) {
        DomElement child = buildContext.getChildren().get(0);
        Size size = child.getLayouter().layout(buildContext, new ConstraintBox(
                0,constraintBox.getMaxWidth() - x.getValue(), 0, constraintBox.getMaxHeight()-y.getValue()
        ));
        child.setRelativeBound(new Rect(x.getValue(), y.getValue(), size.getWidth(), size.getHeight()));
        return new Size(constraintBox.getMaxWidth(), constraintBox.getMaxHeight());
    }


    @Override
    public double getMaxIntrinsicWidth(DomElement buildContext, double height) {
        return buildContext.getChildren().isEmpty() ? 0 : buildContext.getChildren().get(0).getLayouter().getMaxIntrinsicWidth(buildContext.getChildren().get(0), height);
    }

    @Override
    public double getMaxIntrinsicHeight(DomElement buildContext, double width) {
        return buildContext.getChildren().isEmpty() ? 0 : buildContext.getChildren().get(0).getLayouter().getMaxIntrinsicHeight(buildContext.getChildren().get(0), width);
    }
    @Override
    public List<Widget> build(DomElement buildContext) {
        return Collections.singletonList(child.getValue());
    }
}
