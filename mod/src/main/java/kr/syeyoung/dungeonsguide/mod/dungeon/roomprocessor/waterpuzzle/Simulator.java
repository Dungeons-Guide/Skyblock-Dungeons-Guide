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

package kr.syeyoung.dungeonsguide.mod.dungeon.roomprocessor.waterpuzzle;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

import java.util.List;
import java.util.Scanner;

public class Simulator {

    @AllArgsConstructor @Getter
    public enum NodeType {
        BLOCK(false), AIR(false), WATER(true), SOURCE(true);

        private boolean water;
    }

    @Data @AllArgsConstructor
    public static class Node {
        private int waterLevel;
        private NodeType nodeType;
        private boolean update;
    }
    @Getter @Data
    public static class Pt {
        private static final Pt[][] cache = new Pt[50][50];

        public static Pt ofPt(int x, int y) {
            return cache[y+5][x+5];
//            return new Pt(x,y);
        }

        static {
            for (int y = 0; y < 50; y++) {
                for (int x = 0; x < 50; x++) {
                    cache[y][x] = new Pt(x-5, y-5);
                }
            }
        }


        private final int x, y;

        public Pt up() {return cache[y+4][x+5];}
        public Pt down() {return cache[y+6][x+5];}
        public Pt left() {return cache[y+5][x+4];}
        public Pt right() {return cache[y+5][x+6];}

        public boolean check(int w, int h) {
            return x < 0 || y < 0 || x >= w || y >= h;
        }

        public Node get(Node[][] nodes) {
            if (check(nodes[0].length, nodes.length)) {
                return new Node(0, NodeType.BLOCK, false);
            }
            return nodes[y][x];
        }
        public NodeType getType(Node[][] nodes) {
            if (check(nodes[0].length, nodes.length)) {
                return NodeType.BLOCK;
            }
            return nodes[y][x].nodeType;
        }

        public boolean getUpdate(Node[][] nodes) {
            if (check(nodes[0].length, nodes.length)) {
                return false;
            }
            return nodes[y][x].update;
        }

        public void set(Node[][] nodes, NodeType nodeType) {
            get(nodes).nodeType = nodeType;
            get(nodes).waterLevel = 0;
            get(nodes).update = true;
        }

        public int getFlowDirection(Node[][] nodes) {
            int right = 0;
            Pt rightPt = this.right();
            while (true) {
                right++;
                if (right == 8) {
                    right = 999; break;
                }
                if (rightPt.getType(nodes) == NodeType.BLOCK) {
                    right = 999; break;
                }
                if (rightPt.down().getType(nodes) != NodeType.BLOCK) {
                    break;
                }

                rightPt = rightPt.right();
            }
            int left = 0;

            Pt leftPt = this.left();
            while (true) {
                left++;
                if (left == 8) {
                    left = 999; break;
                }
                if (leftPt.getType(nodes) == NodeType.BLOCK) {
                    left = 999; break;
                }
                if (leftPt.down().getType(nodes) != NodeType.BLOCK) {
                    break;
                }

                leftPt = leftPt.left();
            }

            if (left == right) return 0;
            if (left > right) return 1;
            return -1;
        }

        public boolean shouldUpdate(Node[][] nodes) {
            return getUpdate(nodes) || right().getUpdate(nodes)
                    || left().getUpdate(nodes)
                    || up().getUpdate(nodes)
                    || down().getUpdate(nodes);
        }
    }
    // if there is waterLevel bigger coming nearby, make water
    // if there is waterLevel bigger coming nearby, delete water
    //
//    public static void simulateTicks(Node[][] nodes) {
//        Node[][]
//
//        while(simulateSingleTick(nodes));
//    }

    public static Node[][] clone(Node[][] nodes) {
        Node[][] newNodes = new Node[nodes.length][nodes[0].length];

        for (int y = 0; y < nodes.length; y++) {
            for (int x = 0; x < nodes[y].length; x++) {
                newNodes[y][x] = new Node(nodes[y][x].waterLevel, nodes[y][x].nodeType, nodes[y][x].update);
            }
        }
        return newNodes;
    }

