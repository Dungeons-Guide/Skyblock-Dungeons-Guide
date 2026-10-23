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


import com.mojang.blaze3d.platform.cursor.CursorType;
import com.mojang.blaze3d.platform.cursor.CursorTypes;
import org.lwjgl.glfw.GLFW;

import java.util.*;


// TODO: native cursor integration (User32 / X11 / XCursor / Foundation, .cur fallback) comes later. on 26.1 every cursor is a glfw standard cursor
public class GLCursors {

    private static Map<EnumCursor, CursorType> enumCursorCursorMap = new HashMap<>();


    // needs glfw, runs on CLIENT_STARTED
    public static void setupCursors() {
        if (enumCursorCursorMap.size() != 0) return;
        CursorType resizeNWSE = CursorType.createStandardCursor(GLFW.GLFW_RESIZE_NWSE_CURSOR, "resize_nwse", CursorType.DEFAULT);
        CursorType resizeNESW = CursorType.createStandardCursor(GLFW.GLFW_RESIZE_NESW_CURSOR, "resize_nesw", CursorType.DEFAULT);
        for (EnumCursor value : EnumCursor.values()) {
            CursorType c;
            switch (value) {
                case POINTING_HAND:
                    c = CursorTypes.POINTING_HAND; break;
                case OPEN_HAND:
                case CLOSED_HAND:
                    c = CursorTypes.RESIZE_ALL; break;
                case BEAM_CURSOR:
                    c = CursorTypes.IBEAM; break;
                case MOVE_BAR_LEFT:
                case MOVE_BAR_RIGHT:
                case MOVE_BAR_LEFT_RIGHT:
                case RESIZE_LEFT:
                case RESIZE_RIGHT:
                case RESIZE_LEFT_RIGHT:
                    c = CursorTypes.RESIZE_EW; break;
                case MOVE_BAR_UP:
                case MOVE_BAR_DOWN:
                case MOVE_BAR_UP_DOWN:
                case RESIZE_UP:
                case RESIZE_DOWN:
                case RESIZE_UP_DOWN:
                    c = CursorTypes.RESIZE_NS; break;
                case RESIZE_TL:
                case RESIZE_DR:
                case RESIZE_TLDR:
                    c = resizeNWSE; break;
                case RESIZE_TR:
                case RESIZE_DL:
                case RESIZE_TRDL:
                    c = resizeNESW; break;
                case CROSS:
                    c = CursorTypes.CROSSHAIR; break;
                case NOT_ALLOWED:
                    c = CursorTypes.NOT_ALLOWED; break;
                case DEFAULT:
                case TEST:
                default:
                    c = CursorTypes.ARROW; break;
            }
            enumCursorCursorMap.put(value, c);
        }
    }

    public static CursorType getCursor(EnumCursor enumCursor) {
        return enumCursorCursorMap.get(enumCursor);
    }
}
