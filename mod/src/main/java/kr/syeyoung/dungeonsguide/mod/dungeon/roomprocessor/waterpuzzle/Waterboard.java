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

package kr.syeyoung.dungeonsguide.mod.dungeon.roomprocessor.waterpuzzle;

import kr.syeyoung.dungeonsguide.mod.dungeon.roomprocessor.waterpuzzle.fallback.Simulator;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomprocessor.waterpuzzle.fallback.WaterPathfinder;
import lombok.AllArgsConstructor;
import net.minecraft.client.renderer.texture.Stitcher;
import scala.actors.threadpool.Arrays;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


public class Waterboard {
    private Simulator.Node[][] currentState;
    private Simulator.Pt[] targets;
    private Simulator.Pt[] nonTargets;
    private Map<String, Simulator.Pt[]> switchFlips;


    public Waterboard(Simulator.Node[][] currentState, Simulator.Pt[] targets, Simulator.Pt[] nonTargets, Map<String, Simulator.Pt[]> switchFlips) {
        this.currentState = currentState;
        this.targets = targets;
        this.nonTargets = nonTargets;
        this.switchFlips = switchFlips;
        Iterator<Map.Entry<String, Simulator.Pt[]>> entryIterator = switchFlips.entrySet().iterator();
        while (entryIterator.hasNext()) {
            Map.Entry<String, Simulator.Pt[]> entry = entryIterator.next();
            if (entry.getValue().length == 0) entryIterator.remove();
        }
    }

    public static class Action {
        private int move;
        private String name;

        public Action(int move, String name) {
            this.move = move;
            this.name = name;
        }

        public int getMove() {
            return move;
        }

        public String getName() {
            return name;
        }
    }

    public native Action[] nativeSolve(double temperatureMultiplier, double targetTemperature, int targetIterations);

    public List<Action> solveUsingFallback(double temperatureMultiplier, double targetTemperature, int targetIterations) {
        WaterPathfinder waterPathfinder = new WaterPathfinder(currentState, targets, nonTargets, switchFlips);
        List<WaterPathfinder.AdvanceAction> actions = waterPathfinder.pathfind(temperatureMultiplier, targetTemperature, targetIterations);

        List<Action> actionList = new ArrayList<>(actions.size());
        for (WaterPathfinder.AdvanceAction action : actions) {
            actionList.add(new Action(action.getMoves(), action.getKey()));
        }
        return actionList;
    }

    public List<Action> solve(double temperatureMultiplier, double targetTemperature, int targetIterations) {
        return Arrays.asList(nativeSolve(temperatureMultiplier, targetTemperature, targetIterations));
    }
}
