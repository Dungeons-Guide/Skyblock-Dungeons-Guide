package kr.syeyoung.dungeonsguide.utils;

import java.text.DecimalFormat;
import java.util.regex.Pattern;

public class TextUtils {
    private static final Pattern STRIP_COLOR_PATTERN = Pattern.compile("(?i)§[0-9A-FK-OR]");

    private static final Pattern NUMBERS_SLASHES = Pattern.compile("[^0-9 /]");

    private static final Pattern SCOREBOARD_CHARACTERS = Pattern.compile("[^a-z A-Z:0-9/'.]");

    private static final Pattern FLOAT_CHARACTERS = Pattern.compile("[^.0-9\\-]");

    private static final Pattern INTEGER_CHARACTERS = Pattern.compile("[^0-9]");

    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#,###.##");

    public static String formatDouble(double number) {
        return DECIMAL_FORMAT.format(number);
    }

    public static String stripColor(String input) {
        return STRIP_COLOR_PATTERN.matcher(input).replaceAll("");
    }

    public static String keepScoreboardCharacters(String text) {
        return SCOREBOARD_CHARACTERS.matcher(text).replaceAll("");
    }

    public static String keepFloatCharactersOnly(String text) {
        return FLOAT_CHARACTERS.matcher(text).replaceAll("");
    }

    public static String keepIntegerCharactersOnly(String text) {
        return INTEGER_CHARACTERS.matcher(text).replaceAll("");
    }

    public static String getNumbersOnly(String text) {
        return NUMBERS_SLASHES.matcher(text).replaceAll("");
    }

    public static String removeDuplicateSpaces(String text) {
        return text.replaceAll("\\s+", " ");
    }

    public static String reverseText(String originalText) {
        StringBuilder newString = new StringBuilder();
        String[] parts = originalText.split(" ");
        for (int i = parts.length; i > 0; i--) {
            String textPart = parts[i - 1];
            boolean foundCharacter = false;
            for (char letter : textPart.toCharArray()) {
                if (letter > ')') {
                foundCharacter = true;
                newString.append((new StringBuilder(textPart)).reverse().toString());
                break;
            }
        }
        newString.append(" ");
        if (!foundCharacter)
            newString.insert(0, textPart);
        newString.insert(0, " ");
    }
    return removeDuplicateSpaces(newString.toString().trim());
}

    public static String getOrdinalSuffix(int n) {
        if (n >= 11 && n <= 13)
            return "th";
        switch (n % 10) {
            case 1:
                return "st";
            case 2:
                return "nd";
            case 3:
                return "rd";
        }
        return "th";
    }
}