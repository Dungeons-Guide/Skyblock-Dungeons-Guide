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

        ScaledResolution sr = new ScaledResolution(Minecraft.getMinecraft());
        if (rectangle.x < sr.getScaledWidth() / 2) {
            this.x = rectangle.x;
            this.width = rectangle.width;
        } else {
            this.x = rectangle.x + rectangle.width - sr.getScaledWidth();
            this.width = -rectangle.width;
        }

        if (rectangle.y < sr.getScaledHeight() / 2) {
            this.y = rectangle.y;
            this.height = rectangle.height;
        } else {
            this.y = rectangle.y +rectangle.height - sr.getScaledHeight();
            this.height = -rectangle.height;
        }
    }

    private int x;
    private int y;
    private int width;
    private int height;

    public Rectangle getRectangle() {
        return getRectangle(new ScaledResolution(Minecraft.getMinecraft()));
    }
    public Rectangle getRectangle(ScaledResolution scaledResolution) {
        int realX = x < 0 ? scaledResolution.getScaledWidth() + x : x;
        int realY = y < 0 ? scaledResolution.getScaledHeight() + y : y;

        return new Rectangle(Math.min(realX + width, realX), Math.min(realY + height, realY),
                Math.abs(width), Math.abs(height));
    }
}
