package kr.syeyoung.dungeonsguide.dungeon.actions.tree;

import kr.syeyoung.dungeonsguide.dungeon.actions.Action;

import java.util.*;

public class ActionTreeUtil {
    public static List<Action> linearifyActionTree(ActionTree input) {
        ActionTree tree = copyActionTree(input);

        List<Action> actions = new ArrayList<Action>();

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
