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

package kr.syeyoung.dungeonsguide.mod.gui.elements;

import kr.syeyoung.dungeonsguide.mod.gui.BindableAttribute;
import kr.syeyoung.dungeonsguide.mod.gui.DomElement;
import kr.syeyoung.dungeonsguide.mod.gui.Widget;
import kr.syeyoung.dungeonsguide.mod.gui.layouter.Layouter;
import kr.syeyoung.dungeonsguide.mod.gui.primitive.Rect;
import kr.syeyoung.dungeonsguide.mod.gui.renderer.Renderer;
import kr.syeyoung.dungeonsguide.mod.gui.renderer.RenderingContext;
import kr.syeyoung.dungeonsguide.mod.gui.xml.AnnotatedExportOnlyWidget;
import kr.syeyoung.dungeonsguide.mod.gui.xml.annotations.Export;

import java.util.Arrays;
import java.util.List;

public class NegativeStencil extends AnnotatedExportOnlyWidget implements Renderer {
    @Export(attributeName = "_")
    public final BindableAttribute<Widget> children = new BindableAttribute<>(Widget.class);
    @Export(attributeName = "_stencil")
    public final BindableAttribute<Widget> stencil = new BindableAttribute<>(Widget.class);

    @Override
    public List<Widget> build(DomElement buildContext) {
        return Arrays.asList(children.getValue(), stencil.getValue());
    }

    @Override
    protected Layouter createLayouter() {
        return Stack.StackingLayouter.INSTANCE;
    }

    @Override
    public void doRender(float partialTicks, RenderingContext context, DomElement buildContext) {
        DomElement theThingToDraw = buildContext.getChildren().get(0);
        DomElement stencil = buildContext.getChildren().get(1);

        context.beginStencilMask();

        context.pose().pushMatrix();
        Rect original = stencil.getRelativeBound();
        context.pose().translate((float) (original.getX()), (float) (original.getY()));

        double absXScale = buildContext.getAbsBounds().getWidth() / buildContext.getSize().getWidth();
        double absYScale = buildContext.getAbsBounds().getHeight() / buildContext.getSize().getHeight();

        Rect elementABSBound = new Rect(
                (buildContext.getAbsBounds().getX() + original.getX() * absXScale),
                (buildContext.getAbsBounds().getY() + original.getY() * absYScale),
                (original.getWidth() * absXScale),
                (original.getHeight() * absYScale)
        );
        stencil.setAbsBounds(elementABSBound);

        stencil.getRenderer().doRender(
                partialTicks,context, stencil);
        context.pose().popMatrix();


        context.beginStencilMasked(0);


        original = theThingToDraw.getRelativeBound();
        context.pose().translate((float) (original.getX()), (float) (original.getY()));

        absXScale = buildContext.getAbsBounds().getWidth() / buildContext.getSize().getWidth();
        absYScale = buildContext.getAbsBounds().getHeight() / buildContext.getSize().getHeight();

        elementABSBound = new Rect(
                (buildContext.getAbsBounds().getX() + original.getX() * absXScale),
                (buildContext.getAbsBounds().getY() + original.getY() * absYScale),
                (original.getWidth() * absXScale),
                (original.getHeight() * absYScale)
        );
        theThingToDraw.setAbsBounds(elementABSBound);

        theThingToDraw.getRenderer().doRender(
                partialTicks,context, theThingToDraw);

        context.endStencil();
    }
}
