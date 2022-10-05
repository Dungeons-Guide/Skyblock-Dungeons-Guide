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

package kr.syeyoung.dungeonsguide.features.impl.party.playerpreview.datarenders.impl;

import kr.syeyoung.dungeonsguide.features.impl.party.playerpreview.api.playerprofile.dataclasses.DungeonSpecificData;
import kr.syeyoung.dungeonsguide.features.impl.party.playerpreview.api.playerprofile.dataclasses.DungeonStat;
import kr.syeyoung.dungeonsguide.features.impl.party.playerpreview.api.playerprofile.dataclasses.DungeonType;
import kr.syeyoung.dungeonsguide.features.impl.party.playerpreview.api.playerprofile.PlayerProfile;
import kr.syeyoung.dungeonsguide.features.impl.party.playerpreview.datarenders.IDataRenderer;
import kr.syeyoung.dungeonsguide.utils.RenderUtils;
import kr.syeyoung.dungeonsguide.utils.TextUtils;
import kr.syeyoung.dungeonsguide.utils.XPUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraftforge.fml.client.config.GuiUtils;

import java.awt.*;
import java.util.Arrays;

public class DataRendererDungeonLv implements IDataRenderer {
    private final DungeonType dungeonType;
    public DataRendererDungeonLv(DungeonType dungeonType) {
        this.dungeonType = dungeonType;
    }
    @Override
    public Dimension renderData(PlayerProfile playerProfile) {
        FontRenderer fr = Minecraft.getMinecraft().fontRendererObj;
        DungeonSpecificData<DungeonStat> dungeonStatDungeonSpecificData = playerProfile.getDungeonStats().get(dungeonType);
        if (dungeonStatDungeonSpecificData == null) {
            fr.drawString(dungeonType.getFamiliarName(), 0,0, 0xFFFF5555);
            fr.drawString("Unknown", fr.getStringWidth(dungeonType.getFamiliarName()+" "),0,0xFFFFFFFF);
        } else {
            XPUtils.XPCalcResult xpCalcResult = XPUtils.getCataXp(dungeonStatDungeonSpecificData.getData().getExperience());
            fr.drawString(dungeonType.getFamiliarName(), 0,0, 0xFFFF5555);
            fr.drawString(xpCalcResult.getLevel()+"", fr.getStringWidth(dungeonType.getFamiliarName()+" "),0,0xFFFFFFFF);

            RenderUtils.renderBar(0, fr.FONT_HEIGHT, 100,xpCalcResult.getRemainingXp() == 0 ? 1 : (float) (xpCalcResult.getRemainingXp() / xpCalcResult.getNextLvXp()));
        }

        return new Dimension(100, fr.FONT_HEIGHT*2);
    }

    @Override
    public Dimension renderDummy() {
        FontRenderer fr = Minecraft.getMinecraft().fontRendererObj;
        fr.drawString(dungeonType.getFamiliarName(), 0,0, 0xFFFF5555);
        fr.drawString("99", fr.getStringWidth(dungeonType.getFamiliarName()+" "),0,0xFFFFFFFF);
        RenderUtils.renderBar(0, fr.FONT_HEIGHT, 100,1.0f);
        return new Dimension(100, fr.FONT_HEIGHT*2);
    }
    @Override
    public Dimension getDimension() {
        return new Dimension(100, Minecraft.getMinecraft().fontRendererObj.FONT_HEIGHT*2);
    }

    @Override
    public void onHover(PlayerProfile playerProfile, int mouseX, int mouseY) {
        DungeonSpecificData<DungeonStat> dungeonStatDungeonSpecificData = playerProfile.getDungeonStats().get(dungeonType);
        if (dungeonStatDungeonSpecificData == null) return;
        XPUtils.XPCalcResult xpCalcResult = XPUtils.getCataXp(dungeonStatDungeonSpecificData.getData().getExperience());
        ScaledResolution scaledResolution = new ScaledResolution(Minecraft.getMinecraft());
        GuiUtils.drawHoveringText(Arrays.asList("§bCurrent Lv§7: §e"+xpCalcResult.getLevel(),"§bExp§7: §e"+TextUtils.format((long)xpCalcResult.getRemainingXp()) + "§7/§e"+TextUtils.format((long)xpCalcResult.getNextLvXp()), "§bTotal Xp§7: §e"+ TextUtils.format((long)dungeonStatDungeonSpecificData.getData().getExperience())),mouseX, mouseY,
                scaledResolution.getScaledWidth(), scaledResolution.getScaledHeight(), -1, Minecraft.getMinecraft().fontRendererObj);
    }
}
