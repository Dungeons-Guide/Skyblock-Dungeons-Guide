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

package kr.syeyoung.dungeonsguide.mod.gui.elements.popups;

import kr.syeyoung.dungeonsguide.mod.gui.DomElement;
import kr.syeyoung.dungeonsguide.mod.gui.Widget;
import kr.syeyoung.dungeonsguide.mod.gui.layouter.Layouter;
import kr.syeyoung.dungeonsguide.mod.gui.primitive.ConstraintBox;
import kr.syeyoung.dungeonsguide.mod.gui.primitive.Size;
import kr.syeyoung.dungeonsguide.mod.gui.renderer.Renderer;
import kr.syeyoung.dungeonsguide.mod.gui.renderer.RenderingContext;
import net.minecraft.client.Minecraft;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.DefaultTooltipPositioner;
import net.minecraft.client.gui.screens.inventory.tooltip.TooltipRenderUtil;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.FormattedText;
import org.joml.Vector2ic;

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

    // vanilla's GuiGraphicsExtractor.tooltip, with this element's size as the screen like 1.8.9's drawHoveringText call (D101)
    @Override
    public void doRender(float partialTicks, RenderingContext context, DomElement buildContext) {
        Font font = Minecraft.getInstance().font;
        List<ClientTooltipComponent> lines = new ArrayList<>();
        for (String line : tooltip) {
            lines.add(ClientTooltipComponent.create(Language.getInstance().getVisualOrder(FormattedText.of(line))));
        }
        if (lines.isEmpty()) return;

        int textWidth = 0;
        int tempHeight = lines.size() == 1 ? -2 : 0;
        for (ClientTooltipComponent line : lines) {
            textWidth = Math.max(textWidth, line.getWidth(font));
            tempHeight += line.getHeight(font);
        }
        int w = textWidth;
        int h = tempHeight;
        Vector2ic positioned = DefaultTooltipPositioner.INSTANCE.positionTooltip(
                (int) buildContext.getRelativeBound().getWidth(),
                (int) buildContext.getRelativeBound().getHeight(), 0, 0, w, h);
        int x = positioned.x();
        int y = positioned.y();

        context.drawVanilla(graphics -> {
            TooltipRenderUtil.extractTooltipBackground(graphics, x, y, w, h, null);
            int localY = y;
            for (int i = 0; i < lines.size(); i++) {
                ClientTooltipComponent line = lines.get(i);
                line.extractText(graphics, font, x, localY);
                localY += line.getHeight(font) + (i == 0 ? 2 : 0);
            }
        });
    }
}
