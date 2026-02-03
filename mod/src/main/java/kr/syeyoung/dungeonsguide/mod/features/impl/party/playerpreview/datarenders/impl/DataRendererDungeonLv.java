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
import kr.syeyoung.dungeonsguide.mod.features.impl.party.playerpreview.api.playerprofile.dataclasses.DungeonSpecificData;
import kr.syeyoung.dungeonsguide.mod.features.impl.party.playerpreview.api.playerprofile.dataclasses.DungeonStat;
import kr.syeyoung.dungeonsguide.mod.features.impl.party.playerpreview.api.playerprofile.dataclasses.DungeonType;
import kr.syeyoung.dungeonsguide.mod.features.impl.party.playerpreview.datarenders.IDataRenderer;
import kr.syeyoung.dungeonsguide.mod.gui.renderer.RenderingContext;
import kr.syeyoung.dungeonsguide.mod.utils.RenderUtils;
import kr.syeyoung.dungeonsguide.mod.utils.TextUtils;
import kr.syeyoung.dungeonsguide.mod.utils.XPUtils;
import kr.syeyoung.modapi.ModAPI;
import kr.syeyoung.modapi.rendering.UFontCalculator;

import java.awt.*;
import java.util.Arrays;
import java.util.List;

public class DataRendererDungeonLv implements IDataRenderer {
    private final DungeonType dungeonType;
    public DataRendererDungeonLv(DungeonType dungeonType) {
        this.dungeonType = dungeonType;
    }
    @Override
    public Dimension renderData(RenderingContext context, PlayerProfile playerProfile) {
        DungeonSpecificData<DungeonStat> dungeonStatDungeonSpecificData = playerProfile.getDungeonStats().get(dungeonType);

        UFontCalculator fr = ModAPI.getAPI().getFontCalculator();
        if (dungeonStatDungeonSpecificData == null) {
            context.drawString(dungeonType.getFamiliarName(), 0,0, 0xFFFF5555);
            context.drawString("Unknown", fr.getStringWidth(dungeonType.getFamiliarName()+" "),0,0xFFFFFFFF);
        } else {
            XPUtils.XPCalcResult xpCalcResult = XPUtils.getCataXp(dungeonStatDungeonSpecificData.getData().getExperience());
            context.drawString(dungeonType.getFamiliarName(), 0,0, 0xFFFF5555);
            context.drawString(xpCalcResult.getLevel()+"", fr.getStringWidth(dungeonType.getFamiliarName()+" "),0,0xFFFFFFFF);

            RenderUtils.renderBar(context, 0, fr.getFontHeight(), 100,xpCalcResult.getRemainingXp() == 0 ? 1 : (float) (xpCalcResult.getRemainingXp() / xpCalcResult.getNextLvXp()));
        }

        return new Dimension(100, fr.getFontHeight()*2);
    }

    @Override
    public Dimension renderDummy(RenderingContext context) {
        UFontCalculator fr = ModAPI.getAPI().getFontCalculator();
        context.drawString(dungeonType.getFamiliarName(), 0,0, 0xFFFF5555);
        context.drawString("99", fr.getStringWidth(dungeonType.getFamiliarName()+" "),0,0xFFFFFFFF);
        RenderUtils.renderBar(context, 0, fr.getFontHeight(), 100,1.0f);
        return new Dimension(100, fr.getFontHeight()*2);
    }
    @Override
    public Dimension getDimension() {
        return new Dimension(100, ModAPI.getAPI().getFontCalculator().getFontHeight()*2);
    }

    @Override
    public List<String> onHover(PlayerProfile playerProfile) {
        DungeonSpecificData<DungeonStat> dungeonStatDungeonSpecificData = playerProfile.getDungeonStats().get(dungeonType);
        if (dungeonStatDungeonSpecificData == null) return null;
        XPUtils.XPCalcResult xpCalcResult = XPUtils.getCataXp(dungeonStatDungeonSpecificData.getData().getExperience());
        return Arrays.asList("§bCurrent Lv§7: §e"+xpCalcResult.getLevel(),"§bExp§7: §e"+TextUtils.format((long)xpCalcResult.getRemainingXp()) + "§7/§e"+TextUtils.format((long)xpCalcResult.getNextLvXp()), "§bTotal Xp§7: §e"+ TextUtils.format((long)dungeonStatDungeonSpecificData.getData().getExperience()));
    }
}
