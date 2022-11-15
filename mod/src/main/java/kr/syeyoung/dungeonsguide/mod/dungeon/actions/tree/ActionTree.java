/*
 * Dungeons Guide - The most intelligent Hypixel Skyblock Dungeons Mod
 * Copyright (C) 2021  cyoung06
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
import kr.syeyoung.dungeonsguide.mod.dungeon.roomfinder.DungeonRoom;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.jetbrains.annotations.NotNull;

import java.util.*;

@Data
public class ActionTree implements Cloneable {
    @EqualsAndHashCode.Exclude
    private Set<ActionTree> parent;
    private AbstractAction current;
    private Set<ActionTree> children;

    @Override
    public int hashCode() { return current == null ? 0 : current.hashCode(); }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ActionTree that = (ActionTree) o;
        return Objects.equals(parent, that.parent) && Objects.equals(current, that.current) && Objects.equals(children, that.children);
    }

    public static ActionTree buildActionTree(Set<AbstractAction> actions, DungeonRoom dungeonRoom) {
        ActionRoot root = new ActionRoot();
        root.setPreRequisite(actions);
        ActionTree tree = new ActionTree();
        tree.setParent(new HashSet<>());
        tree.setCurrent(root);
        HashSet<ActionTree> set = new HashSet<>();
        for (AbstractAction action : actions) {
            set.add(buildActionTree(tree, action, dungeonRoom, new HashMap<>()));
        }
        tree.setChildren(set);
        return tree;
    }
    public static ActionTree buildActionTree(AbstractAction actions, DungeonRoom dungeonRoom) {
        return buildActionTree(null, actions, dungeonRoom, new HashMap<>());
    }



    private static ActionTree buildActionTree(ActionTree parent, @NotNull AbstractAction action,@NotNull DungeonRoom dungeonRoom, @NotNull Map<AbstractAction, ActionTree> alreadyBuilt) {
        if (alreadyBuilt.containsKey(action))  {
            ActionTree tree = alreadyBuilt.get(action);
            tree.getParent().add(parent);
            return tree;
        }

        ActionTree tree = new ActionTree();
        alreadyBuilt.put(action, tree);
        tree.setParent(new HashSet<>());
        if (parent != null) {
            tree.getParent().add(parent);
        }
        tree.setCurrent(action);
        HashSet<ActionTree> set = new HashSet<>();

        Set<AbstractAction> preRequisites = action.getPreRequisites(dungeonRoom);
        if(preRequisites != null){
            for (AbstractAction action2 : preRequisites) {
                ActionTree e = buildActionTree(tree, action2, dungeonRoom, alreadyBuilt);
                set.add(e);
            }
        }

        tree.setChildren(set);
        return tree;
    }
}
