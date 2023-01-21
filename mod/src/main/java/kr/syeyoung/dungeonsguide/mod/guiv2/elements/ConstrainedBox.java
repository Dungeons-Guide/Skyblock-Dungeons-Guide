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
import java.util.Objects;

public class ConstrainedBox extends AnnotatedExportOnlyWidget implements Layouter {

    @Export(attributeName = "minWidth")
    public final BindableAttribute<Double> minWidth = new BindableAttribute<>(Double.class, 0.0);
    @Export(attributeName = "minHeight")
    public final BindableAttribute<Double> minHeight = new BindableAttribute<>(Double.class, 0.0);
    @Export(attributeName = "maxWidth")
    public final BindableAttribute<Double> maxWidth = new BindableAttribute<>(Double.class, Double.POSITIVE_INFINITY);
    @Export(attributeName = "maxHeight")
    public final BindableAttribute<Double> maxHeight = new BindableAttribute<>(Double.class, Double.POSITIVE_INFINITY);


    @Export(attributeName = "_")
    public final BindableAttribute<Widget> child = new BindableAttribute<>(Widget.class);


    @Override
    public List<Widget> build(DomElement buildContext) {
        return child.getValue() == null ? Collections.EMPTY_LIST : Collections.singletonList(child.getValue());
    }

    public ConstrainedBox() {
        minWidth.addOnUpdate((a, b) -> getDomElement().requestRelayoutParent());
        minHeight.addOnUpdate((a, b) -> getDomElement().requestRelayoutParent());
        maxWidth.addOnUpdate((a, b) -> getDomElement().requestRelayoutParent());
        maxHeight.addOnUpdate((a, b) -> getDomElement().requestRelayoutParent());
    }

    @Override
    public Size layout(DomElement buildContext, ConstraintBox constraintBox) {

        double minWidth = Layouter.clamp(this.minWidth.getValue(), constraintBox.getMinWidth(), constraintBox.getMaxWidth());
        double minHeight = Layouter.clamp(this.minHeight.getValue(), constraintBox.getMinHeight(), constraintBox.getMaxHeight());

        double maxWidth = Layouter.clamp(this.maxWidth.getValue(), constraintBox.getMinWidth(), constraintBox.getMaxWidth());
        double maxHeight = Layouter.clamp(this.maxHeight.getValue(), constraintBox.getMinHeight(), constraintBox.getMaxHeight());

        if (getDomElement().getChildren().isEmpty()) {
            return new Size(maxWidth == Double.POSITIVE_INFINITY ? 0 : maxWidth,
                    maxHeight == Double.POSITIVE_INFINITY ? 0 : maxHeight);
        }

        DomElement child = getDomElement().getChildren().get(0);
        Size dim = child.getLayouter().layout(child, new ConstraintBox(
                minWidth, maxWidth, minHeight, maxHeight
        )); // force size heh.
        child.setRelativeBound(new Rect(0, 0, dim.getWidth(), dim.getHeight()));
        return dim;
    }

    @Override
    public boolean canCutRequest() {
        return Objects.equals(minWidth.getValue(), maxWidth.getValue())
                && Objects.equals(minHeight.getValue(), maxHeight.getValue());
    }

    @Override
    public double getMaxIntrinsicHeight(DomElement buildContext, double width) {
        DomElement child = buildContext.getChildren().get(0);
        return Layouter.clamp(child.getLayouter().getMaxIntrinsicHeight(child, width),
                minHeight.getValue() == Double.POSITIVE_INFINITY ? 0 : minHeight.getValue(), maxHeight.getValue());
    }

    @Override
    public double getMaxIntrinsicWidth(DomElement buildContext, double height) {
        DomElement child = buildContext.getChildren().get(0);
        return Layouter.clamp(child.getLayouter().getMaxIntrinsicWidth(child, height),
                minWidth.getValue() == Double.POSITIVE_INFINITY ? 0 : minWidth.getValue(), maxWidth.getValue());
    }
}
