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

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Pointer;

public interface User32 extends Library {

    public static User32 INSTANCE = (User32) Native
            .loadLibrary("User32", User32.class);

    /** @see #LoadCursorW(Pointer, int) */
    public static final int IDC_ARROW = 32512;
    /** @see #LoadCursorW(Pointer, int) */
    public static final int IDC_IBEAM = 32513;
    /** @see #LoadCursorW(Pointer, int) */
    public static final int IDC_WAIT = 32514;
    /** @see #LoadCursorW(Pointer, int) */
    public static final int IDC_CROSS = 32515;
    /** @see #LoadCursorW(Pointer, int) */
    public static final int IDC_UPARROW = 32516;
    /** @see #LoadCursorW(Pointer, int) */
    public static final int IDC_SIZENWSE = 32642;
    /** @see #LoadCursorW(Pointer, int) */
    public static final int IDC_SIZENESW = 32643;
    /** @see #LoadCursorW(Pointer, int) */
    public static final int IDC_SIZEWE = 32644;
    /** @see #LoadCursorW(Pointer, int) */
    public static final int IDC_SIZENS = 32645;
    /** @see #LoadCursorW(Pointer, int) */
    public static final int IDC_SIZEALL = 32646;
    /** @see #LoadCursorW(Pointer, int) */
    public static final int IDC_NO = 32648;
    /** @see #LoadCursorW(Pointer, int) */
    public static final int IDC_HAND = 32649;
    /** @see #LoadCursorW(Pointer, int) */
    public static final int IDC_APPSTARTING = 32650;
    /** @see #LoadCursorW(Pointer, int) */
    public static final int IDC_HELP = 32651;
    /** @see #LoadCursorW(Pointer, int) */
    public static final int IDC_ICON = 32641;
    /** @see #LoadCursorW(Pointer, int) */
    public static final int IDC_SIZE = 32640;

    /** http://msdn.microsoft.com/en-us/library/ms648391(VS.85).aspx */
    public Pointer LoadCursorW(Pointer hInstance,
                                 int lpCursorName);

}
