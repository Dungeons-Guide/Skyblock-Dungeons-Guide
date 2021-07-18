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

package kr.syeyoung.dungeonsguide.config.types;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;

import java.awt.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GUIRectangle {
    public GUIRectangle(Rectangle rectangle) {
        if (rectangle.x < Minecraft.getMinecraft().displayWidth / 2) {
            this.x = rectangle.x;
            this.width = rectangle.width;
        } else {
            this.x = rectangle.x + rectangle.width - Minecraft.getMinecraft().displayWidth;
            this.width = -rectangle.width;
        }

        if (rectangle.y < Minecraft.getMinecraft().displayHeight / 2) {
            this.y = rectangle.y;
            this.height = rectangle.height;
        } else {
            this.y = rectangle.y +rectangle.height - Minecraft.getMinecraft().displayHeight;
            this.height = -rectangle.height;
        }
    }

    private int x;
    private int y;
    private int width;
    private int height;

    public Rectangle getRectangle() {
        return getRectangleNoScale();
    }
//    public Rectangle getRectangle(ScaledResolution scaledResolution) {
//        double realX = (int) (x < 0 ? scaledResolution.getScaledWidth() + x : x);
//        double realY = (int) (y < 0 ? scaledResolution.getScaledHeight() + y : y);
//
//        return new Rectangle((int)Math.min(realX + width, realX), (int)Math.min(realY + height, realY),
//                (int)Math.abs(width), (int)Math.abs(height));
//    }
    public Rectangle getRectangleNoScale() {
        double realX = (int) (x < 0 ? Minecraft.getMinecraft().displayWidth + x : x);
        double realY = (int) (y < 0 ? Minecraft.getMinecraft().displayHeight + y : y);

        return new Rectangle((int)Math.min(realX + width, realX), (int)Math.min(realY + height, realY),
                (int)Math.abs(width), (int)Math.abs(height));
    }
}
