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

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class TextHUDFeature extends GuiFeature {
    protected TextHUDFeature(String category, String name, String description, String key, boolean keepRatio, int width, int height) {
        super(category, name, description, key, keepRatio, width, height);
        this.parameters.put("textStyles", new FeatureParameter<List<TextStyle>>("textStyles", "", "", new ArrayList<TextStyle>(), "list_textstyle"));
    }

    @Override
    public void drawHUD(float partialTicks) {
        drawTextWithStylesAssociated(getText(), 0, 0, getStylesMap());
    }

    public abstract List<String> getUsedTextStyle();
    public List<StyledText> getDummyText() {
        return getText();
    }
    public abstract List<StyledText> getText();

    public List<TextStyle> getStyles() {
        return this.<List<TextStyle>>getParameter("textStyles").getValue();
    }
    public Map<String, TextStyle> getStylesMap() {
        List<TextStyle> styles = getStyles();
        Map<String, TextStyle> res = new HashMap<String, TextStyle>();
        for (TextStyle ts:styles) {
            res.put(ts.getGroupName(), ts);
        }
        return res;
    }

    public List<StyleTextAssociated> drawTextWithStylesAssociated(List<StyledText> texts, int x, int y, Map<String, TextStyle> styleMap) {
        int currX = x;
        int currY = y;
        FontRenderer fr = Minecraft.getMinecraft().fontRendererObj;
        int maxHeightForLine = 0;
        List<StyleTextAssociated> associateds = new ArrayList<StyleTextAssociated>();
        for (StyledText st : texts) {
            TextStyle ts = styleMap.get(st);
            String[] lines = st.getText().split("\n");
            for (int i = 0; i < lines.length; i++) {
                String str = lines[i];
                Dimension d = drawFragmentText(fr, str, ts, currX, currY, false);
                associateds.add(new StyleTextAssociated(st, new Rectangle(currX, currY, d.width, d.height)));
                currX += d.width;
                if (maxHeightForLine < d.height)
                    maxHeightForLine = d.height;

                if (i+1 != lines.length) {
                    currY += maxHeightForLine;
                    currX = x;
                    maxHeightForLine = 0;
                }
            }
        }
        return associateds;
    }

    public List<StyleTextAssociated> calculate(List<StyledText> texts, int x, int y, Map<String, TextStyle> styleMap) {
        int currX = x;
        int currY = y;
        FontRenderer fr = Minecraft.getMinecraft().fontRendererObj;
        int maxHeightForLine = 0;
        List<StyleTextAssociated> associateds = new ArrayList<StyleTextAssociated>();
        for (StyledText st : texts) {
            TextStyle ts = styleMap.get(st);
            String[] lines = st.getText().split("\n");
            for (int i = 0; i < lines.length; i++) {
                String str = lines[i];
                Dimension d = drawFragmentText(fr, str, ts, currX, currY, true);
                associateds.add(new StyleTextAssociated(st, new Rectangle(currX, currY, d.width, d.height)));
                currX += d.width;
                if (maxHeightForLine < d.height)
                    maxHeightForLine = d.height;

                if (i+1 != lines.length) {
                    currY += maxHeightForLine;
                    currX = x;
                    maxHeightForLine = 0;
                }
            }
        }
        return associateds;
    }

    @Data
    @AllArgsConstructor
    public static class StyleTextAssociated {
        private StyledText styledText;
        private Rectangle rectangle;
    }

    public Dimension drawFragmentText(FontRenderer fr, String content, TextStyle style, int x, int y, boolean stopDraw) {
        if (stopDraw)
            return new Dimension(fr.getStringWidth(content), fr.FONT_HEIGHT);

        if (!style.getColor().isChroma()) {
            fr.drawString(content, x, y, style.getColor().getRGB());
            return new Dimension(fr.getStringWidth(content), fr.FONT_HEIGHT);
        }else {
            char[] charArr = content.toCharArray();
            int width = 0;
            for (int i = 0; i < charArr.length; i++) {
                fr.drawString(String.valueOf(charArr[i]), x + width, y, RenderUtils.getChromaColorAt(x + width, y, style.getColor().getChromaSpeed()));
                width += fr.getCharWidth(charArr[i]);
            }
            return new Dimension(width, fr.FONT_HEIGHT);
        }
    }

    @Override
    public String getEditRoute(final GuiConfig config) {
        ConfigPanelCreator.map.put("base." + getKey() , new Supplier<MPanel>() {
            @Override
            public MPanel get() {
                return new PanelDefaultParameterConfig(config, TextHUDFeature.this);
            }
        });
        return "base." + getKey() ;
    }
}
