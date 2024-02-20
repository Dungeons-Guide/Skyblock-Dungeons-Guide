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

package kr.syeyoung.dungeonsguide.mod.dungeon.pathfinding;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import net.minecraft.util.BlockPos;
import net.minecraft.util.Vec3;
import net.minecraft.util.Vec3i;

import java.util.List;

@AllArgsConstructor
@Getter
public class PathfindResult {

    private List<PathfindNode> nodeList;
    private double cost;


    @AllArgsConstructor
    @Getter
    @Data
    public static class PathfindNode {
        private float x, y, z;

        public PathfindNode(double x, double y, double z, NodeType type) {
            this.x = (float) x;
            this.y = (float) y;
            this.z = (float) z;
            this.type = type;
        }

        private NodeType type;

        public double distanceSq(Vec3 position) {
            return position.squareDistanceTo(new Vec3(x,y,z));
        }
        public double distanceSq(Vec3i position) {
            return position.distanceSq(x,y,z);
        }

        public enum NodeType {
            STONK_EXIT, STONK_WALK, WALK, ETHERWARP, DIG_DOWN, ECHEST, DIG_UP, TELEPORT_INTO, SUPERBOOM;
        }
    }
}

