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

package kr.syeyoung.dungeonsguide.mod.config.types.coloredit;

import kr.syeyoung.dungeonsguide.mod.config.types.AColor;
import kr.syeyoung.dungeonsguide.mod.gui.BindableAttribute;
import kr.syeyoung.dungeonsguide.mod.gui.DomElement;
import kr.syeyoung.dungeonsguide.mod.gui.Widget;
import kr.syeyoung.dungeonsguide.mod.gui.layouter.Layouter;
import kr.syeyoung.dungeonsguide.mod.gui.primitive.ConstraintBox;
import kr.syeyoung.dungeonsguide.mod.gui.primitive.Size;
import kr.syeyoung.dungeonsguide.mod.gui.renderer.Renderer;
import kr.syeyoung.dungeonsguide.mod.gui.renderer.RenderingContext;
import kr.syeyoung.dungeonsguide.mod.shader.ShaderManager;
import kr.syeyoung.dungeonsguide.mod.shader.ShaderProgram;

import java.awt.*;
import java.util.Collections;
import java.util.List;
import kr.syeyoung.dungeonsguide.mod.shader.RoundRectUniforms;

public class ValueBar extends Widget implements Renderer, Layouter {
    public final BindableAttribute<AColor> color = new BindableAttribute<>(AColor.class);

    private final float[] hsv = new float[3];
    public ValueBar(BindableAttribute<AColor> aColorBindableAttribute) {
        color.addOnUpdate((old, neu) -> {
            Color.RGBtoHSB(neu.getRed(), neu.getGreen(), neu.getBlue(), hsv);
        });
        aColorBindableAttribute.exportTo(color);
    }

    @Override
    public List<Widget> build(DomElement buildContext) {
        return Collections.emptyList();
    }

    @Override
    public Size layout(DomElement buildContext, ConstraintBox constraintBox) {
        return new Size(constraintBox.getMaxWidth(), constraintBox.getMaxHeight());
    }

    @Override
    public void doRender(float partialTicks, RenderingContext context, DomElement buildContext) {
        ShaderProgram shaderProgram = ShaderManager.getShader("shaders/roundrect");
        RoundRectUniforms uniforms = new RoundRectUniforms((float)(5.0f * buildContext.getAbsBounds().getWidth() / buildContext.getSize().getWidth()),
                (float) buildContext.getAbsBounds().getWidth()/2, (float) buildContext.getAbsBounds().getHeight()/2,
                (float) (buildContext.getAbsBounds().getX()+buildContext.getAbsBounds().getWidth()/2),
                context.getImageHeight() - (float) (buildContext.getAbsBounds().getY() + buildContext.getAbsBounds().getHeight()/2),
                0.0f);

        double width = buildContext.getSize().getWidth();
        double height = buildContext.getSize().getHeight();

        int bottom = 0xFF000000 | Color.HSBtoRGB(hsv[0], hsv[1], 0);
        int top = 0xFF000000 | Color.HSBtoRGB(hsv[0], hsv[1], 1.0f);
        context.drawShape(shaderProgram, uniforms,
                0, (float) height, (float) width, (float) height, (float) width, 0, 0, 0,
                bottom, bottom, top, top);


        context.drawRect(0, (1-hsv[2]) * height, width, (1-hsv[2]) * height+1, 0xFFFFFFFF);
    }

    @Override
    public boolean mouseClicked(int absMouseX, int absMouseY, double relMouseX, double relMouseY, int mouseButton, boolean childHandled) {
        if (childHandled) return false;
        getDomElement().obtainFocus();

        double value = 1 - relMouseY / getDomElement().getSize().getHeight();
        value = Layouter.clamp(value, 0, 1);

        int rgb = Color.HSBtoRGB(hsv[0], hsv[1], (float) value);
        AColor aColor = new AColor((rgb & 0xFFFFFF) | (color.getValue().getRGB() & 0xFF000000), true);
        aColor.setChroma(color.getValue().isChroma());
        aColor.setChromaSpeed(color.getValue().getChromaSpeed());
        color.setValue(aColor);


        return true;
    }

    @Override
    public void mouseClickMove(int absMouseX, int absMouseY, double relMouseX, double relMouseY, int clickedMouseButton, long timeSinceLastClick) {

        double value = 1 - relMouseY / getDomElement().getSize().getHeight();
        value = Layouter.clamp(value, 0, 1);

        int rgb = Color.HSBtoRGB(hsv[0], hsv[1], (float) value);
        AColor aColor = new AColor((rgb & 0xFFFFFF) | (color.getValue().getRGB() & 0xFF000000), true);
        aColor.setChroma(color.getValue().isChroma());
        aColor.setChromaSpeed(color.getValue().getChromaSpeed());
        color.setValue(aColor);
    }

    @Override
    public void mouseReleased(int absMouseX, int absMouseY, double relMouseX, double relMouseY, int state) {
        if (!getDomElement().isFocused()) return;

        double value = 1 - relMouseY / getDomElement().getSize().getHeight();
        value = Layouter.clamp(value, 0, 1);

        int rgb = Color.HSBtoRGB(hsv[0], hsv[1], (float) value);
        AColor aColor = new AColor((rgb & 0xFFFFFF) | (color.getValue().getRGB() & 0xFF000000), true);
        aColor.setChroma(color.getValue().isChroma());
        aColor.setChromaSpeed(color.getValue().getChromaSpeed());
        color.setValue(aColor);
    }
}
