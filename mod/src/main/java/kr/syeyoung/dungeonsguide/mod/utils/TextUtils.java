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

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Pattern;

public class TextUtils {
    private static final Pattern STRIP_COLOR_PATTERN = Pattern.compile("(?i)§[0-9A-FK-OR]");

    private static final Pattern SCOREBOARD_CHARACTERS = Pattern.compile("[^a-z A-Z:0-9/'.]");

    private static final Pattern INTEGER_CHARACTERS = Pattern.compile("[^0-9]");

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

    public static String formatByte(long bytes) {
        // generate if elses. show up to 2 decimal places to PB
        if (bytes < 1024) return bytes+" B";
        else if (bytes < 1024L * 1024) return String.format("%.2f", bytes / 1024.0) + " KB";
        else if (bytes < 1024L * 1024 * 1024) return String.format("%.2f", bytes / (1024.0 * 1024)) + " MB";
        else if (bytes < 1024L * 1024 * 1024 * 1024) return String.format("%.2f", bytes / (1024.0 * 1024 * 1024)) + " GB";
        return String.format("%.2f", bytes / (1024.0 * 1024 * 1024 * 1024)) + " TB";
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

    public static String getNearestFormattedText(Component component) {
        return LegacyComponentSerializer.legacySection().serialize(component);
    }

    public static boolean compareString(String a, String b) {
        if (a.equals(b)) return true;

        if (a.replace("§r", "").equals(b.replace("§r", ""))) {
            System.out.println("MISMATCH!! equals: Origin: '"+a+"' Found: '"+b+"'");
            Thread.dumpStack();
            return true;
        }
        return false;
    }

    public static boolean startsWith(String a, String b) {
        if (a.startsWith(b)) return true;

        if (a.replace("§r", "").startsWith(b.replace("§r", ""))) {
            System.out.println("MISMATCH!! startsWith: Origin: '"+a+"' Found: '"+b+"'");
            Thread.dumpStack();
            return true;
        }
        return false;
    }

    public static boolean contains(String a, String b) {
        if (a.contains(b)) return true;

        if (a.replace("§r", "").contains(b.replace("§r", ""))) {
            System.out.println("MISMATCH!! startsWith: Origin: '"+a+"' Found: '"+b+"'");
            Thread.dumpStack();
            return true;
        }
        return false;
    }

    public static boolean isInteger(String s) {
        if (s == null || s.isEmpty()) return false;
        int i = 0;
        if (s.charAt(0) == '-' || s.charAt(0) == '+') {
            if (s.length() == 1) return false;
            i = 1;
        }
        for (; i < s.length(); i++) {
            if (!Character.isDigit(s.charAt(i))) return false;
        }
        return true;
    }

    public static String toHex(byte[] bytes) {
        char[] hexArray = "0123456789abcdef".toCharArray();
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    public static String toString(InputStream input) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(input))) {
            char[] buffer = new char[4096];
            int n;
            while ((n = reader.read(buffer)) != -1) {
                sb.append(buffer, 0, n);
            }
        }
        return sb.toString();
    }
}