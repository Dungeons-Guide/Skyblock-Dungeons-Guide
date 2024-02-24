/*
 * Dungeons Guide - The most intelligent Hypixel Skyblock Dungeons Mod
 * Copyright (C) 2024  cyoung06 (syeyoung)
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

package kr.syeyoung.dungeonsguide.mod.dungeon.pathfinding;

import net.minecraft.util.EnumFacing;

// 3D SHADOW CASTING!!!
public class ShadowCast {
    public static interface Checker {
        public boolean checkIfBlocked(int x, int y, int z);
        public boolean mark(int x, int y, int z);
    }


    // z is radiating outward.
    // **** (sq)
    // *** (sq)
    // ** (sq)
    // * (sq)
    //c

    public static void realShadowcast(Checker checker, int centerX, int centerY, int centerZ, int radius, double zOffset) {
        shadowcast(checker, centerX, centerY, centerZ, 1,0, 0.5 / (0.5 - zOffset), 0, 0.5 / (0.5 - zOffset), radius, zOffset,
                1 ,0,0,
                0, 1, 0,
                0,0, 1 // let's start testing with identity matrix!
        );
    }

    // Transform matrix is multiplied with stuff to transform coordiantes. Equation is as follows
    //
    // [ 11 21 31 ] [ x ]   [cx]   [trx]
    // [ 12 22 32 ] [ y ] + [cy] = [try]
    // [ 13 23 33 ] [ z ]   [cz]   [trz]
    public static void shadowcast(Checker checker, int centerX, int centerY, int centerZ, int startZ, double startSlopeX, double endSlopeX, double startSlopeY, double endSlopeY, int radius, double zOffset,
                                  int trMatrix11, int trMatrix21, int trMatrix31,
                                  int trMatrix12, int trMatrix22, int trMatrix32,
                                  int trMatrix13, int trMatrix23, int trMatrix33) {
        if (startZ > radius) return;
        // boom. radius is manhatten radius. lol.


        double realZ = startZ - zOffset;
        int sampleStartX =
            for (int y = (int) Math.floor(startSlopeY * (realZ - 0.5)); y <= Math.ceil(endSlopeY * (realZ + 0.5)); y++) {
                double currentSlopeY = y / realZ;
                if (currentSlopeY < startSlopeY || currentSlopeY > endSlopeY) continue;

                boolean globalBlockStatus = false;
                double nextStartSlopeX = startSlopeX;
                double startSlopeYY = Math.max(startSlopeY, (y - 0.5) / (realZ + 0.5));
                double endSlopeYY = Math.min(endSlopeY, (y + 0.5) / (realZ + 0.5));

                for (int x = (int) Math.floor(startSlopeX * (realZ - 0.5)); x < Math.ceil(endSlopeX * (realZ + 0.5)); x++) {
                    double currentSlopeX = x / realZ;
                    if (currentSlopeX < startSlopeX || currentSlopeX > endSlopeX) continue;

                    int trX = centerX + x * trMatrix11 + y * trMatrix21 + startZ * trMatrix31;
                    int trY = centerY + x * trMatrix12 + y * trMatrix22 + startZ * trMatrix32;
                    int trZ = centerZ + x * trMatrix13 + y * trMatrix23 + startZ * trMatrix33;
                    boolean localBlocked =checker.checkIfBlocked(trX, trY, trZ) && startZ != 0;

                    // TRY BINARY SPATIAL PARTITIONING LATER, HERE, I try NATIVE METHOD.
                    if (!localBlocked) {
                        checker.mark(trX, trY, trZ);
                    }
                    if (globalBlockStatus) {
                        // currently blocked..
                        if (!localBlocked) {
                            nextStartSlopeX = (x - 0.5) / (realZ - 0.5);
                        }
                    } else {
                        if (localBlocked) {
                            // omg it now is blocked
                            double nextEndSlopeX = (x - 0.5) / (realZ + 0.5);

                            if (nextEndSlopeX > nextStartSlopeX) {
                                shadowcast(checker, centerX, centerY, centerZ, startZ + 1, nextStartSlopeX, nextEndSlopeX,
                                        startSlopeYY ,endSlopeYY , radius, zOffset, trMatrix11, trMatrix21, trMatrix31, trMatrix12, trMatrix22, trMatrix32, trMatrix13, trMatrix23, trMatrix33);
                            }
                        }
                    }
                    globalBlockStatus = localBlocked;
                }
                if (!globalBlockStatus) {
                    shadowcast(checker, centerX, centerY, centerZ, startZ + 1, nextStartSlopeX, endSlopeX,
                           startSlopeYY ,endSlopeYY , radius, zOffset, trMatrix11, trMatrix21, trMatrix31, trMatrix12, trMatrix22, trMatrix32, trMatrix13, trMatrix23, trMatrix33);
                }
            }
    }
}
