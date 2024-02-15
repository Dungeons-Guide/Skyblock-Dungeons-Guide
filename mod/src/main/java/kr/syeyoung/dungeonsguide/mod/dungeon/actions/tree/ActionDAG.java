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

package kr.syeyoung.dungeonsguide.mod.dungeon.actions.tree;

import kr.syeyoung.dungeonsguide.mod.dungeon.roomfinder.DungeonRoom;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

public class ActionDAG {
    @Getter
    private final int count;
    @Getter
    private final ActionDAGNode actionDAGNode;

    @Getter
    private List<ActionDAGNode> allNodes;

    private List<ActionDAGNode> orNodes;

    private DungeonRoom dungeonRoom;

    public ActionDAG(DungeonRoom dungeonRoom, int count, ActionDAGNode node, List<ActionDAGNode> allNodes) {
        this.dungeonRoom = dungeonRoom;
        this.count = count;
        this.actionDAGNode = node;
        this.allNodes = allNodes;

        orNodes = allNodes.stream().filter(a -> a.getType() == ActionDAGNode.NodeType.OR).collect(Collectors.toList());
    }
    public Iterator<List<ActionDAGNode>> topologicalSortIterator(int dagId) {
        return new TopologicalSortIterator(dagId);
    }
    public Iterable<List<ActionDAGNode>> topologicalSort(int dagId) {
        return new Iterable<List<ActionDAGNode>>() {
            @NotNull
            @Override
            public Iterator<List<ActionDAGNode>> iterator() {
                return new TopologicalSortIterator(dagId);
            }
        };
    }

    public boolean isValidStatus(int[] nodeStatus) {
        for (ActionDAGNode orNode : orNodes) {
            int enabledCount = 0;
            for (ActionDAGNode otentialRequire : orNode.getPotentialRequires()) {
                if (nodeStatus[otentialRequire.getId()] != 0) enabledCount++;
            }
            if (enabledCount != 1) return false;
        }
        return true;
    }

    public int[] getNodeStatus(int dagId) {
        int[] enabled = new int[allNodes.size()];
        // 0: disabled
        // 1: completed
        // 2: pruned due to parent completed
        // 3: enabled
        boolean[] visited = new boolean[allNodes.size()];
        boolean[] complete = new boolean[allNodes.size()];
        Stack<Integer> stack = new Stack<>();
        stack.add(allNodes.size() - 1);
        while (!stack.isEmpty()) {
            int idx = stack.peek();
            ActionDAGNode current = allNodes.get(idx);
            boolean found = false;
            for (int i = 0; i < current.getPotentialRequires().size(); i++) {
                ActionDAGNode next = current.getPotentialRequires().get(i);
                if (visited[next.getId()]) continue;
                if (!next.isEnabled(dagId)) continue;
                stack.push(next.getId());
                found = true;
                break;
            }
            if (found) continue;

            idx = stack.pop();
            enabled[idx] = 3;
            visited[idx] = true;

            if (current.getAction().isComplete(dungeonRoom)) {
                if (current.getAction().childComplete()) {
                    // check if children are complete
                    boolean smh = false;
                    for (ActionDAGNode potentialRequire : current.getPotentialRequires(dagId)) {
                        if (!complete[potentialRequire.getId()]) {
                            smh = true;
                            break;
                        }
                    }
                    if (smh) {
                        complete[idx] = false;
                    } else {
                        complete[idx] = true;
                        enabled[idx] = 1;
                    }
                } else {
                    complete[idx] = true;
                    enabled[idx] = 1;
                }
            }
        }
        Queue<Integer> toVisit = new LinkedList<>();
        toVisit.add(allNodes.size() - 1);
        while (!toVisit.isEmpty()) {
            int node = toVisit.poll();
            boolean smh = allNodes.get(node).getRequiredBy().size() == 0;
            for (ActionDAGNode actionDAGNode : allNodes.get(node).getRequiredBy()) {
                if (enabled[actionDAGNode.getId()] == 3) {
                    smh = true;
                    break;
                }
            }
            if (!smh) {
                enabled[node] = 2;
            }

            for (ActionDAGNode potentialRequire : allNodes.get(node).getPotentialRequires(dagId)) {
                toVisit.add(potentialRequire.getId());
            }
        }
        return enabled;
    }


    public class TopologicalSortIterator implements Iterator<List<ActionDAGNode>> {
        private int dagId;
        private boolean[] visited = new boolean[allNodes.size()];

        private  int[] degree = new int[allNodes.size()];
        private boolean[] sanityCheck = new boolean[allNodes.size()];
        private Stack<Integer> path = new Stack<>();
        private List<ActionDAGNode> nextSolution;
        private int solutionSize;


        private TopologicalSortIterator(int dagId) {
            this.dagId = dagId;

            int[] nodeStatus = getNodeStatus(dagId);
            if (isValidStatus(nodeStatus)) {

                for (int i = 0; i < allNodes.size(); i++) {
                    degree[i] = (int) allNodes.get(i).getPotentialRequires(dagId)
                            .stream().filter(a -> nodeStatus[a.getId()] ==3).count();
                    this.visited[i] = nodeStatus[i] != 3;
                    sanityCheck[i] = allNodes.get(i).getAction().isSanityCheck();
                    if (!this.visited[i]) solutionSize++;
                }
                path.add(0);
                nextSolution = findNext(true);

                if (nextSolution == null) {
                    System.out.println("WTF NULL PATH???");
                }
            }


        }
        @Override
        public boolean hasNext() {
            return nextSolution != null;
        }

        @Override
        public List<ActionDAGNode> next() {
            List<ActionDAGNode> solution = nextSolution;
            nextSolution = findNext(false);
            return solution;
        }

        public List<ActionDAGNode> findNext(boolean first) {
            if (solutionSize == 0 && !first) {
                return null;
            } else if (solutionSize == 0) {
                return new ArrayList<>();
            }
            if (!first) {
                int current = path.pop();
                ActionDAGNode currentNode = allNodes.get(current);
                for (ActionDAGNode dagNode : currentNode.getRequiredBy()) {
                    degree[dagNode.getId()]++;
                }
                visited[currentNode.getId()] = false;
                if (sanityCheck[current])
                    path.push(allNodes.size());
                else
                    path.push(current + 1);
            }
            while(true) {
                boolean found = false;
                for (int i = path.peek(); i < allNodes.size(); i++) {
                    if (!visited[i] && degree[i] == 0 && sanityCheck[i]) {
                        path.pop();
                        path.push(i);
                        break;
                    }
                }
                for (int i = path.peek(); i < allNodes.size(); i++) {
                    if (!visited[i] && degree[i] == 0) {
                        ActionDAGNode currentNode = allNodes.get(i);
                        for (ActionDAGNode dagNode : currentNode.getRequiredBy()) {
                            degree[dagNode.getId()]--;
                        }
                        path.pop();
                        path.push(i);
                        path.push(0);
                        visited[i] = true;
                        found = true;
                        break;
                    }
                }
                if (path.size() == solutionSize+1) {
                    path.pop(); // has weird 0
                    return path.stream().map(allNodes::get).collect(Collectors.toList());
                }
                if (found) {
                    continue;
                }
                // backtrack!

                path.pop();
                if (path.size() == 0) {
                    return null;
                }
                int current = path.pop();
                ActionDAGNode currentNode = allNodes.get(current);
                for (ActionDAGNode dagNode : currentNode.getRequiredBy()) {
                    degree[dagNode.getId()]++;
                }
                visited[currentNode.getId()] = false;
                if (sanityCheck[current])
                    path.push(allNodes.size());
                else
                    path.push(current + 1);
            }
        }
    }
}
