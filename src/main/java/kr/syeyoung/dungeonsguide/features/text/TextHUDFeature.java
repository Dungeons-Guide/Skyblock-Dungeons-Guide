package kr.syeyoung.dungeonsguide.features.text;

import com.google.common.base.Supplier;
import kr.syeyoung.dungeonsguide.config.guiconfig.ConfigPanelCreator;
import kr.syeyoung.dungeonsguide.config.guiconfig.GuiConfig;
import kr.syeyoung.dungeonsguide.config.guiconfig.PanelDefaultParameterConfig;
import kr.syeyoung.dungeonsguide.config.types.AColor;
import kr.syeyoung.dungeonsguide.features.AbstractFeature;
import kr.syeyoung.dungeonsguide.features.FeatureParameter;
import kr.syeyoung.dungeonsguide.features.GuiFeature;
import kr.syeyoung.dungeonsguide.gui.MPanel;
import kr.syeyoung.dungeonsguide.utils.RenderUtils;
import lombok.AllArgsConstructor;
import lombok.Data;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;

import java.awt.*;
import java.util.*;
import java.util.List;

public abstract class TextHUDFeature extends GuiFeature implements StyledTextProvider {
    protected TextHUDFeature(String category, String name, String description, String key, boolean keepRatio, int width, int height) {
        super(category, name, description, key, keepRatio, width, height);
        this.parameters.put("textStylesNEW", new FeatureParameter<List<TextStyle>>("textStylesNEW", "", "", new ArrayList<TextStyle>(), "list_textStyle"));
    }

    @Override
    public void drawHUD(float partialTicks) {
        if (isHUDViewable()) {
            List<StyledText> asd = getText();

            if (doesScaleWithHeight()) {
                FontRenderer fr = getFontRenderer();
                double scale = getFeatureRect().getRectangle().getHeight() / (fr.FONT_HEIGHT* countLines(asd));
                GlStateManager.scale(scale, scale, 0);
            }
            StyledTextRenderer.drawTextWithStylesAssociated(getText(), 0, 0, getStylesMap());
        }
    }

    public boolean doesScaleWithHeight() {
        return true;
    }

    @Override
    public void drawDemo(float partialTicks) {
        List<StyledText> asd = getDummyText();
        if (doesScaleWithHeight()) {
            FontRenderer fr = getFontRenderer();
            double scale = getFeatureRect().getRectangle().getHeight() / (fr.FONT_HEIGHT * countLines(asd));
            GlStateManager.scale(scale, scale, 0);
        }
        StyledTextRenderer.drawTextWithStylesAssociated(getDummyText(), 0, 0, getStylesMap());
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
}
