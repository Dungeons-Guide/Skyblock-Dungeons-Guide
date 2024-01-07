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
import kr.syeyoung.dungeonsguide.mod.guiv2.primitive.Rect;
import kr.syeyoung.dungeonsguide.mod.guiv2.renderer.Renderer;
import kr.syeyoung.dungeonsguide.mod.guiv2.renderer.RenderingContext;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.AnnotatedExportOnlyWidget;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.annotations.Export;
import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.opengl.GL11;

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

        GL11.glEnable(GL11.GL_STENCIL_TEST);
        GL11.glClearStencil(0);
        GL11.glClear(GL11.GL_STENCIL_BUFFER_BIT);

        GL11.glStencilMask(0xFF);
        GL11.glColorMask(false, false, false, false);
        GL11.glStencilFunc(GL11.GL_ALWAYS, 1, 0xFF);
        GL11.glStencilOp(GL11.GL_KEEP, GL11.GL_REPLACE, GL11.GL_REPLACE);

        GlStateManager.pushMatrix();
        Rect original = stencil.getRelativeBound();
        GlStateManager.translate(original.getX(), original.getY(), 0);

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
        GlStateManager.popMatrix();


        GL11.glStencilFunc(GL11.GL_EQUAL, 0, 0xFF);
        GL11.glStencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_KEEP);
        GL11.glColorMask(true, true, true, true);


        original = theThingToDraw.getRelativeBound();
        GlStateManager.translate(original.getX(), original.getY(), 0);

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

        GL11.glDisable(GL11.GL_STENCIL_TEST);
    }
}
