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

package kr.syeyoung.dungeonsguide.mod.overlay;

import kr.syeyoung.dungeonsguide.mod.config.types.GUIRectangle;
import kr.syeyoung.dungeonsguide.mod.guiv2.DomElement;
import kr.syeyoung.dungeonsguide.mod.guiv2.primitive.ConstraintBox;
import kr.syeyoung.dungeonsguide.mod.guiv2.primitive.Rect;
import kr.syeyoung.dungeonsguide.mod.guiv2.primitive.Size;
import lombok.RequiredArgsConstructor;
import net.minecraft.client.Minecraft;

import java.awt.*;
import java.util.function.Supplier;

@RequiredArgsConstructor
public class GUIRectanglePositioner implements Positioner {
    public final Supplier<GUIRectangle> rectSupplier;

    @Override
    public Rect position(DomElement domElement, double screenWidth, double screenHeight) {
        GUIRectangle posSize = rectSupplier.get();

        double x = posSize.getX(), y = posSize.getY();
        x = x * screenWidth / Minecraft.getMinecraft().displayWidth;
        y = y * screenHeight / Minecraft.getMinecraft().displayHeight;
        if (Math.abs(x) > screenWidth / 2) {
            x = x < 0 ? -screenWidth/2 : screenWidth/2;
        }
        if (Math.abs(y) > screenHeight/ 2) {
            y = y < 0 ? -screenHeight/2 : screenHeight/2;
        }



        double realX = (int) (x < 0 ? screenWidth + x : x);
        double realY = (int) (y < 0 ? screenHeight + y : y);

        Size size = domElement.getLayouter().layout(domElement,
                new ConstraintBox(Math.abs(posSize.getWidth()),
                        Math.abs(posSize.getWidth()),
                        Math.abs(posSize.getHeight()),
                        Math.abs(posSize.getHeight())));
        return new Rect(Math.min(realX + posSize.getWidth(), realX), Math.min(realY + posSize.getHeight(), realY),
                Math.abs(posSize.getWidth()), Math.abs(posSize.getHeight()));
    }
}
