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

public class AspectRatioFitter extends AnnotatedExportOnlyWidget implements Layouter {
    @Export(attributeName = "$")
    public final BindableAttribute<Widget> widget = new BindableAttribute<>(Widget.class);
    @Export(attributeName = "width")
    public final BindableAttribute<Integer> width = new BindableAttribute<>(Integer.class, 1);
    @Export(attributeName = "height")
    public final BindableAttribute<Integer> height = new BindableAttribute<>(Integer.class, 1);

    @Export(attributeName = "fit")
    public final BindableAttribute<Flexible.FlexFit> fit = new BindableAttribute<>(Flexible.FlexFit.class, Flexible.FlexFit.TIGHT);


    @Override
    public Size layout(DomElement buildContext, ConstraintBox constraintBox) {
        // ratio is width/height


        double heightIfWidthFit = constraintBox.getMaxWidth() * height.getValue()  / width.getValue();
        double widthIfHeightFit = constraintBox.getMaxHeight() * width.getValue() / height.getValue();

        DomElement target = null;
        if (!buildContext.getChildren().isEmpty())
            target = buildContext.getChildren().get(0);
        if (heightIfWidthFit <= constraintBox.getMaxHeight()) {
            if (target != null) {
                Size size = target.getLayouter().layout(target, new ConstraintBox(
                        fit.getValue() == Flexible.FlexFit.LOOSE ? constraintBox.getMinWidth() : constraintBox.getMaxWidth(), constraintBox.getMaxWidth(),
                        fit.getValue() == Flexible.FlexFit.LOOSE ? constraintBox.getMinHeight() : heightIfWidthFit, heightIfWidthFit
                ));
                target.setRelativeBound(new Rect(0,0,size.getWidth(), size.getHeight()));
                return size;
            }
            return new Size(constraintBox.getMaxWidth(), heightIfWidthFit);
        } else if (widthIfHeightFit <= constraintBox.getMaxWidth()){
            if (target != null) {
                Size size = target.getLayouter().layout(target, new ConstraintBox(
                        fit.getValue() == Flexible.FlexFit.LOOSE ? constraintBox.getMinWidth() : widthIfHeightFit, widthIfHeightFit,
                        fit.getValue() == Flexible.FlexFit.LOOSE ? constraintBox.getMinHeight() : constraintBox.getMaxHeight(), constraintBox.getMaxHeight()
                ));
                target.setRelativeBound(new Rect(0,0,size.getWidth(), size.getHeight()));
                return size;
            }

            return new Size(widthIfHeightFit, constraintBox.getMaxHeight());
        } else {
            throw new IllegalStateException("How is this possible mathmatically?");
        }
    }

    @Override
    public double getMaxIntrinsicWidth(DomElement buildContext, double height) {
        return height * this.width.getValue() / this.height.getValue();
    }

    @Override
    public double getMaxIntrinsicHeight(DomElement buildContext, double width) {
        return width * this.height.getValue() / this.width.getValue();
    }

    @Override
    public boolean canCutRequest() {
        return Flexible.FlexFit.TIGHT == fit.getValue();
    }

    @Override
    public List<Widget> build(DomElement buildContext) {
        return Collections.singletonList(widget.getValue());
    }
}
