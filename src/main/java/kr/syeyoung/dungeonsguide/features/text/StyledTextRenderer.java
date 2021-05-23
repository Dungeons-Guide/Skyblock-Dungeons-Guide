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

import kr.syeyoung.dungeonsguide.utils.RenderUtils;
import lombok.AllArgsConstructor;
import lombok.Data;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class StyledTextRenderer {

    public static enum Alignment {
        LEFT, CENTER, RIGHT
    }



    public static List<StyleTextAssociated> drawTextWithStylesAssociated(List<StyledText> texts, int x, int y,int width, Map<String, TextStyle> styleMap, Alignment alignment) {
        String[] totalLines = (texts.stream().map( a-> a.getText()).collect(Collectors.joining())+" ").split("\n");


        int currentLine = 0;
        FontRenderer fr = Minecraft.getMinecraft().fontRendererObj;
        int currX = alignment == Alignment.LEFT ? x : alignment == Alignment.CENTER ? (x+width-fr.getStringWidth(totalLines[currentLine]))/2 : (x+width-fr.getStringWidth(totalLines[currentLine]));
        int currY = y;
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
                    currentLine++;
                    currX = alignment == Alignment.LEFT ? x : alignment == Alignment.CENTER ? (x+width-fr.getStringWidth(totalLines[currentLine]))/2 : (x+width-fr.getStringWidth(totalLines[currentLine]));
                    maxHeightForLine = 0;
                }
            }
            if (st.getText().endsWith("\n")) {
                currY += maxHeightForLine;
                currentLine++;
                currX = alignment == Alignment.LEFT ? x : alignment == Alignment.CENTER ? (x+width-fr.getStringWidth(totalLines[currentLine]))/2 : (x+width-fr.getStringWidth(totalLines[currentLine]));
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

        GlStateManager.enableBlend();
        GL14.glBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ONE_MINUS_SRC_ALPHA);
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
