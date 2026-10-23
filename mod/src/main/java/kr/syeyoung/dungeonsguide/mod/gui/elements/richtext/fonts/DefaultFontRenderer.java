/*
 * Dungeons Guide - The most intelligent Hypixel Skyblock Dungeons Mod
 * Copyright (C) 2023  cyoung06 (syeyoung)
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

package kr.syeyoung.dungeonsguide.mod.gui.elements.richtext.fonts;

import kr.syeyoung.dungeonsguide.mod.gui.elements.richtext.FlatTextSpan;
import kr.syeyoung.dungeonsguide.mod.gui.elements.richtext.shaders.Shader;
import kr.syeyoung.dungeonsguide.mod.gui.elements.richtext.styles.ITextStyle;
import kr.syeyoung.dungeonsguide.mod.gui.renderer.RenderingContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.font.TextRenderable;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;

import java.util.ArrayList;
import java.util.List;

/**
 * DG's rich text on top of vanilla's Font: glyph metrics and drawing come from vanilla (D79).
 * Vanilla's font is 8 units tall at text size 8.
 */
public class DefaultFontRenderer implements FontRenderer {
    public static DefaultFontRenderer DEFAULT_RENDERER = new DefaultFontRenderer();

    private static Font font() {
        return Minecraft.getInstance().font;
    }

    private static Style styleOf(ITextStyle textStyle) {
        return Style.EMPTY.withBold(textStyle.isBold()).withItalic(textStyle.isItalics());
    }

    @Override
    public double getWidth(char text, ITextStyle textStyle) {
        if (text == '\n') return 0;
        return font().getSplitter().stringWidth(FormattedCharSequence.forward(String.valueOf(text), styleOf(textStyle))) * textStyle.getSize() / 8;
    }

    @Override
    public double getBaselineHeight(ITextStyle textStyle) {
        return 7 * textStyle.getSize() / 8.0;
    }

    @Override
    public void render(RenderingContext context, FlatTextSpan lineElement, double x, double y, double currentScale) {
        if (lineElement.textStyle.hasBackground()) {
            context.drawRect(x, y, x + lineElement.getWidth(), y + lineElement.textStyle.getSize(),
                    lineElement.textStyle.getBackgroundShader().getColor(x, y));
        }
        y += lineElement.textStyle.getTopAscent();

        if (lineElement.textStyle.isShadow())
            render(context, lineElement, x+1,y+1, true);
        render(context, lineElement, x,y, false);
    }

    private void render(RenderingContext context, FlatTextSpan lineElement, double x, double y, boolean isShadow) {
        ITextStyle textStyle = lineElement.textStyle;
        // the shadow pass draws everything in the shadow color
        Shader textShader = isShadow ? textStyle.getShadowShader() : textStyle.getTextShader();

        List<TextRenderable> glyphs = new ArrayList<>();
        font().prepareText(FormattedCharSequence.forward(new String(lineElement.value), styleOf(textStyle)), 0, 0, textShader.getColor(x, y), false, false, 0)
                .visit(new Font.GlyphVisitor() {
                    @Override
                    public void acceptGlyph(TextRenderable.Styled glyph) {
                        glyphs.add(glyph);
                    }

                    @Override
                    public void acceptEffect(TextRenderable effect) {
                        glyphs.add(effect);
                    }
                });

        double scale = textStyle.getSize() / 8.0;
        context.pose().pushMatrix();
        // 1.8.9 drew glyphs one unit down
        context.pose().translate((float) x, (float) (y + 1));
        context.pose().scale((float) scale, (float) scale);
        context.drawGlyphs(glyphs);
        context.pose().popMatrix();

        double endX = x + lineElement.getWidth();
        double baseline = getBaselineHeight(textStyle);
        if (textStyle.isStrikeThrough()) {
            Shader shader = isShadow ? textShader : textStyle.getStrikeThroughShader();
            context.drawRect(x, y + baseline/2, endX, y + baseline/2+1, shader.getColor(x, y));
        }
        if (textStyle.isUnderline()) {
            Shader shader = isShadow ? textShader : textStyle.getUnderlineShader();
            context.drawRect(x, y + baseline, endX, y + baseline+1, shader.getColor(x, y));
        }
    }
}
