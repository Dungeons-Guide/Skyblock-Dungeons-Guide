package kr.syeyoung.dungeonsguide.utils;

import net.minecraft.block.material.MapColor;
import org.w3c.dom.css.Rect;

import javax.vecmath.Vector2d;
import java.awt.*;
import java.awt.image.BufferedImage;

public class MapUtils {

    private static Color[] colorMasks = new Color[128 * 128];
    private static byte[] colors;

    public static void clearMap() {
        colorMasks = new Color[128 * 128];
        colors = null;
    }

    public static void record(byte[] colors, int x, int y, Color c) {
        MapUtils.colors = colors;
        colorMasks[y *128 +x] = new Color(255, 255, 255, 50);
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
            theColor = MapColor.mapColorArray[j / 4].func_151643_b(j & 3);
        }

        return theColor;
    }

    public static Point findFirstColorWithIn(byte[] colors, byte color, Rectangle dimension) {
        for (int y = dimension.y; y < (dimension.y + dimension.height);y++) {
            for (int x = dimension.x; x < (dimension.x + dimension.width); x ++) {
                if (getMapColorAt(colors, x ,y) == color) {
                    record(colors, x, y, new Color(255, 0, 0, 40));
                    return new Point(x,y);
                }
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

}
