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
import kr.syeyoung.dungeonsguide.utils.TextUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.client.config.GuiUtils;

import java.awt.*;
import java.util.Arrays;

public class DataRendererLilyWeight implements DataRenderer {
    @Override
    public Dimension renderData(PlayerProfile playerProfile) {
        FontRenderer fr = Minecraft.getMinecraft().fontRendererObj;
        if (playerProfile.getLilyWeight() == null)
            fr.drawString("§eLily Weight §cAPI DISABLED", 0,0,-1);
        else
            fr.drawString("§eLily Weight §b"+String.format("%.3f", playerProfile.getLilyWeight().getTotal()), 0,0,-1);
        return new Dimension(100, fr.FONT_HEIGHT);
    }

    @Override
    public Dimension renderDummy() {
        FontRenderer fr = Minecraft.getMinecraft().fontRendererObj;
        fr.drawString("§eLily Weight §b300", 0,0,-1);
        return new Dimension(100, fr.FONT_HEIGHT);
    }
    @Override
    public Dimension getDimension() {
        return new Dimension(100, Minecraft.getMinecraft().fontRendererObj.FONT_HEIGHT);
    }

    @Override
    public void onHover(PlayerProfile playerProfile, int mouseX, int mouseY) {
        PlayerProfile.LilyWeight lilyWeight= playerProfile.getLilyWeight();
        if (lilyWeight == null) return;
        ScaledResolution scaledResolution = new ScaledResolution(Minecraft.getMinecraft());
        GuiUtils.drawHoveringText(Arrays.asList(
                        "§bDungeon Weights§7: §e"+ String.format("%.3f",lilyWeight.getCatacombs_base()+lilyWeight.getCatacombs_master()+lilyWeight.getCatacombs_exp()),
                        "   §bCatacomb Completion§7: §e"+String.format("%.3f",lilyWeight.getCatacombs_base()),
                        "   §bMaster Completion§7: §e"+String.format("%.3f", lilyWeight.getCatacombs_master()),
                        "   §bExperience§7: §e"+String.format("%.3f", lilyWeight.getCatacombs_exp()),
                        "§bSkill Weights§7: §e"+ String.format("%.3f", lilyWeight.getSkill_base() + lilyWeight.getSkill_overflow()),
                        "   §bSkill Weight§7: §e"+String.format("%.3f", lilyWeight.getSkill_base()),
                        "   §bOverflow Weight§7: §e"+String.format("%.3f", lilyWeight.getSkill_overflow()),
                        "§bSlayer Weight§7: §e"+String.format("%.3f", lilyWeight.getSlayer()),
                        "§bTotal§7: §e"+String.format("%.3f", lilyWeight.getTotal())
                ),mouseX, mouseY,
                scaledResolution.getScaledWidth(), scaledResolution.getScaledHeight(), -1, Minecraft.getMinecraft().fontRendererObj);
    }
}
