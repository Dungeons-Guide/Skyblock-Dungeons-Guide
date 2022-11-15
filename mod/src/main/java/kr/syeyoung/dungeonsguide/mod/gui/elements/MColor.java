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

package kr.syeyoung.dungeonsguide.mod.gui.elements;


import kr.syeyoung.dungeonsguide.mod.gui.MPanel;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.minecraft.client.gui.Gui;

import java.awt.*;

@AllArgsConstructor
@NoArgsConstructor
public class MColor extends MPanel {
    @Getter
    @Setter
    private Color color = Color.white;
    @Getter
    @Setter
    private Dimension size = new Dimension(20,15);
    @Override
    public void render(int absMousex, int absMousey, int relMousex0, int relMousey0, float partialTicks, Rectangle scissor) {
        Rectangle rectangle = getBounds();

        int x = (rectangle.width - getSize().width) / 2;
        int y = (rectangle.height - getSize().height) / 2;

        Gui.drawRect(x,y,x+getSize().width,y+getSize().height, getColor().getRGB());
    }
}
