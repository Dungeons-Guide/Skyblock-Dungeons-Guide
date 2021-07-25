/*
 *     Dungeons Guide - The most intelligent Hypixel Skyblock Dungeons Mod
 *     Copyright (C) 2021  cyoung06
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Affero General Public License as published
 *     by the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Affero General Public License for more details.
 *
 *     You should have received a copy of the GNU Affero General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package kr.syeyoung.dungeonsguide.features.impl.party.playerpreview;

import kr.syeyoung.dungeonsguide.features.impl.party.api.PlayerProfile;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;

import java.awt.*;

public class DataRendererSetUrOwn implements DataRenderer {
    @Override
    public Dimension renderData(PlayerProfile playerProfile) {
        FontRenderer fr = Minecraft.getMinecraft().fontRendererObj;
        fr.drawString("§aCustomize at /dg", 0,0,-1);
        fr.drawString("§a-> Party Kicker", 0,fr.FONT_HEIGHT,-1);
        fr.drawString("§a-> View Player Stats", 0,fr.FONT_HEIGHT*2,-1);
        fr.drawString("§a-> Edit", 0,fr.FONT_HEIGHT*3,-1);
        return new Dimension(100, fr.FONT_HEIGHT*4);
    }

    @Override
    public void onHover(PlayerProfile playerProfile, int mouseX, int mouseY) {

    }

    @Override
    public Dimension renderDummy() {
        FontRenderer fr = Minecraft.getMinecraft().fontRendererObj;
        fr.drawString("§aCustomize at /dg", 0,0,-1);
        fr.drawString("§a-> Party Kicker", 0,fr.FONT_HEIGHT,-1);
        fr.drawString("§a-> View Player Stats", 0,fr.FONT_HEIGHT*2,-1);
        fr.drawString("§a-> Edit", 0,fr.FONT_HEIGHT*3,-1);
        return new Dimension(100, fr.FONT_HEIGHT*4);
    }
    @Override
    public Dimension getDimension() {
        return new Dimension(100, Minecraft.getMinecraft().fontRendererObj.FONT_HEIGHT*4);
    }
}
