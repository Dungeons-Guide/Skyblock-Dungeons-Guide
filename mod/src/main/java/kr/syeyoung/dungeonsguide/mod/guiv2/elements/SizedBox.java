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
import kr.syeyoung.dungeonsguide.mod.guiv2.renderer.SingleChildRenderer;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.AnnotatedExportOnlyWidget;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.DomElementRegistry;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.annotations.Export;

import java.awt.*;
import java.util.Collections;
import java.util.List;

public class SizedBox extends AnnotatedExportOnlyWidget implements Layouter {

    @Export(attributeName = "width")
    public final BindableAttribute<Double> width = new BindableAttribute<>(Double.class, Double.POSITIVE_INFINITY);
    @Export(attributeName = "height")
    public final BindableAttribute<Double> height = new BindableAttribute<>(Double.class, Double.POSITIVE_INFINITY);
    @Export(attributeName = "$")
    public final BindableAttribute<Widget> child = new BindableAttribute<>(Widget.class);


    @Override
    public List<Widget> build(DomElement buildContext) {
        return child.getValue() == null ? Collections.EMPTY_LIST : Collections.singletonList(child.getValue());
    }

    public SizedBox() {
        width.addOnUpdate(a -> getDomElement().requestRelayout());
        height.addOnUpdate(a -> getDomElement().requestRelayout());
    }

    @Override
    public Size layout(DomElement buildContext, ConstraintBox constraintBox) {

        double width =  Math.min(this.width.getValue(), constraintBox.getMaxWidth());
        double height =  Math.min(this.height.getValue(), constraintBox.getMaxHeight());

        if (getDomElement().getChildren().isEmpty()) {
            return new Size(width, height);
        }

        DomElement child = getDomElement().getChildren().get(0);
        Size dim = child.getLayouter().layout(child, new ConstraintBox(
                width, width, height, height
        )); // force size heh.
        child.setRelativeBound(new Rect(0,0,dim.getWidth(),dim.getHeight()));
        return dim;
    }
}
