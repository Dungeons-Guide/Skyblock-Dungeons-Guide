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

package kr.syeyoung.dungeonsguide.mod.features.impl.cosmetics;

import kr.syeyoung.dungeonsguide.mod.config.types.TCString;
import kr.syeyoung.dungeonsguide.mod.features.FeatureParameter;
import kr.syeyoung.dungeonsguide.mod.features.SimpleFeature;
import kr.syeyoung.dungeonsguide.mod.features.impl.cosmetics.widget.WidgetNicknamePrefix;
import kr.syeyoung.dungeonsguide.mod.guiv2.Widget;
import kr.syeyoung.dungeonsguide.mod.guiv2.elements.CompatLayer;

public class FeatureNicknamePrefix extends SimpleFeature {
    public FeatureNicknamePrefix() {
        super("Cosmetics", "Nickname Prefix", "Click on Edit to choose prefix cosmetic", "cosmetic.prefix");
        addParameter("dummy", new FeatureParameter("dummy", "dummy", "dummy", "dummy", TCString.INSTANCE));
    }

    @Override
    public Widget getConfigureWidget() {
//        return new CompatLayer(new PrefixSelectorGUI("prefix", new String[] {
//                "§9Party §8> §r%prefix% §a[RANK§6+§a] %name%§f: TEST",
//                "§2Guild > §r%prefix% §a[RANK§6+§a] %name% §3[Vet]§f: TEST",
//                "§dTo §r%prefix% §r§a[RANK§r§6+§r§a] %name%§r§7: §r§7TEST§r",
//                "§dFrom §r%prefix% §r§a[RANK§r§6+§r§a] %name%§r§7: §r§7TEST§r",
//                "§r%prefix% §b[RANK§c+§b] %name%§f: TEST",
//                "§r§bCo-op > §r%prefix% §a[RANK§6+§a] %name%§f: §rTEST§r"
//        }, a->a.replace("&", "§")));
        return new WidgetNicknamePrefix();
    }

    @Override
    public boolean isDisableable() {
        return false;
    }
}
