package kr.syeyoung.dungeonsguide.mod.dungeon.pathfinding.cachedpathfind;

import kr.syeyoung.dungeonsguide.mod.dungeon.pathfinding.algorithmSetting.AlgorithmSettingRegistry;
import kr.syeyoung.dungeonsguide.mod.features.impl.etc.FeatureCollectDiagnostics;
import lombok.Getter;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PathfindPresetRegistry {
    @Getter
    private List<PathfindPreset> loadedPresets = new ArrayList<>();
    private Map<String, PathfindPreset> presetsById = new HashMap<>();

    @Getter
    private static PathfindPresetRegistry INSTANCE;

    public void register(PathfindPreset preset) {
        if (presetsById.containsKey(preset.getPresetId())) throw new IllegalStateException("Dupliucate preset:: "+preset.getPresetId());
        this.loadedPresets.add(preset);
        this.presetsById.put(preset.getPresetId(), preset);

        AlgorithmSettingRegistry.registerAlgorithmSetting(preset.getAlgorithmSetting());
        for (RoomPreset value : preset.getPresets().values()) {
            if (value.isOverridingParentAlgorithmSetting())
                AlgorithmSettingRegistry.registerAlgorithmSetting(value.getAlgorithmSetting());
        }

    }

    public PathfindPreset getPreset(String presetId) {
        return this.presetsById.get(presetId);
    }

    public boolean unregister(PathfindPreset preset) {
        if (!loadedPresets.remove(preset)) {
            return false;
        }
        presetsById.remove(preset.getPresetId());
        return true;
    }

    public PathfindPresetRegistry(File toWatch) {
        if (INSTANCE != null) throw new IllegalStateException("Already initialized");
        PathfindPresetRegistry.INSTANCE = this;

        loadAll(toWatch);
    }

    private void loadAll(File toWatch) {
        for (File file : toWatch.listFiles()) {
            try {
                register(PathfindPreset.loadFromFile(file));
            } catch (Exception e) {
                FeatureCollectDiagnostics.queueSendLogAsync(e);
                e.printStackTrace();
            }
        }
    }

    public void saveAll() throws IOException {
        for (PathfindPreset loadedPreset : loadedPresets) {
            loadedPreset.save();
        }
    }
}
