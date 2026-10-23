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

package kr.syeyoung.dungeonsguide.mod.gui.elements;

import kr.syeyoung.dungeonsguide.mod.gui.BindableAttribute;
import kr.syeyoung.dungeonsguide.mod.gui.DomElement;
import kr.syeyoung.dungeonsguide.mod.gui.Widget;
import kr.syeyoung.dungeonsguide.mod.gui.layouter.Layouter;
import kr.syeyoung.dungeonsguide.mod.gui.primitive.ConstraintBox;
import kr.syeyoung.dungeonsguide.mod.gui.primitive.Size;
import kr.syeyoung.dungeonsguide.mod.gui.renderer.Renderer;
import kr.syeyoung.dungeonsguide.mod.gui.renderer.RenderingContext;
import kr.syeyoung.dungeonsguide.mod.gui.xml.AnnotatedExportOnlyWidget;
import kr.syeyoung.dungeonsguide.mod.gui.xml.annotations.Export;

import java.util.Collections;
import java.util.List;

public class Line extends AnnotatedExportOnlyWidget implements Layouter, Renderer{
    @Export(attributeName = "thickness")
    public final BindableAttribute<Float> thickness =new BindableAttribute<>(Float.class, 1.0f);

    @Export(attributeName = "factor")
    public final BindableAttribute<Integer> factor =new BindableAttribute<>(Integer.class, 1); // normal line

    @Export(attributeName = "pattern")
    public final BindableAttribute<Short> pattern =new BindableAttribute<>(Short.class, null); // normal line

    @Export(attributeName = "color")
    public final BindableAttribute<Integer> color = new BindableAttribute<>(Integer.class, 0xFF000000);
    @Export(attributeName = "dir")
    public final BindableAttribute<Orientation> direction = new BindableAttribute<>(Orientation.class, Orientation.HORIZONTAL);


    public float r = 0;
    public float g = 0;
    public float b = 0;
    public float a = 1;


    public static enum Orientation {
        VERTICAL, HORIZONTAL
    }

    public Line() {
        thickness.addOnUpdate((a,b) -> getDomElement().requestRelayout());
        color.addOnUpdate((old, color) -> {
            a = ((color >> 24) & 0xFF) / 255.0f;
            r = ((color >> 16) &0xFF) /255.0f;
            g = ((color >> 8) & 0xFF) / 255.0f;
            b = (color & 0xFF) / 255.0f;
        });
    }


    @Override
    public List<Widget> build(DomElement buildContext) {
        return Collections.EMPTY_LIST;
    }
    
    @Override
    public Size layout(DomElement buildContext, ConstraintBox constraintBox) {
        double thickness = this.thickness.getValue();
        Orientation orientation = direction.getValue();
        if (orientation == Orientation.HORIZONTAL) {
            return new Size(
                    constraintBox.getMaxWidth(),
                    Layouter.clamp( thickness, constraintBox.getMinHeight(), constraintBox.getMaxHeight())
            );
        } else {
            return new Size(
                    Layouter.clamp( thickness, constraintBox.getMinWidth(), constraintBox.getMaxWidth()),
                    constraintBox.getMaxHeight()
            );
        }
    }

    @Override
    public double getMaxIntrinsicWidth(DomElement buildContext, double height) {
        return direction.getValue() == Orientation.HORIZONTAL ? 0 : this.thickness.getValue();
    }
    @Override
    public double getMaxIntrinsicHeight(DomElement buildContext, double width) {
        return direction.getValue() == Orientation.VERTICAL ? 0 : this.thickness.getValue();
    }

    @Override
    public void doRender(float partialTicks, RenderingContext context, DomElement buildContext) {
        double w = buildContext.getSize().getWidth(), h = buildContext.getSize().getHeight();

        int color = this.color.getValue();
        boolean horizontal = direction.getValue() == Orientation.HORIZONTAL;
        // 1.8.9 used glLineWidth in pixels (thickness * scale), so the quad is thickness wide in local units
        double thickness = this.thickness.getValue();

        Short pattern = this.pattern.getValue();
        if (pattern == null) {
            if (horizontal) context.drawLine(0, h / 2.0, w, h / 2.0, thickness, color);
            else context.drawLine(w / 2.0, 0, w / 2.0, h, thickness, color);
            return;
        }

        // TODO: stipple is redone on the cpu every frame, walking every pixel and one quad per dash. decided later
        // glLineStipple: bit i (from the lsb) of the pattern covers factor pixels, repeating every 16 * factor pixels
        double scale = horizontal ? buildContext.getAbsBounds().getWidth() / w : buildContext.getAbsBounds().getHeight() / h;
        double length = horizontal ? w : h;
        int pixels = (int) Math.ceil(length * scale);
        int factor = this.factor.getValue();
        int start = -1;
        for (int pixel = 0; pixel <= pixels; pixel++) {
            boolean on = pixel < pixels && ((pattern >> ((pixel / factor) % 16)) & 1) == 1;
            if (on && start == -1) start = pixel;
            if (!on && start != -1) {
                double from = start / scale, to = Math.min(pixel / scale, length);
                if (horizontal) context.drawLine(from, h / 2.0, to, h / 2.0, thickness, color);
                else context.drawLine(w / 2.0, from, w / 2.0, to, thickness, color);
                start = -1;
            }
        }
    }
}
