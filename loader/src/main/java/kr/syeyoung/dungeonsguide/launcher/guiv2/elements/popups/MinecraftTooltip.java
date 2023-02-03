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

package kr.syeyoung.dungeonsguide.launcher.guiv2.elements.popups;

import kr.syeyoung.dungeonsguide.launcher.guiv2.DomElement;
import kr.syeyoung.dungeonsguide.launcher.guiv2.Widget;
import kr.syeyoung.dungeonsguide.launcher.guiv2.layouter.Layouter;
import kr.syeyoung.dungeonsguide.launcher.guiv2.primitive.ConstraintBox;
import kr.syeyoung.dungeonsguide.launcher.guiv2.primitive.Size;
import kr.syeyoung.dungeonsguide.launcher.guiv2.renderer.Renderer;
import kr.syeyoung.dungeonsguide.launcher.guiv2.renderer.RenderingContext;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.client.config.GuiUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MinecraftTooltip extends Widget implements Renderer, Layouter {
    public List<String> tooltip = new ArrayList<>();

    public void setTooltip(List<String> tooltip) {
        this.tooltip = tooltip;
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
    public void doRender(int absMouseX, int absMouseY, double relMouseX, double relMouseY, float partialTicks, RenderingContext context, DomElement buildContext) {
        GuiUtils.drawHoveringText(tooltip,0,0,
                (int) buildContext.getRelativeBound().getWidth(),
                (int) buildContext.getRelativeBound().getHeight(), -1, Minecraft.getMinecraft().fontRendererObj);
    }
}
