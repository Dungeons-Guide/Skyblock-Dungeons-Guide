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
            if (!stringListEntry.getValue().isEmpty())
                availableActions.add(new AdvanceAction(stringListEntry.getValue(), stringListEntry.getKey(), 10, total, 3));
        }

        availableActions.add(new AdvanceAction(new ArrayList<>(), "nothing", 1, total, 1)); // it can handle 1 moves. yes.
        this.begin = new State(begin, new int[total.size()]);

    }

    private float fScore(NodeNode node) {
        Simulator.Node[][] newstate = Simulator.clone(node.state.state);
        int updatedTicks = 40;

        int[] newFlips = Arrays.copyOf(node.state.flips, node.state.flips.length);
        for (int j = 0; j < 40; j++) {
            boolean[] prevState = new boolean[targets.size() + nonTargets.size()];
            for (int i = 0; i < targets.size(); i++) {
                prevState[i] = targets.get(i).getType(newstate).isWater();
            }
            for (int i = 0; i < nonTargets.size(); i++) {
                prevState[i + targets.size()] = nonTargets.get(i).getType(newstate).isWater();
            }

            if (!Simulator.simulateSingleTick(newstate)) {
                break;
            }

            for (int i = 0; i < targets.size(); i++) {
                if (!prevState[i] && targets.get(i).getType(newstate).isWater()) {
                    newFlips[i]++;
                    updatedTicks = j;
                }
            }
            for (int i = 0; i < nonTargets.size(); i++) {
                if (!prevState[i + targets.size()] && nonTargets.get(i).getType(newstate).isWater()) {
                    newFlips[i+targets.size()]++;
                    updatedTicks = j;
                }
            }
        }


        // loooook into the future~


        int cnt = 0;
        for (int i = 0; i < node.state.flips.length; i++) {
            if ((newFlips[i] % 2 == 1) != (i < targets.size())) {
                cnt += i < targets.size() ? 10 : 10; // predict moves. admissible heuristic.
            }
        }

//        if (flips > 0)
        if (cnt == 0 && updatedTicks == 40) return 0;
        return updatedTicks + cnt;
//        else
//            return 50;
//        if (cnt == 0) return updatedTicks;
//        if (updatedTicks < 10 && cnt > 4) return (10-updatedTicks) * cnt;
//        return cnt * 30; // dijkstra lol
    }

    private boolean isDone(NodeNode node) {
        for (int i = 0; i < node.state.flips.length; i++) {
            if ((node.state.flips[i] % 2 == 1) != (i < targets.size())) {
                return false;
            }
        }
        Simulator.Node[][] newstate = Simulator.clone(node.state.state);
        for (int i = 0; i < 30; i++) {
            if (!Simulator.simulateSingleTick(newstate)) {
                break;
            }
        }
        for (Simulator.Pt target : nonTargets) {
            if (!target.get(node.state.state).getNodeType().isWater() && target.get(newstate).getNodeType().isWater()) {
                return false;
            }
        }

        return true;
    }

    public NodeNode pathfind() {
        PriorityQueue<NodeNode> nodes = new PriorityQueue<>(Comparator.comparingDouble((NodeNode a) -> a.f).thenComparing(a -> a.g).thenComparing(NodeNode::hashCode));
        NodeNode start = openNode(begin, 0);
        start.f = fScore(start);
        start.g = 0;
        nodes.add(start);

        int stuff = 0;


        while (!nodes.isEmpty()) {
            NodeNode node = nodes.poll();
            if (isDone(node)) {
                return node; // first solution ggs
            }

            if (stuff % 1000 == 0) {
                System.out.println(stuff + " / " + node.f + " / " + fScore(node) + " / " + nodes.size());
            }
            stuff++;
            if (nodes.size() > 500000) return null;

            for (AdvanceAction availableAction : availableActions) {

                State newState = availableAction.generateNew(node.state);
                if (newState == null) continue; // if nothing happened as result of action, end tree here.
                NodeNode newNodeNode = openNode(newState, node.totalFlips + (availableAction.flips.size() > 0 ? 1 : 0));
                if (newNodeNode == null) continue; // already visited.

                float newG = node.g + availableAction.cost;
                if (newNodeNode.g > newG) {
                    newNodeNode.g = newG;

//                    boolean nope = false;
//                    for (int i = targets.size(); i < newNodeNode.state.flips.length; i++) {
//                        if (newNodeNode.state.flips[i] % 2 == 1) {
//                            nope = true;
//                            break;
//                        }
//                    }
//                    if (nope) continue;

                    float heuristic = fScore(newNodeNode) ;
//                    if (heuristic > 50) continue;
                    newNodeNode.f = newNodeNode.g + heuristic ;// heuristic
                    newNodeNode.parent = node;
                    newNodeNode.parentToMeAction = availableAction;


                    nodes.add(newNodeNode);
                }

            }
        }

        return null;
    }

    public NodeNode openNode(State state, int flips) {
        NodeNode nodeNode = new NodeNode(state, null, null, Float.MAX_VALUE, Float.MAX_VALUE, flips);
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
        @EqualsAndHashCode.Exclude
        private AdvanceAction parentToMeAction;

        @EqualsAndHashCode.Exclude
        private float f;
        @EqualsAndHashCode.Exclude
        private float g;

        private int totalFlips;
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

//            boolean foundWater = false;
            for (Simulator.Pt flip : flips) {
                Simulator.Node node = flip.get(nodes);
//                if (node.getWaterLevel() > 0 ||
//                        flip.left().get(nodes).getWaterLevel() > 0 ||
//                        flip.right().get(nodes).getWaterLevel() > 0 ||
//                        flip.up().get(nodes).getWaterLevel() > 0) foundWater = true;

                if (node.getNodeType() == Simulator.NodeType.BLOCK) flip.set(nodes, Simulator.NodeType.AIR);
                else flip.set(nodes, Simulator.NodeType.BLOCK);
            }
//            if (!flips.isEmpty() && !foundWater) return null;
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
        long start = System.currentTimeMillis();
        String config =
                "X      XWXWX      X\n" +
                "XX      X XX      X\n" +
                "X     XXX X X      \n" +
                "XXXXXXXXX XXXXXXXXX\n" +
                "        X      XXXX\n" +
                "XX X X XXXXXX X XXX\n" +
                "   X X        X    \n" +
                " XX  XXXXX XXXXXXX \n" +
                " XX XXXXXX     XXX \n" +
                "           XXX XXX \n" +
                " XXXXXX XX XXX     \n" +
                "      X XX XXXXXXX \n" +
                "XXX X X XX         \n" +
                "    X X  XXXXXXXX X\n" +
                " XXXX XX X      X X\n" +
                "      XX X XXXXXX X\n" +
                "XXXXX XX X X      X\n" +
                "XX X     X X XXXXX \n" +
                "XX X XXX X X XXXXX \n" +
                "XX X XXX   X       \n" +
                "   X XXXX XXXX XXXX\n" +
                " XXX XXXX XXXX XXX \n" +
                " XXX XXXX XXXX XXX \n" +
                "                   \n" +
                " XXX XXXX XXXX XXX \n";
        String[][] configuration =  Arrays.stream(config.split("\n")).map(a -> a.split("")).toArray(String[][]::new);

        int[] targets = {0, 4, 9, 14, 18};
        boolean[] targetActivate = {false, false, true, true,true};

        Simulator.Node[][] nodes = new Simulator.Node[25][19];
        Map<String, List<Simulator.Pt>> switchFlips = new HashMap<>();
        switchFlips.put("57:0", Arrays.asList(Simulator.Pt.ofPt(2, 9), Simulator.Pt.ofPt(5, 16), Simulator.Pt.ofPt(10, 4), Simulator.Pt.ofPt(14, 20), Simulator.Pt.ofPt(15, 13)));
        switchFlips.put("41:0", Arrays.asList(Simulator.Pt.ofPt(3, 12), Simulator.Pt.ofPt(4, 5), Simulator.Pt.ofPt(13, 5), Simulator.Pt.ofPt(18, 16)));
        switchFlips.put("155:0", Arrays.asList(Simulator.Pt.ofPt(2, 16), Simulator.Pt.ofPt(6, 5), Simulator.Pt.ofPt(8, 4)));
        switchFlips.put("135:0", Arrays.asList(Simulator.Pt.ofPt(2, 5), Simulator.Pt.ofPt(8, 18), Simulator.Pt.ofPt(10, 10), Simulator.Pt.ofPt(15, 4), Simulator.Pt.ofPt(16, 16), Simulator.Pt.ofPt(18, 20)));
        switchFlips.put("5", Arrays.asList());
        switchFlips.put("6", Arrays.asList());
        switchFlips.put("mainStream", Arrays.asList(Simulator.Pt.ofPt(9,0)));

        for (int y = 0; y < configuration.length; y++) {
            for (int x = 0; x < configuration[y].length; x++) {
                Simulator.NodeType nodeType = null;
                String val = configuration[y][x];
                if (val.equals(" ")) {
                    nodeType = Simulator.NodeType.AIR;
                } else if (val.equals("W")) {
                    nodeType = Simulator.NodeType.SOURCE;
                } else if (val.equals("X")) {
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

        WaterPathfinder waterPathfinder = new WaterPathfinder(nodes, ptTargets, notTargets, switchFlips);
        NodeNode result = waterPathfinder.pathfind();
        System.out.println(result.state);
        int totalCost = 0;
        NodeNode treeWalk = result;
        while (treeWalk.parent != null) {
            System.out.println(treeWalk.parentToMeAction.key + "/" + treeWalk.parentToMeAction.cost + "/" + treeWalk.parentToMeAction.moves);

            totalCost += treeWalk.parentToMeAction.moves;
            treeWalk = treeWalk.parent;
        }

        System.out.println(totalCost * 5 / 20.0 +"s");
        System.out.println(System.currentTimeMillis() - start);
//[19:38:47] [DG-WaterPuzzle-Calculator/INFO] (STDOUT) [kr.syeyoung.dungeonsguide.mod.dungeon.roomprocessor.waterpuzzle.WaterPathfinder:<init>:62]: {
//
//            172:0=[],
//            173:0=[],
//            mainStream=[Simulator.Pt(x=9, y=0)]}
    }
}
