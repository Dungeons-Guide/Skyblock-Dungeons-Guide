package kr.syeyoung.dungeonsguide.dungeon.actions.tree;

import kr.syeyoung.dungeonsguide.dungeon.actions.Action;
import kr.syeyoung.dungeonsguide.dungeon.actions.ActionRoot;
import kr.syeyoung.dungeonsguide.dungeon.roomfinder.DungeonRoom;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Data
public class ActionTree {
    @EqualsAndHashCode.Exclude
    private ActionTree parent;
    private Action current;
    private Set<ActionTree> children;

    @Override
    public int hashCode() { return current == null ? 0 : current.hashCode(); }


    public static ActionTree buildActionTree(Set<Action> actions, DungeonRoom dungeonRoom) {
        ActionRoot root = new ActionRoot();
        root.setPreRequisite(actions);
        ActionTree tree = new ActionTree();
        tree.setParent(null);
        tree.setCurrent(root);
        HashSet<ActionTree> set = new HashSet();
        for (Action action : actions) {
            set.add(buildActionTree(tree, action, dungeonRoom, new HashMap<Action, ActionTree>()));
        }
        tree.setChildren(set);
        return tree;
    }


    private static ActionTree buildActionTree(ActionTree parent, Action action, DungeonRoom dungeonRoom, Map<Action, ActionTree> alreadyBuilt) {
        if (action == null) return null;
        if (alreadyBuilt.containsKey(action)) return alreadyBuilt.get(action);

        ActionTree tree = new ActionTree();
        alreadyBuilt.put(action, tree);
        tree.setParent(parent);
        tree.setCurrent(action);
        HashSet<ActionTree> set = new HashSet();
        for (Action action2 : action.getPreRequisites(dungeonRoom)) {
            set.add(buildActionTree(tree, action2, dungeonRoom, alreadyBuilt));
        }
        tree.setChildren(set);
        return tree;
    }
}
