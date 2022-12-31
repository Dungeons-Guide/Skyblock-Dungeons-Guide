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

import kr.syeyoung.dungeonsguide.mod.guiv2.Controller;
import kr.syeyoung.dungeonsguide.mod.guiv2.DomElement;
import kr.syeyoung.dungeonsguide.mod.guiv2.DomElementRegistry;
import kr.syeyoung.dungeonsguide.mod.guiv2.layouter.NullLayouter;
import kr.syeyoung.dungeonsguide.mod.guiv2.renderer.DrawNothingRenderer;
import kr.syeyoung.dungeonsguide.mod.guiv2.renderer.OnlyChildrenRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.opengl.GL11;

public class Placeholder {
    public static class PRenderer extends DrawNothingRenderer {

        public PRenderer(DomElement domElement) {
            super(domElement);
        }

        @Override
        public void doRender(int absMouseX, int absMouseY, int relMouseX, int relMouseY, float partialTicks) {
            int w = getDomElement().getRelativeBound().width, h = getDomElement().getRelativeBound().height;
            Gui.drawRect(0,0,w,h, 0xFFFFFFFF);
            GlStateManager.color(0,0,0,1);
            GL11.glLineWidth(2.0f);
            GL11.glEnable(GL11.GL_LINE_SMOOTH);
            GL11.glBegin(GL11.GL_LINE_LOOP);
            GL11.glVertex2f(0,0);
            GL11.glVertex2f(w, 0);
            GL11.glVertex2f(w, h);
            GL11.glVertex2f(0, h);
            GL11.glEnd();
            GL11.glBegin(GL11.GL_LINES);
            GL11.glVertex2f(0,0);
            GL11.glVertex2f(w,h);
            GL11.glVertex2f(w,0);
            GL11.glVertex2f(0, h);
            GL11.glDisable(GL11.GL_LINE_SMOOTH);
            GL11.glEnd();

            super.doRender(absMouseX, absMouseY, relMouseX, relMouseY, partialTicks);
        }
    }
    public static class PController extends Controller {

        public PController(DomElement element) {
            super(element);
            loadAttributes();
            loadDom();
        }
    }


    public static final DomElementRegistry.DomElementCreator CREATOR = new DomElementRegistry.GeneralDomElementCreator(
            NullLayouter::new, PRenderer::new, PController::new
    );
}
