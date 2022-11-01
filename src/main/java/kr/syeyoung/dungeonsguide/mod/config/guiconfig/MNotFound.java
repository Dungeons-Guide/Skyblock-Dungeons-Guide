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

package kr.syeyoung.dungeonsguide.mod.config.guiconfig;

import kr.syeyoung.dungeonsguide.mod.gui.MPanel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;

import java.awt.*;

public class MNotFound extends MPanel {
    @Override
    public void resize(int parentWidth, int parentHeight) {
        setBounds(new Rectangle(0,0,parentWidth,parentHeight));
    }

    @Override
    public void render(int absMousex, int absMousey, int relMousex0, int relMousey0, float partialTicks, Rectangle scissor) {
        FontRenderer fr = Minecraft.getMinecraft().fontRendererObj;
        fr.drawString("404 Not Found", (getBounds().width - fr.getStringWidth("404 Not Found")) / 2, (getBounds().height - fr.FONT_HEIGHT) / 2, -1);
    }
}
