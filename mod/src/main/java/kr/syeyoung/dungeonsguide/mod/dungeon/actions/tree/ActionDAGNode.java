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
import kr.syeyoung.dungeonsguide.mod.dungeon.actions.ActionChangeState;
import kr.syeyoung.dungeonsguide.mod.dungeon.actions.ActionRoot;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomfinder.DungeonRoom;
import lombok.Getter;
import lombok.Setter;

import java.util.*;
import java.util.stream.Collectors;

public class ActionDAGNode {
    @Getter
    private final Set<ActionDAGNode> requiredBy = new HashSet<>();
    @Getter
    private final AbstractAction action;
    @Getter
    private final List<ActionDAGNode> potentialRequires = new ArrayList<>();

    @Setter @Getter
    private NodeType type;
    @Getter @Setter
    private int maximumDepth;

    public ActionDAGNode(AbstractAction action, NodeType type) {
        this.action = action;
        this.type = type;
    }


    public enum NodeType {
        OPTIONAL, AND
    }

    public boolean isDisablable() {
        return requiredBy.stream().allMatch(a -> a.type == NodeType.OPTIONAL) && requiredBy.size() != 0;
    }

    private int bitIdx = -1;
    @Getter @Setter
    private int id = 0;
    public int setIdx(int bitIdx) {
        if (!isDisablable()) return bitIdx;
        this.bitIdx = bitIdx;
        return bitIdx + 1;
    }

    public boolean isComplete(DungeonRoom dungeonRoom) {
        return action.isComplete(dungeonRoom) && (!action.childComplete() || requiredBy.stream().allMatch(a -> a.isComplete(dungeonRoom)));
    }

    public boolean isEnabled(int dagId) {
        if (bitIdx == -1) return requiredBy.size() == 0 || requiredBy.stream().anyMatch(a -> a.isEnabled(dagId));
        return (dagId >> bitIdx  & 0x1) == 1 || requiredBy.stream().anyMatch(a -> a.isEnabled(dagId));
    }

    public List<ActionDAGNode> getPotentialRequires(int dagId) {
        return potentialRequires.stream().filter(a -> a.isEnabled(dagId)).collect(Collectors.toList());
    }

    @Override
    public String toString() {
        return "ActionDAGNode{" +
                "action=" + action +
                ", type=" + type +
                ", id=" + id +
                '}';
    }
}
