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
import kr.syeyoung.dungeonsguide.mod.guiv2.renderer.DrawNothingRenderer;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.DomElementRegistry;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.annotations.Export;
import lombok.AllArgsConstructor;
import lombok.Data;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class Text {

    public static class TRenderer extends DrawNothingRenderer {
        FontRenderer fr;
        private TWidget tController;
        public TRenderer(DomElement domElement) {
            super(domElement);
            fr = Minecraft.getMinecraft().fontRendererObj;
            tController = (TWidget) domElement.getWidget();
        }

        @Override
        public void doRender(int absMouseX, int absMouseY, int relMouseX, int relMouseY, float partialTicks) {
            int y = 0;
            int color = tController.color.getValue();
            int yInc = (int) (fr.FONT_HEIGHT * tController.lineSpacing.getValue());
            int width = getDomElement().getRelativeBound().width;

            GlStateManager.enableTexture2D();
            if (tController.textAlign.getValue() == TWidget.TextAlign.LEFT) {
                for (TWidget.WrappedTextData wrappedText : tController.wrappedTexts) {
                    fr.drawString(wrappedText.text, 0, y, color);
                    y += yInc;
                }
            } else if (tController.textAlign.getValue() == TWidget.TextAlign.CENTER) {
                for (TWidget.WrappedTextData wrappedText : tController.wrappedTexts) {
                    fr.drawString(wrappedText.text, (width-wrappedText.width)/2, y, color);
                    y += yInc;
                }
            } else {
                for (TWidget.WrappedTextData wrappedText : tController.wrappedTexts) {
                    fr.drawString(wrappedText.text, width - wrappedText.width, y, color);
                    y += yInc;
                }
            }

        }
    }

    public static class TLayouter implements Layouter {
        private TWidget tController;
        public TLayouter(DomElement element) {
            super(element);
            tController = (TWidget) element.getWidget();
        }

        @Override
        public Dimension layout(ConstraintBox constraintBox) {
            tController.wrappedTexts.clear();

            FontRenderer fr = tController.fontRenderer.getValue();
            String text = tController.text.getValue();

            boolean hadToWrap = false;
            int maxWidth2 = 0;


            TWidget.WordBreak wordBreak = tController.wordBreak.getValue();

            String[] splitByLine = text.split("\n");
            for (String line : splitByLine) {
                String[] splitByWord = line.split(" ");
                int maxWidth = constraintBox.getMaxWidth();
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
                        if (wordBreak == TWidget.WordBreak.WORD) {
                            String current = s;
                            if (fr.getStringWidth(s) > maxWidth) {
                                // there is no hope. just continue.
                                current = currentLine.toString()+ " "+s;
                            } else {
                                tController.wrappedTexts.add(new TWidget.WrappedTextData(currentWidth, currentLine.toString()));
                                current = s;
                            }

                            // binary search unsplittable.
                            while (fr.getStringWidth(current) > maxWidth) {
                                currentLine = new StringBuilder("");
                                String remaining = "";
                                currentWidth = 0;
                                int remainingWidth = maxWidth - currentWidth;
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

                                tController.wrappedTexts.add(new TWidget.WrappedTextData(currentWidth, currentLine.toString()));

                                current = remaining;
                            }
                            currentLine = new StringBuilder(current);
                            currentWidth = fr.getStringWidth(current);
                        } else if (wordBreak == TWidget.WordBreak.NEVER) {
                            currentLine.append(" ").append(s);
                            currentWidth += strWidth;
                            break;
                        } else {
                            // binary search correct length-
                            String current = " "+s;
                            int remainingWidth = maxWidth - currentWidth;
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

                            tController.wrappedTexts.add(new TWidget.WrappedTextData(currentWidth, currentLine.toString()));
                            currentLine = new StringBuilder(remaining);
                            currentWidth = fr.getStringWidth(remaining);
                        }
                    }
                }
                if (currentWidth > maxWidth2) maxWidth2 = currentWidth;
                tController.wrappedTexts.add(new TWidget.WrappedTextData(currentWidth, currentLine.toString()));
            }



            return new Dimension(hadToWrap ? constraintBox.getMaxWidth() :
                    clamp(maxWidth2, constraintBox.getMinWidth(), constraintBox.getMaxWidth()),
                    clamp((int) (fr.FONT_HEIGHT * tController.lineSpacing.getValue()) * tController.wrappedTexts.size(), constraintBox.getMinHeight(), constraintBox.getMaxHeight()));
        }
    }

    public static class TWidget extends Widget {

        @Export(attributeName = "text")
        public final BindableAttribute<String> text = new BindableAttribute<>(String.class, "");

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

        public TWidget(DomElement element) {
            super(element);
            loadAttributes();
            loadDom();
            text.addOnUpdate(a -> element.requestRelayout());
        }
    }



    public static final DomElementRegistry.DomElementCreator CREATOR = new DomElementRegistry.GeneralDomElementCreator(
            TLayouter::new, TRenderer::new, TWidget::new
    );
}
