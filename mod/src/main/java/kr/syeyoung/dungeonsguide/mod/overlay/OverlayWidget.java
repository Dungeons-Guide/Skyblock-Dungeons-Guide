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

package kr.syeyoung.dungeonsguide.mod.overlay;

import kr.syeyoung.dungeonsguide.mod.guiv2.DomElement;
import kr.syeyoung.dungeonsguide.mod.guiv2.Widget;
import kr.syeyoung.dungeonsguide.mod.guiv2.layouter.Layouter;
import kr.syeyoung.dungeonsguide.mod.guiv2.layouter.SingleChildPassingLayouter;
import kr.syeyoung.dungeonsguide.mod.guiv2.primitive.ConstraintBox;
import kr.syeyoung.dungeonsguide.mod.guiv2.primitive.Rect;
import kr.syeyoung.dungeonsguide.mod.guiv2.primitive.Size;
import kr.syeyoung.dungeonsguide.mod.guiv2.renderer.Renderer;
import kr.syeyoung.dungeonsguide.mod.guiv2.renderer.RenderingContext;
import lombok.AllArgsConstructor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.profiler.Profiler;

import java.util.Collections;
import java.util.List;

@AllArgsConstructor
public class OverlayWidget extends Widget implements Renderer, Layouter {
    public Widget wrappingWidget;
    public OverlayType overlayType;
    public Positioner positionSize;
    public String name;

    @Override
    public List<Widget> build(DomElement buildContext) {
        return Collections.singletonList(wrappingWidget);
    }

    @Override
    public void doRender(float partialTicks, RenderingContext context, DomElement buildContext) {
        if (buildContext.getChildren().isEmpty()) return;
        OverlayType type = buildContext.getContext().getValue(OverlayType.class, OverlayManager.OVERLAY_TYPE_KEY);
        if (this.overlayType.ordinal() < type.ordinal()) return;


        DomElement value = buildContext.getChildren().get(0);

        Rect original = value.getRelativeBound();
        if (original == null) return;

        Profiler profiler = Minecraft.getMinecraft().mcProfiler;
        profiler.startSection("Dungeons Guide Overlay Render :: "+name);
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

        value.getRenderer().doRender(
                partialTicks, context, value);

        profiler.endSection();
    }

    @Override
    public Size layout(DomElement buildContext, ConstraintBox constraintBox) {
        Profiler profiler = Minecraft.getMinecraft().mcProfiler;
        profiler.startSection("Dungeons Guide Overlay Layout :: "+name);
        Size s = SingleChildPassingLayouter.INSTANCE.layout(buildContext, constraintBox);;
        profiler.endSection();
        return s;
    }

    @Override
    public boolean canCutRequest() {
        return false;
    }
}
