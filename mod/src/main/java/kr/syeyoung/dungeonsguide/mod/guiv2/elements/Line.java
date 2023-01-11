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
import kr.syeyoung.dungeonsguide.mod.guiv2.renderer.DrawNothingRenderer;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.DomElementRegistry;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.annotations.Export;
import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.opengl.GL11;

import java.awt.*;

public class Line {

    public static class LLayouter extends Layouter {
        private LWidget controller;
        public LLayouter(DomElement element) {
            super(element);
            this.controller = (LWidget) element.getWidget();
        }

        @Override
        public Dimension layout(ConstraintBox constraintBox) {
            float thickness = controller.thickness.getValue();
            LWidget.Orientation orientation = controller.direction.getValue();
            if (orientation == LWidget.Orientation.HORIZONTAL) {
                return new Dimension(
                        constraintBox.getMaxWidth(),
                        clamp((int) thickness, constraintBox.getMinHeight(), constraintBox.getMaxHeight())
                );
            } else {
                return new Dimension(
                        clamp((int) thickness, constraintBox.getMinWidth(), constraintBox.getMaxWidth()),
                        constraintBox.getMaxHeight()
                );
            }
        }
    }

    public static class LRenderer extends DrawNothingRenderer {
        private LWidget controller;
        public LRenderer(DomElement domElement) {
            super(domElement);
            this.controller = (LWidget) domElement.getWidget();
        }

        @Override
        public void doRender(int absMouseX, int absMouseY, int relMouseX, int relMouseY, float partialTicks) {
            int w = getDomElement().getRelativeBound().width, h = getDomElement().getRelativeBound().height;
            GlStateManager.color(controller.r,controller.g,controller.b,controller.a);
            GlStateManager.disableTexture2D();
            GL11.glLineWidth(controller.thickness.getValue());

            Short pattern = controller.pattern.getValue();
            if (pattern != null) {
                GL11.glLineStipple(controller.factor.getValue(), pattern);
                GL11.glEnable(GL11.GL_LINE_STIPPLE);
            }

            GL11.glBegin(GL11.GL_LINES);
            if (controller.direction.getValue() == LWidget.Orientation.HORIZONTAL) {
                GL11.glVertex2f(0,h/2.0f);
                GL11.glVertex2f(w, h/2.0f);
            } else {
                GL11.glVertex2f(w/2.0f,0);
                GL11.glVertex2f(w/2.0f, h);
            }
            GL11.glEnd();

            if (pattern != null) {
                GL11.glDisable(GL11.GL_LINE_STIPPLE);
            }
            super.doRender(absMouseX, absMouseY, relMouseX, relMouseY, partialTicks);
        }

        @Override
        public Rectangle applyTransformation(DomElement target) {
            return target.getRelativeBound();
        }
    }
    public static class LWidget extends Widget {

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

        public LWidget(DomElement element) {
            super(element);
            thickness.addOnUpdate(a -> element.requestRelayout());
            color.addOnUpdate(color -> {
                a = ((color >> 24) & 0xFF) / 255.0f;
                r = ((color >> 16) &0xFF) /255.0f;
                g = ((color >> 8) & 0xFF) / 255.0f;
                b = (color & 0xFF) / 255.0f;
            });
            loadAttributes();
            // shouldn't be any child lol

        }
    }

    public static final DomElementRegistry.DomElementCreator CREATOR = new DomElementRegistry.GeneralDomElementCreator(
            LLayouter::new, LRenderer::new, LWidget::new
    );
}
