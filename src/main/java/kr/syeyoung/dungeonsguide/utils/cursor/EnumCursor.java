/*
 * Dungeons Guide - The most intelligent Hypixel Skyblock Dungeons Mod
 * Copyright (C) 2021  cyoung06
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

package kr.syeyoung.dungeonsguide.utils.cursor;

import lombok.AllArgsConstructor;
import lombok.Getter;


@AllArgsConstructor
@Getter
public enum EnumCursor {
    DEFAULT(32512,68,"arrowCursor","arrowCursor"),
    POINTING_HAND(32649,60,"pointingHandCursor", "pointingHandCursor"),
    OPEN_HAND(32649,60,"openHandCursor","openHandCursor"),
    CLOSED_HAND(32646,52,"closedHandCursor","closedHandCursor"),
    BEAM_CURSOR(32513, 152, "IBeamCursor", "IBeamCursor"),
    RESIZE_LEFT(32644, 70,"resizeLeftCursor", "resizeLeftCursor"),
    RESIZE_RIGHT(32644, 96,"resizeRightCursor", "resizeRightCursor"),
    RESIZE_LEFT_RIGHT(32644, 108, "resizeLeftRightCursor", "resizeLeftRightCursor"),
    RESIZE_UP(32645,138,"resizeUpCursor", "resizeUpCursor"),
    RESIZE_DOWN(32645,16,"resizeDownCursor", "resizeDownCursor"),
    RESIZE_UP_DOWN(32645,116,"resizeUpDownCursor", "resizeUpDownCursor"),
    CROSS(32515, 34,"crosshairCursor", "crosshairCursor");


    private int windows;
    private int linux;
    private String macos;
    private String altFileName;
}
