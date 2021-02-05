package kr.syeyoung.dungeonsguide.features.text;

import kr.syeyoung.dungeonsguide.features.FeatureParameter;
import kr.syeyoung.dungeonsguide.features.GuiFeature;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public abstract class TextHUDFeature extends GuiFeature {
    protected TextHUDFeature(String category, String name, String description, String key, boolean keepRatio, int width, int height) {
        super(category, name, description, key, keepRatio, width, height);
        this.parameters.put("textStyles", new FeatureParameter<List<TextStyle>>("textStyles", "", "", new ArrayList<TextStyle>(), "list_textstyle"));
    }

    public abstract List<String> getUsedTextStyle();

    @Override
    public void drawHUD(float partialTicks) {

    }
}
