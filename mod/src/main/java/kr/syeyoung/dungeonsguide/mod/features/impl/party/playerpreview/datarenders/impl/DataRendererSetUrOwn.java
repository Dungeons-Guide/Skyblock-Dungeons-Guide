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
import kr.syeyoung.dungeonsguide.mod.gui.renderer.RenderingContext;
import kr.syeyoung.modapi.ModAPI;
import kr.syeyoung.modapi.rendering.UFontCalculator;

import java.awt.*;
import java.util.List;

public class DataRendererSetUrOwn implements IDataRenderer {
    @Override
    public Dimension renderData(RenderingContext context, PlayerProfile playerProfile) {
        UFontCalculator fr = ModAPI.getAPI().getFontCalculator();
        context.drawString("§aCustomize at /dg", 0,0,-1);
        context.drawString("§a-> Dungeon Party", 0,fr.getFontHeight(),-1);
        context.drawString("§a-> View player stats when join", 0,fr.getFontHeight()*2,-1);
        context.drawString("§a-> Configure", 0,fr.getFontHeight()*3,-1);
        return new Dimension(100, fr.getFontHeight()*4);
    }

    @Override
    public List<String> onHover(PlayerProfile playerProfile) {
        return null;
    }

    @Override
    public Dimension renderDummy(RenderingContext context) {
        UFontCalculator fr = ModAPI.getAPI().getFontCalculator();
        context.drawString("§aCustomize at /dg", 0,0,-1);
        context.drawString("§a-> Dungeon Party", 0,fr.getFontHeight(),-1);
        context.drawString("§a-> View player stats when join", 0,fr.getFontHeight()*2,-1);
        context.drawString("§a-> Configure", 0,fr.getFontHeight()*3,-1);
        return new Dimension(100, fr.getFontHeight()*4);
    }
    @Override
    public Dimension getDimension() {
        return new Dimension(100, ModAPI.getAPI().getFontCalculator().getFontHeight()*4);
    }
}
