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
import kr.syeyoung.dungeonsguide.mod.guiv2.PassthroughManager;
import kr.syeyoung.dungeonsguide.mod.guiv2.Widget;
import kr.syeyoung.dungeonsguide.mod.guiv2.layouter.Layouter;
import kr.syeyoung.dungeonsguide.mod.guiv2.primitive.ConstraintBox;
import kr.syeyoung.dungeonsguide.mod.guiv2.primitive.Rect;
import kr.syeyoung.dungeonsguide.mod.guiv2.primitive.Size;
import kr.syeyoung.dungeonsguide.mod.guiv2.renderer.Renderer;
import kr.syeyoung.dungeonsguide.mod.guiv2.renderer.RenderingContext;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.AnnotatedExportOnlyWidget;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.shader.Framebuffer;

import java.util.Collections;
import java.util.List;

public class Passthrough extends AnnotatedExportOnlyWidget implements Layouter, Renderer{

    public Passthrough() {
    }


    @Override
    public List<Widget> build(DomElement buildContext) {
        return Collections.EMPTY_LIST;
    }
    
    @Override
    public Size layout(DomElement buildContext, ConstraintBox constraintBox) {
        return new Size(constraintBox.getMaxWidth(), constraintBox.getMaxHeight());
    }

    @Override
    public void doRender(float partialTicks, RenderingContext context, DomElement buildContext) {
        Rect rect = buildContext.getAbsBounds();
        double w = buildContext.getSize().getWidth();
        double h = buildContext.getSize().getHeight();

        int screenHeight = Minecraft.getMinecraft().displayHeight;
        int screenWidth = Minecraft.getMinecraft().displayWidth;

        Framebuffer framebuffer = PassthroughManager.INSTANCE.getFramebuffer();

        framebuffer.bindFramebufferTexture();
        GlStateManager.color(1f, 1f, 1f, 1f);

        double sx = rect.getX() / screenWidth;
        double sy = (rect.getY() - 40) / screenHeight;
        double ex = (rect.getX() + rect.getWidth())/ screenWidth;
        double ey = (rect.getY() + rect.getHeight() - 40) / screenHeight;

        Gui.drawRect(0,0, (int) w, (int) h, PassthroughManager.INSTANCE.getFogColor());

        GlStateManager.color(1,1,1,1);
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();
        worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX);
        worldrenderer
                .pos(0, h, 0.0D)
                .tex(sx, sy).endVertex();
        worldrenderer
                .pos(w,h, 0.0D)
                .tex(ex, sy).endVertex();
        worldrenderer
                .pos(w,0, 0.0D)
                .tex(ex, ey).endVertex();
        worldrenderer
                .pos(0,0, 0.0D)
                .tex(sx, ey).endVertex();
        tessellator.draw();


        framebuffer.unbindFramebufferTexture();

    }
}
