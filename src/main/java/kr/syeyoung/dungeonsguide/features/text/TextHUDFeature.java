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

package kr.syeyoung.dungeonsguide.features.text;

import com.google.common.base.Supplier;
import kr.syeyoung.dungeonsguide.config.guiconfig.old.ConfigPanelCreator;
import kr.syeyoung.dungeonsguide.config.guiconfig.old.GuiConfig;
import kr.syeyoung.dungeonsguide.config.guiconfig.location.GuiGuiLocationConfig;
import kr.syeyoung.dungeonsguide.config.guiconfig.old.PanelDefaultParameterConfig;
import kr.syeyoung.dungeonsguide.config.types.AColor;
import kr.syeyoung.dungeonsguide.features.FeatureParameter;
import kr.syeyoung.dungeonsguide.features.GuiFeature;
import kr.syeyoung.dungeonsguide.gui.MPanel;
import kr.syeyoung.dungeonsguide.gui.elements.MFloatSelectionButton;
import kr.syeyoung.dungeonsguide.gui.elements.MPassiveLabelAndElement;
import kr.syeyoung.dungeonsguide.gui.elements.MToggleButton;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;

import java.util.*;
import java.util.List;

public abstract class TextHUDFeature extends GuiFeature implements StyledTextProvider {
    protected TextHUDFeature(String category, String name, String description, String key, boolean keepRatio, int width, int height) {
        super(category, name, description, key, keepRatio, width, height);
        this.parameters.put("textStylesNEW", new FeatureParameter<List<TextStyle>>("textStylesNEW", "", "", new ArrayList<TextStyle>(), "list_textStyle"));
        this.parameters.put("alignRight", new FeatureParameter<Boolean>("alignRight", "Align Right", "Align text to right", false, "boolean"));
        this.parameters.put("alignCenter", new FeatureParameter<Boolean>("alignCenter", "Align Center", "Align text to center (overrides alignright)", false, "boolean"));
        this.parameters.put("scale", new FeatureParameter<Float>("scale", "Scale", "Scale", 1.0f, "float"));

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
            StyledTextRenderer.drawTextWithStylesAssociated(getText(), 0, 0, (int) (Math.abs(getFeatureRect().getWidth())/scale), getStylesMap(),this.<Boolean>getParameter("alignCenter").getValue() ? StyledTextRenderer.Alignment.CENTER : this.<Boolean>getParameter("alignRight").getValue() ? StyledTextRenderer.Alignment.RIGHT : StyledTextRenderer.Alignment.LEFT);
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
        StyledTextRenderer.drawTextWithStylesAssociated(getDummyText(), 0, 0, (int) (Math.abs(getFeatureRect().getWidth())/scale), getStylesMap(),this.<Boolean>getParameter("alignCenter").getValue() ? StyledTextRenderer.Alignment.CENTER : this.<Boolean>getParameter("alignRight").getValue() ? StyledTextRenderer.Alignment.RIGHT : StyledTextRenderer.Alignment.LEFT);
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
    public String getEditRoute(final GuiConfig config) {
        ConfigPanelCreator.map.put("base." + getKey() , new Supplier<MPanel>() {
            @Override
            public MPanel get() {
                return new PanelDefaultParameterConfig(config, TextHUDFeature.this,
                        Arrays.asList(new MPanel[] {
                                new PanelTextParameterConfig(config, TextHUDFeature.this)
                        }), Collections.singleton("textStylesNEW"));
            }
        });
        return "base." + getKey() ;
    }

    @Override
    public List<MPanel> getTooltipForEditor(GuiGuiLocationConfig guiGuiLocationConfig) {
        List<MPanel> mPanels = super.getTooltipForEditor(guiGuiLocationConfig);
        mPanels.add(new MPassiveLabelAndElement("Align Right", new MToggleButton() {{
            setEnabled(TextHUDFeature.this.<Boolean>getParameter("alignRight").getValue());
            setOnToggle(() ->{
                TextHUDFeature.this.<Boolean>getParameter("alignRight").setValue(isEnabled());
            }); }
        }));
        mPanels.add(new MPassiveLabelAndElement("Align Center", new MToggleButton() {{
            setEnabled(TextHUDFeature.this.<Boolean>getParameter("alignCenter").getValue());
            setOnToggle(() ->{
                TextHUDFeature.this.<Boolean>getParameter("alignCenter").setValue(isEnabled());
            }); }
        }));
        if (!doesScaleWithHeight()) {
            mPanels.add(new MPassiveLabelAndElement("Scale", new MFloatSelectionButton(TextHUDFeature.this.<Float>getParameter("scale").getValue()) {{
                setOnUpdate(() ->{
                    TextHUDFeature.this.<Float>getParameter("scale").setValue(this.getData());
                }); }
            }));
        }

        return mPanels;
    }
}
