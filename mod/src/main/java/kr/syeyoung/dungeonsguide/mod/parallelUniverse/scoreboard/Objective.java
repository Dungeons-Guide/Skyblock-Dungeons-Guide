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
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.numbers.NumberFormat;
import net.minecraft.world.scores.criteria.ObjectiveCriteria;

import java.util.*;

@RequiredArgsConstructor
public class Objective {
    @Getter
    private final String objectiveName;
    @Getter @Setter
    private Component displayName;
    @Getter @Setter
    private ObjectiveCriteria.RenderType displayType;
    @Getter @Setter
    private NumberFormat numberFormat;
    private SortedSet<Score> scores = new TreeSet<>(Comparator.comparingInt(Score::getScore).reversed());
    private Map<String, Score> currentObjects = new HashMap<>();

    public SortedSet<Score> getScores() {
        return Collections.unmodifiableSortedSet(scores);
    }

    public void updateScore(String playerName, int score, Component display, NumberFormat numberFormat) {
        removeScore(playerName);
        addScore(playerName, score, display, numberFormat);
    }

    public void removeScore(String playerName) {
        if (currentObjects.containsKey(playerName)) {
            Score scoreObj = currentObjects.remove(playerName);
            scores.remove(scoreObj);
        }
    }
    public void addScore(String playerName, int score, Component display, NumberFormat numberFormat) {
        if (!currentObjects.containsKey(playerName)) {
            Score scoreObj = new Score(playerName, score, display, numberFormat);
            currentObjects.put(playerName, scoreObj);
            scores.add(scoreObj);
        }
    }

}
