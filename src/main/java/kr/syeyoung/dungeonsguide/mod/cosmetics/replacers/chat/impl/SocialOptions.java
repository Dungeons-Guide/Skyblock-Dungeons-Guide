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

package kr.syeyoung.dungeonsguide.mod.cosmetics.replacers.chat.impl;

import kr.syeyoung.dungeonsguide.mod.cosmetics.CosmeticsManager;
import kr.syeyoung.dungeonsguide.mod.cosmetics.data.ActiveCosmetic;
import kr.syeyoung.dungeonsguide.mod.cosmetics.data.CosmeticData;
import kr.syeyoung.dungeonsguide.mod.cosmetics.replacers.chat.IChatReplacer;
import kr.syeyoung.dungeonsguide.mod.cosmetics.replacers.chat.ReplacerUtil;
import kr.syeyoung.dungeonsguide.mod.utils.TextUtils;
import net.minecraft.util.ChatComponentText;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SocialOptions implements IChatReplacer {
    @Nullable
    static CosmeticData getPrefix(@Nullable List<ActiveCosmetic> activeCosmeticsList, @NotNull CosmeticsManager cosmeticsManager) {
        if (activeCosmeticsList == null) return null;

        for (ActiveCosmetic activeCosmetic : activeCosmeticsList) {
            CosmeticData cosmeticData = cosmeticsManager.getCosmeticDataMap().get(activeCosmetic.getCosmeticData());
            if (cosmeticData != null && cosmeticData.getCosmeticType().equals("prefix")) {
                return cosmeticData;
            }
        }
        return null;
    }

    @Nullable
    static CosmeticData getColor(@Nullable List<ActiveCosmetic> activeCosmeticsList, @NotNull CosmeticsManager cosmeticsManager) {
        if (activeCosmeticsList == null) return null;

        for (ActiveCosmetic activeCosmetic : activeCosmeticsList) {
            CosmeticData cosmeticData = cosmeticsManager.getCosmeticDataMap().get(activeCosmetic.getCosmeticData());
            if (cosmeticData != null && cosmeticData.getCosmeticType().equals("color")) {
                return cosmeticData;
            }
        }
        return null;
    }

    static List<ActiveCosmetic> getActiveCosmeticsFromUsername(String username, CosmeticsManager cosmeticsManager) {
        username = username.toLowerCase();
        return cosmeticsManager.getActiveCosmeticByPlayerNameLowerCase().get(username);
    }

    static void yes(ClientChatReceivedEvent event, CosmeticsManager cosmeticsManager) {

        List<ActiveCosmetic> activeCosmeticList = getActiveCosmeticsFromUsername(event.message.getChatStyle().getChatClickEvent().getValue().split(" ")[1], cosmeticsManager);

        CosmeticData color = getColor(activeCosmeticList, cosmeticsManager);
        CosmeticData prefix = getPrefix(activeCosmeticList, cosmeticsManager);

        String building = transformWithCosmetics(event.message.getUnformattedTextForChat(), color, prefix);

        if (building != null) {
            ChatComponentText newChatCompText = new ChatComponentText(building);
            newChatCompText.setChatStyle(event.message.getChatStyle());
            newChatCompText.getSiblings().addAll(event.message.getSiblings());

            event.message = newChatCompText;
        }

    }

    /**
     * STEPS:
     * <ol>
     *     <li>split into x parts                       <br>`§8[§720§8]`  `§a[VIP]`   `Kokoniara§f`</li>
     *     <li>if prefix append                         <br>`tester` `§8[§720§8]`  `§a[VIP]`   `Kokoniara§f`</li>
     *     <li>if color, reset color of last element    <br>`tester` `§8[§720§8]`  `§a[VIP]`   `Kokoniara`</li>
     *     <li>add our color and reset color at end     <br>`tester` `§8[§720§8]`  `§a[VIP]`   `§eKokoniara§f`</li>
     *     <li>remerge                                  <br>`tester §8[§720§8] §a[VIP] §eKokoniara§f`</li>
     * </ol>
     *
     * @param raw    input to modify
     * @param color  color cosmetic data
     * @param prefix prefix cosmetic data
     * @return transformed string that contains cosmetics
     */
    @Nullable
    private static String transformWithCosmetics(String raw, CosmeticData color, CosmeticData prefix) {
        // step 1
        ArrayList<String> split = new ArrayList<>(Arrays.asList(raw.split(" ")));

        // step 2
        if (prefix != null) {
            String prefixString = prefix.getData();
            split.add(0, prefixString.replace("&", "§"));
        }

        // step 3
        if (color != null) {
            String last = split.get(split.size() - 1);
            split.remove(split.size() - 1);

            last = TextUtils.stripColor(last);


            // step 4
            String colorCode = color.getData().replace("&", "§");

            last = colorCode + last + "§r";

            split.add(last);
        }

        // step 5
        StringBuilder formated = new StringBuilder();
        for (int i = 0; i < split.size(); i++) {
            String s = split.get(i);

            s = s.trim();

            if (i == split.size() - 1) {
                formated.append(s);
            } else {
                formated.append(s).append(" ");
            }
        }

        return formated.toString();
    }

    @Override
    public boolean isApplyable(ClientChatReceivedEvent event) {
        return ReplacerUtil.isNotNullAndDoesStartWith(event.message, "/socialoptions");
    }

    @Override
    public void transformIntoCosmeticsForm(ClientChatReceivedEvent event, CosmeticsManager cosmeticsManager) {
        yes(event, cosmeticsManager);
    }


}
