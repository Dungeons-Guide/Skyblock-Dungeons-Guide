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

public class ShortUtils {
    public static short rotateCounterClockwise(short integer) {
        int res = 0;
        for(int i=0; i<16; i++){
            int x = i % 4;
            int y = i / 4;
            res |= (integer >> i & 0x1) << ((4-x-1)*4 + y);
        }
        return (short) (res & 0xFFFF);
    }
    public static short rotateClockwise(short integer) {
        int res = 0;
        for(int i=0; i<16; i++){
            int x = i % 4;
            int y = i / 4;
            res |= (integer >> i & 0x1) << (x *4 +(4 - y - 1));
        }
        return (short) (res & 0xFFFF);
    }

    public static short topLeftifyInt(short integer) {
        int it = integer & 0xFFFF;
        while ((it & (0x1111)) == 0) it >>= 1;
        while ((it & (0xF)) == 0) it >>= 4;
        return (short) (it & 0xFFFF);
    }
}
