/*
 * Dungeons Guide - The most intelligent Hypixel Skyblock Dungeons Mod
 * Copyright (C) 2023  Linnea Gräf
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
import kr.syeyoung.dungeonsguide.mod.gui.renderer.RenderingContext;
import kr.syeyoung.modapi.ModAPI;
import kr.syeyoung.modapi.rendering.UFontCalculator;

import java.awt.*;
import java.util.Collections;
import java.util.List;

public class DataRendererMagicPower extends DataRendererTalismanBase {

    public int getMagicPower(int[] rarityTallies) {
        int magicPower = 0;
        for (Rarity value : Rarity.values()) {
            magicPower += rarityTallies[value.getIndex()] * value.getMagicPower();
        }
        return magicPower;
    }

    private String renderMagicPower(int magicPower) {
        return "§eMP §f" + magicPower;
    }

    private String renderMagicPower(PlayerProfile playerProfile) {
        return getTalismanRarityTallies(playerProfile)
                .map(this::getMagicPower)
                .map(this::renderMagicPower)
                .orElse("§eMP §cAPI DISABLED");
    }

    @Override
    public Dimension renderData(RenderingContext context, PlayerProfile playerProfile) {
        UFontCalculator fr = ModAPI.getAPI().getFontCalculator();
        context.drawString(renderMagicPower(playerProfile), 0, 0, -1);
        return new Dimension(100, fr.getFontHeight());
    }

    @Override
    public Dimension renderDummy(RenderingContext context) {
        UFontCalculator fr = ModAPI.getAPI().getFontCalculator();
        context.drawString(renderMagicPower(699), 0, 0, -1);
        return new Dimension(100, fr.getFontHeight());
    }

    @Override
    public Dimension getDimension() {
        return new Dimension(100, ModAPI.getAPI().getFontCalculator().getFontHeight());
    }

    @Override
    public List<String> onHover(PlayerProfile playerProfile) {
        return Collections.singletonList(renderMagicPower(playerProfile));
    }


}
