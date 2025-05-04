package kr.syeyoung.dungeonsguide.mod.pathfinding.preset;

import kr.syeyoung.dungeonsguide.mod.pathfinding.abilitysetting.AlgorithmSettingRegistry;
import kr.syeyoung.dungeonsguide.mod.features.impl.etc.FeatureCollectDiagnostics;
import kr.syeyoung.dungeonsguide.mod.pathfinding.precalculation.TSPCacheRegistry;
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

    public static PathfindPreset DEFAULT_PRESET;

    static {
        try {
            DEFAULT_PRESET = PathfindPreset.loadFromStream(PathfindPresetRegistry.class.getResourceAsStream("/defaultPreset.json"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    public void register(PathfindPreset preset) {
        if (presetsById.containsKey(preset.getPresetId())) throw new IllegalStateException("Dupliucate preset:: "+preset.getPresetId());
        this.loadedPresets.add(preset);
        this.presetsById.put(preset.getPresetId(), preset);

        AlgorithmSettingRegistry.registerAlgorithmSetting(preset.getAlgorithmSetting());
        for (RoomPreset value : preset.getPresets().values()) {
            if (value.isOverridingParentAlgorithmSetting())
                AlgorithmSettingRegistry.registerAlgorithmSetting(value.getAlgorithmSetting());
        }
        if (TSPCacheRegistry.getINSTANCE() != null)
            TSPCacheRegistry.getINSTANCE().loadPreset(preset);
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

        register(DEFAULT_PRESET);
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
            if (loadedPreset == DEFAULT_PRESET) continue;
            loadedPreset.save();
        }
    }
}
