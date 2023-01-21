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
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.AnnotatedExportOnlyWidget;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.annotations.Export;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

// yes it's that flexible from flutter
public class Flexible extends AnnotatedExportOnlyWidget implements Layouter {

    @Export(attributeName = "_")
    public final BindableAttribute<Widget> widget = new BindableAttribute<>(Widget.class);

    @Export(attributeName = "flex")
    public final BindableAttribute<Integer> flex = new BindableAttribute<>(Integer.class, 1);

    @Export(attributeName = "fit")
    public final BindableAttribute<FlexFit> fit = new BindableAttribute<>(FlexFit.class, FlexFit.TIGHT);

    public static enum FlexFit {
        TIGHT, LOOSE
    }

    public Flexible() {
        flex.addOnUpdate((a,b) -> Optional.ofNullable(getDomElement().getParent()).ifPresent(DomElement::requestRelayout));
        fit.addOnUpdate((a,b) -> Optional.ofNullable(getDomElement().getParent()).ifPresent(DomElement::requestRelayout));
    }

    @Override
    public List<Widget> build(DomElement buildContext) {
        return Collections.singletonList(widget.getValue());
    }

    @Override
    public Size layout(DomElement buildContext, ConstraintBox constraintBox) {
        FlexFit fit = this.fit.getValue();
        ConstraintBox box =
                fit == FlexFit.TIGHT ?
                        new ConstraintBox(constraintBox.getMaxWidth() == Double.POSITIVE_INFINITY ? 0 : constraintBox.getMaxWidth(), constraintBox.getMaxWidth()
                                , constraintBox.getMaxHeight() == Double.POSITIVE_INFINITY ? 0 : constraintBox.getMaxHeight(), constraintBox.getMaxHeight())
                        : ConstraintBox.loose(constraintBox.getMaxWidth(), constraintBox.getMaxHeight());
        DomElement child = buildContext.getChildren().get(0);

        Size dim = child.getLayouter().layout(child, box);

        buildContext.getChildren().get(0).setRelativeBound(new Rect(0,0, dim.getWidth(),dim.getHeight()));
        return dim;
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
    public boolean canCutRequest() {
        return Flexible.FlexFit.TIGHT == fit.getValue();
    }
}
