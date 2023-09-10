package kr.syeyoung.dungeonsguide.mod.features.impl.advanced;

import kr.syeyoung.dungeonsguide.mod.features.SimpleFeature;

public class FeatureAuthHide extends SimpleFeature {
    public FeatureAuthHide() {
        super("Debug", "Hide Authentication Error (Requires Restart)", "Toggles Authentication Error visiblity", "auth", false);
    }
}
