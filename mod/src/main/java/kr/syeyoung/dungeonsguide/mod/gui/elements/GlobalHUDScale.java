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

import kr.syeyoung.dungeonsguide.mod.features.FeatureRegistry;
import kr.syeyoung.dungeonsguide.mod.gui.DomElement;
import kr.syeyoung.dungeonsguide.mod.gui.Widget;
import kr.syeyoung.dungeonsguide.mod.gui.layouter.Layouter;
import kr.syeyoung.dungeonsguide.mod.gui.primitive.ConstraintBox;
import kr.syeyoung.dungeonsguide.mod.gui.primitive.Position;
import kr.syeyoung.dungeonsguide.mod.gui.primitive.Rect;
import kr.syeyoung.dungeonsguide.mod.gui.primitive.Size;
import kr.syeyoung.dungeonsguide.mod.gui.renderer.Renderer;
import kr.syeyoung.dungeonsguide.mod.gui.renderer.RenderingContext;
import kr.syeyoung.modapi.ModAPI;

import java.util.Collections;
import java.util.List;

public class GlobalHUDScale extends Widget implements Layouter, Renderer {
    
    private final Widget widget;
    public GlobalHUDScale(Widget widget) {
        this.widget = widget;
        scale = getScale();
    }


    private double getScale() {
        boolean useMc = FeatureRegistry.GLOBAL_HUD_SCALE.<Boolean>getParameter("mc").getValue();
        if (useMc) return ModAPI.getAPI().getScaleFactor();
        else return FeatureRegistry.GLOBAL_HUD_SCALE.<Double>getParameter("scale").getValue();
    }
    
    private double scale;


    @Override
    public List<Widget> build(DomElement buildContext) {
        return Collections.singletonList(widget);
    }
    @Override
    public Size layout(DomElement buildContext, ConstraintBox constraintBox) {
        this.scale = getScale();
        DomElement child = buildContext.getChildren().get(0);
        Size dims = child.getLayouter().layout(child, new ConstraintBox(
                (constraintBox.getMinWidth() / scale),
                (constraintBox.getMaxWidth() / scale),
                (constraintBox.getMinHeight() / scale),
                (constraintBox.getMaxHeight() / scale)
        ));
        child.setRelativeBound(new Rect(0,0,
                (dims.getWidth() * scale),
                (dims.getHeight() * scale)));
        child.setSize(new Size(dims.getWidth(), dims.getHeight()));

        return new Size(dims.getWidth() * scale, dims.getHeight() * scale);
    }

    @Override
    public double getMaxIntrinsicHeight(DomElement buildContext, double width) {
        DomElement child = buildContext.getChildren().get(0);
        return child.getLayouter().getMaxIntrinsicHeight(child, width / scale) * scale;
    }

    @Override
    public double getMaxIntrinsicWidth(DomElement buildContext, double height) {
        DomElement child = buildContext.getChildren().get(0);
        return child.getLayouter().getMaxIntrinsicWidth(child, height / scale) * scale;
    }

    @Override
    public Position transformPoint(DomElement element, Position pos) {
        Rect elementRect = element.getRelativeBound();
        double relX = pos.getX() - elementRect.getX();
        double relY = pos.getY() - elementRect.getY();
        return new Position(relX / scale, relY / scale);
    }


    @Override
    public void doRender(float partialTicks, RenderingContext context, DomElement buildContext) {
        DomElement value = buildContext.getChildren().get(0);

        Rect original = value.getRelativeBound();
        context.ctx().translate(original.getX(), original.getY(), 0);
        context.ctx().scale(scale, scale, 1);

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
