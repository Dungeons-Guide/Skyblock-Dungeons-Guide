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

package kr.syeyoung.dungeonsguide.mod.utils;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentContents;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.network.chat.Style;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemLore;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.regex.Pattern;

public class TextUtils {
    private static final Pattern STRIP_COLOR_PATTERN = Pattern.compile("(?i)§[0-9A-FK-OR]");

    private static final Pattern SCOREBOARD_CHARACTERS = Pattern.compile("[^a-z A-Z:0-9/'.]");

    private static final Pattern INTEGER_CHARACTERS = Pattern.compile("[^0-9]");

    // a copy that shares no mutable component with the original (Component.copy() shares the siblings). D106
    public static MutableComponent copyComponent(Component component) {
        ComponentContents contents = component.getContents();
        if (contents instanceof TranslatableContents translatable) {
            Object[] args = translatable.getArgs().clone();
            for (int i = 0; i < args.length; i++) {
                if (args[i] instanceof Component arg) args[i] = copyComponent(arg);
            }
            contents = new TranslatableContents(translatable.getKey(), translatable.getFallback(), args);
        }
        MutableComponent copy = MutableComponent.create(contents).setStyle(component.getStyle());
        for (Component sibling : component.getSiblings()) {
            copy.append(copyComponent(sibling));
        }
        return copy;
    }

    // TODO: D5 stopgap (D125): 1.8.9's display.Lore strings, one legacy string per lore line, empty if none
    public static List<String> getFormattedLore(ItemStack itemStack) {
        ItemLore lore = itemStack.get(DataComponents.LORE);
        if (lore == null) return Collections.emptyList();
        List<String> lines = new ArrayList<>();
        for (Component line : lore.lines()) {
            lines.add(getFormattedText(line));
        }
        return lines;
    }

    // TODO: D5 stopgap (D125): 1.8.9's ItemStack.getDisplayName() as a legacy string
    public static String getFormattedName(ItemStack itemStack) {
        return getFormattedText(itemStack.getHoverName());
    }

    // TODO: D5 stopgap until chat reading moves to styled component matching.
    // what 1.8.9's IChatComponent.getFormattedText() returned: for every component, its style's codes, its text, then §r
    public static String getFormattedText(Component component) {
        StringBuilder sb = new StringBuilder();
        appendFormattedText(sb, component, Style.EMPTY);
        return sb.toString();
    }

    private static void appendFormattedText(StringBuilder sb, Component component, Style parentStyle) {
        Style style = component.getStyle().applyTo(parentStyle);
        sb.append(getFormattingCode(style));
        component.getContents().visit(text -> {
            sb.append(text);
            return Optional.empty();
        });
        sb.append(ChatFormatting.RESET);
        for (Component sibling : component.getSiblings()) {
            appendFormattedText(sb, sibling, style);
        }
    }

    // 1.8.9's ChatStyle.getFormattingCode order
    private static String getFormattingCode(Style style) {
        StringBuilder sb = new StringBuilder();
        if (style.getColor() != null) {
            ChatFormatting color = ChatFormatting.getByName(style.getColor().serialize());
            if (color != null) sb.append(color);
        }
        if (style.isBold()) sb.append(ChatFormatting.BOLD);
        if (style.isItalic()) sb.append(ChatFormatting.ITALIC);
        if (style.isUnderlined()) sb.append(ChatFormatting.UNDERLINE);
        if (style.isObfuscated()) sb.append(ChatFormatting.OBFUSCATED);
        if (style.isStrikethrough()) sb.append(ChatFormatting.STRIKETHROUGH);
        return sb.toString();
    }

    public static String stripColor(String input) {
        return STRIP_COLOR_PATTERN.matcher(input).replaceAll("");
    }

    public static String keepScoreboardCharacters(String text) {
        return SCOREBOARD_CHARACTERS.matcher(text).replaceAll("");
    }

    public static String keepIntegerCharactersOnly(String text) {
        return INTEGER_CHARACTERS.matcher(text).replaceAll("");
    }

    public static String join(List list, String delimiter) {
        if (list.isEmpty()) return "";
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < list.size() - 1; i++) {
            stringBuilder.append(list.get(i).toString()).append(delimiter);
        }
        stringBuilder.append(list.get(list.size() - 1).toString());
        return stringBuilder.toString();
    }


    private static final TreeMap<Long, String> suffixes = new TreeMap<Long, String>();

    static {
        suffixes.put(1000L, "k");
        suffixes.put(1000000L, "m");
        suffixes.put(1000000000L, "b");
    }

    public static String format(long value) {
//        return String.valueOf(value);
        if (value == Long.MIN_VALUE)
            return format(-9223372036854775807L);
        if (value < 0L)
            return "-" + format(-value);
        if (value < 1000L)
            return Long.toString(value);
        Map.Entry<Long, String> e = suffixes.floorEntry(value);
        Long divideBy = e.getKey();
        String suffix = e.getValue();
        long truncated = value * 10 / divideBy ;
        boolean hasDecimal = (truncated < 100L && (truncated / 10.0D) != (truncated / 10L));
        return hasDecimal ? ((truncated / 10.0D) + suffix) : ((truncated / 10L) + suffix);
    }
    public static long reverseFormat(String str2) {
        String str = str2.toLowerCase();
        if (str.length() == 0) return 0;
        String integerPart = str.substring(0, str.length() - 1);
        long multiplier = 1;
        if (str.endsWith("k")) multiplier = 1000;
        else if (str.endsWith("m")) multiplier = 1000000;
        else if (str.endsWith("b")) multiplier = 1000000000;
        else integerPart = str;
        return (long) (Double.parseDouble(integerPart) * multiplier);
    }

    public static String formatTime(long ms) {
        long seconds = (long) Math.ceil(ms / 1000.0);
        long hr = seconds / (60 * 60); seconds -= hr * 60 * 60;
        long min = seconds / 60; seconds -= min * 60;

        StringBuilder stringBuilder = new StringBuilder();
        if (hr > 0) {
            stringBuilder.append(hr).append("h ");
        }
        if (hr > 0 || min > 0) {
            stringBuilder.append(min).append("m ");
        }
        if (hr > 0 || min > 0 || seconds > 0) {
            stringBuilder.append(seconds).append("s ");
        }

        return stringBuilder.toString();
    }
    public static String insertDashUUID(String uuid) {
        StringBuilder sb = new StringBuilder(uuid);
        sb.insert(8, "-");
        sb = new StringBuilder(sb.toString());
        sb.insert(13, "-");
        sb = new StringBuilder(sb.toString());
        sb.insert(18, "-");
        sb = new StringBuilder(sb.toString());
        sb.insert(23, "-");

        return sb.toString();
    }

}