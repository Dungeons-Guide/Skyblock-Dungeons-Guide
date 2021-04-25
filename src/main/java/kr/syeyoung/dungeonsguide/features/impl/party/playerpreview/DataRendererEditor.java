package kr.syeyoung.dungeonsguide.features.impl.party.playerpreview;

import kr.syeyoung.dungeonsguide.config.guiconfig.GuiConfig;
import kr.syeyoung.dungeonsguide.gui.MPanel;

public class DataRendererEditor extends MPanel {
    private FeatureViewPlayerOnJoin feature;
    private GuiConfig config;

    public DataRendererEditor(GuiConfig config, FeatureViewPlayerOnJoin featureViewPlayerOnJoin) {
        this.config = config;
        this.feature = featureViewPlayerOnJoin;
    }
}
