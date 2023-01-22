/*
 * Dungeons Guide - The most intelligent Hypixel Skyblock Dungeons Mod
 * Copyright (C) 2023  cyoung06 (syeyoung)
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

package kr.syeyoung.dungeonsguide.mod.parallelUniverse.scoreboard;

import java.util.HashMap;
import java.util.Map;

public class ScoreboardManager {

    public static final ScoreboardManager INSTANCE = new ScoreboardManager();
    private ScoreboardManager() {}

    private Map<String, Objective> objectiveMap = new HashMap<>();
    private String sidebarObjective;
    private String tablistObjective;
    private String belowNameObjective;

    public void displayScoreboard(int slot, String objectiveName) {
        if (slot == 0) tablistObjective = objectiveName;
        else if (slot == 1) sidebarObjective = objectiveName;
        else if (slot == 2) belowNameObjective = objectiveName;
    }

    public Objective getSidebarObjective() {
        return objectiveMap.get(sidebarObjective);
    }
    public Objective getTablistObjective() {
        return objectiveMap.get(tablistObjective);
    }
    public Objective getBelownameObjective() {
        return objectiveMap.get(belowNameObjective);
    }

    public Objective getObjective(String name) {
        return objectiveMap.get(name);
    }

    public void addObjective(Objective objective) {
        objectiveMap.put(objective.getObjectiveName(), objective);
    }

    public void removeObjective(String name){
        objectiveMap.remove(name);

        if (name.equals(sidebarObjective)) sidebarObjective = null;
        if (name.equals(tablistObjective)) tablistObjective = null;
        if (name.equals(belowNameObjective)) belowNameObjective = null;
    }


    public void clear() {
        objectiveMap.clear();
        belowNameObjective = null; tablistObjective = null; sidebarObjective = null;
    }
}
