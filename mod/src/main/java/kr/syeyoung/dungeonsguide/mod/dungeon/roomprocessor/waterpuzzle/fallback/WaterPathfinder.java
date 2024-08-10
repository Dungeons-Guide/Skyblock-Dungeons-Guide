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

package kr.syeyoung.dungeonsguide.mod.dungeon.roomprocessor.waterpuzzle.fallback;

import kr.syeyoung.dungeonsguide.mod.NativeLoader;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomprocessor.waterpuzzle.Waterboard;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.IOException;
import java.util.*;

public class WaterPathfinder {
    // A* on waterboard... lol graph search
    Set<AdvanceAction> availableActions = new HashSet<>();


    private State begin;

    private Simulator.Pt[] targets;
    private Simulator.Pt[] nonTargets;
    private int maxMatch = 0;

    public WaterPathfinder(Simulator.Node[][] begin, Simulator.Pt[] targets, Simulator.Pt[] nonTargets, Map<String, Simulator.Pt[]> switchFlips) {
        this.targets = targets;
        this.nonTargets = nonTargets;

        ArrayList<Simulator.Pt> total = new ArrayList();
        total.addAll(Arrays.asList(targets));
        total.addAll(Arrays.asList(nonTargets));
        maxMatch = total.size();
        for (Map.Entry<String, Simulator.Pt[]> stringListEntry : switchFlips.entrySet()) {
            if (stringListEntry.getValue().length != 0)
                availableActions.add(new AdvanceAction(stringListEntry.getValue(), stringListEntry.getKey(), total, 3));
        }

//        availableActions.add(new AdvanceAction(new ArrayList<>(), "nothing", 1, total, 1)); // it can handle 1 moves. yes.
        this.begin = new State(begin, new int[total.size()]);

    }

    private boolean isDone(State state) {
        for (int i = 0; i < state.flips.length; i++) {
            if ((state.flips[i] % 2 == 1) != (i < targets.length)) {
                return false;
            }
        }
        Simulator.Node[][] newstate = Simulator.clone(state.state);
        for (int i = 0; i < 30; i++) {
            if (!Simulator.simulateSingleTick(newstate)) {
                break;
            }
        }
        for (Simulator.Pt target : nonTargets) {
            if (!target.get(state.state).getNodeType().isWater() && target.get(newstate).getNodeType().isWater()) {
                return false;
            }
        }

        return true;
    }

