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

import kr.syeyoung.dungeonsguide.mod.guiv2.BindableAttribute;
import kr.syeyoung.dungeonsguide.mod.guiv2.DomElement;
import kr.syeyoung.dungeonsguide.mod.guiv2.Widget;
import kr.syeyoung.dungeonsguide.mod.guiv2.elements.richtext.BreakWord;
import kr.syeyoung.dungeonsguide.mod.guiv2.elements.richtext.RichText;
import kr.syeyoung.dungeonsguide.mod.guiv2.elements.richtext.TextSpan;
import kr.syeyoung.dungeonsguide.mod.guiv2.elements.richtext.shaders.SingleColorShader;
import kr.syeyoung.dungeonsguide.mod.guiv2.elements.richtext.styles.ParentDelegatingTextStyle;
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
import java.util.Locale;
import java.util.concurrent.CopyOnWriteArrayList;

public class Text extends AnnotatedExportOnlyWidget {
    @Export(attributeName = "text")
    public final BindableAttribute<String> text = new BindableAttribute<>(String.class, "");

    private final ParentDelegatingTextStyle textStyle = ParentDelegatingTextStyle.ofDefault();
    private final RichText richText = new RichText(new TextSpan(textStyle, ""), BreakWord.WORD, false,RichText.TextAlign.LEFT);
    @Override
    public List<Widget> build(DomElement buildContext) {
        return Collections.singletonList(richText);
    }
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
        text.addOnUpdate((a,b) ->  {
            updateText();
        });
        wordBreak.addOnUpdate((a,b) -> {
            richText.setBreakWord(b == WordBreak.WORD ? BreakWord.WORD : BreakWord.ALL);
        });
        lineSpacing.addOnUpdate((a,b) -> {
            updateText();
        });
        textAlign.addOnUpdate((a,b) -> {
            richText.setAlign(b == TextAlign.LEFT ? RichText.TextAlign.LEFT : b == TextAlign.CENTER ? RichText.TextAlign.CENTER : RichText.TextAlign.RIGHT);
        });
        color.addOnUpdate((a,b) -> {
            updateText();
        });
    }

    private void updateText() {
        textStyle.textShader = new SingleColorShader(color.getValue());
        textStyle.underlineShader = new SingleColorShader(color.getValue());
        textStyle.strikeThroughShader = new SingleColorShader(color.getValue());
        textStyle.topAscent = lineSpacing.getValue() - 1;
        TextSpan textSpan = new TextSpan(textStyle, "");
        for (TextSpan textSpan2 : parseText(text.getValue())) {
            textSpan.addChild(textSpan2);
        }
        richText.setRootSpan(textSpan);
    }

    private static final int[] colorCode = new int[32];
    static {
        for(int i = 0; i < 32; ++i) {
            int j = (i >> 3 & 1) * 85;
            int k = (i >> 2 & 1) * 170 + j;
            int l = (i >> 1 & 1) * 170 + j;
            int i1 = (i & 1) * 170 + j;
            if (i == 6) {
                k += 85;
            }

            if (i >= 16) {
                k /= 4;
                l /= 4;
                i1 /= 4;
            }

            colorCode[i] = (k & 255) << 16 | (l & 255) << 8 | i1 & 255;
        }
    }
    private List<TextSpan> parseText(String text) {
        Boolean boldStyle = null, strikethroughStyle = null, underlineStyle = null, italicStyle = null;
        Integer textColor = null;
        List<TextSpan> spans = new ArrayList<>();

        StringBuilder stringBuilder = new StringBuilder();
        for(int i = 0; i < text.length(); ++i) {
            char c0 = text.charAt(i);
            if (c0 == 167 && i + 1 < text.length()) {
                if (!stringBuilder.toString().isEmpty()) {
                    spans.add(new TextSpan(
                            new ParentDelegatingTextStyle()
                                    .setTextShader(textColor == null ? null : new SingleColorShader(textColor | 0xFF000000))
                                    .setBold(boldStyle)
                                    .setStrikeThroughShader(textColor == null ? null : new SingleColorShader(textColor | 0xFF000000))
                                    .setStrikeThrough(strikethroughStyle)
                                    .setUnderlineShader(textColor == null ? null : new SingleColorShader(textColor | 0xFF000000))
                                    .setUnderline(underlineStyle)
                                    .setItalics(italicStyle),
                            stringBuilder.toString()
                    ));
                    stringBuilder = new StringBuilder();
                }


                int i1 = "0123456789abcdefklmnor".indexOf(text.toLowerCase(Locale.ENGLISH).charAt(i + 1));
                if (i1 < 16) {
                    boldStyle = false;
                    strikethroughStyle = false;
                    underlineStyle = false;
                    italicStyle = false;
                    if (i1 < 0) {
                        i1 = 15;
                    }
                    textColor = colorCode[i1];
                } else if (i1 == 16) {
                } else if (i1 == 17) {
                    boldStyle = true;
                } else if (i1 == 18) {
                    strikethroughStyle = true;
                } else if (i1 == 19) {
                    underlineStyle = true;
                } else if (i1 == 20) {
                    italicStyle = true;
                } else {
                    boldStyle = false;
                    strikethroughStyle = false;
                    underlineStyle = false;
                    italicStyle = false;
                    textColor = null;
                }

                ++i;
            } else {
                stringBuilder.append(c0);
            }
        }

        if (!stringBuilder.toString().isEmpty()) {
            spans.add(new TextSpan(
                    new ParentDelegatingTextStyle()
                            .setTextShader(textColor == null ? null : new SingleColorShader(textColor | 0xFF000000))
                            .setBold(boldStyle)
                            .setStrikeThroughShader(textColor == null ? null : new SingleColorShader(textColor | 0xFF000000))
                            .setStrikeThrough(strikethroughStyle)
                            .setUnderlineShader(textColor == null ? null : new SingleColorShader(textColor | 0xFF000000))
                            .setUnderline(underlineStyle)
                            .setItalics(italicStyle),
                    stringBuilder.toString()
            ));
        }
        return spans;
    }
}
