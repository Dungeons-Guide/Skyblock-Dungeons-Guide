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

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import net.minecraft.scoreboard.IScoreObjectiveCriteria;

import java.util.*;

@RequiredArgsConstructor
public class Objective {
    @Getter
    private final String objectiveName;
    @Getter @Setter
    private String displayName;
    @Getter @Setter
    private IScoreObjectiveCriteria.EnumRenderType displayType;
    private SortedSet<Score> scores = new TreeSet<>(Comparator.comparingInt(Score::getScore).reversed());
    private Map<String, Score> currentObjects = new HashMap<>();

    public SortedSet<Score> getScores() {
        return Collections.unmodifiableSortedSet(scores);
    }

    public void updateScore(String playerName, int score) {
        removeScore(playerName);
        addScore(playerName, score);
    }

    public void removeScore(String playerName) {
        if (currentObjects.containsKey(playerName)) {
            Score scoreObj = currentObjects.remove(playerName);
            scores.remove(scoreObj);
        }
    }
    public void addScore(String playerName, int score) {
        if (!currentObjects.containsKey(playerName)) {
            Score scoreObj = new Score(playerName, score);
            currentObjects.put(playerName, scoreObj);
            scores.add(scoreObj);
        }
    }

}
