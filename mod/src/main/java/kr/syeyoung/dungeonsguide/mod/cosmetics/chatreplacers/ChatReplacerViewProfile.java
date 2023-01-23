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

package kr.syeyoung.dungeonsguide.mod.cosmetics.chatreplacers;

import kr.syeyoung.dungeonsguide.mod.cosmetics.ActiveCosmetic;
import kr.syeyoung.dungeonsguide.mod.cosmetics.CosmeticData;
import kr.syeyoung.dungeonsguide.mod.cosmetics.CosmeticsManager;
import kr.syeyoung.dungeonsguide.mod.cosmetics.IChatReplacer;
import kr.syeyoung.dungeonsguide.mod.utils.TextUtils;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.Tuple;
import net.minecraftforge.client.event.ClientChatReceivedEvent;

import java.util.ArrayList;
import java.util.List;

public class ChatReplacerViewProfile implements IChatReplacer {
    @Override
    public boolean isAcceptable(ClientChatReceivedEvent event) {
        for (IChatComponent sibling : event.message.getSiblings()) {
            if (sibling.getChatStyle() != null && sibling.getChatStyle().getChatClickEvent() != null && sibling.getChatStyle().getChatClickEvent().getValue().startsWith("/viewprofile ")) return true;
        }
        return false;
    }

    @Override
    public void translate(ClientChatReceivedEvent event, CosmeticsManager cosmeticsManager) {
        List<Tuple<IChatComponent, IChatComponent>> replacements = new ArrayList<>();
        for (IChatComponent sibling : event.message.getSiblings()) {
            if (sibling.getChatStyle() != null && sibling.getChatStyle().getChatClickEvent() != null && sibling.getChatStyle().getChatClickEvent().getValue().startsWith("/viewprofile ")) {
                String uid = sibling.getChatStyle().getChatClickEvent().getValue().split(" ")[1];
                // TODO: make cosmeticsManager handle usernames instead of uuids
                // apparently now hypixel's /viewprofile command gives the nickname
//                List<ActiveCosmetic> cData = cosmeticsManager.getActiveCosmeticByPlayer().get(UUID.fromString(uid));
                List<ActiveCosmetic> cData = null;

                if (cData != null) {
                    CosmeticData color=null, prefix=null;
                    for (ActiveCosmetic activeCosmetic : cData) {
                        CosmeticData cosmeticData = cosmeticsManager.getCosmeticDataMap().get(activeCosmetic.getCosmeticData());
                        if (cosmeticData !=null && cosmeticData.getCosmeticType().equals("color")) {
                            color = cosmeticData;
                        } else if (cosmeticData != null && cosmeticData.getCosmeticType().equals("prefix")) {
                            prefix = cosmeticData;
                        }
                    }

                    String[] splitInto = sibling.getUnformattedTextForChat().split(" ");
                    int lastValidNickname = -1;
                    int lastPrefix = -1;
                    for (int i = 0; i < splitInto.length; i++) {
                        String s = TextUtils.stripColor(splitInto[i]);
                        char c = s.charAt(0);
                        if (('a' <= c && c <= 'z') || ('A' <= c && c <= 'Z') || ('0' <= c && c <= '9') || c == '_' || c == '-') {
                            lastValidNickname = i;
                            if (i >= 1) {
                                String str = TextUtils.stripColor(splitInto[i-1]);
                                if (str.startsWith("[") && str.endsWith("]"))break;
                            }
                        }
                    }
                    if (lastValidNickname == -1) continue;

                    if (lastValidNickname -1 >= 0 && TextUtils.stripColor(splitInto[lastValidNickname - 1]).charAt(0) == '[') lastPrefix = lastValidNickname -1;
                    else lastPrefix = lastValidNickname;

                    String building = "";
                    for (int i = 0; i < lastPrefix; i++) {
                        building += splitInto[i] +" ";
                    }
                    if (prefix != null) building += prefix.getData().replace("&", "§") + " ";
                    for (int i = lastPrefix; i < lastValidNickname; i++) {
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

                    ChatComponentText newChatCompText = new ChatComponentText(building);
                    newChatCompText.setChatStyle(sibling.getChatStyle());
                    replacements.add(new Tuple<>(sibling, newChatCompText));
                    break;
                }
            }
        }

        for (Tuple<IChatComponent, IChatComponent> replacement : replacements) {
            int index = event.message.getSiblings().indexOf(replacement.getFirst());
            event.message.getSiblings().set(index, replacement.getSecond());
        }
    }
}