    public List<AdvanceAction> pathfind(double temperatureMultiplier, double targetTemperature, int targetIterations) {
        Random r = new Random(); // yes. Probablistic method.

        ArrayList<Simulator.Pt> total = new ArrayList();
        total.addAll(Arrays.asList(targets));
        total.addAll(Arrays.asList(nonTargets));

        List<AdvanceAction> currentActions = new ArrayList<>();
        for (int i = 0; i < 30; i++) {
            currentActions.add(new AdvanceAction(new Simulator.Pt[0], "nothing", total, 1));
        }

        List<Integer> idxes = new ArrayList<>();
        int idx = currentActions.size();
        for (AdvanceAction availableAction : availableActions) {
            for (int j = 0; j < 3; j ++) {
                currentActions.add(availableAction); // add 15 actions.
                idxes.add(idx++);
            }
        }

        double lastEvaluation = Integer.MAX_VALUE;

        double temperature = 2.3;
        int iteration = 0;
        int lastTarget = 0;
        
        double currentMinimum = 99999999;
        int lastUpdated = 0;
        List<AdvanceAction> currentMinimumActions = Collections.emptyList();
        
        while(true) {
            long start = System.nanoTime();

            int swapX = r.nextInt(currentActions.size());
            int swapY = r.nextInt(idxes.size());

            if (idxes.contains(swapX)) continue;

            int realSwapY = idxes.get(swapY);


            // do swap
            AdvanceAction actionAtX = currentActions.get(swapX);
            AdvanceAction actionAtY = currentActions.get(realSwapY);

            currentActions.set(swapX, actionAtY);
            currentActions.set(realSwapY, actionAtX);

            // did swap.

            // evaluate solution.
            State begin = this.begin;
            int chainLength = 0;
            int untilAction = 0;
            boolean done = false;
            for (AdvanceAction currentAction : currentActions) {
                chainLength += currentAction.moves;
                untilAction += 1;
                begin = currentAction.generateNew(begin);

                if (isDone(begin)) {
                    done = true;
                    break;
                }
            }


            double additionalPenalty = 0;
            if (done) {
                int flipIdxes = 0;
                for (int i = 0; i < untilAction; i++) {
                    if (currentActions.get(i).flips.length != 0) {
                        flipIdxes += i;
                    }
                }
                additionalPenalty += flipIdxes/10.0;
            } else {
                additionalPenalty += 10000;

                for (int i = 0; i < begin.flips.length; i++) {
                    if ((begin.flips[i] % 2 == 1) != (i < targets.length)) {
                        additionalPenalty += 20;
                    }
                }

            }

            double currentEvaluation = additionalPenalty + chainLength;


            double variation = currentEvaluation - lastEvaluation;
            boolean accepted = false;
            if (variation < 0) {
                accepted = true;
                lastTarget = untilAction;
            } else {
                if (r.nextDouble() < Math.exp(-variation / temperature)) {
                    accepted = true;
                }
            }

            if (accepted) {
                idxes.remove(swapY);
                idxes.add(swapX);

                lastEvaluation = currentEvaluation;
            } else {
                currentActions.set(swapX, actionAtX);
                currentActions.set(realSwapY, actionAtY);
            }

            iteration++;
            long dur = System.nanoTime() - start;
            if (iteration % 100 == 0) {
                System.out.println(iteration + "/" + lastEvaluation + "/" + temperature + "/" + chainLength + "/" + currentMinimum + "/" + lastTarget);
                System.out.println("Iteraiton took "+ dur);
            }
            temperature *= temperatureMultiplier;

            if (lastEvaluation > 10000 && temperature < 2.0) {
                temperature = 2.3; // :D
            }
            
            if (currentEvaluation <= currentMinimum) {
                currentMinimum = currentEvaluation;
                lastUpdated = iteration;
                currentMinimumActions = new ArrayList<>(currentActions.subList(0, untilAction));
            }

            if (iteration - lastUpdated > targetIterations && currentMinimum < 10000) {
                return currentMinimumActions;
            }

            if (temperature < targetTemperature) {
                return currentMinimumActions;
            }
        }
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


    @Getter
    @AllArgsConstructor
    public static class AdvanceAction {
        private final Simulator.Pt[] flips;
        private final String key;
        private final List<Simulator.Pt> targets;
        private int moves;

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
            for (int i = 0; i < moves; i ++)
                Simulator.simulateSingleTick(nodes);

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
        boolean[] targetActivate = {true, false, false, true,true};

        Simulator.Node[][] nodes = new Simulator.Node[25][19];
        Map<String, Simulator.Pt[]> switchFlips = new LinkedHashMap<>();
        switchFlips.put("mainStream", Arrays.asList(Simulator.Pt.ofPt(9,0)).toArray(new Simulator.Pt[0]));
        switchFlips.put("57:0", Arrays.asList(Simulator.Pt.ofPt(2, 9), Simulator.Pt.ofPt(5, 16), Simulator.Pt.ofPt(10, 4), Simulator.Pt.ofPt(14, 20), Simulator.Pt.ofPt(15, 13)).toArray(new Simulator.Pt[0]));
        switchFlips.put("41:0", Arrays.asList(Simulator.Pt.ofPt(3, 12), Simulator.Pt.ofPt(4, 5), Simulator.Pt.ofPt(13, 5), Simulator.Pt.ofPt(18, 16)).toArray(new Simulator.Pt[0]));
        switchFlips.put("155:0", Arrays.asList(Simulator.Pt.ofPt(2, 16), Simulator.Pt.ofPt(6, 5), Simulator.Pt.ofPt(8, 4)).toArray(new Simulator.Pt[0]));
        switchFlips.put("135:0", Arrays.asList(Simulator.Pt.ofPt(2, 5), Simulator.Pt.ofPt(8, 18), Simulator.Pt.ofPt(10, 10), Simulator.Pt.ofPt(15, 4), Simulator.Pt.ofPt(16, 16), Simulator.Pt.ofPt(18, 20)).toArray(new Simulator.Pt[0]));
        switchFlips.put("5", new Simulator.Pt[0]);
        switchFlips.put("6", new Simulator.Pt[0]);

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

        try {
            NativeLoader.extractLibraryAndLoad("waterboard");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        Waterboard waterPathfinder = new Waterboard(nodes, ptTargets.toArray(new Simulator.Pt[0]), notTargets.toArray(new Simulator.Pt[0]), switchFlips);
        List<Waterboard.Action> actions = waterPathfinder.solve(0.9999, 0.1, 2000);

        int cost = 0;
        for (Waterboard.Action action : actions) {
            System.out.println(action.getName() + " / "+action.getMove());
            cost += action.getMove();
        }

        System.out.println(cost * 5 / 20.0 +"s");
        System.out.println(System.currentTimeMillis() - start);
//[19:38:47] [DG-WaterPuzzle-Calculator/INFO] (STDOUT) [kr.syeyoung.dungeonsguide.mod.dungeon.roomprocessor.waterpuzzle.fallback.WaterPathfinder:<init>:62]: {
//
//            172:0=[],
//            173:0=[],
//            mainStream=[Simulator.Pt(x=9, y=0)]}
    }
}
