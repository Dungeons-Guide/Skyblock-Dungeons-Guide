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

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
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
    }
    @Getter @AllArgsConstructor
    public static class Pt {
        private final int x, y;

        public Pt up() {return new Pt(x, y-1);}
        public Pt down() {return new Pt(x, y+1);}
        public Pt left() {return new Pt(x-1, y);}
        public Pt right() {return new Pt(x+1, y);}

        public boolean check(int w, int h) {
            return x < 0 || y < 0 || x >= w || y >= h;
        }

        public Node get(Node[][] nodes) {
            if (check(nodes[0].length, nodes.length)) {
                return new Node(0, NodeType.BLOCK);
            }
            return nodes[y][x];
        }

    }
    // if there is waterLevel bigger coming nearby, make water
    // if there is waterLevel bigger coming nearby, delete water
    //
    public static void simulateTicks(Node[][] nodes, List<Pt> startingPt) {
        Queue<Pt> list = new LinkedList<>();
        list.addAll(startingPt);

        while (!list.isEmpty()) {
            Pt pt = list.poll();
            if (pt.check(nodes[0].length, nodes.length)) continue;
            Node n = pt.get(nodes);
            if (n.nodeType == NodeType.BLOCK) continue;

            int maxWaterLv = 0;
            if (pt.up().get(nodes).nodeType.isWater()) {
                maxWaterLv = Math.max(maxWaterLv, 8);
            }
            if (pt.left().get(nodes).nodeType.isWater()) {
                if (n.nodeType == NodeType.WATER || pt.left().down().get(nodes).nodeType == NodeType.BLOCK)
                    maxWaterLv = Math.max(maxWaterLv, pt.left().get(nodes).waterLevel-1);
            }
            if (pt.right().get(nodes).nodeType.isWater()) {
                if (n.nodeType == NodeType.WATER || pt.left().down().get(nodes).nodeType == NodeType.BLOCK)
                    maxWaterLv = Math.max(maxWaterLv, pt.right().get(nodes).waterLevel-1);
            }
            if (maxWaterLv < n.waterLevel) maxWaterLv = 0;


            if (maxWaterLv > 0) {
                n.waterLevel = maxWaterLv;
                n.nodeType = NodeType.WATER;
            } else {
                n.waterLevel = 0;
                n.nodeType = NodeType.AIR;
            }

            list.add(pt.down());
            list.add(pt.left());
            list.add(pt.right());
        }
    }


    public static boolean simulateSingleTick(Node[][] nodes) {
        Node[][] nodesNew = new Node[nodes.length][nodes[0].length];

        boolean update = false;
        for (int y = 0; y < nodes.length; y++) {
            for (int x = 0; x < nodes[y].length; x++) {
                Node prev = nodes[y][x];

                Pt pt = new Pt(x,y);

                int maxWaterLv = Math.max(0, prev.waterLevel - 1);
                if (pt.up().get(nodes).nodeType.isWater()) {
                    maxWaterLv = 8;
                }
                if (pt.left().get(nodes).nodeType.isWater()) {
                    if (prev.nodeType == NodeType.WATER || pt.left().down().get(nodes).nodeType == NodeType.BLOCK)
                        maxWaterLv = Math.max(maxWaterLv, pt.left().get(nodes).waterLevel-1);
                }
                if (pt.right().get(nodes).nodeType.isWater()) {
                    if (prev.nodeType == NodeType.WATER || pt.left().down().get(nodes).nodeType == NodeType.BLOCK)
                        maxWaterLv = Math.max(maxWaterLv, pt.right().get(nodes).waterLevel-1);
                }

                nodesNew[y][x] = new Node(prev.waterLevel, prev.nodeType);
                nodesNew[y][x].setWaterLevel(maxWaterLv);
                if (maxWaterLv == 0 && nodesNew[y][x].nodeType == NodeType.WATER)
                    nodesNew[y][x].setNodeType(NodeType.AIR);
                else if (maxWaterLv > 0 && nodesNew[y][x].nodeType == NodeType.AIR)
                    nodesNew[y][x].setNodeType(NodeType.WATER);

                if (prev.nodeType != nodesNew[y][x].nodeType) update = true;
                if (prev.waterLevel != nodesNew[y][x].waterLevel) update = true;
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
        NodeType[][] nodeTypes = {
                {NodeType.SOURCE, NodeType.BLOCK, NodeType.BLOCK, NodeType.BLOCK, NodeType.BLOCK, NodeType.BLOCK, NodeType.BLOCK, NodeType.BLOCK, NodeType.BLOCK},
                {NodeType.AIR,    NodeType.AIR,   NodeType.AIR,   NodeType.AIR,   NodeType.AIR  , NodeType.AIR,   NodeType.AIR,   NodeType.AIR,   NodeType.AIR },
                {NodeType.AIR,    NodeType.AIR,   NodeType.AIR,   NodeType.AIR,   NodeType.AIR  , NodeType.AIR,   NodeType.AIR,   NodeType.AIR,   NodeType.AIR },
                {NodeType.AIR,    NodeType.AIR,   NodeType.AIR,   NodeType.AIR,   NodeType.AIR  , NodeType.AIR,   NodeType.AIR,   NodeType.AIR,   NodeType.AIR },
                {NodeType.BLOCK,  NodeType.AIR,   NodeType.AIR,   NodeType.AIR,   NodeType.AIR  , NodeType.AIR,   NodeType.AIR,   NodeType.AIR,   NodeType.AIR }
        };

        Node[][] nodes = new Node[nodeTypes.length][nodeTypes[0].length];
        for (int y = 0; y < nodes.length; y++) {
            for (int x = 0; x < nodes[y].length; x++) {
                nodes[y][x] = new Node(0, nodeTypes[y][x]);
            }
        }
        Scanner scanner = new Scanner(System.in);
        while(true) {
            print(nodes);
            while (simulateSingleTick(nodes)) {
                print(nodes);
            }

            int x = scanner.nextInt();
            int y= scanner.nextInt();
            nodes[y][x] = new Node(0,
                    nodes[y][x].nodeType == NodeType.BLOCK ?
                        NodeType.AIR : NodeType.BLOCK);
        }
    }
}
