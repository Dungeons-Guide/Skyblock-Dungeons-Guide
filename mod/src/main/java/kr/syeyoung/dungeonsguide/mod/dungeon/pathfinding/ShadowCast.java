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

import net.minecraft.util.BlockPos;

import java.util.*;

// 3D SHADOW CASTING!!!
public class ShadowCast {
    public static interface Checker {
        public boolean checkIfBlocked(int x, int y, int z);
    }


    // z is radiating outward.
    // **** (sq)
    // *** (sq)
    // ** (sq)
    // * (sq)
    //c

    private static final int[][] TRANSFORM_MATRICES = {
            // rotated vers
            {1,0,0, 0,1,0,0,0,1},
            {0,1,0, 0,0,1,1,0,0},
            {0,0,1, 1,0,0,0,1,0}, // you might ask, "but shouldn't there be 6 rotated versions?" well xyz and yxz is same thing, because prism is symmetric. So divide by two.

            // flipping!
            {-1,0,0, 0,1,0,0,0,1},
            {0,-1,0, 0,0,1,1,0,0},
            {0,0,-1, 1,0,0,0,1,0},

            {1,0,0, 0,-1,0,0,0,1},
            {0,1,0, 0,0,-1,1,0,0},
            {0,0,1, -1,0,0,0,1,0},

            {-1,0,0, 0,-1,0,0,0,1},
            {0,-1,0, 0,0,-1,1,0,0},
            {0,0,-1, -1,0,0,0,1,0},

            {1,0,0, 0,1,0,0,0,-1},
            {0,1,0, 0,0,1,-1,0,0},
            {0,0,1, 1,0,0,0,-1,0},

            {-1,0,0, 0,1,0,0,0,-1},
            {0,-1,0, 0,0,1,-1,0,0},
            {0,0,-1, 1,0,0,0,-1,0},

            {1,0,0, 0,-1,0,0,0,-1},
            {0,1,0, 0,0,-1,-1,0,0},
            {0,0,1, -1,0,0,0,-1,0},
            {-1,0,0, 0,-1,0,0,0,-1},
            {0,-1,0, 0,0,-1,-1,0,0},
            {0,0,-1, -1,0,0,0,-1,0},

    };
    public static List<BlockPos> realShadowcast(Checker checker, int centerX, int centerY, int centerZ, int radius) {
        LinkedList<BlockPos> result = new LinkedList<>();
        for (int[] matrix : TRANSFORM_MATRICES) {
            shadowcast(checker, centerX, centerY, centerZ, 1,0, 1, 0, 1, radius,
                    matrix[0] ,matrix[1],matrix[2],
                    matrix[3], matrix[4], matrix[5],
                    matrix[6],matrix[7], matrix[8], result
            );
        }
        return result;
    }

