/*
 * Dungeons Guide - The most intelligent Hypixel Skyblock Dungeons Mod
 * Copyright (C) 2023  cyoung06 (syeyoung)
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

package kr.syeyoung.dungeonsguide.mod.cosmetics.surgical;

import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;

import java.util.*;

public class SurgicalReplacer {

    public static List<ChatComponentText> getChatStyleOf(String str, ChatStyle parentStyle) {
        boolean randomStyle = parentStyle.getObfuscated();
        boolean boldStyle = parentStyle.getBold();
        boolean strikethroughStyle = parentStyle.getStrikethrough();
        boolean underlineStyle = parentStyle.getUnderlined();
        boolean italicStyle = parentStyle.getItalic();
        char possibleLastColorChar = parentStyle.getColor() == null ? 'f' : "0123456789abcdefklmnor".charAt(parentStyle.getColor().getColorIndex());
        boolean isLegalColor = true;
        char[] charArr = str.toCharArray();

        List<ChatComponentText> list = new LinkedList<>();

        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < charArr.length; i++) {
            char c0 = charArr[i];
            if (c0 == 167 && i + 1 < charArr.length) {
                if (stringBuilder.length() != 0) {
                    if (isLegalColor) {
                        ChatComponentText chatComponents = new ChatComponentText(
                                stringBuilder.toString());
                        chatComponents.setChatStyle(new ChatStyle()
                                .setBold(boldStyle)
                                .setObfuscated(randomStyle)
                                .setStrikethrough(strikethroughStyle)
                                .setUnderlined(underlineStyle)
                                .setItalic(italicStyle)
                                .setColor(EnumChatFormatting.func_175744_a("0123456789abcdefklmnor".indexOf(possibleLastColorChar)))
                                .setChatHoverEvent(parentStyle.getChatHoverEvent())
                                .setChatClickEvent(parentStyle.getChatClickEvent()));
                        list.add(chatComponents);
                        stringBuilder = new StringBuilder();
                    } else {
                        ChatComponentText chatComponents = new ChatComponentText(
                                "§"+possibleLastColorChar+
                                        (randomStyle ? "§k" : "")+
                                        (boldStyle ? "§l" : "")+
                                        (italicStyle ? "§o" : "")+
                                        (underlineStyle ? "§n" : "")+
                                        (strikethroughStyle ? "§m" : "") +stringBuilder.toString());
                        chatComponents.setChatStyle(new ChatStyle()
                                .setBold(boldStyle)
                                .setObfuscated(randomStyle)
                                .setStrikethrough(strikethroughStyle)
                                .setUnderlined(underlineStyle)
                                .setItalic(italicStyle)
                                .setColor(EnumChatFormatting.WHITE)
                                .setChatHoverEvent(parentStyle.getChatHoverEvent())
                                .setChatClickEvent(parentStyle.getChatClickEvent()));
                        list.add(chatComponents);
                        stringBuilder = new StringBuilder();
                    }
                }
                int i1 = "0123456789abcdefklmnor".indexOf(Character.toLowerCase(charArr[i + 1]));
                if (i1 < 16) {
                    randomStyle = false;
                    boldStyle = false;
                    strikethroughStyle = false;
                    underlineStyle = false;
                    italicStyle = false;
                    if (i1 >= 0) {
                        possibleLastColorChar = "0123456789abcdef".charAt(i1);
                        isLegalColor = true;
                    } else {
                        possibleLastColorChar = charArr[i+1];
                        isLegalColor = false;
                    }
                } else if (i1 == 16) {
                    randomStyle = true;
                } else if (i1 == 17) {
                    boldStyle = true;
                } else if (i1 == 18) {
                    strikethroughStyle = true;
                } else if (i1 == 19) {
                    underlineStyle = true;
                } else if (i1 == 20) {
                    italicStyle = true;
                } else {
                    randomStyle = false;
                    boldStyle = false;
                    strikethroughStyle = false;
                    underlineStyle = false;
                    italicStyle = false;
                    possibleLastColorChar = 'f';
                }

                ++i;
            } else {
                stringBuilder.append(c0);
            }
        }
        if (isLegalColor) {
            ChatComponentText chatComponents = new ChatComponentText(
                    stringBuilder.toString());
            chatComponents.setChatStyle(new ChatStyle()
                    .setBold(boldStyle)
                    .setObfuscated(randomStyle)
                    .setStrikethrough(strikethroughStyle)
                    .setUnderlined(underlineStyle)
                    .setItalic(italicStyle)
                    .setColor(EnumChatFormatting.func_175744_a("0123456789abcdefklmnor".indexOf(possibleLastColorChar)))
                    .setChatHoverEvent(parentStyle.getChatHoverEvent())
                    .setChatClickEvent(parentStyle.getChatClickEvent()));
            list.add(chatComponents);
        } else {
            ChatComponentText chatComponents = new ChatComponentText(
                    "§"+possibleLastColorChar+
                            (randomStyle ? "§k" : "")+
                            (boldStyle ? "§b" : "")+
                            (italicStyle ? "§o" : "")+
                            (underlineStyle ? "§n" : "")+
                            (strikethroughStyle ? "§m" : "") +stringBuilder.toString());
            chatComponents.setChatStyle(new ChatStyle()
                    .setBold(boldStyle)
                    .setObfuscated(randomStyle)
                    .setStrikethrough(strikethroughStyle)
                    .setUnderlined(underlineStyle)
                    .setItalic(italicStyle)
                    .setColor(EnumChatFormatting.WHITE)
                    .setChatHoverEvent(parentStyle.getChatHoverEvent())
                    .setChatClickEvent(parentStyle.getChatClickEvent()));
            list.add(chatComponents);
        }
        return list;
    }

    public static LinkedList<IChatComponent> linearifyMoveColorCharToStyle(IChatComponent iChatComponent) {
        LinkedList<IChatComponent> chatComponents = new LinkedList<>();
        for (IChatComponent component : iChatComponent) {
            if (component instanceof ChatComponentText) {
                chatComponents.addAll(getChatStyleOf(((ChatComponentText) component).getChatComponentText_TextValue(), component.getChatStyle().createDeepCopy()));
            } else {
                IChatComponent neuCopy = component.createCopy();
                neuCopy.getSiblings().clear();
                chatComponents.add(neuCopy.setChatStyle(component.getChatStyle().createDeepCopy()));
            }
        }
        return chatComponents;
    }

    public static ChatStyle getChatStyleAt(List<IChatComponent> chatComponents, int idx) {
        int i = 0;
        for (IChatComponent chatComponent : chatComponents) {
            i += chatComponent.getUnformattedTextForChat().length();
            if (i > idx) return chatComponent.getChatStyle().createDeepCopy();
        }
        return null;
    }
    public static LinkedList<IChatComponent> inject(LinkedList<IChatComponent> linearified, List<IChatComponent> toInjectLinearified, int idx, int len) {
        LinkedList<IChatComponent> clone = new LinkedList<>();
        int currLen = 0;
        boolean injected = false;
        while (!linearified.isEmpty()) {
            if (currLen == idx && !injected) {
                clone.addAll(toInjectLinearified);
                injected = true;
            }

            IChatComponent toProcess = linearified.poll();
            int procLen = toProcess.getUnformattedTextForChat().length();

            if (currLen + procLen <= idx) {
                clone.add(toProcess);
                currLen += procLen;
                continue;
            }

            if (currLen + procLen > idx && currLen < idx) {
                ChatComponentText chatComponents = new ChatComponentText(
                        toProcess.getUnformattedTextForChat().substring(0, idx - currLen)
                );
                chatComponents.setChatStyle(toProcess.getChatStyle());

                clone.add(chatComponents);

                ChatComponentText next = new ChatComponentText(
                        toProcess.getUnformattedTextForChat().substring(idx-currLen)
                );
                next.setChatStyle(toProcess.getChatStyle());
                linearified.addFirst(next);

                currLen = idx;
                continue;
            }

            if (currLen + procLen <= idx + len) {
                currLen += procLen;
                continue;
            }
            if (currLen + procLen > idx + len && currLen < idx + len) {
                ChatComponentText next = new ChatComponentText(
                        toProcess.getUnformattedTextForChat().substring(idx + len - currLen)
                );
                next.setChatStyle(toProcess.getChatStyle());
                clone.add(next);
                currLen += procLen;
                continue;
            }
            clone.add(toProcess);
            currLen += procLen;
        }
        return clone;
    }
    public static IChatComponent combine(List<IChatComponent> components) {
        ChatComponentText chatComponents =  new ChatComponentText("");
        chatComponents.getSiblings().addAll(components);
        return chatComponents;
    }
}
