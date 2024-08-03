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
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.PriorityBlockingQueue;

public class WaterPathfinder {
    // A* on waterboard... lol graph search
    Set<AdvanceAction> availableActions = new HashSet<>();


    private State begin;
    private Map<State, NodeNode> mapping = new ConcurrentHashMap<>();

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
            availableActions.add(new AdvanceAction(stringListEntry.getValue(), stringListEntry.getKey(), 5, total, 3));
        }

        availableActions.add(new AdvanceAction(new ArrayList<>(), "nothing", 1, total, 1)); // it can handle 1 moves. yes.
        this.begin = new State(begin, new int[total.size()]);

    }

    private float fScore(NodeNode node) {
        int cnt = 0;
        for (int i = 0; i < node.state.flips.length; i++) {
            if ((node.state.flips[i] % 2 == 1) != (i < targets.size())) {
                cnt += i < targets.size() ? 1 : 100;
            }
        }

        return cnt * 5; // dijkstra lol
    }

    private boolean isDone(NodeNode node) {
        for (int i = 0; i < node.state.flips.length; i++) {
            if ((node.state.flips[i] % 2 == 1) != (i < targets.size())) {
                return false;
            }
        }
        return true;
    }

    public NodeNode pathfind() {
        PriorityBlockingQueue<NodeNode> nodes = new PriorityBlockingQueue<>(1000, Comparator.comparingDouble((NodeNode a) -> a.f).thenComparing(a -> a.g).thenComparing(NodeNode::hashCode));
        NodeNode start = openNode(begin);
        start.f = fScore(start);
        start.g = 0;
        nodes.add(start);


        while (!nodes.isEmpty()) {
            NodeNode node = nodes.poll();
            if (isDone(node)) return node; // first solution ggs

            for (AdvanceAction availableAction : availableActions) {

                State newState = availableAction.generateNew(node.state);
                if (newState == null) continue; // if nothing happened as result of action, end tree here.
                NodeNode newNodeNode = openNode(newState);
                if (newNodeNode == null) continue; // already visited.

                float newG = node.g + availableAction.cost;
                if (newNodeNode.g > newG) {
                    newNodeNode.g = newG;
                    float heuristic = fScore(newNodeNode);
                    if (heuristic > 100) {
                        continue;
                    }
                    newNodeNode.f = newNodeNode.g + heuristic;// heuristic
                    newNodeNode.parent = node;
                    newNodeNode.parentToMeAction = availableAction;


                    nodes.add(newNodeNode);
                }

            }
        }

        return null;
    }

    public NodeNode openNode(State state) {
        NodeNode nodeNode = new NodeNode(state, null, null, Float.MAX_VALUE, Float.MAX_VALUE);
        NodeNode previous = mapping.putIfAbsent(state, nodeNode);
        if (previous != null) return null;
        return nodeNode;
    }

    @AllArgsConstructor
    @Getter
    public static class State {
        private final Simulator.Node[][] state;
        private final int[] flips;

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            State state1 = (State) o;
            if (!Arrays.equals(flips, state1.flips)) return false;
            for (int y = 0; y < state.length; y++) {
                for (int x = 0; x < state[y].length; x++) {
                    if (!state[y][x].equals(state1.state[y][x])) {
                        return false;
                    }
                }
            }

            return true;
        }

        @Override
        public int hashCode() {
            return Arrays.hashCode(flips) << 31 | Arrays.deepHashCode(state);
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

    @Data
    @AllArgsConstructor
    public static class NodeNode {
        private State state;
        @EqualsAndHashCode.Exclude
        private NodeNode parent;
        private AdvanceAction parentToMeAction;

        private float f, g;
        // dist curr
    }


    @Getter
    @AllArgsConstructor
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
            for (int i = 0; i < moves; i++) {
                boolean status = Simulator.simulateSingleTick(nodes);
                if (i == 0 && !status && flips.isEmpty()) return null;
            }

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


    // test pathfinder
    public static void main(String[] args) {
        int[][] configuration = {
                {0, 0, 0, 0, 0, 0, 0, 0, 0, 9, 0, 9, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0, 0, 9, 8, 9, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 9, 9, 9, 9, 9, 7, 9, 9, 9, 9, 9, 9, 0, 0},
                {0, 0, 0, 0, 0, 9, 0, 0, 0, 2, 0, 3, 0, 0, 0, 0, 9, 0, 0},
                {0, 9, 9, 9, 9, 9, 0, 9, 9, 9, 1, 9, 9, 9, 9, 4, 9, 0, 0},
                {0, 9, 0, 0, 4, 0, 0, 0, 0, 0, 0, 9, 0, 0, 9, 0, 9, 9, 9},
                {9, 9, 0, 9, 9, 9, 9, 9, 3, 9, 4, 9, 9, 9, 9, 5, 0, 0, 9},
                {9, 0, 6, 0, 0, 9, 9, 9, 0, 9, 0, 9, 0, 0, 9, 6, 9, 0, 9},
                {9, 3, 9, 9, 5, 9, 9, 9, 0, 9, 2, 9, 9, 9, 9, 0, 9, 0, 9},
                {9, 0, 9, 9, 0, 9, 9, 1, 0, 9, 0, 0, 0, 0, 0, 0, 9, 2, 9},
                {9, 0, 9, 9, 0, 9, 9, 0, 9, 9, 9, 9, 9, 0, 9, 9, 9, 0, 9},
                {9, 0, 9, 9, 0, 9, 9, 0, 9, 9, 9, 9, 9, 0, 9, 9, 9, 0, 9},
        };
        int[] targets = {1, 4, 7, 13, 17};
        boolean[] targetActivate = {true, true, true, true, true};

        Simulator.Node[][] nodes = new Simulator.Node[configuration.length][configuration[0].length];
        Map<String, List<Simulator.Pt>> switchFlips = new HashMap<>();
        switchFlips.put("1", new ArrayList<>());
        switchFlips.put("2", new ArrayList<>());
        switchFlips.put("3", new ArrayList<>());
        switchFlips.put("4", new ArrayList<>());
        switchFlips.put("5", new ArrayList<>());
        switchFlips.put("6", new ArrayList<>());
        switchFlips.put("7", new ArrayList<>());

        for (int y = 0; y < configuration.length; y++) {
            for (int x = 0; x < configuration[y].length; x++) {
                Simulator.NodeType nodeType = null;
                int config = configuration[y][x];
                if (config < 8) {
                    nodeType = Simulator.NodeType.AIR;
                    if (config > 0) {
                        switchFlips.get(String.valueOf(config)).add(new Simulator.Pt(x, y));
                    }
                    if (config == 7) {
                        nodeType = Simulator.NodeType.BLOCK;
                    }
                } else if (config == 8) {
                    nodeType = Simulator.NodeType.SOURCE;
                } else if (config == 9) {
                    nodeType = Simulator.NodeType.BLOCK;
                }
                nodes[y][x] = new Simulator.Node(0, nodeType, false);
            }
        }

        List<Simulator.Pt> ptTargets = new ArrayList<>();
        List<Simulator.Pt> notTargets = new ArrayList<>();
        for (int i = 0; i < targets.length; i++) {
            if (targetActivate[i])
                ptTargets.add(new Simulator.Pt(targets[i], configuration.length - 1));
            else
                notTargets.add(new Simulator.Pt(targets[i], configuration.length - 1));
        }
        ;


        WaterPathfinder waterPathfinder = new WaterPathfinder(nodes, ptTargets, notTargets, switchFlips);
        NodeNode result = waterPathfinder.pathfind();
        System.out.println(result.state);
        NodeNode treeWalk = result;
        while (treeWalk.parent != null) {
            System.out.println(treeWalk.parentToMeAction.key + "/" + treeWalk.parentToMeAction.cost + "/" + treeWalk.parentToMeAction.moves);
            treeWalk = treeWalk.parent;
        }
//        System.out.println(result);

    }
}
