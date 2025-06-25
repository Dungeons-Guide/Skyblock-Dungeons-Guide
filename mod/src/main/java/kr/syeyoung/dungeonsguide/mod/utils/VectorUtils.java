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

package kr.syeyoung.dungeonsguide.mod.utils;

import kr.syeyoung.modapi.data.Vector3D;
import lombok.AllArgsConstructor;
import lombok.Data;

import javax.vecmath.Vector2d;

public class VectorUtils {
    // Ior rooms, different coordinate system is used. Y Increases as marker goes down. X is the same.


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

    public static double distSquared(Vector3D lookVec, Vector3D posVec, Vector3D objectVec) {
        Vector3D v = objectVec.clone().subtract(posVec);
        double t = v.dotProduct(lookVec);
        Vector3D p = posVec.clone().add(lookVec.x * t, lookVec.y * t, lookVec.z * t);
        return p.distanceSq(objectVec) / p.distanceSq(posVec);
    }
}
