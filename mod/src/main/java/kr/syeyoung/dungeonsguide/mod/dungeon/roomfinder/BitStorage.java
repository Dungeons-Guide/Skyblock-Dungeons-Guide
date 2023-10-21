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

package kr.syeyoung.dungeonsguide.mod.dungeon.roomfinder;

public class BitStorage {
    private long[] arr;
    private final int dataPerLong;
    private int xLen, yLen, zLen;

    private final int mask;
    private final int bitsPer;
    public BitStorage(int xLen, int yLen, int zLen, int bitsPer) {
        dataPerLong = (int) Math.floor(64.0 / bitsPer);
        this.bitsPer = bitsPer;
        mask = (1 << bitsPer) - 1;
        arr = new long[xLen * yLen * zLen / dataPerLong + 1]; // plus 1 , because I don't wanna do floating point op for dividing and ceiling

        this.xLen = xLen;
        this.yLen = yLen;
        this.zLen = zLen;
    }

    public int getMask() {
        return mask;
    }

    public boolean store(int x, int y, int z, int data) {
        int bitIdx = x * yLen * zLen + y * zLen + z;
        int location = bitIdx / dataPerLong;
        int bitStart = (bitsPer * (bitIdx % dataPerLong));
        int orig = read(x,y,z);
        if (orig == data) return false;
        long theBit = arr[location];
        theBit &= ~((long) mask << bitStart);
        theBit |= (long) (data & mask) << bitStart;
        arr[location] = theBit;

        return true;
    }

    public int read(int x, int y, int z) {
        int bitIdx = x * yLen * zLen + y * zLen + z;
        int location = bitIdx / dataPerLong;
        int bitStart = (bitsPer * (bitIdx % dataPerLong));
        long theBit = arr[location];

        return (int) ((theBit >>> bitStart) & mask);
    }
}
