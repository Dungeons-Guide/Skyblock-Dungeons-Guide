package kr.syeyoung.dungeonsguide.mod.features.impl.secret.precalclist;

import kr.syeyoung.dungeonsguide.mod.config.types.TCString;
import kr.syeyoung.dungeonsguide.mod.features.FeatureParameter;
import kr.syeyoung.dungeonsguide.mod.features.SimpleFeature;
import kr.syeyoung.dungeonsguide.mod.gui.Widget;
import kr.syeyoung.dungeonsguide.mod.pathfinding.preset.PathfindPreset;
import kr.syeyoung.dungeonsguide.mod.pathfinding.preset.PathfindPresetRegistry;

import java.util.List;

public class FeaturePrecalcList extends SimpleFeature {
    public FeaturePrecalcList() {
        super("Pathfinding & Secrets", "Precalculations", "Browse Precalculations", "secret.precalclist");
        setEnabled(true);

        addParameter("chosenOne", new FeatureParameter<String>("chosenOne", "", "", "0d4644ea-a02a-4407-a7f3-f9cd33b27ee7", TCString.INSTANCE, (o) -> {this.selectedPresetId = o;}));
    }

    private String selectedPresetId;

    public String getSelectedPresetId() {
        return getSelectedPreset().getPresetId();
    }

    public PathfindPreset getSelectedPreset() {
        PathfindPreset preset = PathfindPresetRegistry.getINSTANCE().getPreset(selectedPresetId);
        if (preset == null) return PathfindPresetRegistry.DEFAULT_PRESET;
        return preset;
    }

    public void setSelectedPreset(PathfindPreset preset) {
        if (preset != null)
            this.<String>getParameter("chosenOne").setValue(preset.getPresetId());
    }

    @Override
    public boolean isDisableable() {
        return false;
    }

    @Override
    public void setupConfigureWidget(List<Widget> widgets) {
//        super.setupConfigureWidget(widgets);
        widgets.add(new WidgetPrecalcList());
    }
}
