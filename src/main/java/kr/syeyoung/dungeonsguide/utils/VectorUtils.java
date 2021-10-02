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

import lombok.AllArgsConstructor;
import lombok.Data;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.util.Vec3;

import javax.vecmath.Vector2d;

public class VectorUtils {
    // Ior rooms, different coordinate system is used. Y Increses as marker goes down. X is same.


    public static Vector2d rotateCounterClockwise(Vector2d vector2d) {
        return new Vector2d(vector2d.y, -vector2d.x);
    }
    public static Vector2d rotateClockwise(Vector2d vector2d) {
        return new Vector2d(-vector2d.y, vector2d.x);
    }

    @Data @AllArgsConstructor
    public static class ProjectionResult {
        private float x;
        private float y;
        private boolean back;
    }

    public static double distSquared(Vec3 lookVec, Vec3 posVec, Vec3 objectVec) {
        Vec3 v = objectVec.subtract(posVec);
        double t = v.dotProduct(lookVec);
        Vec3 p = posVec.addVector(lookVec.xCoord * t, lookVec.yCoord * t, lookVec.zCoord * t);
        return p.squareDistanceTo(objectVec) / p.squareDistanceTo(posVec);
    }
}
