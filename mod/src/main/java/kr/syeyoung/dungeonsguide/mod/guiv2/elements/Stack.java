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
import kr.syeyoung.dungeonsguide.mod.guiv2.primitive.ConstraintBox;
import kr.syeyoung.dungeonsguide.mod.guiv2.Widget;
import kr.syeyoung.dungeonsguide.mod.guiv2.DomElement;
import kr.syeyoung.dungeonsguide.mod.guiv2.primitive.Rect;
import kr.syeyoung.dungeonsguide.mod.guiv2.primitive.Size;
import kr.syeyoung.dungeonsguide.mod.guiv2.renderer.Renderer;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.AnnotatedExportOnlyWidget;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.DomElementRegistry;
import kr.syeyoung.dungeonsguide.mod.guiv2.layouter.Layouter;
import kr.syeyoung.dungeonsguide.mod.guiv2.renderer.OnlyChildrenRenderer;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.annotations.Export;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.data.WidgetList;

import java.awt.*;
import java.util.Collections;
import java.util.List;

public class Stack extends AnnotatedExportOnlyWidget {
    public static class StackingLayouter implements Layouter {
        public static final StackingLayouter INSTANCE = new StackingLayouter();
        @Override
        public Size layout(DomElement buildContext, ConstraintBox constraintBox) {
            double maxW = 0, maxH = 0;
            for (DomElement child : buildContext.getChildren()) {
                Size dim = child.getLayouter().layout(child, constraintBox);
                if (maxW< dim.getWidth()) maxW = dim.getWidth();
                if (maxH< dim.getHeight()) maxH = dim.getHeight();
                child.setRelativeBound(new Rect(0,0,dim.getWidth(), dim.getHeight()));
            }
            return new Size(maxW, maxH);
        }
    }


    @Export(attributeName = "$")
    public final BindableAttribute<WidgetList> widgets = new BindableAttribute<>(WidgetList.class);
    @Override
    public List<Widget> build(DomElement buildContext) {
        return widgets.getValue();
    }

    @Override
    protected Layouter createLayouter() {
        return StackingLayouter.INSTANCE;
    }

    @Override
    protected Renderer createRenderer() {
        return OnlyChildrenRenderer.INSTANCE;
    }
}
