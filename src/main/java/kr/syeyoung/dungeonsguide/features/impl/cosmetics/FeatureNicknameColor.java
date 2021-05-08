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

import kr.syeyoung.dungeonsguide.config.guiconfig.ConfigPanelCreator;
import kr.syeyoung.dungeonsguide.config.guiconfig.GuiConfig;
import kr.syeyoung.dungeonsguide.features.FeatureParameter;
import kr.syeyoung.dungeonsguide.features.SimpleFeature;

public class FeatureNicknameColor extends SimpleFeature {
    public FeatureNicknameColor() {
        super("Cosmetics", "Nickname Color", "Click on Edit to choose nickname color cosmetic", "cosmetic.nickname");
        this.parameters.put("dummy", new FeatureParameter("dummy", "dummy", "dummy", "dummy", "string"));
    }

    @Override
    public String getEditRoute(final GuiConfig config) {
        ConfigPanelCreator.map.put("base." + getKey() , () -> new PrefixSelectorGUI(config, "color", new String[] {
                "§9Party §8> §r§a[RANK§6+§a] %prefix%%name%§f: TEST",
                "§2Guild > §r§a[RANK§6+§a] %prefix%%name% §3[Vet]§f: TEST",
                "§dTo §r§r§a[RANK§r§6+§r§a] %prefix%%name%§r§7: §r§7TEST§r",
                "§dFrom §r§r§a[RANK§r§6+§r§a] %prefix%%name%§r§7: §r§7TEST§r",
                "§r§b[RANK§c+§b] %prefix%%name%§f: TEST",
                "§r§bCo-op > §r§a[RANK§6+§a] %prefix%%name%§f: §rTEST§r"
        }));
        return "base." + getKey();
    }

    @Override
    public boolean isDisyllable() {
        return false;
    }
}
