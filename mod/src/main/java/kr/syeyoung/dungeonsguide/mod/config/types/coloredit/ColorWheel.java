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
import kr.syeyoung.dungeonsguide.mod.guiv2.BindableAttribute;
import kr.syeyoung.dungeonsguide.mod.guiv2.DomElement;
import kr.syeyoung.dungeonsguide.mod.guiv2.Widget;
import kr.syeyoung.dungeonsguide.mod.guiv2.layouter.Layouter;
import kr.syeyoung.dungeonsguide.mod.guiv2.primitive.ConstraintBox;
import kr.syeyoung.dungeonsguide.mod.guiv2.primitive.Size;
import kr.syeyoung.dungeonsguide.mod.guiv2.renderer.Renderer;
import kr.syeyoung.dungeonsguide.mod.guiv2.renderer.RenderingContext;
import kr.syeyoung.dungeonsguide.mod.shader.ShaderManager;
import kr.syeyoung.dungeonsguide.mod.shader.ShaderProgram;
import net.minecraft.client.Minecraft;
import org.lwjgl.opengl.GL20;

import java.awt.*;
import java.util.Collections;
import java.util.List;

public class ColorWheel extends Widget implements Renderer, Layouter {
    public final BindableAttribute<AColor> color = new BindableAttribute<>(AColor.class);
    private final float[] hsv = new float[3];
    public ColorWheel(BindableAttribute<AColor> aColorBindableAttribute) {
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
        double len = Math.min(constraintBox.getMaxWidth(), constraintBox.getMaxHeight());
        return new Size(len, len);
    }

    @Override
    public void doRender(float partialTicks, RenderingContext context, DomElement buildContext) {
        double rad = buildContext.getAbsBounds().getWidth() / 2.0;
        ShaderProgram shaderProgram = ShaderManager.getShader("shaders/chromacircle");
        shaderProgram.useShader();
        shaderProgram.uploadUniform("radius", (float) rad);
        shaderProgram.uploadUniform("value", hsv[2]);
        shaderProgram.uploadUniform("centerPos",
                (float) (buildContext.getAbsBounds().getX()+buildContext.getAbsBounds().getWidth()/2),
                Minecraft.getMinecraft().displayHeight - (float) (buildContext.getAbsBounds().getY() + buildContext.getAbsBounds().getHeight()/2));
        shaderProgram.uploadUniform("smoothness", 0.0f);
        context.drawRect(0,0,buildContext.getSize().getWidth(), buildContext.getSize().getHeight(), 0xFFFFFFFF);
        GL20.glUseProgram(0);

        double angle = (hsv[0] - Math.floor(hsv[0])) * Math.PI * 2;
        double x = Math.sin(angle) * hsv[1] * rad;
        double y = -Math.cos(angle) * hsv[1] * rad;

        shaderProgram = ShaderManager.getShader("shaders/donut");
        shaderProgram.useShader();
        shaderProgram.uploadUniform("radius", 5.0f);
        shaderProgram.uploadUniform("thickness", 1f);
        shaderProgram.uploadUniform("centerPos",
                (float) (buildContext.getAbsBounds().getX()+rad+x),
                Minecraft.getMinecraft().displayHeight - (float) (buildContext.getAbsBounds().getY() + rad+y));
        shaderProgram.uploadUniform("smoothness", 0.0f);
        context.drawRect(0,0,buildContext.getSize().getWidth(), buildContext.getSize().getHeight(), 0xFFFFFFFF);
        GL20.glUseProgram(0);
    }

    @Override
    public boolean mouseClicked(int absMouseX, int absMouseY, double relMouseX, double relMouseY, int mouseButton) {

        double radius = getDomElement().getSize().getWidth()/2;
        double dx = relMouseX - radius;
        double dy = relMouseY - radius;

        double hue = Math.atan2(dx, -dy) / (Math.PI * 2);
        double dist = Math.sqrt(dx * dx + dy * dy) / radius;

        if (dist > 1.0) return false;
        getDomElement().obtainFocus();

        int rgb = Color.HSBtoRGB((float) hue, (float) dist, hsv[2]);
        AColor aColor = new AColor((rgb & 0xFFFFFF) | (color.getValue().getRGB() & 0xFF000000), true);
        aColor.setChroma(color.getValue().isChroma());
        aColor.setChromaSpeed(color.getValue().getChromaSpeed());
        color.setValue(aColor);

        return true;
    }

    @Override
    public void mouseClickMove(int absMouseX, int absMouseY, double relMouseX, double relMouseY, int clickedMouseButton, long timeSinceLastClick) {
        double radius = getDomElement().getSize().getWidth()/2;
        double dx = relMouseX - radius;
        double dy = relMouseY - radius;

        double hue = Math.atan2(dx, -dy) / (Math.PI * 2);
        double dist = Math.min(1, Math.sqrt(dx * dx + dy * dy) / radius);

        int rgb = Color.HSBtoRGB((float) hue, (float) dist, hsv[2]);
        AColor aColor = new AColor((rgb & 0xFFFFFF) | (color.getValue().getRGB() & 0xFF000000), true);
        aColor.setChroma(color.getValue().isChroma());
        aColor.setChromaSpeed(color.getValue().getChromaSpeed());
        color.setValue(aColor);

    }

    @Override
    public void mouseReleased(int absMouseX, int absMouseY, double relMouseX, double relMouseY, int state) {
        if (!getDomElement().isFocused()) return;
        double radius = getDomElement().getSize().getWidth()/2;
        double dx = relMouseX - radius;
        double dy = relMouseY - radius;

        double hue = Math.atan2(dx, -dy) / (Math.PI * 2);
        double dist = Math.min(1, Math.sqrt(dx * dx + dy * dy) / radius);

        int rgb = Color.HSBtoRGB((float) hue, (float) dist, hsv[2]);
        AColor aColor = new AColor((rgb & 0xFFFFFF) | (color.getValue().getRGB() & 0xFF000000), true);
        aColor.setChroma(color.getValue().isChroma());
        aColor.setChromaSpeed(color.getValue().getChromaSpeed());
        color.setValue(aColor);

    }
}
