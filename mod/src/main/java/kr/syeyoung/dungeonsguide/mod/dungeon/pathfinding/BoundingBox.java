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

import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.Vec3;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


public class BoundingBox {
    private final List<AxisAlignedBB> boundingBoxes = new ArrayList<>();

    public List<AxisAlignedBB> getBoundingBoxes() {
        return boundingBoxes;
    }

    public void addBoundingBox(AxisAlignedBB bb) {
        boundingBoxes.add(bb);
    }

    public boolean isIn(Vec3 vec) {
        for (AxisAlignedBB boundingBox : boundingBoxes) {
            if (boundingBox.isVecInside(vec)) return true;
        }
        return false;
    }

    public boolean isIn(double x, double y, double z) {
        for (AxisAlignedBB boundingBox : boundingBoxes) {
            if (boundingBox.minX <= x && x <= boundingBox.maxX &&
                boundingBox.minY <= y && y <= boundingBox.maxY &&
                boundingBox.minZ <= z && z <= boundingBox.maxZ) return true;
        }
        return false;
    }

    public BoundingBox multiply(double scalar) {
        BoundingBox bb = new BoundingBox();
        for (AxisAlignedBB a : boundingBoxes) {
            bb.addBoundingBox(new AxisAlignedBB(a.minX * scalar, a.minY* scalar, a.minZ* scalar, a.maxX* scalar, a.maxY* scalar, a.maxZ* scalar));
        }
        return bb;
    }
    public BoundingBox translate(double x, double y, double z) {
        BoundingBox bb = new BoundingBox();
        for (AxisAlignedBB a : boundingBoxes) {
            bb.addBoundingBox(new AxisAlignedBB(a.minX + x, a.minY + y, a.minZ + z, a.maxX + x, a.maxY + y, a.maxZ + z));
        }
        return bb;
    }

    public Vec3 center() {
        double xSum = 0, ySum = 0, zSum = 0;
        double denominator = 0;
        for (AxisAlignedBB boundingBox : boundingBoxes) {
            double lcx = (boundingBox.minX + boundingBox.maxX) / 2;
            double lcy = (boundingBox.minY + boundingBox.maxY) / 2;
            double lcz = (boundingBox.minZ + boundingBox.maxZ) / 2;
            double area = (boundingBox.maxX - boundingBox.minX) * (boundingBox.maxY - boundingBox.minY) * (boundingBox.maxZ - boundingBox.minZ);
            denominator += area;
            xSum += lcx * area;
            ySum += lcy * area;
            zSum += lcz * area;
        }
        return new Vec3(xSum/denominator, ySum/denominator, zSum / denominator);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BoundingBox that = (BoundingBox) o;

        return boundingBoxes.equals(that.boundingBoxes);
    }

    @Override
    public int hashCode() {
        return boundingBoxes.hashCode();
    }

    public static BoundingBox of(AxisAlignedBB bb) {
        BoundingBox boundingBox = new BoundingBox();
        boundingBox.addBoundingBox(bb);
        return boundingBox;
    }
}
