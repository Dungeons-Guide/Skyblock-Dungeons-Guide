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
import kr.syeyoung.dungeonsguide.mod.guiv2.primitive.Size;
import kr.syeyoung.dungeonsguide.mod.guiv2.renderer.Renderer;
import kr.syeyoung.dungeonsguide.mod.guiv2.renderer.RenderingContext;
import kr.syeyoung.dungeonsguide.mod.guiv2.renderer.SingleChildRenderer;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.AnnotatedExportOnlyWidget;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.annotations.Export;
import kr.syeyoung.dungeonsguide.mod.shader.ShaderManager;
import kr.syeyoung.dungeonsguide.mod.shader.ShaderProgram;
import net.minecraft.client.Minecraft;
import org.lwjgl.opengl.GL20;

import java.util.Collections;
import java.util.List;

public class CircularRect extends AnnotatedExportOnlyWidget {

    @Export(attributeName = "backgroundColor")
    public final BindableAttribute<Integer> color = new BindableAttribute<>(Integer.class, 0xFFFFFFFF);

    @Export(attributeName = "_")
    public final BindableAttribute<Widget> child = new BindableAttribute<>(Widget.class);

    @Override
    protected Renderer createRenderer() {
        return new BRender();
    }

    @Override
    public List<Widget> build(DomElement buildContext) {
        return child.getValue() == null ? Collections.EMPTY_LIST : Collections.singletonList(child.getValue());
    }

    public class BRender extends SingleChildRenderer {
        @Override
        public void doRender(float partialTicks, RenderingContext renderingContext, DomElement buildContext) {
            Size size = buildContext.getSize();
            double radius = Math.min(size.getWidth(), size.getHeight()) / 2;

            ShaderProgram shaderProgram = ShaderManager.getShader("shaders/roundrect");
            shaderProgram.useShader();
            shaderProgram.uploadUniform("radius", (float)(radius * buildContext.getAbsBounds().getWidth() / buildContext.getSize().getWidth()));
            shaderProgram.uploadUniform("halfSize", (float) buildContext.getAbsBounds().getWidth()/2, (float) buildContext.getAbsBounds().getHeight()/2);
            shaderProgram.uploadUniform("centerPos",
                    (float) (buildContext.getAbsBounds().getX()+buildContext.getAbsBounds().getWidth()/2),
                    Minecraft.getMinecraft().displayHeight - (float) (buildContext.getAbsBounds().getY() + buildContext.getAbsBounds().getHeight()/2));
            shaderProgram.uploadUniform("smoothness", 1.0f);
            renderingContext.drawRect(0,0,buildContext.getSize().getWidth(), buildContext.getSize().getHeight(), color.getValue());
            GL20.glUseProgram(0);
            super.doRender(partialTicks, renderingContext, buildContext);
        }
    }
}
