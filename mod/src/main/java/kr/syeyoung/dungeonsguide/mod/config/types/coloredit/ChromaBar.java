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
import kr.syeyoung.dungeonsguide.mod.gui.primitive.Rect;
import kr.syeyoung.dungeonsguide.mod.gui.primitive.Size;
import kr.syeyoung.dungeonsguide.mod.gui.renderer.Renderer;
import kr.syeyoung.dungeonsguide.mod.gui.renderer.RenderingContext;
import kr.syeyoung.dungeonsguide.mod.utils.RenderUtils;
import kr.syeyoung.modapi.ModAPI;

import java.util.Collections;
import java.util.List;

public class ChromaBar extends Widget implements Renderer, Layouter {
    public final BindableAttribute<AColor> color = new BindableAttribute<>(AColor.class);
    public ChromaBar(BindableAttribute<AColor> aColorBindableAttribute) {
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
        if (this.color.getValue().isChroma()) {
            Rect abs = getDomElement().getAbsBounds();
            int color = RenderUtils.getColorAt(abs.getX(), abs.getY() + abs.getHeight(), this.color.getValue());
            context.drawChromaRoundRect(
                    (float)(5.0f * buildContext.getAbsBounds().getWidth() / buildContext.getSize().getWidth()),
                    (float) buildContext.getAbsBounds().getWidth()/2, (float) buildContext.getAbsBounds().getHeight()/2,
                    (float) (buildContext.getAbsBounds().getX()+buildContext.getAbsBounds().getWidth()/2),
                    ModAPI.getAPI().getDisplayHeight() - (float) (buildContext.getAbsBounds().getY() + buildContext.getAbsBounds().getHeight()/2),
                    0.0f,
                    0,0,width, height, color
            );
        } else {

            context.drawChromaGradientRoundRect(
                    (float)(5.0f * buildContext.getAbsBounds().getWidth() / buildContext.getSize().getWidth()),
                    (float) buildContext.getAbsBounds().getWidth()/2, (float) buildContext.getAbsBounds().getHeight()/2,
                    (float) (buildContext.getAbsBounds().getX()+buildContext.getAbsBounds().getWidth()/2),
                    ModAPI.getAPI().getDisplayHeight() - (float) (buildContext.getAbsBounds().getY() + buildContext.getAbsBounds().getHeight()/2),
                    0.0f,
                    0,0,width, height, 0xFFFF0000, 0xFFFF00FF
            );
        }
        double alpha = this.color.getValue().getChromaSpeed();
        context.drawRect(0, (alpha) * height, width, (alpha) * height+1, 0xFFFFFFFF);
    }

    @Override
    public boolean mouseClicked(int absMouseX, int absMouseY, double relMouseX, double relMouseY, int mouseButton, boolean childHandled) {
        if (childHandled) return false;
        getDomElement().obtainFocus();

        float alpha = (float) (relMouseY / getDomElement().getSize().getHeight());
        alpha = (float) Layouter.clamp(alpha, 0, 1);

        AColor aColor = new AColor(color.getValue().getRGB(), true);
        aColor.setChroma(alpha != 0);
        aColor.setChromaSpeed(alpha);
        color.setValue(aColor);


        return true;
    }

    @Override
    public void mouseClickMove(int absMouseX, int absMouseY, double relMouseX, double relMouseY, int clickedMouseButton, long timeSinceLastClick) {

        float alpha = (float) (relMouseY / getDomElement().getSize().getHeight());
        alpha = (float) Layouter.clamp(alpha, 0, 1);

        AColor aColor = new AColor(color.getValue().getRGB(), true);
        aColor.setChroma(alpha != 0);
        aColor.setChromaSpeed(alpha);
        color.setValue(aColor);

    }

    @Override
    public void mouseReleased(int absMouseX, int absMouseY, double relMouseX, double relMouseY, int state) {
        if (!getDomElement().isFocused()) return;

        float alpha = (float) (relMouseY / getDomElement().getSize().getHeight());
        alpha = (float) Layouter.clamp(alpha, 0, 1);

        AColor aColor = new AColor(color.getValue().getRGB(), true);
        aColor.setChroma(alpha != 0);
        aColor.setChromaSpeed(alpha);
        color.setValue(aColor);
    }
}
