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

public class IntrinsicHeight extends AnnotatedExportOnlyWidget implements Layouter {
    @Export(attributeName = "_")
    public final BindableAttribute<Widget> widget = new BindableAttribute<>(Widget.class);

    @Override
    public List<Widget> build(DomElement buildContext) {
        return Collections.singletonList(widget.getValue());
    }

    @Override
    public Size layout(DomElement buildContext, ConstraintBox constraintBox) {
        DomElement elem = buildContext.getChildren().get(0);
        double height = elem.getLayouter().getMaxIntrinsicHeight(elem, constraintBox.getMaxWidth() == Double.POSITIVE_INFINITY ? 0 : constraintBox.getMaxWidth());
        Size size = elem.getLayouter().layout(elem, new ConstraintBox(
                constraintBox.getMinWidth(), constraintBox.getMaxWidth(), height, height
        ));
        elem.setRelativeBound(new Rect(0,0,size.getWidth(), size.getHeight()));
        return size;
    }

    @Override
    public double getMaxIntrinsicWidth(DomElement buildContext, double height) {
        DomElement elem = buildContext.getChildren().get(0);
        return elem.getLayouter().getMaxIntrinsicWidth(elem, height);
    }

    @Override
    public double getMaxIntrinsicHeight(DomElement buildContext, double width) {
        DomElement elem = buildContext.getChildren().get(0);
        return elem.getLayouter().getMaxIntrinsicHeight(elem, width);
    }
}
