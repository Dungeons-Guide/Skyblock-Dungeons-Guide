package kr.syeyoung.dungeonsguide.utils;

import java.text.DecimalFormat;
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

    public static String join(List list, String delimeter) {
        if (list.isEmpty()) return "";
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < list.size() - 1; i++) {
            stringBuilder.append(list.get(i).toString()).append(delimeter);
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
        String integerPart = str.substring(0, str.length() - 1);
        long multiplier = 1;
        if (str.endsWith("k")) multiplier = 1000;
        else if (str.endsWith("m")) multiplier = 1000000;
        else if (str.endsWith("b")) multiplier = 1000000000;
        else integerPart = str;
        return (long) (Double.parseDouble(integerPart) * multiplier);
    }

    public static String formatTime(long ms) {
        long seconds = ms / 1000;
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

}