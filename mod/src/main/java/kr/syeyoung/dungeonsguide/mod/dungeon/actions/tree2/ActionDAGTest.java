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
import kr.syeyoung.dungeonsguide.mod.dungeon.actions.ActionRoot;
import kr.syeyoung.dungeonsguide.mod.dungeon.actions.PathfindImpossibleException;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomfinder.DungeonRoom;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;

import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

public class ActionDAGTest {
    @AllArgsConstructor
    @EqualsAndHashCode(callSuper = false)
    public static class DummyAction extends AbstractAction {
        private String name;
        @Override
        public String toString() {
            return name;
        }

        @Override
        public boolean isIdempotent() {
            return true;
        }

        @Override
        public ActionDAGBuilder buildActionDAG(ActionDAGBuilder builder, DungeonRoom dungeonRoom) {
            return builder;
        }
    }
    public static void main(String args[]) throws PathfindImpossibleException {

        ActionDAGBuilder actionDAGBuilder = new ActionDAGBuilder(null);
        actionDAGBuilder
                .requires(new DummyAction("1"))
                    .requires(new DummyAction("2"))
                        .optional(new DummyAction("3")).requires(new DummyAction("8")).end().end()
                        .optional(new DummyAction("4")).end().end()
                    .requires(new DummyAction("5"));

        ActionDAG dag = actionDAGBuilder.build();

        for (int i = 0; i < dag.getCount(); i++) {
            Iterator<List<ActionDAGNode>> sorted = dag.topologicalSort(i);
            while (sorted.hasNext()) {
                System.out.println(sorted.next().stream().map(a -> a.getAction().toString()).collect(Collectors.toList()));
            }
        }


    }
}
