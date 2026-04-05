package kr.syeyoung.modapi.util;

public enum EnumDyeColor
{
    WHITE,
    ORANGE,
    MAGENTA,
    LIGHT_BLUE,
    YELLOW,
    LIME,
    PINK,
    GRAY,
    SILVER,
    CYAN,
    PURPLE,
    BLUE,
    BROWN,
    GREEN,
    RED,
    BLACK;

    public static final EnumDyeColor[] VALUES = new EnumDyeColor[16];

    static {
        for (EnumDyeColor value : values()) {
            VALUES[value.ordinal()] = value;
        }
    }
}
