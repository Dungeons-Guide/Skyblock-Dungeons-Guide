/*
 *     Dungeons Guide - The most intelligent Hypixel Skyblock Dungeons Mod
 *     Copyright (C) 2021  cyoung06
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Affero General Public License as published
 *     by the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Affero General Public License for more details.
 *
 *     You should have received a copy of the GNU Affero General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package kr.syeyoung.dungeonsguide.utils;

import lombok.Getter;
import net.minecraft.block.material.MapColor;
import org.w3c.dom.css.Rect;

import javax.vecmath.Vector2d;
import java.awt.*;
import java.awt.image.BufferedImage;

public class MapUtils {

    private static Color[] colorMasks = new Color[128 * 128];
    @Getter
    private static byte[] colors;

    public static void clearMap() {
        colorMasks = new Color[128 * 128];
        colors = null;
    }

    public static void record(byte[] colors, int x, int y, Color c) {
        MapUtils.colors = colors;
        colorMasks[y *128 +x] = c;
    }

    public static byte getMapColorAt(byte[] colors, int x, int y) {
        if (y <0 || y>= 128 || x < 0 || x >= 128) return 0;
        return colors[y * 128 +x];
    }

    public static BufferedImage getImage() {
        BufferedImage bufferedImage = new BufferedImage(128, 128,BufferedImage.TYPE_INT_ARGB);
        if (colors == null) return bufferedImage;
        Graphics graphics = bufferedImage.getGraphics();
        for (int y = 0; y < 128; y++) {
            for (int x = 0; x <128; x++) {
                bufferedImage.setRGB(x,y, getRGBColorAt(colors, x, y));
                if (colorMasks[y * 128 + x] != null) {
                    graphics.setColor(colorMasks[y *128 +x]);
                    graphics.drawLine(x,y,x,y);
                }
            }
        }
        return bufferedImage;
    }

    public static int getRGBColorAt(byte[] colors, int x, int y) {
        if (y <0 || y>= 128 || x < 0 || x >= 128) return 0;
        int i = y * 128 +x;
        int j = colors[i] & 255;

        int theColor;
        if (j / 4 == 0)
        {
            theColor = (i + i / 128 & 1) * 8 + 16 << 24;
        }
        else
        {
            theColor = MapColor.mapColorArray[j / 4].getMapColor(j & 3);
        }

        return theColor;
    }

    public static Point findFirstColorWithIn(byte[] colors, byte color, Rectangle dimension) {
        boolean found = true;
        for (int y = dimension.y; y < (dimension.y + dimension.height);y++) {
            for (int x = dimension.x; x < (dimension.x + dimension.width); x ++) {
                if (getMapColorAt(colors, x ,y) == color && found) {
                    record(colors, x, y, new Color(255, 0, 0, 40));
                    return new Point(x,y);
                }
                found = getMapColorAt(colors, x,y) == 0;
            }
        }
        return null;
    }

    public static Point findFirstColorWithInNegate(byte[] colors, byte color, Rectangle dimension) {
        for (int y = dimension.y; y < (dimension.y + dimension.height);y++) {
            for (int x = dimension.x; x < (dimension.x + dimension.width); x ++) {
                if (getMapColorAt(colors, x ,y) != color) {
                    record(colors, x, y, new Color(255, 0, 0, 40));
                    return new Point(x,y);
                }
            }
        }
        return null;
    }

    public static int getWidthOfColorAt(byte[] colors, byte color, Point point) {
        for (int x = point.x; x < 128; x++) {
            record(colors, x, point.y, new Color(0, 255, 0, 40));
            if (getMapColorAt(colors, x, point.y) != color) return x - point.x;
        }
        return 128 - point.x;
    }
    public static int getHeightOfColorAt(byte[] colors, byte color, Point point) {
        for (int y = point.y; y < 128; y++) {
            record(colors, point.x, y, new Color(0, 255, 0, 40));
            if (getMapColorAt(colors, point.x,y) != color) return y - point.y;
        }
        return 128 - point.y;
    }

    public static int getLengthOfColorExtending(byte[] colors, byte color, Point basePoint, Vector2d vector2d) {
        for (int i = 0; i < 128; i++) {
            int x = (int) (basePoint.x + vector2d.x * i);
            int y = (int) (basePoint.y + vector2d.y * i);
            record(colors, x, y, new Color(0, 0, 255, 40));
            if (getMapColorAt(colors, x,y) != color) return i;
        }
        return -1;
    }


    public static boolean matches(byte[] colors, byte[] stencil, int targetColor, int x, int y) {
        for (int i = y; i < y + stencil.length; i++) {
            for (int j = x; j < x + 8; j++) {
                boolean current = getMapColorAt(colors, j, i) == targetColor;
                boolean expected = ((stencil[i - y] >> (7-(j-x))) & 0x1) == 1;
                if (current != expected) return false;
            }
        }
        return true;
    }

    public static int readDigit(byte[] colors, int x, int y) {
        for (int i = 0; i < NUMBER_STENCIL.length; i++) {
            if (matches(colors, NUMBER_STENCIL[i],34, x, y)) return i;
        }
        return -1;
    }
    public static int readNumber(byte[] colors, int x, int y, int gap) {
        int number = 0;
        for (int i = x; i < 128; i += gap) {
            int digit = readDigit(colors, i, y);
            if (digit != -1) number = number * 10 + digit;
        }
        return number;
    }

    private static final byte[][] NUMBER_STENCIL = {
            {0x0, 0x7F, 0x7F, 0x77, 0x77, 0x77, 0x77, 0x77, 0x77, 0x77, 0x77, 0x7F, 0x7F, 0x0},
            {0x0, 0x1E, 0x1E, 0x1E, 0xE, 0xE, 0xE, 0xE, 0xE, 0xE, 0xE, 0xE, 0xE, 0x0},
            {0x0, 0x7F, 0x7F, 0x7F, 0x77, 0x77, 0x7, 0x7F, 0x7F, 0x70, 0x7F, 0x7F, 0x7F, 0x0},
            {0x0, 0x7F, 0x7F, 0x7F, 0x7, 0x7, 0x1F, 0x1F, 0x7, 0x7, 0x7F, 0x7F, 0x7F, 0x0},
            {0x0, 0x6E, 0x6E, 0x6E, 0x6E, 0x6E, 0x6E, 0x7F, 0x7F, 0x7F, 0xE, 0xE, 0xE, 0x0},
            {0x0, 0x7F, 0x7F, 0x7F, 0x70, 0x7F, 0x7F, 0x7, 0x77, 0x77, 0x7F, 0x7F, 0x7F, 0x0},
            {0x0, 0x7F, 0x7F, 0x7F, 0x70, 0x7F, 0x7F, 0x77, 0x77, 0x77, 0x77, 0x7F, 0x7F, 0x0},
            {0x0, 0x7F, 0x7F, 0x7F, 0x7, 0x7, 0x7, 0x7, 0x7, 0x7, 0x7, 0x7, 0x7, 0x0},
            {0x0, 0x7F, 0x7F, 0x77, 0x77, 0x77, 0x3E, 0x77, 0x77, 0x77, 0x77, 0x7F, 0x7F, 0x0},
            {0x0, 0x7F, 0x7F, 0x77, 0x77, 0x77, 0x77, 0x7F, 0x7F, 0x7,  0x7F, 0x7F, 0x7F, 0x0}
    };



}
