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

package kr.syeyoung.dungeonsguide.features.impl.cosmetics;

import com.google.common.base.Supplier;
import kr.syeyoung.dungeonsguide.config.guiconfig.ConfigPanelCreator;
import kr.syeyoung.dungeonsguide.config.guiconfig.GuiConfig;
import kr.syeyoung.dungeonsguide.config.guiconfig.PanelDefaultParameterConfig;
import kr.syeyoung.dungeonsguide.features.FeatureParameter;
import kr.syeyoung.dungeonsguide.features.SimpleFeature;
import kr.syeyoung.dungeonsguide.features.impl.party.playerpreview.DataRendererEditor;
import kr.syeyoung.dungeonsguide.features.impl.party.playerpreview.FeatureViewPlayerOnJoin;
import kr.syeyoung.dungeonsguide.gui.MPanel;

import java.util.Arrays;
import java.util.Collections;

public class FeatureNicknamePrefix extends SimpleFeature {
    public FeatureNicknamePrefix() {
        super("Cosmetics", "Nickname Prefix", "Click on Edit to choose prefix cosmetic", "cosmetic.prefix");
        this.parameters.put("dummy", new FeatureParameter("dummy", "dummy", "dummy", "dummy", "string"));
    }

    @Override
    public String getEditRoute(final GuiConfig config) {
        ConfigPanelCreator.map.put("base." + getKey() , () -> new PrefixSelectorGUI(config, "prefix", new String[] {
                "§9Party §8> §r§a[RANK§6+§a] §r%prefix% %name%§f: TEST",
                "§2Guild > §r§a[RANK§6+§a] §r%prefix% %name% §3[Vet]§f: TEST",
                "§dTo §r§a[RANK§r§6+§r§a] §r%prefix% %name%§r§7: §r§7TEST§r",
                "§dFrom §r§a[RANK§r§6+§r§a] §r%prefix% %name%§r§7: §r§7TEST§r",
                "§b[RANK§c+§b] §r%prefix% %name%§f: TEST",
                "§bCo-op > §r§a[RANK§6+§a] §r%prefix% %name%§f: §rTEST§r"
        }, a->a.replace("&", "§")));
        return "base." + getKey();
    }

    @Override
    public boolean isDisyllable() {
        return false;
    }
}
