package kr.syeyoung.dungeonsguide.features.text;

import kr.syeyoung.dungeonsguide.utils.RenderUtils;
import lombok.AllArgsConstructor;
import lombok.Data;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class StyledTextRenderer {
    public static List<StyleTextAssociated> drawTextWithStylesAssociated(List<StyledText> texts, int x, int y, Map<String, TextStyle> styleMap) {
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

    public static List<StyleTextAssociated> calculate(List<StyledText> texts, int x, int y, Map<String, TextStyle> styleMap) {
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

    public static Dimension drawFragmentText(FontRenderer fr, String content, TextStyle style, int x, int y, boolean stopDraw) {
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
}
