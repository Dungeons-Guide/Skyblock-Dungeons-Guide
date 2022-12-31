/*
 * Dungeons Guide - The most intelligent Hypixel Skyblock Dungeons Mod
 * Copyright (C) 2022  cyoung06 (syeyoung)
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

package kr.syeyoung.dungeonsguide.mod.guiv2;

import lombok.AllArgsConstructor;
import lombok.Getter;


// Idea heavily taken from flutter.
@AllArgsConstructor @Getter
public class ConstraintBox {
    private int minWidth;
    private int maxWidth;
    private int minHeight;
    private int maxHeight;

    public static ConstraintBox loose(int width, int height) {
        return new ConstraintBox(0,width,0,height);
    }

    public static ConstraintBox tight(int width, int height) {
        return new ConstraintBox(width, width, height, height);
    }
}
