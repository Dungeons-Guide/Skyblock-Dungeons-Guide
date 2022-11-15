/*
 * Dungeons Guide - The most intelligent Hypixel Skyblock Dungeons Mod
 * Copyright (C) 2021  cyoung06
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

package kr.syeyoung.dungeonsguide.mod.features.impl.secret.mechanicbrowser;


import kr.syeyoung.dungeonsguide.mod.gui.MPanel;

import lombok.AllArgsConstructor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;

import java.awt.*;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

@AllArgsConstructor
public class MechanicBrowserElement extends MPanel {
    private Supplier<String> name;
    private boolean isCategory = false;
    private BiConsumer<MechanicBrowserElement, Point> onClick;
    @Override
    public void render(int absMousex, int absMousey, int relMousex0, int relMousey0, float partialTicks, Rectangle scissor) {
        if (isCategory || isFocused)
            Gui.drawRect(0, 0, bounds.width, bounds.height, 0xFF444444);
        else if (lastAbsClip.contains(absMousex, absMousey))
            Gui.drawRect(0, 0, bounds.width, bounds.height, 0xFF555555);
        Minecraft.getMinecraft().fontRendererObj.drawString((String)name.get(), 4, 1, 0xFFEEEEEE);
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(Minecraft.getMinecraft().fontRendererObj.getStringWidth(name.get()) + 8, Minecraft.getMinecraft().fontRendererObj.FONT_HEIGHT);
    }

    @Override
    public void mouseClicked(int absMouseX, int absMouseY, int relMouseX, int relMouseY, int mouseButton) {
        if (lastAbsClip.contains(absMouseX, absMouseY) && onClick != null)
            onClick.accept(this, new Point(lastParentPoint.x + bounds.x, lastParentPoint.y + bounds.y));
    }

    @Override
    public void mouseMoved(int absMouseX, int absMouseY, int relMouseX0, int relMouseY0) {
        if (lastAbsClip.contains(absMouseX, absMouseY) && onClick != null)
            setCursor(EnumCursor.POINTING_HAND);
    }
}
