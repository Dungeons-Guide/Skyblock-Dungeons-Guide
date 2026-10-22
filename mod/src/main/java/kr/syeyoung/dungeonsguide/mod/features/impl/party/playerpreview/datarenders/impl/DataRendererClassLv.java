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
import kr.syeyoung.dungeonsguide.mod.features.impl.party.playerpreview.api.playerprofile.dataclasses.ClassSpecificData;
import kr.syeyoung.dungeonsguide.mod.features.impl.party.playerpreview.api.playerprofile.dataclasses.DungeonClass;
import kr.syeyoung.dungeonsguide.mod.features.impl.party.playerpreview.datarenders.IDataRenderer;
import kr.syeyoung.dungeonsguide.mod.utils.RenderUtils;
import kr.syeyoung.dungeonsguide.mod.utils.TextUtils;
import kr.syeyoung.dungeonsguide.mod.utils.XPUtils;
import net.minecraft.client.Minecraft;
import kr.syeyoung.dungeonsguide.mod.gui.renderer.RenderingContext;
import net.minecraft.client.gui.Font;

import java.awt.*;
import java.util.Arrays;
import java.util.List;

public class DataRendererClassLv implements IDataRenderer {
    private final DungeonClass dungeonClass;
    public DataRendererClassLv(DungeonClass dungeonClass) {
        this.dungeonClass = dungeonClass;
    }
    @Override
    public Dimension renderData(RenderingContext context, PlayerProfile playerProfile) {
        Font fr = Minecraft.getInstance().font;
        ClassSpecificData<PlayerProfile.PlayerClassData> dungeonStatDungeonSpecificData = playerProfile.getPlayerClassData().get(dungeonClass);
        boolean selected = playerProfile.getSelectedClass() == dungeonClass;
        if (dungeonStatDungeonSpecificData == null) {
            context.drawString(dungeonClass.getFamiliarName(), 0,0, 0xFF55ffff, false);
            context.drawString("Unknown", fr.width(dungeonClass.getFamiliarName()+" "),0,0xFFFFFFFF, false);
            if (selected)
                context.drawString("★", fr.width(dungeonClass.getFamiliarName()+" Unknown "),0,0xFFAAAAAA, false);
        } else {
            XPUtils.XPCalcResult xpCalcResult = XPUtils.getCataXp(dungeonStatDungeonSpecificData.getData().getExperience());
            context.drawString(dungeonClass.getFamiliarName(), 0,0, 0xFF55ffff, false);
            context.drawString(xpCalcResult.getLevel()+"", fr.width(dungeonClass.getFamiliarName()+" "),0,0xFFFFFFFF, false);
            if (selected)
                context.drawString("★", fr.width(dungeonClass.getFamiliarName()+" "+xpCalcResult.getLevel()+" "),0,0xFFAAAAAA, false);

            context.renderBar(0, fr.lineHeight, 100,xpCalcResult.getRemainingXp() == 0 ? 1 : (float) (xpCalcResult.getRemainingXp() / xpCalcResult.getNextLvXp()));
        }

        return new Dimension(100, fr.lineHeight*2);
    }

    @Override
    public Dimension renderDummy(RenderingContext context) {
        Font fr = Minecraft.getInstance().font;
        context.drawString(dungeonClass.getFamiliarName(), 0,0, 0xFF55ffff, false);
        context.drawString("99", fr.width(dungeonClass.getFamiliarName()+" "),0,0xFFFFFFFF, false);
        context.drawString("★", fr.width(dungeonClass.getFamiliarName()+" 99 "),0,0xFFAAAAAA, false);
        context.renderBar(0, fr.lineHeight, 100,1.0f);
        return new Dimension(100, fr.lineHeight*2);
    }
    @Override
    public Dimension getDimension() {
        return new Dimension(100, Minecraft.getInstance().font.lineHeight*2);
    }

    @Override
    public List<String> onHover(PlayerProfile playerProfile) {
        ClassSpecificData<PlayerProfile.PlayerClassData> dungeonStatDungeonSpecificData = playerProfile.getPlayerClassData().get(dungeonClass);
        if (dungeonStatDungeonSpecificData == null) return null;
        XPUtils.XPCalcResult xpCalcResult = XPUtils.getCataXp(dungeonStatDungeonSpecificData.getData().getExperience());

        return Arrays.asList("§bCurrent Lv§7: §e"+xpCalcResult.getLevel(),"§bExp§7: §e"+ TextUtils.format((long)xpCalcResult.getRemainingXp()) + "§7/§e"+TextUtils.format((long)xpCalcResult.getNextLvXp()), "§bTotal Xp§7: §e"+ TextUtils.format((long)dungeonStatDungeonSpecificData.getData().getExperience()));
    }
}
