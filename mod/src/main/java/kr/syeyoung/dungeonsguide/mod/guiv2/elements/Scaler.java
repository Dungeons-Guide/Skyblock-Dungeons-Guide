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
import kr.syeyoung.dungeonsguide.mod.guiv2.primitive.Position;
import kr.syeyoung.dungeonsguide.mod.guiv2.primitive.Rect;
import kr.syeyoung.dungeonsguide.mod.guiv2.primitive.Size;
import kr.syeyoung.dungeonsguide.mod.guiv2.renderer.Renderer;
import kr.syeyoung.dungeonsguide.mod.guiv2.renderer.RenderingContext;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.AnnotatedExportOnlyWidget;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.annotations.Export;
import net.minecraft.client.renderer.GlStateManager;

import java.util.Collections;
import java.util.List;

public class Scaler extends AnnotatedExportOnlyWidget implements Layouter, Renderer {

    @Export(attributeName = "scale")
    public final BindableAttribute<Double> scale = new BindableAttribute<>(Double.class, 1.0);


    @Export(attributeName = "_")
    public final BindableAttribute<Widget> child = new BindableAttribute<>(Widget.class);


    @Override
    public List<Widget> build(DomElement buildContext) {
        return Collections.singletonList(child.getValue());
    }
    @Override
    public Size layout(DomElement buildContext, ConstraintBox constraintBox) {
        DomElement child = buildContext.getChildren().get(0);
        Size dims = child.getLayouter().layout(child, new ConstraintBox(
                (constraintBox.getMinWidth() / scale.getValue()),
                (constraintBox.getMaxWidth() / scale.getValue()),
                (constraintBox.getMinHeight() / scale.getValue()),
                (constraintBox.getMaxHeight() / scale.getValue())
        ));
        child.setRelativeBound(new Rect(0,0,
                (dims.getWidth() * scale.getValue()),
                (dims.getHeight() * scale.getValue())));
        child.setSize(new Size(dims.getWidth(), dims.getHeight()));

        return new Size(dims.getWidth() * scale.getValue(), dims.getHeight() * scale.getValue());
    }

    @Override
    public double getMaxIntrinsicHeight(DomElement buildContext, double width) {
        DomElement child = buildContext.getChildren().get(0);
        return child.getLayouter().getMaxIntrinsicHeight(child, width / scale.getValue()) * scale.getValue();
    }

    @Override
    public double getMaxIntrinsicWidth(DomElement buildContext, double height) {
        DomElement child = buildContext.getChildren().get(0);
        return child.getLayouter().getMaxIntrinsicWidth(child, height / scale.getValue()) * scale.getValue();
    }

    @Override
    public Position transformPoint(DomElement element, Position pos) {
        Rect elementRect = element.getRelativeBound();
        double relX = pos.getX() - elementRect.getX();
        double relY = pos.getY() - elementRect.getY();
        return new Position(relX / scale.getValue(), relY / scale.getValue());
    }


    @Override
    public void doRender(float partialTicks, RenderingContext context, DomElement buildContext) {
        DomElement value = buildContext.getChildren().get(0);

        Rect original = value.getRelativeBound();
        GlStateManager.translate(original.getX(), original.getY(), 0);
        GlStateManager.scale(scale.getValue(), scale.getValue(), 1);

        double absXScale = buildContext.getAbsBounds().getWidth() / buildContext.getSize().getWidth();
        double absYScale = buildContext.getAbsBounds().getHeight() / buildContext.getSize().getHeight();

        Rect elementABSBound = new Rect(
                (buildContext.getAbsBounds().getX() + original.getX() * absXScale),
                (buildContext.getAbsBounds().getY() + original.getY() * absYScale),
                (original.getWidth() * absXScale),
                (original.getHeight() * absYScale)
        );
        value.setAbsBounds(elementABSBound);

        value.getRenderer().doRender(
                partialTicks, context, value);
    }
}
