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
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.ScaledResolution;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class DataRendererTalismans extends DataRendererTalismanBase {

    @Override
    public Dimension renderData(PlayerProfile playerProfile) {
        String str = "";
        int[] rawData = getTalismanRarityTallies(playerProfile).orElse(null);
        if (rawData != null)
            for (Rarity r : Rarity.values()) {
                str = r.getColor() + rawData[r.getIndex()] +" "+ str;
            }

        FontRenderer fr = Minecraft.getInstance().font;
        if (rawData == null)
            fr.drawString("§eTalis §cAPI DISABLED", 0,0,-1);
        else
            fr.drawString("§eTalis §f"+str, 0,0,-1);
        return new Dimension(100, fr.FONT_HEIGHT);
    }

    @Override
    public Dimension renderDummy() {
        String str = "";
        for (Rarity r : Rarity.values()) {
            str = r.getColor() + (r.getIndex()+5)*2+" "+str;
        }

        FontRenderer fr = Minecraft.getInstance().font;
        fr.drawString("§eTalis §f" + str, 0,0,-1);
        return new Dimension(100, fr.FONT_HEIGHT);
    }
    @Override
    public Dimension getDimension() {
        return new Dimension(100, Minecraft.getInstance().font.FONT_HEIGHT);
    }

    @Override
    public List<String> onHover(PlayerProfile playerProfile) {
        int[] rawData = getTalismanRarityTallies(playerProfile).orElse(null);
        if (rawData == null) return null;
        ScaledResolution scaledResolution = new ScaledResolution(Minecraft.getInstance());
        List<String> list = new ArrayList<>();

        for (Rarity r : Rarity.values()) {
            list.add(r.getColor()+r.name()+"§7: §e"+rawData[r.getIndex()]);
        }
        return list;
    }


}
