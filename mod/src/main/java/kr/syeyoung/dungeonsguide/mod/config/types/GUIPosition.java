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

package kr.syeyoung.dungeonsguide.mod.config.types;

import kr.syeyoung.dungeonsguide.mod.guiv2.primitive.Rect;
import kr.syeyoung.dungeonsguide.mod.guiv2.primitive.Size;
import lombok.*;
import net.minecraft.client.Minecraft;

@Data @AllArgsConstructor @NoArgsConstructor
public class GUIPosition {
    private OffsetType xType;
    private double xOffset;
    private OffsetType yType;
    private double yOffset;

    private Double width;
    private Double height;

    @Getter @RequiredArgsConstructor
    public enum OffsetType {
        START, CENTER, END
    }

    public static double intersectArea(Rect a, Rect b)
    {
        double x = Math.max(a.getX(), b.getX());
        double num1 = Math.min(a.getX() + a.getWidth(), b.getX() + b.getWidth());
        double y = Math.max(a.getY(), b.getY());
        double num2 = Math.min(a.getY() + a.getHeight(), b.getY() + b.getHeight());
        if (num1 >= x && num2 >= y)
            return (num1 - x) * (num2 - y);
        else
            return 0;
    }
    public static GUIPosition of(Rect widgetLoc, double screenWidth, double screenHeight) {
        double triWidth = screenWidth / 3;
        double triHeight = screenHeight / 3;

        OffsetType xOffset;
        {
            double left = intersectArea(widgetLoc, new Rect(0, 0, triWidth, screenHeight));
            double center = intersectArea(widgetLoc, new Rect(triWidth, 0, triWidth, screenHeight));
            double right = intersectArea(widgetLoc, new Rect(screenWidth - triWidth, 0, triWidth, screenHeight));
            if (left >= center && left >= right) xOffset = OffsetType.START;
            else if (center >= right && center >= left) xOffset = OffsetType.CENTER;
            else xOffset = OffsetType.END;
        }
        OffsetType yOffset;
        {
            double top = intersectArea(widgetLoc, new Rect(0, 0, screenWidth, triHeight));
            double center = intersectArea(widgetLoc, new Rect(0, triHeight, screenWidth, triHeight));
            double bottom = intersectArea(widgetLoc, new Rect(0, screenHeight - triHeight, screenWidth, triHeight));
            if (top >= center && top >= bottom) yOffset = OffsetType.START;
            else if (center >= bottom && center >= top) yOffset = OffsetType.CENTER;
            else yOffset = OffsetType.END;
        }

        GUIPosition guiPosition = new GUIPosition();
        guiPosition.xType = xOffset;
        guiPosition.yType = yOffset;

        if (guiPosition.xType == OffsetType.START) {
            guiPosition.xOffset = widgetLoc.getX();
        } else if (guiPosition.xType == OffsetType.CENTER) {
            guiPosition.xOffset = widgetLoc.getX() + (widgetLoc.getWidth() - screenWidth)/2;
        } else {
            guiPosition.xOffset = widgetLoc.getX() + widgetLoc.getWidth() - screenWidth;
        }

        if (guiPosition.yType == OffsetType.START) {
            guiPosition.yOffset = widgetLoc.getY();
        } else if (guiPosition.yType == OffsetType.CENTER) {
            guiPosition.yOffset = widgetLoc.getY() + (widgetLoc.getHeight() - screenHeight)/2;
        } else  {
            guiPosition.yOffset = widgetLoc.getY() + widgetLoc.getHeight() - screenHeight;
        }
        double realScreenWidth = Minecraft.getMinecraft().displayWidth;
        double realScreenHeight = Minecraft.getMinecraft().displayHeight;

        guiPosition.xOffset = guiPosition.getXOffset() * realScreenWidth / screenWidth;
        guiPosition.yOffset = guiPosition.getYOffset() * realScreenHeight / screenHeight;

        return guiPosition;
    }

    public Rect position(double screenWidth0, double screenHeight0, Size widgetSize) {
        double screenWidth = Minecraft.getMinecraft().displayWidth;
        double screenHeight = Minecraft.getMinecraft().displayHeight;
        double x = 0;
        double xOff = xOffset * screenWidth0 / screenWidth;
        double yOff = yOffset * screenHeight0 / screenHeight;

        if (xType != OffsetType.CENTER && Math.abs(xOff) > screenWidth0/3) xOff = xOff > 0 ? screenWidth0/3 : -screenWidth0/3;
        if (xType == OffsetType.CENTER && Math.abs(xOff) > screenWidth0 /6) xOff = xOff > 0 ? screenWidth0 / 6 : -screenWidth/6;
        if (yType != OffsetType.CENTER && Math.abs(yOff) > screenHeight0/3) yOff = yOff > 0 ? screenHeight0/3 : -screenHeight0/3;
        if (yType == OffsetType.CENTER && Math.abs(yOff) > screenHeight0/6) yOff = yOff > 0 ? screenHeight0/6 : -screenHeight0/6;


        if (xType == OffsetType.START) {
            x = xOff;
        } else if (xType == OffsetType.CENTER) {
            x = xOff + (screenWidth0 - widgetSize.getWidth())/2;
        } else if (xType == OffsetType.END) {
            x = xOff + screenWidth0 - widgetSize.getWidth();
        }
        double y = 0;

        if (yType == OffsetType.START) {
            y = yOff;
        } else if (yType == OffsetType.CENTER) {
            y = yOff + (screenHeight0 - widgetSize.getHeight())/2;
        } else if (yType == OffsetType.END) {
            y = yOff + screenHeight0 - widgetSize.getHeight();
        }


        return new Rect(x, y, widgetSize.getWidth(), widgetSize.getHeight());
    }

    @Override
    public GUIPosition clone()  {
        return new GUIPosition(this.getXType(), this.getXOffset(), this.getYType(), this.getYOffset(), this.getWidth(), this.getHeight());
    }
}
