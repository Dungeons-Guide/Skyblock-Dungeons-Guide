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
import kr.syeyoung.dungeonsguide.mod.gui.primitive.Rect;
import kr.syeyoung.dungeonsguide.mod.gui.renderer.Renderer;
import kr.syeyoung.dungeonsguide.mod.gui.renderer.RenderingContext;
import kr.syeyoung.dungeonsguide.mod.gui.xml.AnnotatedExportOnlyWidget;
import kr.syeyoung.dungeonsguide.mod.gui.xml.annotations.Export;

import java.util.Collections;
import java.util.List;

public class Clip extends AnnotatedExportOnlyWidget implements Renderer {

    @Override
    public void doRender(float partialTicks, RenderingContext context, DomElement buildContext) {
        if (buildContext.getChildren().isEmpty()) return;
        if (buildContext.getSize().getWidth() <= 0 || buildContext.getSize().getHeight() <= 0)
            return;
        context.pushClip(buildContext.getAbsBounds(), buildContext.getSize(), 0,0, buildContext.getSize().getWidth(), buildContext.getSize().getHeight());

        DomElement value = buildContext.getChildren().get(0);

        Rect original = value.getRelativeBound();
        context.ctx().translate(original.getX(), original.getY(), 0);

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
        context.popClip();
    }

    @Export(attributeName = "_")
    public final BindableAttribute<Widget> widget = new BindableAttribute<>(Widget.class);

    @Override
    public List<Widget> build(DomElement buildContext) {
        return Collections.singletonList(widget.getValue());
    }
}
