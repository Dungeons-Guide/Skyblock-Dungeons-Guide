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

package kr.syeyoung.dungeonsguide.cosmetics.chatreplacers;

import kr.syeyoung.dungeonsguide.cosmetics.ActiveCosmetic;
import kr.syeyoung.dungeonsguide.cosmetics.CosmeticData;
import kr.syeyoung.dungeonsguide.cosmetics.CosmeticsManager;
import kr.syeyoung.dungeonsguide.cosmetics.IChatReplacer;
import kr.syeyoung.dungeonsguide.utils.TextUtils;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.Tuple;
import net.minecraftforge.client.event.ClientChatReceivedEvent;

import java.util.ArrayList;
import java.util.List;

public class ChatReplacerCoop implements IChatReplacer {
    @Override
    public boolean isAcceptable(ClientChatReceivedEvent event) {
        for (IChatComponent sibling : event.message.getSiblings()) {
            if (sibling.getUnformattedTextForChat().startsWith("§bCo-op > ")) return true;
        }
        return false;
    }

    @Override
    public void translate(ClientChatReceivedEvent event, CosmeticsManager cosmeticsManager) {
        List<Tuple<IChatComponent, IChatComponent>> replaceMents = new ArrayList<>();
        List<IChatComponent> iChatComponents = new ArrayList<>( event.message.getSiblings() );
        IChatComponent sibling = iChatComponents.get(0); iChatComponents.remove(sibling);


        String[] splitInto = sibling.getUnformattedTextForChat().split(" ");
        int lastValidNickname = -1;
        int lastprefix = -1;
        for (int i = 0; i < splitInto.length; i++) {
            String s = TextUtils.stripColor(splitInto[i]);
            char c = s.charAt(0);
            if (('a' <= c && c <= 'z') || ('A' <= c && c <= 'Z') || ('0' <= c && c <= '9') || c == '_' || c == '-') {
                lastValidNickname = i;
            }
        }
        if (lastValidNickname == -1) return;

        if (lastValidNickname -1 >= 0 && TextUtils.stripColor(splitInto[lastValidNickname - 1]).charAt(0) == '[') lastprefix = lastValidNickname -1;
        else lastprefix = lastValidNickname;


        List<ActiveCosmetic> cDatas = cosmeticsManager.getActiveCosmeticByPlayerNameLowerCase().get(TextUtils.stripColor(splitInto[lastValidNickname].substring(0, splitInto[lastValidNickname].length()-1)).toLowerCase());

        if (cDatas == null) return;

            CosmeticData color = null, prefix = null;
            for (ActiveCosmetic activeCosmetic : cDatas) {
                CosmeticData cosmeticData = cosmeticsManager.getCosmeticDataMap().get(activeCosmetic.getCosmeticData());
                if (cosmeticData != null && cosmeticData.getCosmeticType().equals("color")) {
                    color = cosmeticData;
                } else if (cosmeticData != null && cosmeticData.getCosmeticType().equals("prefix")) {
                    prefix = cosmeticData;
                }
            }

        String building = "";
        for (int i = 0; i < lastprefix; i++) {
            building += splitInto[i] +" ";
        }
        if (prefix != null) building += prefix.getData().replace("&", "§") + " ";
        for (int i = lastprefix; i < lastValidNickname; i++) {
            building += splitInto[i] +" ";
        }
        if (color != null) {
            String nick = splitInto[lastValidNickname];
            building += color.getData().replace("&","§");
            boolean foundLegitChar = false;
            boolean foundColor = false;
            for (char c : nick.toCharArray()) {
                if (foundColor) {
                    foundColor = false; continue;
                }
                if (c == '§' && !foundLegitChar) foundColor = true;
                else {
                    foundLegitChar = true;
                    building += c;
                }
            }
            building += " ";
        } else {
            building += splitInto[lastValidNickname] + " ";
        }
        for (int i = lastValidNickname+1; i<splitInto.length; i++) {
            building += splitInto[i] + " ";
        }
        if (sibling.getUnformattedTextForChat().charAt(sibling.getUnformattedText().length()-1) != ' ')
            building = building.substring(0, building.length() - 1);

        ChatComponentText chatComponents1 = new ChatComponentText(building);
        chatComponents1.setChatStyle(sibling.getChatStyle());
        event.message.getSiblings().clear();
        event.message.getSiblings().add(chatComponents1);
        event.message.getSiblings().addAll(iChatComponents);
    }
}
