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
import kr.syeyoung.dungeonsguide.mod.guiv2.renderer.OnlyChildrenRenderer;
import kr.syeyoung.dungeonsguide.mod.guiv2.renderer.Renderer;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.AnnotatedExportOnlyWidget;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.annotations.Bind;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.annotations.Export;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.data.WidgetList;

import java.util.List;

public class Wrap extends AnnotatedExportOnlyWidget implements Layouter {

    @Export(attributeName = "minimumWidth")
    public final BindableAttribute<Double> minimumWidth = new BindableAttribute<>(Double.class);
    @Export(attributeName = "gap")
    public final BindableAttribute<Double> gap = new BindableAttribute<>(Double.class);

    @Export(attributeName = "_")
    public final BindableAttribute<WidgetList> widgetListBindableAttribute = new BindableAttribute<>(WidgetList.class);

    @Override
    public List<Widget> build(DomElement buildContext) {
        return widgetListBindableAttribute.getValue();
    }

    @Override
    public Size layout(DomElement buildContext, ConstraintBox constraintBox) {
        List<DomElement> elements = buildContext.getChildren();
        if (elements.isEmpty()) return new Size(constraintBox.getMinWidth(),constraintBox.getMinHeight());
        int itemPerRow = (int) ((constraintBox.getMaxWidth()+gap.getValue()) / (minimumWidth.getValue() + gap.getValue()));
        if (itemPerRow == 0) itemPerRow = 1;
        if (itemPerRow == Integer.MAX_VALUE) itemPerRow = elements.size();
        int rows = (int) Math.ceil((double)elements.size() / itemPerRow);

        double maxWidth = (constraintBox.getMaxWidth() + gap.getValue()) / itemPerRow - gap.getValue();
        double maxHeight = (constraintBox.getMaxHeight() + gap.getValue()) / rows - gap.getValue();
        double h = 0;
        for (int y = 0; y < rows; y++) {
            double maxH = 0;
            for (int x = 0; x < itemPerRow; x++) {
                if (elements.size() <= y * itemPerRow + x) continue;
                DomElement element = elements.get(y * itemPerRow + x);
                double height = element.getLayouter().getMaxIntrinsicHeight(element, maxWidth);
                if (height > maxH) maxH = height;
            }
            for (int x = 0; x < itemPerRow; x++) {
                if (elements.size() <= y * itemPerRow + x) continue;
                DomElement element = elements.get(y * itemPerRow + x);
                Size size = element.getLayouter().layout(element, new ConstraintBox(
                        maxWidth, maxWidth, maxH, maxH
                ));
                element.setRelativeBound(new Rect(
                        x * (maxWidth + gap.getValue()),
                        h,
                        size.getWidth(),
                        size.getHeight()
                ));
            }

            h += maxH + gap.getValue();
        }

        return new Size(constraintBox.getMaxWidth(), Layouter.clamp(h - gap.getValue(), 0, constraintBox.getMaxHeight()));
    }

    @Override
    public double getMaxIntrinsicWidth(DomElement buildContext, double height) {
        return buildContext.getChildren().stream()
                .map(a -> a.getLayouter().getMaxIntrinsicWidth(a, height) + gap.getValue())
                .reduce(Double::sum)
                .orElse(gap.getValue()) - gap.getValue();
    }

    @Override
    public double getMaxIntrinsicHeight(DomElement buildContext, double width) {
        List<DomElement> elements = buildContext.getChildren();
        if (elements.isEmpty()) return 0;
        int itemPerRow = (int) ((width+gap.getValue()) / (minimumWidth.getValue() + gap.getValue()));
        if (itemPerRow == 0) itemPerRow = 1;
        if (itemPerRow == Integer.MAX_VALUE) itemPerRow = elements.size();
        int rows = (int) Math.ceil((double)elements.size() / itemPerRow);
        double maxWidth = (width + gap.getValue()) / itemPerRow - gap.getValue();

        double sum = 0;
        for (int y = 0; y < rows; y++) {
            double maxH = 0;
            for (int x = 0; x < itemPerRow; x++) {
                if (elements.size() <= y * itemPerRow + x) continue;
                DomElement element = elements.get(y * itemPerRow + x);
                double h = element.getLayouter().getMaxIntrinsicHeight(element, maxWidth);
                if (h > maxH) maxH = h;
            }
            sum += maxH + gap.getValue();
        }
        sum -= gap.getValue();
        if (sum < 0) sum = 0;

        return sum;
    }

    @Override
    protected Renderer createRenderer() {
        return OnlyChildrenRenderer.INSTANCE;
    }
}
