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

import kr.syeyoung.dungeonsguide.mod.guiv2.*;
import kr.syeyoung.dungeonsguide.mod.guiv2.layouter.Layouter;
import kr.syeyoung.dungeonsguide.mod.guiv2.primitive.ConstraintBox;
import kr.syeyoung.dungeonsguide.mod.guiv2.primitive.Size;
import kr.syeyoung.dungeonsguide.mod.guiv2.renderer.Renderer;
import kr.syeyoung.dungeonsguide.mod.guiv2.renderer.RenderingContext;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.AnnotatedExportOnlyWidget;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.annotations.Export;
import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.opengl.GL11;

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
    public void doRender(int absMouseX, int absMouseY, double relMouseX, double relMouseY, float partialTicks, RenderingContext context, DomElement buildContext) {
        double w = buildContext.getSize().getWidth(), h = buildContext.getSize().getHeight();

        GlStateManager.color(r,g,b,a);
        GlStateManager.disableTexture2D();
        GL11.glLineWidth(thickness.getValue());

        Short pattern = this.pattern.getValue();
        if (pattern != null) {
            GL11.glLineStipple(factor.getValue(), pattern);
            GL11.glEnable(GL11.GL_LINE_STIPPLE);
        }

        GL11.glBegin(GL11.GL_LINES);
        if (direction.getValue() == Orientation.HORIZONTAL) {
            GL11.glVertex2d(0,h/2.0f);
            GL11.glVertex2d(w, h/2.0f);
        } else {
            GL11.glVertex2d(w/2.0f,0);
            GL11.glVertex2d(w/2.0f, h);
        }
        GL11.glEnd();
        GlStateManager.enableTexture2D();

        if (pattern != null) {
            GL11.glDisable(GL11.GL_LINE_STIPPLE);
        }
    }
}