    public static boolean doTick(Node[][] nodes, Node[][] nodesNew, Pt pt) {
        int y = pt.y, x = pt.x;
        Node prev = pt.get(nodes);
        int maxWaterLv = Math.max(0, prev.nodeType == NodeType.SOURCE ? 8 : prev.waterLevel - 1);
        if (prev.nodeType == NodeType.AIR || prev.nodeType == NodeType.WATER) {
            if (pt.up().getType(nodes).isWater()) {
                maxWaterLv = 8;
            }
            if (pt.left().getType(nodes).isWater()) {
                boolean isSource = pt.left().getType(nodes) == NodeType.SOURCE;
                NodeType bottomLeft = pt.left().down().getType(nodes);
                if (prev.nodeType == NodeType.WATER  // if was water
                        || (pt.left().shouldUpdate(nodes) && (bottomLeft == NodeType.BLOCK || (isSource && bottomLeft != NodeType.AIR)) && pt.left().getFlowDirection(nodes) >= 0)) {
                    maxWaterLv = Math.max(maxWaterLv, pt.left().get(nodes).waterLevel - 1);
                }
            }
            if (pt.right().getType(nodes).isWater()) {
                boolean isSource = pt.right().getType(nodes) == NodeType.SOURCE;
                NodeType bottomRight = pt.right().down().getType(nodes);
                if (prev.nodeType == NodeType.WATER
                        || (pt.right().shouldUpdate(nodes) && (bottomRight == NodeType.BLOCK || (isSource && bottomRight != NodeType.AIR)) && pt.right().getFlowDirection(nodes) <= 0))
                    maxWaterLv = Math.max(maxWaterLv, pt.right().get(nodes).waterLevel - 1);
            }
        }

        nodesNew[y][x] = new Node(prev.waterLevel, prev.nodeType, false);
//        nodesNew[y][x].setNodeType(prev.nodeType);
        nodesNew[y][x].setWaterLevel(maxWaterLv);
        if (maxWaterLv == 0 && nodesNew[y][x].nodeType == NodeType.WATER)
            nodesNew[y][x].setNodeType(NodeType.AIR);
        else if (maxWaterLv > 0 && nodesNew[y][x].nodeType == NodeType.AIR)
            nodesNew[y][x].setNodeType(NodeType.WATER);

        if (prev.nodeType != nodesNew[y][x].nodeType || prev.waterLevel != nodesNew[y][x].waterLevel) {
            nodesNew[y][x].update = true;
        }
        return nodesNew[y][x].update;
    }
    public static boolean simulateSingleTick(Node[][] nodes) {
        Node[][] nodesNew = new Node[nodes.length][nodes[0].length];
        boolean update = false;
        for (int y = 0; y < nodes.length; y++) {
            for (int x = 0; x < nodes[y].length; x++) {
                Pt pt = Pt.ofPt(x,y);


                if (doTick(nodes, nodesNew, pt)) update =true;

                if ( pt.get(nodesNew).waterLevel - pt.get(nodes).waterLevel > 0 && pt.getType(nodes) == NodeType.WATER && pt.get(nodesNew).nodeType == NodeType.WATER) {
                    Node prev = pt.get(nodes);

                    nodes[y][x] = nodesNew[y][x];

                    if (x > 0)
                        doTick(nodes, nodesNew, pt.left());
                    if (x < nodes[0].length - 1)
                        doTick(nodes, nodesNew, pt.right());

                    nodes[y][x] = prev;
                }
            }
        }

        for (int y = 0; y < nodesNew.length; y++) {
            for (int x = 0; x < nodesNew[y].length; x++) {
                nodes[y][x] = nodesNew[y][x];
            }
        }
        return update;
    }

    public static void print(Node[][] nodes) {
        for (int y = 0; y < nodes.length; y++) {
            for (int x = 0; x < nodes[y].length; x++) {
                NodeType type = nodes[y][x].nodeType;
                int cnt = nodes[y][x].waterLevel;
                if (type == NodeType.BLOCK) {
                    System.out.print("X");
                } else if (type == NodeType.AIR) {
                    System.out.print(" ");
                } else if (type == NodeType.WATER) {
                    System.out.print(cnt);
                } else {
                    System.out.print("W");
                }
            }
            System.out.println();
        }
        System.out.println("-----------------");
    }

    // New waterboard simulator pog
    public static void main(String[] args) {
//        NodeType[][] nodeTypes = {
//                {NodeType.BLOCK, NodeType.BLOCK, NodeType.BLOCK, NodeType.BLOCK, NodeType.BLOCK, NodeType.BLOCK, NodeType.AIR, NodeType.SOURCE, NodeType.BLOCK},
//                {NodeType.AIR,    NodeType.AIR,   NodeType.AIR,   NodeType.AIR,   NodeType.AIR  , NodeType.AIR,   NodeType.AIR,   NodeType.AIR,   NodeType.AIR },
//                {NodeType.AIR,    NodeType.AIR,   NodeType.AIR,   NodeType.AIR,   NodeType.AIR  , NodeType.AIR,   NodeType.AIR,   NodeType.AIR,   NodeType.AIR },
//                {NodeType.AIR,    NodeType.AIR,   NodeType.AIR,   NodeType.AIR,   NodeType.AIR  , NodeType.AIR,   NodeType.AIR,   NodeType.AIR,   NodeType.AIR },
//                {NodeType.BLOCK,  NodeType.AIR,   NodeType.AIR,   NodeType.AIR,   NodeType.AIR  , NodeType.AIR,   NodeType.AIR,   NodeType.AIR,   NodeType.AIR }
//        };
//
//        Node[][] nodes = new Node[nodeTypes.length][nodeTypes[0].length];
//        for (int y = 0; y < nodes.length; y++) {
//            for (int x = 0; x < nodes[y].length; x++) {
//                nodes[y][x] = new Node(0, nodeTypes[y][x], false);
//                if (nodeTypes[y][x] == NodeType.SOURCE) {
//                    nodes[y][x].update = true;
//                    nodes[y][x].waterLevel = 8;
//                }
//            }
//        }
//        Scanner scanner = new Scanner(System.in);
//        while(true) {
//            print(nodes);
//            while(simulateSingleTick(nodes))
//                print(nodes);
//
//            int x = scanner.nextInt();
//            int y= scanner.nextInt();
//            nodes[y][x] = new Node(0,
//                    nodes[y][x].nodeType == NodeType.BLOCK ?
//                        NodeType.AIR : NodeType.BLOCK, true);
//        }
    }
}
