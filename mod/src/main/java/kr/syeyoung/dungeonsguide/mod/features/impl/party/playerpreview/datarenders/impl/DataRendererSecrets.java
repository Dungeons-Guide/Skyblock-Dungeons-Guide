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

package kr.syeyoung.dungeonsguide.mod.features.impl.party.playerpreview.datarenders.impl;

import kr.syeyoung.dungeonsguide.mod.features.impl.party.playerpreview.api.playerprofile.PlayerProfile;
import kr.syeyoung.dungeonsguide.mod.features.impl.party.playerpreview.datarenders.IDataRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;

import java.awt.*;
import java.util.List;

public class DataRendererSecrets implements IDataRenderer {
    @Override
    public Dimension renderData(PlayerProfile playerProfile) {
        FontRenderer fr = Minecraft.getMinecraft().fontRendererObj;
        double theint = playerProfile.getTotalSecrets()/ (double)playerProfile.getDungeonStats().values().stream().flatMap(s -> s.getData().getPlays().values().stream())
                .map(fs -> fs.getData().getWatcherKills()).reduce(0, Integer::sum);
        fr.drawString("§eSecrets §b"+playerProfile.getTotalSecrets()+" §7("+
                String.format("%.2f", theint)+"/run)", 0,0,-1);
        return new Dimension(100, fr.FONT_HEIGHT);
    }

    @Override
    public Dimension renderDummy() {
        FontRenderer fr = Minecraft.getMinecraft().fontRendererObj;
        fr.drawString("§eSecrets §b99999 §7(X/run)", 0,0,-1);
        return new Dimension(100, fr.FONT_HEIGHT);
    }
    @Override
    public Dimension getDimension() {
        return new Dimension(100, Minecraft.getMinecraft().fontRendererObj.FONT_HEIGHT);
    }

    @Override
    public List<String> onHover(PlayerProfile playerProfile) {
        return null;
    }
}
