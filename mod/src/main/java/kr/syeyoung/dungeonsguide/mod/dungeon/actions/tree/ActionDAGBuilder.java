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
import kr.syeyoung.dungeonsguide.mod.dungeon.actions.ActionRoot;
import kr.syeyoung.dungeonsguide.mod.dungeon.actions.PathfindImpossibleException;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomfinder.DungeonRoom;
import lombok.Getter;

import java.util.*;
import java.util.function.Supplier;

public class ActionDAGBuilder {
    // honestly I think there should be STATE DAG first but anyways
    private final Map<AbstractAction, ActionDAGNode> idempotentActions;

    protected final DungeonRoom dungeonRoom;
    @Getter
    protected final ActionDAGNode current;
    protected final ActionDAGBuilder parent;

    private ActionDAGBuilder(ActionDAGBuilder parent, ActionDAGNode node) {
        this.dungeonRoom = parent.dungeonRoom;
        this.parent = parent;
        this.current = node;
        this.idempotentActions = parent.idempotentActions;
    }
    public ActionDAGBuilder(DungeonRoom dungeonRoom) {
        this.current = new ActionDAGNode(new ActionRoot());
        this.dungeonRoom = dungeonRoom;
        this.idempotentActions = new HashMap<>();
        this.parent = null;
    }

    public static class ActionDAGBuilderNoMore extends ActionDAGBuilder {
        public ActionDAGBuilderNoMore(ActionDAGBuilder parent) {
            super(parent, null);
        }

        @Override
        public ActionDAGBuilder doAdd(AbstractAction abstractAction, ActionDAGNode.NodeType a) throws PathfindImpossibleException {
            throw new UnsupportedOperationException();
        }
    }

    public ActionDAGBuilder requires(Supplier<AbstractAction> abstractActionSupplier) throws PathfindImpossibleException  {
        AbstractAction abstractAction = abstractActionSupplier.get();
        return doAdd(abstractAction, ActionDAGNode.NodeType.AND);
    }
    public ActionDAGBuilder requires(AbstractAction abstractAction) throws PathfindImpossibleException {
        return doAdd(abstractAction, ActionDAGNode.NodeType.AND);
    }

    public ActionDAGBuilder doAdd(AbstractAction abstractAction, ActionDAGNode.NodeType nodeType) throws PathfindImpossibleException  {
        if (abstractAction.isIdempotent() && idempotentActions.containsKey(abstractAction)) {
            ActionDAGNode actionDAGNode1 = idempotentActions.get(abstractAction);

            if (nodeType == ActionDAGNode.NodeType.AND) {
                Iterator<ActionDAGNode> blah = actionDAGNode1.getRequiredBy().iterator();
                while (blah.hasNext()) {
                    ActionDAGNode node = blah.next();
                    if (node.getOptional().contains(actionDAGNode1)) {
                        blah.remove();
                        node.getOptional().remove(actionDAGNode1);
                    }
                } // OPT + REQ => REMOVE OPT, because well, it's in anyways.
            } else if (nodeType == ActionDAGNode.NodeType.OPTIONAL) {
                for (ActionDAGNode node : actionDAGNode1.getRequiredBy()) {
                    if (node.getRequire().contains(actionDAGNode1)) {
                        return new ActionDAGBuilderNoMore(this);
                    }
                } // OPT + REQ => REMOVE OPT, because well, it's in anyways.
            }


            if (nodeType == ActionDAGNode.NodeType.AND)
                current.getRequire().add(actionDAGNode1);
            else if (nodeType == ActionDAGNode.NodeType.OR)
                current.getOr().add(actionDAGNode1);
            else if (nodeType == ActionDAGNode.NodeType.OPTIONAL)
                current.getOptional().add(actionDAGNode1);
            // art of combining.

            // Here are the rules
            // 1. REQ + REQ => KEEP
            // 2. OR + REQ => KEEP
            // 3. OPT + REQ => Remove OPT
            // 4. OR + OR => KEEP
            // 5. OR + OPT => KEEP
            // 6. OPT + OPT => KEEP
            actionDAGNode1.setMaximumDepth(Math.max(current.getMaximumDepth() + 1, actionDAGNode1.getMaximumDepth()));
            actionDAGNode1.getRequiredBy().add(current);
            return new ActionDAGBuilderNoMore(this);
        }

        ActionDAGNode child = new ActionDAGNode(abstractAction);
        if (nodeType == ActionDAGNode.NodeType.AND)
            current.getRequire().add(child);
        else if (nodeType == ActionDAGNode.NodeType.OR)
            current.getOr().add(child);
        else if (nodeType == ActionDAGNode.NodeType.OPTIONAL)
            current.getOptional().add(child);
        child.getRequiredBy().add(current);

        child.setMaximumDepth(current.getMaximumDepth() + 1);

        if (abstractAction.isIdempotent()) {
            idempotentActions.put(abstractAction, child);
        }

        ActionDAGBuilder actionDAGBuilder = new ActionDAGBuilder(this, child);
        actionDAGBuilder = abstractAction.buildActionDAG(actionDAGBuilder, dungeonRoom);
        return actionDAGBuilder;
    }
    public ActionDAGBuilder end() {
        return parent;
    }

    public ActionDAGBuilder optional(Supplier<AbstractAction> abstractActionSupplier) throws PathfindImpossibleException {
        AbstractAction abstractAction = abstractActionSupplier.get();
        return optional(abstractAction);
    }
    public ActionDAGBuilder optional(AbstractAction abstractAction) throws PathfindImpossibleException {
        return doAdd(abstractAction, ActionDAGNode.NodeType.OPTIONAL);
    }

    public ActionDAGBuilder or(Supplier<AbstractAction> abstractActionSupplier) throws PathfindImpossibleException {
        AbstractAction abstractAction = abstractActionSupplier.get();
        return or(abstractAction);
    }
    public ActionDAGBuilder or(AbstractAction abstractAction) throws PathfindImpossibleException {
        return doAdd(abstractAction, ActionDAGNode.NodeType.OR);
    }

    public ActionDAGBuilder getRoot() {
        if (parent != null) return parent.getRoot();
        return this;
    }
    public ActionDAG build() {
        if (parent != null)
            return getRoot().build();
        Stack<ActionDAGNode> dfs = new Stack<>();
        dfs.push(current);
        Set<ActionDAGNode> visited = new HashSet<>();
        List<ActionDAGNode> allTheNodes = new ArrayList<>();
        int idx = 1;
        int id = 0;
        while (!dfs.isEmpty()) {
            ActionDAGNode current =dfs.peek();
            ActionDAGNode next = current.getAllChildren()
                    .stream().filter(a -> !visited.contains(a))
                    .findFirst().orElse(null);
            if (next == null) {
                visited.add(current);
                dfs.pop();
                idx = current.setIdx(idx);
                current.setId(id++);
                allTheNodes.add(current);
                continue;
            }
            if (dfs.contains(next)) throw new IllegalStateException("Cycle detected!");
            dfs.push(next);
        }
        return new ActionDAG(dungeonRoom, idx, current, allTheNodes);
    }
}
