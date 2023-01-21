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

import kr.syeyoung.dungeonsguide.mod.guiv2.DomElement;
import kr.syeyoung.dungeonsguide.mod.guiv2.Widget;
import kr.syeyoung.dungeonsguide.mod.guiv2.layouter.Layouter;
import kr.syeyoung.dungeonsguide.mod.guiv2.layouter.NullLayouter;
import kr.syeyoung.dungeonsguide.mod.guiv2.renderer.Renderer;
import kr.syeyoung.dungeonsguide.mod.guiv2.renderer.RenderingContext;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.AnnotatedExportOnlyWidget;
import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.opengl.GL11;

import java.util.Collections;
import java.util.List;

public class Placeholder extends AnnotatedExportOnlyWidget implements Renderer {

    @Override
    public List<Widget> build(DomElement buildContext) {
        return Collections.EMPTY_LIST;
    }

    @Override
    public void doRender(int absMouseX, int absMouseY, double relMouseX, double relMouseY, float partialTicks, RenderingContext context, DomElement buildContext) {
        double w = buildContext.getSize().getWidth(), h = buildContext.getSize().getHeight();
        context.drawRect(0,0,w,h, 0xFFFFFFFF);
        GlStateManager.color(0,0,0,1);
        GlStateManager.disableTexture2D();
        GL11.glLineWidth(1.0f);
        GL11.glBegin(GL11.GL_LINE_LOOP);
        GL11.glVertex2d(0,0);
        GL11.glVertex2d(w, 0);
        GL11.glVertex2d(w, h);
        GL11.glVertex2d(0, h);
        GL11.glEnd();
        GL11.glBegin(GL11.GL_LINES);
        GL11.glVertex2d(0,0);
        GL11.glVertex2d(w,h);
        GL11.glVertex2d(w,0);
        GL11.glVertex2d(0, h);
        GL11.glEnd();
    }

    @Override
    protected Layouter createLayouter() {
        return NullLayouter.INSTANCE;
    }
}
