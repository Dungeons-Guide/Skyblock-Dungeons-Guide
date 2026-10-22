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

package kr.syeyoung.dungeonsguide.mod.cosmetics;

import kr.syeyoung.dungeonsguide.mod.DungeonsGuide;
import kr.syeyoung.dungeonsguide.mod.features.FeatureRegistry;
import kr.syeyoung.dungeonsguide.mod.parallelUniverse.tab.TabList;
import kr.syeyoung.dungeonsguide.mod.player.PlayerManager;
import kr.syeyoung.dungeonsguide.mod.utils.TextUtils;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.UUID;

// Reimplement this using ASM. Removing reference to this is too painful.
// 1.8.9 extended NetworkPlayerInfo and replaced vanilla's entries; on 26.1 PlayerTabOverlayMixin passes the tab name vanilla built
public class CustomNetworkPlayerInfo {
    // 1.8.9's displayName was the packet's display name, formatted its text; vanilla's tab name covers both
    public static Component getDisplayName(Component displayName)
    {
        String rawPlayerString = TextUtils.getFormattedText(displayName);

        String actualName = null;
        List<ActiveCosmetic> activeCosmetics;
        for (String s : rawPlayerString.split(" ")) {
            String strippped = TextUtils.stripColor(s);
            if (strippped.startsWith("[")) continue;
            actualName = strippped;
            break;
        }

        if (actualName == null) return displayName;

        // 1.8.9's nameIdCache, the parallel universe tab list
        UUID uuid = TabList.INSTANCE.getPlayer(actualName);
        boolean dg = FeatureRegistry.DG_INDICATOR.isEnabled() && PlayerManager.INSTANCE.getOnlineStatus().getOrDefault(uuid, false);


        activeCosmetics = DungeonsGuide.getDungeonsGuide().getCosmeticsManager().getActiveCosmeticByPlayerNameLowerCase().get(actualName.toLowerCase());
        if (activeCosmetics == null && dg) return Component.literal("\ued00" + rawPlayerString);
        else if (activeCosmetics == null) return displayName;

        CosmeticData color=null;
        for (ActiveCosmetic activeCosmetic : activeCosmetics) {
            CosmeticData cosmeticData = DungeonsGuide.getDungeonsGuide().getCosmeticsManager().getCosmeticDataMap().get(activeCosmetic.getCosmeticData());
            if (cosmeticData == null) continue;
            if (cosmeticData.getCosmeticType().equals("ncolor")) color = cosmeticData;
        }

//        FontRenderer
        if (color != null) { // ᨠ
            String coloredName = color.getData() + actualName;
            if (dg) {
                return Component.literal("\ued00" + rawPlayerString.replace(actualName, coloredName));
            } else {
                return Component.literal(rawPlayerString.replace(actualName, coloredName));
            }
        } else {
            if (dg) {
                return Component.literal("\ued00" + rawPlayerString);
            } else {
                return displayName;
            }
        }
    }
}
