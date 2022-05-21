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

package kr.syeyoung.dungeonsguide.features.text;

import kr.syeyoung.dungeonsguide.utils.RenderUtils;
import lombok.AllArgsConstructor;
import lombok.Data;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class StyledTextRenderer {

    public static enum Alignment {
        LEFT, CENTER, RIGHT
    }

    private static final Method renderChar = ReflectionHelper.findMethod(FontRenderer.class, null, new String[] {"renderChar", "func_181559_a"}, char.class, boolean.class);
    private static final Method doDraw = ReflectionHelper.findMethod(FontRenderer.class, null, new String[] {"doDraw"}, float.class);
    private static final Field posX = ReflectionHelper.findField(FontRenderer.class, "posX", "field_78295_j");
    private static final Field posY = ReflectionHelper.findField(FontRenderer.class, "posY", "field_78296_k");


    public static List<StyleTextAssociated> drawTextWithStylesAssociated(List<StyledText> texts, int x, int y,int width, Map<String, TextStyle> styleMap, Alignment alignment) {
        String[] totalLines = (texts.stream().map( a-> a.getText()).collect(Collectors.joining())+" ").split("\n");


        GlStateManager.enableBlend();
        GlStateManager.enableAlpha();
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ONE_MINUS_SRC_ALPHA);

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
                Dimension d = null;
                try {
                    d = drawFragmentText(fr, str, ts, currX, currY, false);
                } catch (InvocationTargetException | IllegalAccessException e) {
                    e.printStackTrace();
                }
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
                Dimension d = null;
                try {
                    d = drawFragmentText(fr, str, ts, currX, currY, true);
                } catch (InvocationTargetException | IllegalAccessException e) {
                    e.printStackTrace();
                }
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

    private static Dimension drawFragmentText(FontRenderer fr, String content, TextStyle style, int x, int y, boolean stopDraw) throws InvocationTargetException, IllegalAccessException {
        if (stopDraw)
            return new Dimension(fr.getStringWidth(content), fr.FONT_HEIGHT);

        int bgColor = RenderUtils.getColorAt(x,y, style.getBackground());
        if ((bgColor & 0xFF000000) != 0)
            Gui.drawRect(x,y, x+fr.getStringWidth(content), y + fr.FONT_HEIGHT, bgColor);


        posX.set(fr, x+1);
        posY.set(fr, y+1);

        if (style.isShadow()) {
            char[] charArr = content.toCharArray();
            float width = 0;
            for (int i = 0; i < charArr.length; i++) {
                int color = RenderUtils.getColorAt(x + width, y, style.getColor());
                color = (color & 16579836) >> 2 | color & -16777216;

                width +=renderChar(fr, charArr[i], color,true, false, false, false);
            }
        }
        posX.set(fr, x);
        posY.set(fr, y);
        {
            char[] charArr = content.toCharArray();
            float width = 0;
            for (int i = 0; i < charArr.length; i++) {
                int color = RenderUtils.getColorAt(x + width, y, style.getColor());

                width +=renderChar(fr, charArr[i], color, false, false, false, false);
            }
            return new Dimension((int) width, fr.FONT_HEIGHT);
        }
    }

    private static float renderChar(FontRenderer fr, char character, int color, boolean shadow, boolean randomStyle, boolean boldStyle, boolean italicStyle) throws InvocationTargetException, IllegalAccessException {
        RenderUtils.GL_SETCOLOR(color);
        int j = "\u00c0\u00c1\u00c2\u00c8\u00ca\u00cb\u00cd\u00d3\u00d4\u00d5\u00da\u00df\u00e3\u00f5\u011f\u0130\u0131\u0152\u0153\u015e\u015f\u0174\u0175\u017e\u0207\u0000\u0000\u0000\u0000\u0000\u0000\u0000 !\"#$%&\'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_`abcdefghijklmnopqrstuvwxyz{|}~\u0000\u00c7\u00fc\u00e9\u00e2\u00e4\u00e0\u00e5\u00e7\u00ea\u00eb\u00e8\u00ef\u00ee\u00ec\u00c4\u00c5\u00c9\u00e6\u00c6\u00f4\u00f6\u00f2\u00fb\u00f9\u00ff\u00d6\u00dc\u00f8\u00a3\u00d8\u00d7\u0192\u00e1\u00ed\u00f3\u00fa\u00f1\u00d1\u00aa\u00ba\u00bf\u00ae\u00ac\u00bd\u00bc\u00a1\u00ab\u00bb\u2591\u2592\u2593\u2502\u2524\u2561\u2562\u2556\u2555\u2563\u2551\u2557\u255d\u255c\u255b\u2510\u2514\u2534\u252c\u251c\u2500\u253c\u255e\u255f\u255a\u2554\u2569\u2566\u2560\u2550\u256c\u2567\u2568\u2564\u2565\u2559\u2558\u2552\u2553\u256b\u256a\u2518\u250c\u2588\u2584\u258c\u2590\u2580\u03b1\u03b2\u0393\u03c0\u03a3\u03c3\u03bc\u03c4\u03a6\u0398\u03a9\u03b4\u221e\u2205\u2208\u2229\u2261\u00b1\u2265\u2264\u2320\u2321\u00f7\u2248\u00b0\u2219\u00b7\u221a\u207f\u00b2\u25a0\u0000".indexOf(character);

        if (randomStyle && j != -1)
        {
            int k = fr.getCharWidth(character);
            char c1;

            while (true)
            {
                j = fr.fontRandom.nextInt("\u00c0\u00c1\u00c2\u00c8\u00ca\u00cb\u00cd\u00d3\u00d4\u00d5\u00da\u00df\u00e3\u00f5\u011f\u0130\u0131\u0152\u0153\u015e\u015f\u0174\u0175\u017e\u0207\u0000\u0000\u0000\u0000\u0000\u0000\u0000 !\"#$%&\'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_`abcdefghijklmnopqrstuvwxyz{|}~\u0000\u00c7\u00fc\u00e9\u00e2\u00e4\u00e0\u00e5\u00e7\u00ea\u00eb\u00e8\u00ef\u00ee\u00ec\u00c4\u00c5\u00c9\u00e6\u00c6\u00f4\u00f6\u00f2\u00fb\u00f9\u00ff\u00d6\u00dc\u00f8\u00a3\u00d8\u00d7\u0192\u00e1\u00ed\u00f3\u00fa\u00f1\u00d1\u00aa\u00ba\u00bf\u00ae\u00ac\u00bd\u00bc\u00a1\u00ab\u00bb\u2591\u2592\u2593\u2502\u2524\u2561\u2562\u2556\u2555\u2563\u2551\u2557\u255d\u255c\u255b\u2510\u2514\u2534\u252c\u251c\u2500\u253c\u255e\u255f\u255a\u2554\u2569\u2566\u2560\u2550\u256c\u2567\u2568\u2564\u2565\u2559\u2558\u2552\u2553\u256b\u256a\u2518\u250c\u2588\u2584\u258c\u2590\u2580\u03b1\u03b2\u0393\u03c0\u03a3\u03c3\u03bc\u03c4\u03a6\u0398\u03a9\u03b4\u221e\u2205\u2208\u2229\u2261\u00b1\u2265\u2264\u2320\u2321\u00f7\u2248\u00b0\u2219\u00b7\u221a\u207f\u00b2\u25a0\u0000".length());
                c1 = "\u00c0\u00c1\u00c2\u00c8\u00ca\u00cb\u00cd\u00d3\u00d4\u00d5\u00da\u00df\u00e3\u00f5\u011f\u0130\u0131\u0152\u0153\u015e\u015f\u0174\u0175\u017e\u0207\u0000\u0000\u0000\u0000\u0000\u0000\u0000 !\"#$%&\'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_`abcdefghijklmnopqrstuvwxyz{|}~\u0000\u00c7\u00fc\u00e9\u00e2\u00e4\u00e0\u00e5\u00e7\u00ea\u00eb\u00e8\u00ef\u00ee\u00ec\u00c4\u00c5\u00c9\u00e6\u00c6\u00f4\u00f6\u00f2\u00fb\u00f9\u00ff\u00d6\u00dc\u00f8\u00a3\u00d8\u00d7\u0192\u00e1\u00ed\u00f3\u00fa\u00f1\u00d1\u00aa\u00ba\u00bf\u00ae\u00ac\u00bd\u00bc\u00a1\u00ab\u00bb\u2591\u2592\u2593\u2502\u2524\u2561\u2562\u2556\u2555\u2563\u2551\u2557\u255d\u255c\u255b\u2510\u2514\u2534\u252c\u251c\u2500\u253c\u255e\u255f\u255a\u2554\u2569\u2566\u2560\u2550\u256c\u2567\u2568\u2564\u2565\u2559\u2558\u2552\u2553\u256b\u256a\u2518\u250c\u2588\u2584\u258c\u2590\u2580\u03b1\u03b2\u0393\u03c0\u03a3\u03c3\u03bc\u03c4\u03a6\u0398\u03a9\u03b4\u221e\u2205\u2208\u2229\u2261\u00b1\u2265\u2264\u2320\u2321\u00f7\u2248\u00b0\u2219\u00b7\u221a\u207f\u00b2\u25a0\u0000".charAt(j);

                if (k == fr.getCharWidth(c1))
                {
                    break;
                }
            }

            character = c1;
        }

        float f1 = j == -1 || fr.getUnicodeFlag() ? 0.5f : 1f;
        boolean flag = (character == 0 || j == -1 || fr.getUnicodeFlag()) && shadow;

        if (flag)
        {
            posX.set(fr,(float) posX.get(fr) - f1);
            posY.set(fr,(float) posY.get(fr) - f1);
        }

        float f = (float) renderChar.invoke(fr, character, italicStyle);

        if (flag)
        {
            posX.set(fr,(float) posX.get(fr) + f1);
            posY.set(fr,(float) posY.get(fr) + f1);
        }

        if (boldStyle)
        {
            posX.set(fr,(float) posX.get(fr) + f1);

            if (flag)
            {
                posX.set(fr,(float) posX.get(fr) - f1);
                posY.set(fr,(float) posY.get(fr) - f1);
            }

            renderChar.invoke(fr, character, italicStyle);
            posX.set(fr,(float) posX.get(fr) - f1);

            if (flag)
            {
                posX.set(fr,(float) posX.get(fr) + f1);
                posY.set(fr,(float) posY.get(fr) + f1);
            }

            ++f;
        }
        doDraw.invoke(fr, f);

        return f;
    }
}
