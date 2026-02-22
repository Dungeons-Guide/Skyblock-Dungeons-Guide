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

package kr.syeyoung.modapi.rendering;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TextStyleConfig {
    @Builder.Default
    private double size = 8.0;

    @Builder.Default
    private double topAscent = 0.0;

    @Builder.Default
    private double bottomAscent = 0.0;

    @Builder.Default
    private boolean bold = false;

    @Builder.Default
    private boolean italic = false;

    @Builder.Default
    private boolean strikethrough = false;

    @Builder.Default
    private boolean underline = false;

    @Builder.Default
    private boolean shadow = false;

    @Builder.Default
    private int textColor = 0xFFFFFFFF;

    @Builder.Default
    private int shadowColor = 0xFF3F3F3F;

    @Builder.Default
    private int strikethroughColor = 0xFFFFFFFF;

    @Builder.Default
    private int underlineColor = 0xFFFFFFFF;
}

