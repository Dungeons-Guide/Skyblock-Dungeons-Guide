/*
 * Dungeons Guide - The most intelligent Hypixel Skyblock Dungeons Mod
 * Copyright (C) 2022  cyoung06 (syeyoung)
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

package kr.syeyoung.dungeonsguide.mod.guiv2.elements;

import kr.syeyoung.dungeonsguide.mod.guiv2.*;
import kr.syeyoung.dungeonsguide.mod.guiv2.layouter.Layouter;
import kr.syeyoung.dungeonsguide.mod.guiv2.primitive.ConstraintBox;
import kr.syeyoung.dungeonsguide.mod.guiv2.primitive.Size;
import kr.syeyoung.dungeonsguide.mod.guiv2.renderer.Renderer;
import kr.syeyoung.dungeonsguide.mod.guiv2.renderer.RenderingContext;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.AnnotatedExportOnlyWidget;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.annotations.Export;
import lombok.AllArgsConstructor;
import lombok.Data;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Text extends AnnotatedExportOnlyWidget implements Layouter, Renderer {
    @Export(attributeName = "text")
    public final BindableAttribute<String> text = new BindableAttribute<>(String.class, "");

    @Override
    public List<Widget> build(DomElement buildContext) {
        return Collections.EMPTY_LIST;
    }

    @Data @AllArgsConstructor
    public static class WrappedTextData {
        final int width;
        final String text;
    }
    public List<WrappedTextData> wrappedTexts = new ArrayList();
    @Export(attributeName = "font")
    public final BindableAttribute<FontRenderer> fontRenderer =new BindableAttribute<>(FontRenderer.class, Minecraft.getMinecraft().fontRendererObj);

    public static enum WordBreak {
        NEVER, WORD, LETTER
    }
    @Export(attributeName = "break")
    public final BindableAttribute<WordBreak> wordBreak = new BindableAttribute<>(WordBreak.class, WordBreak.WORD);

    @Export(attributeName = "lineSpacing")
    public final BindableAttribute<Double> lineSpacing = new BindableAttribute<>(Double.class, 1.0);


    public static enum TextAlign {
        LEFT, CENTER, RIGHT
    }
    @Export(attributeName = "align")
    public final BindableAttribute<TextAlign> textAlign = new BindableAttribute<>(TextAlign.class, TextAlign.LEFT);

    @Export(attributeName = "color")
    public final BindableAttribute<Integer> color = new BindableAttribute<>(Integer.class, 0xFF000000);

    public Text() {
        text.addOnUpdate(a -> getDomElement().requestRelayout());
        fr = Minecraft.getMinecraft().fontRendererObj;
    }
    private FontRenderer fr;

    @Override
    public void doRender(int absMouseX, int absMouseY, double relMouseX, double relMouseY, float partialTicks, RenderingContext context, DomElement buildContext) {
        int y = 0;
        int color = this.color.getValue();
        int yInc = (int) (fr.FONT_HEIGHT * lineSpacing.getValue());
        double width =buildContext.getSize().getWidth();

        GlStateManager.enableTexture2D();
        if (textAlign.getValue() == TextAlign.LEFT) {
            for (WrappedTextData wrappedText : wrappedTexts) {
                fr.drawString(wrappedText.text, 0, y, color);
                y += yInc;
            }
        } else if (textAlign.getValue() == TextAlign.CENTER) {
            for (WrappedTextData wrappedText : wrappedTexts) {
                fr.drawString(wrappedText.text, (int) ((width-wrappedText.getWidth())/2), y, color);
                y += yInc;
            }
        } else {
            for (WrappedTextData wrappedText : wrappedTexts) {
                fr.drawString(wrappedText.text, (int) (width - wrappedText.getWidth()), y, color);
                y += yInc;
            }
        }
    }


    @Override
    public Size layout(DomElement buildContext, ConstraintBox constraintBox) {
        wrappedTexts.clear();

        FontRenderer fr = fontRenderer.getValue();
        String text = this.text.getValue();

        boolean hadToWrap = false;
        int maxWidth2 = 0;


        WordBreak wordBreak = this.wordBreak.getValue();

        String[] splitByLine = text.split("\n");
        for (String line : splitByLine) {
            String[] splitByWord = line.split(" ");
            double maxWidth = constraintBox.getMaxWidth();
            int currentWidth = 0;
            boolean added = false;
            StringBuilder currentLine = new StringBuilder();
            for (String s : splitByWord) {
                int strWidth = fr.getStringWidth((added ? " " : "") +s);
                if (strWidth + currentWidth <= maxWidth) {
                    if (added) currentLine.append(" ");
                    currentLine.append(s);
                    added = true;
                    currentWidth += strWidth;
                } else {
                    hadToWrap = true;
                    // need to break word.
                    if (wordBreak == WordBreak.WORD) {
                        String current = s;
                        if (fr.getStringWidth(s) > maxWidth) {
                            // there is no hope. just continue.
                            current = currentLine.toString()+ " "+s;
                        } else {
                            wrappedTexts.add(new WrappedTextData(currentWidth, currentLine.toString()));
                            current = s;
                        }

                        // binary search unsplittable.
                        while (fr.getStringWidth(current) > maxWidth) {
                            currentLine = new StringBuilder("");
                            String remaining = "";
                            currentWidth = 0;
                            double remainingWidth = maxWidth - currentWidth;
                            while(current.length() > 1 && remainingWidth > 4) {
                                String query = current.substring(0, current.length()/2);
                                int len = fr.getStringWidth(query);
                                if (len <= remainingWidth) {
                                    currentLine.append(query);
                                    remainingWidth -= len;
                                    current = current.substring(current.length() / 2);
                                } else {
                                    remaining = current.substring(current.length() / 2) + remaining;
                                    current = query;
                                }
                            }
                            remaining = current + remaining;

                            wrappedTexts.add(new WrappedTextData((int) (maxWidth - remainingWidth), currentLine.toString()));

                            current = remaining;
                        }
                        currentLine = new StringBuilder(current);
                        currentWidth = fr.getStringWidth(current);
                    } else if (wordBreak == WordBreak.NEVER) {
                        currentLine.append(" ").append(s);
                        currentWidth += strWidth;
                        break;
                    } else {
                        // binary search correct length-
                        String current = " "+s;
                        double remainingWidth = maxWidth - currentWidth;
                        String remaining = "";
                        while(current.length() > 1 && remainingWidth > 4) {
                            String query = current.substring(0, current.length()/2);
                            int len = fr.getStringWidth(query);
                            if (len <= remainingWidth) {
                                currentLine.append(query);
                                remainingWidth -= len;
                                current = current.substring(current.length() / 2);
                            } else {
                                remaining = current.substring(current.length() / 2) + remaining;
                                current = query;
                            }
                        }
                        remaining = current + remaining;

                        wrappedTexts.add(new WrappedTextData((int) (maxWidth - remainingWidth), currentLine.toString()));
                        currentLine = new StringBuilder(remaining);
                        currentWidth = fr.getStringWidth(remaining);
                    }
                }
            }
            if (currentWidth > maxWidth2) maxWidth2 = currentWidth;
            wrappedTexts.add(new WrappedTextData(currentWidth, currentLine.toString()));
        }



        return new Size(hadToWrap ? constraintBox.getMaxWidth() :
                Layouter.clamp(maxWidth2, constraintBox.getMinWidth(), constraintBox.getMaxWidth()),
                Layouter.clamp( (fr.FONT_HEIGHT * lineSpacing.getValue()) * wrappedTexts.size(), constraintBox.getMinHeight(), constraintBox.getMaxHeight()));
    }
}
