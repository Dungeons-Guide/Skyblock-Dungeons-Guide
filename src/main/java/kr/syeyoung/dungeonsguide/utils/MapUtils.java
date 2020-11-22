package kr.syeyoung.dungeonsguide.utils;

import net.minecraft.block.material.MapColor;
import org.w3c.dom.css.Rect;

import javax.vecmath.Vector2d;
import java.awt.*;

public class MapUtils {
    public static byte getMapColorAt(byte[] colors, int x, int y) {
        if (y <0 || y>= 128 || x < 0 || x >= 128) return 0;
        return colors[y * 128 +x];
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
                if (getMapColorAt(colors, x ,y) == color) return new Point(x,y);
            }
        }
        return null;
    }

    public static Point findFirstColorWithInNegate(byte[] colors, byte color, Rectangle dimension) {
        for (int y = dimension.y; y < (dimension.y + dimension.height);y++) {
            for (int x = dimension.x; x < (dimension.x + dimension.width); x ++) {
                if (getMapColorAt(colors, x ,y) != color) return new Point(x,y);
            }
        }
        return null;
    }

    public static int getWidthOfColorAt(byte[] colors, byte color, Point point) {
        for (int x = point.x; x < 128; x++) {
            if (getMapColorAt(colors, x, point.y) != color) return x - point.x;
        }
        return 128 - point.x;
    }
    public static int getHeightOfColorAt(byte[] colors, byte color, Point point) {
        for (int y = point.y; y < 128; y++) {
            if (getMapColorAt(colors, point.x,y) != color) return y - point.y;
        }
        return 128 - point.y;
    }

    public static int getLengthOfColorExtending(byte[] colors, byte color, Point basePoint, Vector2d vector2d) {
        for (int i = 0; i < 128; i++) {
            int x = (int) (basePoint.x + vector2d.x * i);
            int y = (int) (basePoint.y + vector2d.y * i);
            if (getMapColorAt(colors, x,y) != color) return i;
        }
        return -1;
    }

}