    // Transform matrix is multiplied with stuff to transform coordiantes. Equation is as follows
    //
    // [ 11 21 31 ] [ x ]   [cx]   [trx]
    // [ 12 22 32 ] [ y ] + [cy] = [try]
    // [ 13 23 33 ] [ z ]   [cz]   [trz]
    public static void shadowcast(Checker checker, int centerX, int centerY, int centerZ, int startZ, double startSlopeX, double endSlopeX, double startSlopeY, double endSlopeY, int radius,
                                            int trMatrix11, int trMatrix21, int trMatrix31,
                                            int trMatrix12, int trMatrix22, int trMatrix32,
                                            int trMatrix13, int trMatrix23, int trMatrix33, LinkedList<BlockPos> result) {
        if (startZ > radius) return;
        // boom. radius is manhatten radius. lol.
        double realZ = startZ;

        int startY =  Math.max(0, (int) Math.floor(startSlopeY * (realZ - 0.5) - 0.5));
        int endY = (int) Math.ceil(endSlopeY * (realZ + 0.5) - 0.5);
        int startX = Math.max(0, (int) Math.floor(startSlopeX * (realZ - 0.5) - 0.5));
        int endX = (int) Math.ceil(endSlopeX * (realZ + 0.5) - 0.5);
        boolean[][] blockMap = new boolean[endY - startY + 1][endX - startX + 1];
        for (int y = startY; y <= endY; y++) {
            double currentSlopeY = y / realZ;
            for (int x = startX; x <= endX; x++) {
                int trX = centerX + x * trMatrix11 + y * trMatrix21 + startZ * trMatrix31;
                int trY = centerY + x * trMatrix12 + y * trMatrix22 + startZ * trMatrix32;
                int trZ = centerZ + x * trMatrix13 + y * trMatrix23 + startZ * trMatrix33;
                boolean localBlocked =checker.checkIfBlocked(trX, trY, trZ) && startZ != 0;

                if (endSlopeX == 1 && x == endX && blockMap[y - startY][x-1 - startX]) localBlocked = true;
                if (endSlopeY == 1 && y == endY && blockMap[y-1 - startY][x - startX]) localBlocked = true;

                blockMap[y - startY][x - startX] = localBlocked;

                double currentSlopeX = x / realZ;

                if (currentSlopeY < startSlopeY || currentSlopeY > endSlopeY || currentSlopeX < startSlopeX || currentSlopeX > endSlopeX) continue;
                if (localBlocked) continue;


                result.add(new BlockPos(trX, trY, trZ));
            }
        }
        Set<Integer> xEdge = new TreeSet<>();
        Set<Integer> yEdge = new TreeSet<>();
        for (int y = 0; y < blockMap.length ; y++) {
            for (int x = 0; x < blockMap[y].length; x++) {
                if (y < blockMap.length -1 && blockMap[y][x] != blockMap[y+1][x])
                    yEdge.add(y);
                if (x < blockMap[0].length - 1 && blockMap[y][x] != blockMap[y][x+1])
                    xEdge.add(x);
            }
        }
        yEdge.add(blockMap.length -1);
        xEdge.add(blockMap[0].length - 1);


        int prevY = -1;
        for (Integer y : yEdge) {
            int prevX = -1;
            for (Integer x : xEdge) {
                if (!blockMap[y][x]) {

                    boolean yGood = prevY != -1 && !blockMap[prevY][x];
                    boolean xGood = prevX != -1 && !blockMap[y][prevX];
                    boolean diagonalGood = prevY != -1 && prevX != -1 && !blockMap[prevY][prevX];


                    if (diagonalGood && yGood && xGood) {
                        double startSlopeYY = Math.max(startSlopeY, (prevY + startY + 0.5) / (realZ + 0.5));
                        double endSlopeYY = Math.min(endSlopeY, (y + startY + 0.5) / (realZ + 0.5));
                        double startSlopeXX  = Math.max(startSlopeX, (prevX  + startX+ 0.5) / (realZ + 0.5));
                        double endSlopeXX = Math.min(endSlopeX, (x + startX + 0.5) / (realZ + 0.5));
                        if (startSlopeYY < endSlopeYY && startSlopeXX < endSlopeXX) {
                            shadowcast(checker, centerX, centerY, centerZ, startZ + 1, startSlopeXX, endSlopeXX,
                                    startSlopeYY ,endSlopeYY , radius, trMatrix11, trMatrix21, trMatrix31, trMatrix12, trMatrix22, trMatrix32, trMatrix13, trMatrix23, trMatrix33, result);
                        }
                    } else if ((xGood && !yGood) || (yGood && !xGood)) {
                        double startSlopeYY = yGood ? Math.max(startSlopeY, (prevY + startY + 0.5) / (realZ + 0.5)) : Math.max(startSlopeY, (prevY + startY + 0.5) / (realZ - 0.5));
                        double endSlopeYY = Math.min(endSlopeY, (y + startY + 0.5) / (realZ + 0.5));
                        double startSlopeXX  = xGood ? Math.max(startSlopeX, (prevX  + startX+ 0.5) / (realZ + 0.5)) : Math.max(startSlopeX, (prevX  + startX+ 0.5) / (realZ - 0.5));
                        double endSlopeXX = Math.min(endSlopeX, (x + startX + 0.5) / (realZ + 0.5));
                        if (startSlopeYY < endSlopeYY && startSlopeXX < endSlopeXX) {
                            shadowcast(checker, centerX, centerY, centerZ, startZ + 1, startSlopeXX, endSlopeXX,
                                    startSlopeYY ,endSlopeYY , radius, trMatrix11, trMatrix21, trMatrix31, trMatrix12, trMatrix22, trMatrix32, trMatrix13, trMatrix23, trMatrix33, result);
                        }
                    } else if (!diagonalGood && yGood && xGood) {
                        {
                            double startSlopeYY = Math.max(startSlopeY, (prevY + startY + 0.5) / (realZ - 0.5));
                            double endSlopeYY = Math.min(endSlopeY, (y + startY + 0.5) / (realZ + 0.5));
                            double startSlopeXX = Math.max(startSlopeX, (prevX + startX + 0.5) / (realZ + 0.5));
                            double endSlopeXX = Math.min(endSlopeX, (x + startX + 0.5) / (realZ + 0.5));
                            if (startSlopeYY < endSlopeYY && startSlopeXX < endSlopeXX) {
                                shadowcast(checker, centerX, centerY, centerZ, startZ + 1, startSlopeXX, endSlopeXX,
                                        startSlopeYY, endSlopeYY, radius, trMatrix11, trMatrix21, trMatrix31, trMatrix12, trMatrix22, trMatrix32, trMatrix13, trMatrix23, trMatrix33, result);
                            }
                        }
                        {
                            double startSlopeYY = Math.max(startSlopeY, (prevY + startY + 0.5) / (realZ + 0.5));
                            double endSlopeYY = Math.min(endSlopeY, (prevY + startY + 0.5) / (realZ - 0.5));
                            double startSlopeXX  =  Math.max(startSlopeX, (prevX  + startX+ 0.5) / (realZ - 0.5));
                            double endSlopeXX = Math.min(endSlopeX, (x + startX + 0.5) / (realZ + 0.5));
                            if (startSlopeYY < endSlopeYY && startSlopeXX < endSlopeXX) {
                                shadowcast(checker, centerX, centerY, centerZ, startZ + 1, startSlopeXX, endSlopeXX,
                                        startSlopeYY ,endSlopeYY , radius, trMatrix11, trMatrix21, trMatrix31, trMatrix12, trMatrix22, trMatrix32, trMatrix13, trMatrix23, trMatrix33, result);
                            }
                        }
                    } else {
                        double startSlopeYY = Math.max(startSlopeY, (prevY + startY + 0.5) / (realZ - 0.5));
                        double endSlopeYY = Math.min(endSlopeY, (y + startY + 0.5) / (realZ + 0.5));
                        double startSlopeXX  = Math.max(startSlopeX, (prevX  + startX+ 0.5) / (realZ - 0.5));
                        double endSlopeXX = Math.min(endSlopeX, (x + startX + 0.5) / (realZ + 0.5));
                        if (startSlopeYY < endSlopeYY && startSlopeXX < endSlopeXX) {
                            shadowcast(checker, centerX, centerY, centerZ, startZ + 1, startSlopeXX, endSlopeXX,
                                    startSlopeYY ,endSlopeYY , radius, trMatrix11, trMatrix21, trMatrix31, trMatrix12, trMatrix22, trMatrix32, trMatrix13, trMatrix23, trMatrix33, result);
                        }
                        // normal case
                    }

                }
                prevX = x;
            }
            prevY = y;
        }
    }
}
