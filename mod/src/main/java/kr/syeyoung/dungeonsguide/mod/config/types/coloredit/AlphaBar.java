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
import kr.syeyoung.modapi.ModAPI;

import java.util.Collections;
import java.util.List;

public class AlphaBar extends Widget implements Renderer, Layouter {
    public final BindableAttribute<AColor> color = new BindableAttribute<>(AColor.class);
    public AlphaBar(BindableAttribute<AColor> aColorBindableAttribute) {
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

        double width = buildContext.getSize().getWidth();
        double height = buildContext.getSize().getHeight();
        context.drawGradientRoundRect(
                (float)(5.0f * buildContext.getAbsBounds().getWidth() / buildContext.getSize().getWidth()),
                (float) buildContext.getAbsBounds().getWidth()/2, (float) buildContext.getAbsBounds().getHeight()/2,
                (float) (buildContext.getAbsBounds().getX()+buildContext.getAbsBounds().getWidth()/2),
                ModAPI.getAPI().getDisplayHeight() - (float) (buildContext.getAbsBounds().getY() + buildContext.getAbsBounds().getHeight()/2),
                0.0f,
                0, 0, width, height,  this.color.getValue().getRGB() & 0xFFFFFF | 0xFF000000, this.color.getValue().getRGB() & 0xFFFFFF
        );


        double alpha = this.color.getValue().getAlpha() / 255.0;
        context.drawRect(0, (1-alpha) * height, width, (1-alpha) * height+1, 0xFFFFFFFF);
    }

    @Override
    public boolean mouseClicked(int absMouseX, int absMouseY, double relMouseX, double relMouseY, int mouseButton, boolean childHandled) {
        if (childHandled) return false;
        getDomElement().obtainFocus();

        double alpha = 1 - relMouseY / getDomElement().getSize().getHeight();
        alpha = Layouter.clamp(alpha, 0, 1);
        int alpha2 = (int) (alpha * 255);

        AColor aColor = new AColor((color.getValue().getRGB() & 0xFFFFFF) | (alpha2 << 24), true);
        aColor.setChroma(color.getValue().isChroma());
        aColor.setChromaSpeed(color.getValue().getChromaSpeed());
        color.setValue(aColor);


        return true;
    }

    @Override
    public void mouseClickMove(int absMouseX, int absMouseY, double relMouseX, double relMouseY, int clickedMouseButton, long timeSinceLastClick) {
        double alpha = 1 - relMouseY / getDomElement().getSize().getHeight();
        alpha = Layouter.clamp(alpha, 0, 1);
        int alpha2 = (int) (alpha * 255);

        AColor aColor = new AColor((color.getValue().getRGB() & 0xFFFFFF) | (alpha2 << 24), true);
        aColor.setChroma(color.getValue().isChroma());
        aColor.setChromaSpeed(color.getValue().getChromaSpeed());
        color.setValue(aColor);
    }

    @Override
    public void mouseReleased(int absMouseX, int absMouseY, double relMouseX, double relMouseY, int state) {
        if (!getDomElement().isFocused()) return;

        double alpha = 1 - relMouseY / getDomElement().getSize().getHeight();
        alpha = Layouter.clamp(alpha, 0, 1);
        int alpha2 = (int) (alpha * 255);

        AColor aColor = new AColor((color.getValue().getRGB() & 0xFFFFFF) | (alpha2 << 24), true);
        aColor.setChroma(color.getValue().isChroma());
        aColor.setChromaSpeed(color.getValue().getChromaSpeed());
        color.setValue(aColor);
    }
}
