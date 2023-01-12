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
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.AnnotatedExportOnlyWidget;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.annotations.Export;

import java.util.Collections;
import java.util.List;

public class Padding extends AnnotatedExportOnlyWidget implements Layouter {

    @Export(attributeName = "left")
    public final BindableAttribute<Double> left = new BindableAttribute<>(Double.class, 0.0);
    @Export(attributeName = "right")
    public final BindableAttribute<Double> right = new BindableAttribute<>(Double.class, 0.0);
    @Export(attributeName = "top")
    public final BindableAttribute<Double> top = new BindableAttribute<>(Double.class, 0.0);
    @Export(attributeName = "bottom")
    public final BindableAttribute<Double> bottom = new BindableAttribute<>(Double.class, 0.0);
    @Export(attributeName = "$")
    public final BindableAttribute<Widget> child = new BindableAttribute<>(Widget.class);

    public Padding() {
        left.addOnUpdate(a -> getDomElement().requestRelayout());
        right.addOnUpdate(a -> getDomElement().requestRelayout());
        top.addOnUpdate(a -> getDomElement().requestRelayout());
        bottom.addOnUpdate(a -> getDomElement().requestRelayout());
    }

    @Override
    public List<Widget> build(DomElement buildContext) {
        return Collections.singletonList(child.getValue());
    }

    @Override
    public Size layout(DomElement buildContext, ConstraintBox constraintBox) {
        DomElement domElement = getDomElement().getChildren().get(0);

        double width =  (left.getValue() + right.getValue());
        double height =  (top.getValue() + bottom.getValue());
        Size dim = domElement.getLayouter().layout(domElement, new ConstraintBox(
                constraintBox.getMinWidth() - width,
                constraintBox.getMaxWidth() - width,
                constraintBox.getMinHeight() - height,
                constraintBox.getMaxHeight() - height
        ));

        domElement.setRelativeBound(new Rect(
                left.getValue().intValue(),
                top.getValue().intValue(),
                dim.getWidth(),
                dim.getHeight()
        ));


        return new Size(dim.getWidth() + width, dim.getHeight() + height);
    }
}
