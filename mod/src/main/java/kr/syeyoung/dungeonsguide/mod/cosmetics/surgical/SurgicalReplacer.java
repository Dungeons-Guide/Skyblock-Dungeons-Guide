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

import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import net.minecraft.network.chat.contents.PlainTextContents;

import java.util.*;

public class SurgicalReplacer {

    public static List<MutableComponent> getChatStyleOf(String str, Style parentStyle) {
        boolean randomStyle = parentStyle.isObfuscated();
        boolean boldStyle = parentStyle.isBold();
        boolean strikethroughStyle = parentStyle.isStrikethrough();
        boolean underlineStyle = parentStyle.isUnderlined();
        boolean italicStyle = parentStyle.isItalic();
        char possibleLastColorChar = parentStyle.getColor() == null ? 'f' : legacyColorChar(parentStyle.getColor());
        boolean isLegalColor = true;
        char[] charArr = str.toCharArray();

        List<MutableComponent> list = new LinkedList<>();

        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < charArr.length; i++) {
            char c0 = charArr[i];
            if (c0 == 167 && i + 1 < charArr.length) {
                if (stringBuilder.length() != 0) {
                    if (isLegalColor) {
                        MutableComponent chatComponents = Component.literal(
                                stringBuilder.toString());
                        chatComponents.setStyle(Style.EMPTY
                                .withBold(boldStyle)
                                .withObfuscated(randomStyle)
                                .withStrikethrough(strikethroughStyle)
                                .withUnderlined(underlineStyle)
                                .withItalic(italicStyle)
                                .withColor(ChatFormatting.getById("0123456789abcdefklmnor".indexOf(possibleLastColorChar)))
                                .withHoverEvent(parentStyle.getHoverEvent())
                                .withClickEvent(parentStyle.getClickEvent()));
                        list.add(chatComponents);
                        stringBuilder = new StringBuilder();
                    } else {
                        MutableComponent chatComponents = Component.literal(
                                "§"+possibleLastColorChar+
                                        (randomStyle ? "§k" : "")+
                                        (boldStyle ? "§l" : "")+
                                        (italicStyle ? "§o" : "")+
                                        (underlineStyle ? "§n" : "")+
                                        (strikethroughStyle ? "§m" : "") +stringBuilder.toString());
                        chatComponents.setStyle(Style.EMPTY
                                .withBold(boldStyle)
                                .withObfuscated(randomStyle)
                                .withStrikethrough(strikethroughStyle)
                                .withUnderlined(underlineStyle)
                                .withItalic(italicStyle)
                                .withColor(ChatFormatting.WHITE)
                                .withHoverEvent(parentStyle.getHoverEvent())
                                .withClickEvent(parentStyle.getClickEvent()));
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
            MutableComponent chatComponents = Component.literal(
                    stringBuilder.toString());
            chatComponents.setStyle(Style.EMPTY
                    .withBold(boldStyle)
                    .withObfuscated(randomStyle)
                    .withStrikethrough(strikethroughStyle)
                    .withUnderlined(underlineStyle)
                    .withItalic(italicStyle)
                    .withColor(ChatFormatting.getById("0123456789abcdefklmnor".indexOf(possibleLastColorChar)))
                    .withHoverEvent(parentStyle.getHoverEvent())
                    .withClickEvent(parentStyle.getClickEvent()));
            list.add(chatComponents);
        } else {
            MutableComponent chatComponents = Component.literal(
                    "§"+possibleLastColorChar+
                            (randomStyle ? "§k" : "")+
                            (boldStyle ? "§b" : "")+
                            (italicStyle ? "§o" : "")+
                            (underlineStyle ? "§n" : "")+
                            (strikethroughStyle ? "§m" : "") +stringBuilder.toString());
            chatComponents.setStyle(Style.EMPTY
                    .withBold(boldStyle)
                    .withObfuscated(randomStyle)
                    .withStrikethrough(strikethroughStyle)
                    .withUnderlined(underlineStyle)
                    .withItalic(italicStyle)
                    .withColor(ChatFormatting.WHITE)
                    .withHoverEvent(parentStyle.getHoverEvent())
                    .withClickEvent(parentStyle.getClickEvent()));
            list.add(chatComponents);
        }
        return list;
    }

    // 1.8.9's EnumChatFormatting ordinal into "0123456789abcdef"; 26.1 colors are TextColors
    private static char legacyColorChar(TextColor color) {
        for (ChatFormatting formatting : ChatFormatting.values()) {
            if (formatting.isColor() && color.equals(TextColor.fromLegacyFormat(formatting))) return formatting.getChar();
        }
        return 'f';
    }

    // 1.8.9's IChatComponent iterator: the component itself, then deep copies of its siblings depth first,
    // each with its parent's style applied (1.8.9's setParentStyle)
    public static List<Component> iterate(Component component) {
        List<Component> list = new ArrayList<>();
        list.add(component);
        for (Component sibling : component.getSiblings()) {
            iterate(sibling, component.getStyle(), list);
        }
        return list;
    }

    private static void iterate(Component component, Style parentStyle, List<Component> list) {
        Style style = component.getStyle().applyTo(parentStyle);
        list.add(component.copy().setStyle(style));
        for (Component sibling : component.getSiblings()) {
            iterate(sibling, style, list);
        }
    }

    public static LinkedList<Component> linearifyMoveColorCharToStyle(Component iChatComponent) {
        LinkedList<Component> chatComponents = new LinkedList<>();
        for (Component component : iterate(iChatComponent)) {
            // 1.8.9's ChatComponentText
            if (component.getContents() instanceof PlainTextContents) {
                chatComponents.addAll(getChatStyleOf(((PlainTextContents) component.getContents()).text(), component.getStyle()));
            } else {
                Component neuCopy = MutableComponent.create(component.getContents());
                chatComponents.add(((MutableComponent) neuCopy).setStyle(component.getStyle()));
            }
        }
        return chatComponents;
    }

    public static Style getChatStyleAt(List<Component> chatComponents, int idx) {
        int i = 0;
        for (Component chatComponent : chatComponents) {
            i += chatComponent.getString().length();
            if (i > idx) return chatComponent.getStyle();
        }
        return null;
    }
    public static LinkedList<Component> inject(LinkedList<Component> linearified, List<Component> toInjectLinearified, int idx, int len) {
        LinkedList<Component> clone = new LinkedList<>();
        int currLen = 0;
        boolean injected = false;
        while (!linearified.isEmpty()) {
            if (currLen == idx && !injected) {
                clone.addAll(toInjectLinearified);
                injected = true;
            }

            Component toProcess = linearified.poll();
            int procLen = toProcess.getString().length();

            if (currLen + procLen <= idx) {
                clone.add(toProcess);
                currLen += procLen;
                continue;
            }

            if (currLen + procLen > idx && currLen < idx) {
                MutableComponent chatComponents = Component.literal(
                        toProcess.getString().substring(0, idx - currLen)
                );
                chatComponents.setStyle(toProcess.getStyle());

                clone.add(chatComponents);

                MutableComponent next = Component.literal(
                        toProcess.getString().substring(idx-currLen)
                );
                next.setStyle(toProcess.getStyle());
                linearified.addFirst(next);

                currLen = idx;
                continue;
            }

            if (currLen + procLen <= idx + len) {
                currLen += procLen;
                continue;
            }
            if (currLen + procLen > idx + len && currLen < idx + len) {
                MutableComponent next = Component.literal(
                        toProcess.getString().substring(idx + len - currLen)
                );
                next.setStyle(toProcess.getStyle());
                clone.add(next);
                currLen += procLen;
                continue;
            }
            clone.add(toProcess);
            currLen += procLen;
        }
        return clone;
    }
    public static Component combine(List<Component> components) {
        MutableComponent chatComponents =  Component.literal("");
        chatComponents.getSiblings().addAll(components);
        return chatComponents;
    }
}
