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

package kr.syeyoung.dungeonsguide.mod.dungeon.actions.tree2;

import kr.syeyoung.dungeonsguide.mod.dungeon.actions.AbstractAction;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

public class ActionDAG {
    @Getter
    private final int count;
    @Getter
    private final ActionDAGNode actionDAGNode;

    private List<ActionDAGNode> allNodes;

    public ActionDAG(int count, ActionDAGNode node, List<ActionDAGNode> allNodes) {
        this.count = count;
        this.actionDAGNode = node;
        this.allNodes = allNodes;
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




    public class TopologicalSortIterator implements Iterator<List<ActionDAGNode>> {
        private int dagId;
        private boolean[] visited = new boolean[allNodes.size()];
        private  int[] degree = new int[allNodes.size()];
        private Stack<Integer> path = new Stack<>();
        private List<ActionDAGNode> nextSolution;
        private int solutionSize;


        private TopologicalSortIterator(int dagId) {
            this.dagId = dagId;

            for (int i = 0; i < allNodes.size(); i++) {
                degree[i] = allNodes.get(i).getPotentialRequires(dagId).size();
                visited[i] = !allNodes.get(i).isEnabled(dagId);
                if (!visited[i]) solutionSize++;
            }
            path.add(0);
            nextSolution = findNext();
        }
        @Override
        public boolean hasNext() {
            return nextSolution != null;
        }

        @Override
        public List<ActionDAGNode> next() {
            List<ActionDAGNode> solution = nextSolution;
            nextSolution = findNext();
            return solution;
        }

        public List<ActionDAGNode> findNext() {
            if (path.size() == solutionSize) {
                int current = path.pop();
                ActionDAGNode currentNode = allNodes.get(current);
                for (ActionDAGNode dagNode : currentNode.getRequiredBy()) {
                    degree[dagNode.getId()]++;
                }
                visited[currentNode.getId()] = false;
                path.push(current + 1);
            }
            while(true) {
                boolean found = false;
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
                path.push(current + 1);
            }
        }
    }
}
