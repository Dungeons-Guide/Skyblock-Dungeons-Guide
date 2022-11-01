/*
 *     Dungeons Guide - The most intelligent Hypixel Skyblock Dungeons Mod
 *     Copyright (C) 2021  cyoung06
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Affero General Public License as published
 *     by the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Affero General Public License for more details.
 *
 *     You should have received a copy of the GNU Affero General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package kr.syeyoung.dungeonsguide.mod.dungeon.actions.tree;

import kr.syeyoung.dungeonsguide.mod.dungeon.actions.AbstractAction;

import java.util.*;

public class ActionTreeUtil {
    public static List<AbstractAction> linearifyActionTree(ActionTree input) {
        ActionTree tree = copyActionTree(input);

        List<AbstractAction> actions = new ArrayList<AbstractAction>();

        int plsHalt = 0;
        while (tree.getChildren().size() != 0) {
            plsHalt ++;
            if (plsHalt > 1000000) throw new IllegalStateException("Linearifying process ran for 1 million cycle");
            Set<ActionTree> visited = new HashSet<ActionTree>();
            ActionTree curr = tree;

            int plsHalt2 = 0;
            while (curr.getChildren().size() != 0) {
                plsHalt2 ++;
                if (plsHalt2 > 1000000) throw new IllegalStateException("Finding the leaf of tree ran for 1 million cycles");
                if (visited.contains(curr)) throw new IllegalStateException("Circular Reference Detected");
                visited.add(curr);
                curr = curr.getChildren().iterator().next();
            }

            plsHalt2 =0;
            while(curr.getChildren().size() == 0) {
                plsHalt2 ++;
                if (plsHalt2 > 1000000) throw new IllegalStateException("Building of array ran for 1 million cycles");

                actions.add(curr.getCurrent());
                if (curr.getParent().size() == 0) break;
                for (ActionTree parentTree:curr.getParent())
                    parentTree.getChildren().remove(curr);
                curr = curr.getParent().iterator().next();
            }
        }
        return actions;
    }

    public static ActionTree copyActionTree(ActionTree tree) {
        Map<ActionTree, ActionTree> built = new HashMap<ActionTree, ActionTree>();
        if (tree.getParent().size() != 0) throw new IllegalArgumentException("that is not head of tree");
        return copyActionTree(tree, built);
    }
    private static ActionTree copyActionTree(ActionTree tree, Map<ActionTree, ActionTree> preBuilts) {
        if (preBuilts.containsKey(tree)) return preBuilts.get(tree);

        ActionTree clone = new ActionTree();
        preBuilts.put(tree, clone);

        clone.setCurrent(tree.getCurrent());
        clone.setParent(new HashSet<ActionTree>());
        clone.setChildren(new HashSet<ActionTree>());
        for (ActionTree tree3 : tree.getChildren()) {
            ActionTree clone3 = copyActionTree(tree3, preBuilts);
            clone3.getParent().add(clone);
            clone.getChildren().add(clone3);
        }
        return clone;
    }
}
