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
import kr.syeyoung.dungeonsguide.mod.guiv2.renderer.Renderer;
import kr.syeyoung.dungeonsguide.mod.guiv2.renderer.RenderingContext;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.AnnotatedExportOnlyWidget;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.annotations.Export;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.data.WidgetList;
import net.minecraft.client.renderer.GlStateManager;

import java.util.List;

public class Stack extends AnnotatedExportOnlyWidget implements Renderer {
    @Export(attributeName = "passthrough")
    public final BindableAttribute<Boolean> passthrough = new BindableAttribute<>(Boolean.class, false);
    @Override
    public void doRender(float partialTicks, RenderingContext context, DomElement buildContext) {
        for (int i = buildContext.getChildren().size() - 1; i >= 0; i --) {
            DomElement value = buildContext.getChildren().get(i);
            Rect original = value.getRelativeBound();
            if (original == null) return;
            GlStateManager.pushMatrix();
            GlStateManager.translate(original.getX(), original.getY(), 0);

            double absXScale = buildContext.getAbsBounds().getWidth() / buildContext.getSize().getWidth();
            double absYScale = buildContext.getAbsBounds().getHeight() / buildContext.getSize().getHeight();

            Rect elementABSBound = new Rect(
                    (buildContext.getAbsBounds().getX() + original.getX() * absXScale),
                    (buildContext.getAbsBounds().getY() + original.getY() * absYScale),
                    (original.getWidth() * absXScale),
                    (original.getHeight() * absYScale)
            );
            value.setAbsBounds(elementABSBound);

            if (i > 0 && !passthrough.getValue())
             value.getRenderer().doRender(partialTicks, context, value);
            if (i == 0 || passthrough.getValue())
                value.getRenderer().doRender(
                        partialTicks, context, value);
            GlStateManager.popMatrix();
        }
    }

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

        @Override
        public double getMaxIntrinsicWidth(DomElement buildContext, double height) {
            double max = 0;
            for (DomElement child : buildContext.getChildren()) {
                max = Double.max(max, child.getLayouter().getMaxIntrinsicWidth(child, height));
            }
            return max;
        }

        @Override
        public double getMaxIntrinsicHeight(DomElement buildContext, double width) {
            double max = 0;
            for (DomElement child : buildContext.getChildren()) {
                max = Double.max(max, child.getLayouter().getMaxIntrinsicHeight(child, width));
            }
            return max;
        }
    }


    @Export(attributeName = "_")
    public final BindableAttribute<WidgetList> widgets = new BindableAttribute<>(WidgetList.class);
    @Override
    public List<Widget> build(DomElement buildContext) {
        return widgets.getValue();
    }

    @Override
    protected Layouter createLayouter() {
        return StackingLayouter.INSTANCE;
    }
}
