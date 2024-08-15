package kr.syeyoung.dungeonsguide.mod.dungeon.actions.route;

import kr.syeyoung.dungeonsguide.mod.chat.ChatTransmitter;
import kr.syeyoung.dungeonsguide.mod.dungeon.actions.tree.ActionDAG;
import kr.syeyoung.dungeonsguide.mod.dungeon.actions.tree.ActionDAGNode;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomfinder.DungeonRoom;
import lombok.AllArgsConstructor;
import lombok.Data;
import net.minecraft.util.Vec3;

import java.util.*;

public class TravelingSalesman {

    private static boolean checkImpossible(ActionDAGNode p1Node, List<ActionDAGNode> solution, int nodeIdx, int dagId) {
        for (ActionDAGNode actionDAGNode : p1Node.getRequiredBy()) {
            int idx = solution.indexOf(actionDAGNode);
            if (idx < nodeIdx) {
                return true;
            }
        }
        for (ActionDAGNode potentialRequire : p1Node.getPotentialRequires(dagId)) {
            int idx = solution.indexOf(potentialRequire);
            if (idx > nodeIdx) {
                return true;
            }
        }
        return false;
    }

    public static PartialCalculationResult annealing(int dagId, ActionDAG dag, Vec3 start, DungeonRoom dungeonRoom, Map<String, Object> memoization) {
        Random r = new Random();
        int cnt = 0;
        int actualMoves = 0;
        double localMinCost = Double.POSITIVE_INFINITY;
        List<ActionDAGNode> localMinCostRoute = null;

        List<ActionDAGNode> currentSolution = dag.topologicalSort(dagId).iterator().next(); // get initial solution
        double lastScore = Double.POSITIVE_INFINITY;
        double temperature = 100;
        int len = currentSolution.size();
        while(true) {
            cnt++;

//            if (cnt % 10000 == 0)
//                System.out.println(cnt+"-"+dagId+" : " + lastScore+ " / "+temperature);

            if (cnt > 5000 && lastScore > 1000000000) {
                break;
            }

            if (cnt > 1000000)  {
                ChatTransmitter.sendDebugChat("While traversing "+dagId+ " limit of 1000000 was reached :: "+actualMoves);
                break;
            }

            int p1 = r.nextInt(len);
            int p2 = r.nextInt(len);

            // swap.
            ActionDAGNode p1Node = currentSolution.get(p1);
            ActionDAGNode p2Node = currentSolution.get(p2);

            currentSolution.set(p1, p2Node);
            currentSolution.set(p2, p1Node);
            // check if possible.

            if (checkImpossible(p1Node, currentSolution, p2, dagId) || checkImpossible(p2Node, currentSolution, p1, dagId)) {
                // revert
                currentSolution.set(p1, p1Node);
                currentSolution.set(p2, p2Node);
                continue;
            }



            RoomState roomState = new RoomState();
            roomState.setPlayerPos(start);
            double cost = 0;
            for (ActionDAGNode actionDAGNode : currentSolution) {
                cost += actionDAGNode.getAction().evalulateCost(roomState, dungeonRoom, memoization);
                if (cost == Double.POSITIVE_INFINITY) break;
            }
            if (cost < localMinCost) {
                localMinCost = cost;
                localMinCostRoute = new ArrayList<>(currentSolution);
            }

            double variation = cost - lastScore;
            if (variation < 0 || r.nextDouble() < Math.exp(-variation / temperature)) {
                lastScore = cost;
            } else {
                currentSolution.set(p1, p1Node);
                currentSolution.set(p2, p2Node);
            }

            actualMoves++;

            temperature *= 0.999;
            if (temperature < 0.1) {
                break;
            }
        }

        // process solution
        if (localMinCostRoute != null) {
            Set<ActionDAGNode> sanityChecks = new HashSet<>();
            for (ActionDAGNode actionDAGNode : localMinCostRoute) {
                if (actionDAGNode.getAction().isSanityCheck())
                    sanityChecks.add(actionDAGNode);
            }
            for (ActionDAGNode sanityCheck : sanityChecks) {
                localMinCostRoute.remove(sanityCheck);
                int maxIdx = 0;
                for (ActionDAGNode potentialRequire : sanityCheck.getPotentialRequires(dagId)) {
                    int idx2 = localMinCostRoute.indexOf(potentialRequire);
                    if (idx2 > maxIdx) maxIdx = idx2;
                }
                System.out.println(sanityCheck + " to " + (maxIdx + 1));
                localMinCostRoute.add(maxIdx + 1, sanityCheck);
            }
        }

        return new PartialCalculationResult(dagId, localMinCostRoute, localMinCost, cnt);
    }

    public static PartialCalculationResult bruteforce(int dagId, ActionDAG dag, Vec3 start, DungeonRoom dungeonRoom, Map<String, Object> memoization) {
        int[] nodeStatus = dag.getNodeStatus(dagId);
        int cnt = 0;
        double localMinCost = Double.POSITIVE_INFINITY;
        List<ActionDAGNode> localMinCostRoute = null;

        for (List<ActionDAGNode> actionDAGNodes : dag.topologicalSort(dagId)) {
            cnt++;

            RoomState roomState = new RoomState();
            roomState.setPlayerPos(start);
            double cost = 0;
            for (ActionDAGNode actionDAGNode : actionDAGNodes) {
                cost += actionDAGNode.getAction().evalulateCost(roomState, dungeonRoom, memoization);
                if (cost == Double.POSITIVE_INFINITY) break;
            }
            if (cost < localMinCost) {
                localMinCost = cost;
                localMinCostRoute = actionDAGNodes;
            }

            if (cnt > 1000000)  {
                ChatTransmitter.sendDebugChat("While traversing "+dagId+ " limit of 1000000 was reached");
                break;
            }
        }
        return new PartialCalculationResult(dagId, localMinCostRoute, localMinCost, cnt);
    }

    @Data
    @AllArgsConstructor
    public static class PartialCalculationResult {
        private int dagId;
        private List<ActionDAGNode> route;
        private double cost;
        private int searchSpace;
    }
}
