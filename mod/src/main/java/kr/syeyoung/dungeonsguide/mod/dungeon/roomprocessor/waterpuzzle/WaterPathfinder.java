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

import java.util.*;

public class WaterPathfinder {
    // A* on waterboard... lol graph search
    Set<AdvanceAction> availableActions = new HashSet<>();


    private State begin;
    private Map<State, NodeNode> mapping = new HashMap<>();

    private List<Simulator.Pt> targets = new ArrayList<>();
    private List<Simulator.Pt> nonTargets = new ArrayList<>();
    private int maxMatch = 0;

    public WaterPathfinder(Simulator.Node[][] begin, List<Simulator.Pt> targets, List<Simulator.Pt> nonTargets, Map<String, List<Simulator.Pt>> switchFlips) {
        this.targets = targets;
        this.nonTargets = nonTargets;

        ArrayList<Simulator.Pt> total = new ArrayList();
        total.addAll(targets);
        total.addAll(nonTargets);
        maxMatch = total.size();
        for (Map.Entry<String, List<Simulator.Pt>> stringListEntry : switchFlips.entrySet()) {
            availableActions.add(new AdvanceAction(stringListEntry.getValue(), stringListEntry.getKey(), 10, total,
                    10));
        }

        availableActions.add(new AdvanceAction(new ArrayList<>(), "nothing", 1, total, 10));
        this.begin = new State(begin, new int[total.size()]);

    }

    private float fScore(NodeNode node) {
        int cnt = 0;
        for (int i = 0; i < node.state.flips.length; i++) {
            if ((node.state.flips[i] % 2 == 1) != (i < targets.size())) {
                cnt += 1;
            }
        }
//
//        int smh = node.state.state.length;
//        label: for (int i = node.state.state.length-1; i >= 0; i--) {
//            for (int x = 0; x < node.state.state[i].length; x++) {
//                if (node.state.state[i][x].getNodeType().isWater()) {
//                    smh = i;
//                    break label;
//                }
//            }
//        }
//        return (cnt) * 20;
        return cnt * 200; // dijkstra lol
    }
    public NodeNode pathfind() {
        PriorityQueue<NodeNode> nodes = new PriorityQueue<>(Comparator.comparingDouble((NodeNode a) -> a.f).thenComparing(a -> a.g).thenComparing(a->Arrays.deepHashCode(a.state.state)).thenComparing(NodeNode::hashCode));
        NodeNode start = openNode(begin);
        start.f = fScore(start);
        start.g = 0;
        nodes.add(start);
        while(!nodes.isEmpty()) {
            NodeNode node = nodes.poll();
            if (fScore(node) == 0) return node;
//            System.out.println(nodes.size());


            for (AdvanceAction availableAction : availableActions) {
                State newState = availableAction.generateNew(node.state);
                if (newState == null) continue;
                NodeNode newNodeNode = openNode(newState);

                float newG = node.g + availableAction.cost;
                if (newNodeNode.g > newG) {
                    newNodeNode.g = newG;
                    newNodeNode.f = newNodeNode.g + fScore(newNodeNode) ;// heuristic
                    newNodeNode.parent = node;
                    newNodeNode.parentToMeAction = availableAction;

                    nodes.add(newNodeNode);
                }
            }
        }
        return null;
    }

    public NodeNode openNode(State state) {
        if (mapping.containsKey(state)) {
            return mapping.get(state);
        }
        NodeNode nodeNode = new NodeNode(state, null, null, Float.MAX_VALUE, Float.MAX_VALUE);
        mapping.put(state, nodeNode);
        return nodeNode;
    }

    @AllArgsConstructor @Getter
    public static class State {
        private final Simulator.Node[][] state;
        private final int[] flips;

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            State state1 = (State) o;

            return Arrays.deepEquals(state, state1.state) && Arrays.equals(flips, state1.flips);
        }

        @Override
        public int hashCode() {
            return Arrays.deepHashCode(state) << 31 | Arrays.hashCode(flips);
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            for (int y = 0; y < state.length; y++) {
                for (int x = 0; x < state[y].length; x++) {
                    Simulator.NodeType type = state[y][x].getNodeType();
                    int cnt = state[y][x].getWaterLevel();
                    if (type == Simulator.NodeType.BLOCK) {
                        sb.append("X");
                    } else if (type == Simulator.NodeType.AIR) {
                        sb.append(" ");
                    } else if (type == Simulator.NodeType.WATER) {
                        sb.append(cnt);
                    } else {
                        sb.append("W");
                    }
                }
                sb.append("\n");
            }
            return sb.toString();
        }
    }

    @Data @AllArgsConstructor
    public static class NodeNode {
        private State state;
        private NodeNode parent;
        private AdvanceAction parentToMeAction;

        private float f, g;
        // dist curr
    }


    @Getter @AllArgsConstructor
    public static class AdvanceAction {
        private final List<Simulator.Pt> flips;
        private final String key;
        private final float cost;
        private final List<Simulator.Pt> targets;
        private final int moves;

        public State generateNew(State from) {
            Simulator.Node[][] nodes = Simulator.clone(from.state);
            for (Simulator.Pt flip : flips) {
                Simulator.Node node = flip.get(nodes);
                if (node.getNodeType() == Simulator.NodeType.BLOCK) flip.set(nodes, Simulator.NodeType.AIR);
                else flip.set(nodes, Simulator.NodeType.BLOCK);
            }
            Simulator.simulateSingleTick(nodes);
            for (int i = 0; i < moves; i++) {
                Simulator.simulateSingleTick(nodes);
            }
//            if (1==1) return null;

            int[] newFlips = Arrays.copyOf(from.flips, from.flips.length);

            for (int i = 0; i < targets.size(); i++) {
                Simulator.Pt target = targets.get(i);
                if (!target.get(from.state).getNodeType().isWater() && target.get(nodes).getNodeType().isWater()) {
                    newFlips[i]++;
                }
            }

            return new State(nodes, newFlips);
        }
    }
}
