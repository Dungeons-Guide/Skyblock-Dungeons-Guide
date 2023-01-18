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
import kr.syeyoung.dungeonsguide.mod.guiv2.primitive.Size;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.AnnotatedWidget;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.annotations.Bind;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.annotations.Export;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.annotations.Passthrough;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.util.ResourceLocation;

@Passthrough(exportName = "_", bindName = "_", type = Widget.class)
public class ScrollablePanel extends AnnotatedWidget {
    @Bind(variableName = "contentX")
    public final BindableAttribute<Double> contentX = new BindableAttribute<>(Double.class);
    @Bind(variableName = "contentY")
    public final BindableAttribute<Double> contentY = new BindableAttribute<>(Double.class);
    @Bind(variableName = "contentSize")
    public final BindableAttribute<Size> contentSize = new BindableAttribute<>(Size.class);
    @Bind(variableName = "contentWidth")
    public final BindableAttribute<Double> contentWidth = new BindableAttribute<>(Double.class);
    @Bind(variableName = "contentHeight")
    public final BindableAttribute<Double> contentHeight = new BindableAttribute<>(Double.class);
    @Bind(variableName = "viewportSize")
    public final BindableAttribute<Size> viewportSize = new BindableAttribute<>(Size.class);
    @Bind(variableName = "viewportWidth")
    public final BindableAttribute<Double> viewportWidth = new BindableAttribute<>(Double.class);
    @Bind(variableName = "viewportHeight")
    public final BindableAttribute<Double> viewportHeight = new BindableAttribute<>(Double.class);

    @Bind(variableName = "x")
    public final BindableAttribute<Double> x = new BindableAttribute<>(Double.class);
    @Bind(variableName = "y")
    public final BindableAttribute<Double> y = new BindableAttribute<>(Double.class);

    @Export(attributeName = "direction")
    @Bind(variableName = "direction")
    public final BindableAttribute<Direction> direction = new BindableAttribute<>(Direction.class);
    @Bind(variableName = "verticalThickness")
    @Export(attributeName = "verticalThickness")
    public final BindableAttribute<Double> vertThickness = new BindableAttribute<>(Double.class);
    @Bind(variableName = "horizontalThickness")
    @Export(attributeName = "verticalThickness")
    public final BindableAttribute<Double> horzThickness = new BindableAttribute<>(Double.class);

    @Bind(variableName = "horzRef")
    public final BindableAttribute<DomElement> horzRef = new BindableAttribute<>(DomElement.class);

    @Bind(variableName = "vertRef")
    public final BindableAttribute<DomElement> vertRef = new BindableAttribute<>(DomElement.class);
    @AllArgsConstructor
    @Getter
    public enum Direction {
        HORIZONTAL(true, false),
        VERTICAL(false, true),
        BOTH(true, true);

        private boolean horizontal;
        private boolean vertical;

    }
    public ScrollablePanel() {
        super(new ResourceLocation("dungeonsguide:gui/elements/scrollablePanel.gui"));

        contentSize.addOnUpdate((old, neu) -> {
            contentWidth.setValue(Math.max(0, neu.getWidth() - viewportWidth.getValue()));
            contentHeight.setValue(Math.max(0, neu.getHeight() - viewportHeight.getValue()));
        });

        viewportSize.addOnUpdate((old, neu) -> {
            viewportWidth.setValue(neu.getWidth());
            viewportHeight.setValue(neu.getHeight());
            contentWidth.setValue(Math.max(0, contentSize.getValue().getWidth() - viewportWidth.getValue()));
            contentHeight.setValue(Math.max(0, contentSize.getValue().getHeight() - viewportHeight.getValue()));
        });

        x.addOnUpdate((old, neu) -> {
            if (direction.getValue().isHorizontal())
                contentX.setValue(-neu);
        });
        y.addOnUpdate((old, neu) ->{
            if (direction.getValue().isVertical())
                contentY.setValue(-neu);
        });
        direction.addOnUpdate((old, neu) -> {
            vertThickness.setValue(neu.isVertical() ? 7 : 0.0);
            horzThickness.setValue(neu.isHorizontal() ? 7 :0.0);
        });

    }

    @Override
    public boolean mouseClicked(int absMouseX, int absMouseY, double relMouseX, double relMouseY, int mouseButton) {
        return false;
    }

    @Override
    public boolean mouseScrolled(int absMouseX, int absMouseY, double relMouseX0, double relMouseY0, int scrollAmount) {
        if (direction.getValue().vertical) {
            double old = this.y.getValue(), neu;
            this.y.setValue(
                    neu = Layouter.clamp(this.y.getValue() + scrollAmount,0, contentHeight.getValue())
            );
            return old != neu;
        } else if (direction.getValue().horizontal) {
            double old = this.x.getValue(), neu;
            this.x.setValue(
                    neu = Layouter.clamp(this.x.getValue() + scrollAmount,0, contentWidth.getValue())
            );
            return old != neu;
        }
        return false;
    }
}
