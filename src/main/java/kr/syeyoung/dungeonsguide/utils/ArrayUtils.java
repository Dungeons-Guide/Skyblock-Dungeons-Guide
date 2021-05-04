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

package kr.syeyoung.dungeonsguide.utils;

public class ArrayUtils {
    public static int[][] rotateCounterClockwise(int[][] arr) {
        int[][] res = new int[arr[0].length][arr.length];
        for(int y=0; y<arr.length; y++) {
            for (int x = 0; x< arr[0].length; x++) {
                res[res.length - x - 1][y] = arr[y][x];
            }
        }
        return res;
    }
    public static int[][] rotateClockwise(int[][] arr) {
        int[][] res = new int[arr[0].length][arr.length];
        for(int y=0; y<arr.length; y++) {
            for (int x = 0; x< arr[0].length; x++) {
                res[x][res[0].length - y - 1] = arr[y][x];
            }
        }
        return res;
    }
}
