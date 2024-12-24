package kr.syeyoung.dungeonsguide.mod.dungeon.pathfinding.algorithmSetting;

import kr.syeyoung.dungeonsguide.mod.dungeon.pathfinding.AlgorithmSetting;

import java.util.HashSet;
import java.util.Set;

public class AlgorithmSettingRegistry {
    private static final Set<AlgorithmSetting> ALGORITHM_SETTINGS = new HashSet<>();

    public static void registerAlgorithmSetting(AlgorithmSetting algorithmSetting) {
        if (algorithmSetting == null) throw new IllegalArgumentException("Algorithm Setting is null!");
        ALGORITHM_SETTINGS.add(algorithmSetting);
    }

    public static Set<AlgorithmSetting> getAlgorithmSettings() {
        return ALGORITHM_SETTINGS;
    }
}
