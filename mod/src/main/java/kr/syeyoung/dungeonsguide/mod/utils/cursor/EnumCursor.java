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
    DEFAULT(32512,68,"default", "arrowCursor","arrow_cursor.cur"),
    POINTING_HAND(32649,60,"pointer","pointingHandCursor", "pointing_hand_cursor.cur"),
    OPEN_HAND(32646,58,"openhand","openHandCursor","open_hand_cursor.cur"),
    CLOSED_HAND(32646,52,"closedhand","closedHandCursor","closed_hand_cursor.cur"),
    BEAM_CURSOR(32513, 152, "text","IBeamCursor", "i_beam_cursor.cur"),

    MOVE_BAR_LEFT(32644, 70,"w-resize", "resizeLeftCursor", "resize_left_cursor.cur"),
    MOVE_BAR_RIGHT(32644, 96,"e-resize","resizeRightCursor", "resize_right_cursor.cur"),
    MOVE_BAR_LEFT_RIGHT(32644, 108, "col-resize", "resizeLeftRightCursor", "resize_left_right_cursor.cur"),
    MOVE_BAR_UP(32645,138,"n-resize", "resizeUpCursor", "resize_up_cursor.cur"),
    MOVE_BAR_DOWN(32645,16,"s-resize", "resizeDownCursor", "resize_down_cursor.cur"),
    MOVE_BAR_UP_DOWN(32645,116,"row-resize", "resizeUpDownCursor", "resize_up_down_cursor.cur"),

    RESIZE_LEFT(32644, 70,"w-resize", "_windowResizeWestCursor", "resize_left_cursor.cur"),
    RESIZE_RIGHT(32644, 96,"e-resize","_windowResizeEastCursor", "resize_right_cursor.cur"),
    RESIZE_LEFT_RIGHT(32644, 108, "col-resize","_windowResizeEastWestCursor", "resize_left_right_cursor.cur"),
    RESIZE_UP(32645,138,"n-resize","_windowResizeNorthCursor", "resize_up_cursor.cur"),
    RESIZE_DOWN(32645,16,"s-resize","_windowResizeSouthCursor", "resize_down_cursor.cur"),
    RESIZE_UP_DOWN(32645,116,"row-resize", "_windowResizeNorthSouthCursor", "resize_up_down_cursor.cur"),


    RESIZE_TL(32642, 134, "nw-resize", "_windowResizeNorthWestCursor", "resize_nw.cur"),
    RESIZE_DR(32642, 14, "sw-resize", "_windowResizeSouthEastCursor", "resize_se.cur"),
    RESIZE_TLDR(32642, 14, "nesw-resize", "_windowResizeNorthWestSouthEastCursor", "resize_nwse.cur"),
    RESIZE_TR(32643, 136, "ne-resize", "_windowResizeNorthEastCursor", "resize_ne.cur"),
    RESIZE_DL(32643, 12, "se-resize", "_windowResizeSouthWestCursor", "resize_sw.cur"),
    RESIZE_TRDL(32643, 12, "nwse-resize","_windowResizeNorthEastSouthWestCursor", "resize_nesw.cur"),
    CROSS(32515, 34,"crosshair","crosshairCursor", "crosshair_cursor.cur"),
    NOT_ALLOWED(32648, -1,"not-allowed","operationNotAllowedCursor", "operation_not_allowed_cursor.cur"),
    TEST(-1, -1, null, null, "testnonexistant.cur");


    private int windows;
    private int linux;
    private String xcursor;
    private String macos;
    private String altFileName;
}
