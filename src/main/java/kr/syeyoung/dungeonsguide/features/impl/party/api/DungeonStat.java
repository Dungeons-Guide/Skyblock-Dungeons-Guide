package kr.syeyoung.dungeonsguide.features.impl.party.api;

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
