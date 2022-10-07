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

package kr.syeyoung.dungeonsguide.features.impl.party.playerpreview.api.playerprofile.dataclasses;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class DungeonStat {
    private int highestCompleted;
    private double experience;

    private Map<Integer, FloorSpecificData<PlayedFloor>> plays = new HashMap<>();
    @Data
    public static class PlayedFloor {
        private int times_played;
        private int completions;
        private int watcherKills;

        private int fastestTime;
        private int fastestTimeS;
        private int fastestTimeSPlus;
        private int bestScore;

        private int mostMobsKilled;
        private int mobsKilled;

        private Map<DungeonClass, ClassSpecificData<ClassStatistics>> classStatistics = new HashMap<>();
        @Data
        public static class ClassStatistics {
            private double mostDamage;
        }

        private double mostHealing;
    }
}
