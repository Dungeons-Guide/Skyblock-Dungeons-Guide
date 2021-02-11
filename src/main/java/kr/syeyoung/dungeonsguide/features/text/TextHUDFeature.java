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

public abstract class TextHUDFeature extends GuiFeature {
    protected TextHUDFeature(String category, String name, String description, String key, boolean keepRatio, int width, int height) {
        super(category, name, description, key, keepRatio, width, height);
        this.parameters.put("textStyles", new FeatureParameter<List<TextStyle>>("textStyles", "", "", new ArrayList<TextStyle>(), "list_textStyle"));
    }

    @Override
    public void drawHUD(float partialTicks) {
        if (isHUDViewable()) {
            List<StyledText> asd = getText();

            if (doesScaleWithHeight()) {
                FontRenderer fr = getFontRenderer();
                double scale = getFeatureRect().getHeight() / (fr.FONT_HEIGHT* countLines(asd));
                GlStateManager.scale(scale, scale, 0);
            }
            drawTextWithStylesAssociated(getText(), 0, 0, getStylesMap());
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
            double scale = getFeatureRect().getHeight() / (fr.FONT_HEIGHT * countLines(asd));
            GlStateManager.scale(scale, scale, 0);
        }
        drawTextWithStylesAssociated(getDummyText(), 0, 0, getStylesMap());
    }

    public int countLines(List<StyledText> texts) {
        StringBuilder things = new StringBuilder();
        for (StyledText text : texts) {
            things.append(text.getText());
        }
        String things2 = things.toString().trim();
        int lines = 1;
        for (char c : things2.toString().toCharArray()) {
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
        return this.<List<TextStyle>>getParameter("textStyles").getValue();
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

    public List<StyleTextAssociated> drawTextWithStylesAssociated(List<StyledText> texts, int x, int y, Map<String, TextStyle> styleMap) {
        int currX = x;
        int currY = y;
        FontRenderer fr = Minecraft.getMinecraft().fontRendererObj;
        int maxHeightForLine = 0;
        List<StyleTextAssociated> associateds = new ArrayList<StyleTextAssociated>();
        for (StyledText st : texts) {
            TextStyle ts = styleMap.get(st.getGroup());
            String[] lines = st.getText().split("\n");
            for (int i = 0; i < lines.length; i++) {
                String str = lines[i];
                Dimension d = drawFragmentText(fr, str, ts, currX, currY, false);
                associateds.add(new StyleTextAssociated(st, new Rectangle(currX, currY, d.width, d.height)));
                currX += d.width;
                if (maxHeightForLine < d.height)
                    maxHeightForLine = d.height;

                if (i+1 != lines.length) {
                    currY += maxHeightForLine ;
                    currX = x;
                    maxHeightForLine = 0;
                }
            }
            if (st.getText().endsWith("\n")) {
                currY += maxHeightForLine ;
                currX = x;
                maxHeightForLine = 0;
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
            TextStyle ts = styleMap.get(st.getGroup());
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
            if (st.getText().endsWith("\n")) {
                currY += maxHeightForLine;
                currX = x;
                maxHeightForLine = 0;
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

        Gui.drawRect(x,y, x+fr.getStringWidth(content), y + fr.FONT_HEIGHT, RenderUtils.getColorAt(x,y, style.getBackground()));

        if (!style.getColor().isChroma()) {
            fr.drawString(content, x, y, style.getColor().getRGB(), style.isShadow());
            return new Dimension(fr.getStringWidth(content), fr.FONT_HEIGHT);
        }else {
            char[] charArr = content.toCharArray();
            int width = 0;
            for (int i = 0; i < charArr.length; i++) {
                fr.drawString(String.valueOf(charArr[i]), x + width, y, RenderUtils.getColorAt(x + width, y, style.getColor()), style.isShadow());
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
                return new PanelDefaultParameterConfig(config, TextHUDFeature.this,
                        Arrays.asList(new MPanel[] {
                                new PanelTextParameterConfig(config, TextHUDFeature.this)
                        }), Collections.singleton("textStyles"));
            }
        });
        return "base." + getKey() ;
    }
}
