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

import kr.syeyoung.dungeonsguide.mod.dungeon.actions.AbstractAction;
import lombok.Getter;
import lombok.Setter;

import java.util.*;

public class ActionDAGNode {
    @Getter
    private final Set<ActionDAGNode> requiredBy = new HashSet<>();
    @Getter
    private final AbstractAction action;


    @Getter
    private final List<ActionDAGNode> or = new ArrayList<>();
    @Getter
    private final List<ActionDAGNode> optional = new ArrayList<>();
    @Getter
    private final List<ActionDAGNode> require = new ArrayList<>();

    @Getter @Setter
    private int maximumDepth;

    public ActionDAGNode(AbstractAction action) {
        this.action = action;
    }


    public enum NodeType {
        OPTIONAL, AND, OR
    }

    private int orFactor = -1;
    private int optFactor = -1;
    @Getter @Setter
    private int id = 0;
    public int setIdx(int bitIdx) {
        boolean shouldOpt = false;
        for (ActionDAGNode actionDAGNode : getRequiredBy()) {
            if (actionDAGNode.getOptional().contains(this)) {
                shouldOpt = true;
            }
        }
        if (shouldOpt) {
            optFactor = bitIdx;
            bitIdx *= 2;
        }
        if (or.size() > 0) {
            orFactor = bitIdx;
            bitIdx *= or.size();
        }
        return bitIdx;
    }
    public boolean isOptIncluded(int dagId) {
        return (dagId / optFactor) % 2 == 1;
    }
    public List<ActionDAGNode> getPotentialRequires(int dagId) {
        List<ActionDAGNode> nodes = new ArrayList<>(require);
        if (orFactor > 0) {
            int stuff = (dagId / orFactor) % or.size();
            nodes.add(or.get(stuff));
        }
        for (ActionDAGNode actionDAGNode : optional) {
            if (actionDAGNode.isOptIncluded(dagId)) {
                nodes.add(actionDAGNode);
            }
        }
        return nodes;
    }

    public List<ActionDAGNode> getAllChildren() {
        List<ActionDAGNode> nodes = new ArrayList<>(require);
        nodes.addAll(or);
        nodes.addAll(optional);
        return nodes;
    }
    @Override
    public String toString() {
        return "ActionDAGNode{" +
                "action=" + action +
                ", id=" + id +
                '}';
    }
}
