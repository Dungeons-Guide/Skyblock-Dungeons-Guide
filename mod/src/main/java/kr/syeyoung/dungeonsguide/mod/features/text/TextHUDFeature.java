/*
 * Dungeons Guide - The most intelligent Hypixel Skyblock Dungeons Mod
 * Copyright (C) 2021  cyoung06
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

package kr.syeyoung.dungeonsguide.mod.features.text;

import com.google.common.base.Supplier;
import com.google.gson.JsonObject;
import kr.syeyoung.dungeonsguide.mod.config.guiconfig.ConfigPanelCreator;
import kr.syeyoung.dungeonsguide.mod.config.guiconfig.MFeatureEdit;
import kr.syeyoung.dungeonsguide.mod.config.guiconfig.MParameterEdit;
import kr.syeyoung.dungeonsguide.mod.config.guiconfig.RootConfigPanel;
import kr.syeyoung.dungeonsguide.mod.config.guiconfig.location.GuiGuiLocationConfig;
import kr.syeyoung.dungeonsguide.mod.config.types.AColor;
import kr.syeyoung.dungeonsguide.mod.features.FeatureParameter;
import kr.syeyoung.dungeonsguide.mod.features.GuiFeature;
import kr.syeyoung.dungeonsguide.mod.gui.MPanel;
import kr.syeyoung.dungeonsguide.mod.gui.elements.MFloatSelectionButton;
import kr.syeyoung.dungeonsguide.mod.gui.elements.MPassiveLabelAndElement;
import kr.syeyoung.dungeonsguide.mod.gui.elements.MStringSelectionButton;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;

import java.util.*;

public abstract class TextHUDFeature extends GuiFeature implements StyledTextProvider {
    protected TextHUDFeature(String category, String name, String description, String key, boolean keepRatio, int width, int height) {
        super(category, name, description, key, keepRatio, width, height);
        addParameter("textStylesNEW", new FeatureParameter<List<TextStyle>>("textStylesNEW", "", "", new ArrayList<TextStyle>(), "list_textStyle"));
        addParameter("alignment", new FeatureParameter<String>("alignment", "Alignment", "Alignment", "LEFT", "string"));
        addParameter("scale", new FeatureParameter<Float>("scale", "Scale", "Scale", 1.0f, "float"));
    }

    @Override
    public void drawHUD(float partialTicks) {
        if (isHUDViewable()) {
            List<StyledText> asd = getText();

            double scale = 1;
            if (doesScaleWithHeight()) {
                FontRenderer fr = getFontRenderer();
                scale = getFeatureRect().getRectangle().getHeight() / (fr.FONT_HEIGHT* countLines(asd));
            } else {
                scale = this.<Float>getParameter("scale").getValue();
            }
            GlStateManager.scale(scale, scale, 0);
            StyledTextRenderer.drawTextWithStylesAssociated(asd, 0, 0, (int) (Math.abs(getFeatureRect().getWidth())/scale), getStylesMap(),
                    StyledTextRenderer.Alignment.valueOf(TextHUDFeature.this.<String>getParameter("alignment").getValue()));
        }
    }

    public boolean doesScaleWithHeight() {
        return true;
    }

    @Override
    public void drawDemo(float partialTicks) {
        List<StyledText> asd = getDummyText();
        double scale = 1;
        if (doesScaleWithHeight()) {
            FontRenderer fr = getFontRenderer();
            scale = getFeatureRect().getRectangle().getHeight() / (fr.FONT_HEIGHT* countLines(asd));
        } else {
            scale = this.<Float>getParameter("scale").getValue();
        }
        GlStateManager.scale(scale, scale, 0);

        StyledTextRenderer.drawTextWithStylesAssociated(asd, 0, 0, (int) (Math.abs(getFeatureRect().getWidth())/scale), getStylesMap(),
                StyledTextRenderer.Alignment.valueOf(TextHUDFeature.this.<String>getParameter("alignment").getValue()));
    }

    public int countLines(List<StyledText> texts) {
        StringBuilder things = new StringBuilder();
        for (StyledText text : texts) {
            things.append(text.getText());
        }
        String things2 = things.toString().trim();
        int lines = 1;
        for (char c : things2.toCharArray()) {
            if (c == '\n') lines++;
        }
        return  lines;
    }

    public abstract boolean isHUDViewable();

    public abstract List<String> getUsedTextStyle();
    public List<StyledText> getDummyText() {
        return getText();
    }
    public abstract List<StyledText> getText();

    public List<TextStyle> getStyles() {
        return this.<List<TextStyle>>getParameter("textStylesNEW").getValue();
    }
    private Map<String, TextStyle> stylesMap;
    public Map<String, TextStyle> getStylesMap() {
        if (stylesMap == null) {
            List<TextStyle> styles = getStyles();
            Map<String, TextStyle> res = new HashMap<String, TextStyle>();
            for (TextStyle ts : styles) {
                res.put(ts.getGroupName(), ts);
            }
            for (String str : getUsedTextStyle()) {
                if (!res.containsKey(str))
                    res.put(str, new TextStyle(str, new AColor(0xffffffff, true), new AColor(0x00777777, true), false));
            }
            stylesMap = res;
        }
        return stylesMap;
    }


    @Override
    public String getEditRoute(RootConfigPanel rootConfigPanel) {
        ConfigPanelCreator.map.put("base." + getKey() , new Supplier<MPanel>() {
            @Override
            public MPanel get() {

                MFeatureEdit featureEdit = new MFeatureEdit(TextHUDFeature.this, rootConfigPanel);
                featureEdit.addParameterEdit("textStyleNEW", new PanelTextParameterConfig(TextHUDFeature.this));

                StyledTextRenderer.Alignment alignment = StyledTextRenderer.Alignment.valueOf(TextHUDFeature.this.<String>getParameter("alignment").getValue());
                MStringSelectionButton mStringSelectionButton = new MStringSelectionButton(Arrays.asList("LEFT", "CENTER", "RIGHT"), alignment.name());
                mStringSelectionButton.setOnUpdate(() -> {
                    TextHUDFeature.this.<String>getParameter("alignment").setValue(mStringSelectionButton.getSelected());
                });
                featureEdit.addParameterEdit("alignment", new MParameterEdit(TextHUDFeature.this, TextHUDFeature.this.<String>getParameter("alignment"), rootConfigPanel, mStringSelectionButton, (a) -> false));

                for (FeatureParameter parameter: getParameters()) {
                    if (parameter.getKey().equals("textStylesNEW")) continue;
                    if (parameter.getKey().equals("alignment")) continue;
                    featureEdit.addParameterEdit(parameter.getKey(), new MParameterEdit(TextHUDFeature.this, parameter, rootConfigPanel));
                }
                return featureEdit;
            }
        });
        return "base." + getKey() ;
    }

    @Override
    public List<MPanel> getTooltipForEditor(GuiGuiLocationConfig guiGuiLocationConfig) {
        List<MPanel> mPanels = super.getTooltipForEditor(guiGuiLocationConfig);
        StyledTextRenderer.Alignment alignment = StyledTextRenderer.Alignment.valueOf(this.<String>getParameter("alignment").getValue());
        MStringSelectionButton mStringSelectionButton = new MStringSelectionButton(Arrays.asList("LEFT", "CENTER", "RIGHT"), alignment.name());
        mStringSelectionButton.setOnUpdate(() -> {
            TextHUDFeature.this.<String>getParameter("alignment").setValue(mStringSelectionButton.getSelected());
        });

        mPanels.add(new MPassiveLabelAndElement("Alignment", mStringSelectionButton));
        if (!doesScaleWithHeight()) {
            mPanels.add(new MPassiveLabelAndElement("Scale", new MFloatSelectionButton(TextHUDFeature.this.<Float>getParameter("scale").getValue()) {{
                setOnUpdate(() ->{
                    TextHUDFeature.this.<Float>getParameter("scale").setValue(this.getData());
                }); }
            }));
        }

        return mPanels;
    }

    @Override
    public void onParameterReset() {
        stylesMap = null;
    }

    @Override
    public void loadConfig(JsonObject jsonObject) {
        super.loadConfig(jsonObject);
        StyledTextRenderer.Alignment alignment;
        try {
            alignment = StyledTextRenderer.Alignment.valueOf(TextHUDFeature.this.<String>getParameter("alignment").getValue());
        } catch (Exception e) {alignment = StyledTextRenderer.Alignment.LEFT;}
        TextHUDFeature.this.<String>getParameter("alignment").setValue(alignment.name());
    }
}
