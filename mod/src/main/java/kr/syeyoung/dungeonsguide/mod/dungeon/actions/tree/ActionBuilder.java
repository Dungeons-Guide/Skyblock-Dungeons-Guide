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
import kr.syeyoung.dungeonsguide.mod.dungeon.actions.AtomicAction;
import kr.syeyoung.dungeonsguide.mod.dungeon.actions.PathfindImpossibleException;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomfinder.DungeonRoom;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;

public class ActionBuilder {
    private ActionBuilder(DungeonRoom dungeonRoom, ActionBuilder parent, Set<AbstractAction> preRequisite) {
        this.preRequisite = preRequisite;
        this.dungeonRoom = dungeonRoom;
        this.parent = parent;
    }
    public ActionBuilder(DungeonRoom dungeonRoom) {
        this.preRequisite = new HashSet<>();
        this.dungeonRoom = dungeonRoom;
    }
    private DungeonRoom dungeonRoom;
    private Set<AbstractAction> preRequisite;
    private ActionBuilder parent;

    public ActionBuilder requiresDo(Supplier<AbstractAction> abstractActionSupplier) throws PathfindImpossibleException  {
        AbstractAction abstractAction = abstractActionSupplier.get();
        preRequisite.add(abstractAction);
        return new ActionBuilder(dungeonRoom, this, abstractAction.getPreRequisites(dungeonRoom));
    }

    public ActionBuilder requiresDo(AbstractAction abstractAction) throws PathfindImpossibleException  {
        preRequisite.add(abstractAction);
        return new ActionBuilder(dungeonRoom, this, abstractAction.getPreRequisites(dungeonRoom));
    }
    public ActionBuilder exit() {
        return parent;
    }

    public ActionBuilder and(Supplier<AbstractAction> abstractActionSupplier) {
        AbstractAction abstractAction = abstractActionSupplier.get();
        preRequisite.add(abstractAction);
        return this;
    }
    public ActionBuilder and(AbstractAction abstractAction) {
        preRequisite.add(abstractAction);
        return this;
    }


    public Set<AbstractAction> getPreRequisites() {
        if (parent != null) return parent.getPreRequisites();
        return preRequisite;
    }
    public AtomicAction toAtomicAction(String name)throws PathfindImpossibleException {
        if (parent != null) {
            return parent.toAtomicAction(name);
        }
        ActionTree actionTree = ActionTree.buildActionTree(preRequisite, dungeonRoom);
        AtomicAction atomicAction = new AtomicAction(ActionTreeUtil.linearifyActionTree(actionTree), name);
        return atomicAction;
    }
}
