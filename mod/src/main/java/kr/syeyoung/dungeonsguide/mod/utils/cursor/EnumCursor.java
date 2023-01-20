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

package kr.syeyoung.dungeonsguide.mod.utils.cursor;

import lombok.AllArgsConstructor;
import lombok.Getter;


@AllArgsConstructor
@Getter
public enum EnumCursor {
    /** there are these hidden cursors for macos
     * [NSCursor _windowResizeEastCursor]
     * [NSCursor _windowResizeWestCursor]
     * [NSCursor _windowResizeEastWestCursor]
     * [NSCursor _windowResizeNorthCursor]
     * [NSCursor _windowResizeSouthCursor]
     * [NSCursor _windowResizeNorthSouthCursor]
     * [NSCursor _windowResizeNorthEastCursor]
     * [NSCursor _windowResizeNorthWestCursor]
     * [NSCursor _windowResizeSouthEastCursor]
     * [NSCursor _windowResizeSouthWestCursor]
     * [NSCursor _windowResizeNorthEastSouthWestCursor]
     * [NSCursor _windowResizeNorthWestSouthEastCursor]
     * https://stackoverflow.com/questions/10733228/native-osx-lion-resize-cursor-for-custom-nswindow-or-nsview
     */
    DEFAULT(32512,68,"arrowCursor","arrowCursor.cur"),
    POINTING_HAND(32649,60,"pointingHandCursor", "pointingHandCursor.cur"),
    OPEN_HAND(32646,58,"openHandCursor","openHandCursor.cur"),
    CLOSED_HAND(32646,52,"closedHandCursor","closedHandCursor.cur"),
    BEAM_CURSOR(32513, 152, "IBeamCursor", "IBeamCursor.cur"),

    MOVE_BAR_LEFT(32644, 70,"resizeLeftCursor", "resizeLeftCursor.cur"),
    MOVE_BAR_RIGHT(32644, 96,"resizeRightCursor", "resizeRightCursor.cur"),
    MOVE_BAR_LEFT_RIGHT(32644, 108, "resizeLeftRightCursor", "resizeLeftRightCursor.cur"),
    MOVE_BAR_UP(32645,138,"resizeUpCursor", "resizeUpCursor.cur"),
    MOVE_BAR_DOWN(32645,16,"resizeDownCursor", "resizeDownCursor.cur"),
    MOVE_BAR_UP_DOWN(32645,116,"resizeUpDownCursor", "resizeUpDownCursor.cur"),

    RESIZE_LEFT(32644, 70,"_windowResizeWestCursor", "resizeLeftCursor.cur"),
    RESIZE_RIGHT(32644, 96,"_windowResizeEastCursor", "resizeRightCursor.cur"),
    RESIZE_LEFT_RIGHT(32644, 108, "_windowResizeEastWestCursor", "resizeLeftRightCursor.cur"),
    RESIZE_UP(32645,138,"_windowResizeNorthCursor", "resizeUpCursor.cur"),
    RESIZE_DOWN(32645,16,"_windowResizeSouthCursor", "resizeDownCursor.cur"),
    RESIZE_UP_DOWN(32645,116,"_windowResizeNorthSouthCursor", "resizeUpDownCursor.cur"),


    RESIZE_TL(32642, 134, "_windowResizeNorthWestCursor", "resizeNW.cur"),
    RESIZE_DR(32642, 14, "_windowResizeSouthEastCursor", "resizeSE.cur"),
    RESIZE_TLDR(32642, 14, "_windowResizeNorthWestSouthEastCursor", "resizeNWSE.cur"),
    RESIZE_TR(32643, 136, "_windowResizeNorthEastCursor", "resizeNE.cur"),
    RESIZE_DL(32643, 12, "_windowResizeSouthWestCursor", "resizeSW.cur"),
    RESIZE_TRDL(32643, 12, "_windowResizeNorthEastSouthWestCursor", "resizeNESW.cur"),
    CROSS(32515, 34,"crosshairCursor", "crosshairCursor.cur"),
    NOT_ALLOWED(32648, -1,"operationNotAllowedCursor", "operationNotAllowedCursor.cur"),
    TEST(-1, -1, null, "testnonexistant.cur");


    private int windows;
    private int linux;
    private String macos;
    private String altFileName;
}
