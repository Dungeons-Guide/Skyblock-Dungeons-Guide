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

import kr.syeyoung.dungeonsguide.mod.config.types.AColor;
import kr.syeyoung.dungeonsguide.mod.gui.elements.richtext.styles.ITextStyle;
import kr.syeyoung.modapi.rendering.TextStyleConfig;

/**
 * Adapter to convert ITextStyle to TextStyleConfig for use with modapi
 */
public class TextStyleAdapter {

    public static TextStyleConfig toTextStyleConfig(ITextStyle style) {
        // Extract colors from shaders (chroma colors are calculated at shader level)
        int textColor = extractColorFromShader(style.getTextShader(), 0xFFFFFFFF);
        int shadowColor = extractColorFromShader(style.getShadowShader(), 0xFF3F3F3F);
        int strikethroughColor = extractColorFromShader(style.getStrikeThroughShader(), 0xFFFFFFFF);
        int underlineColor = extractColorFromShader(style.getUnderlineShader(), 0xFFFFFFFF);

        return TextStyleConfig.builder()
                .size(style.getSize())
                .topAscent(style.getTopAscent())
                .bottomAscent(style.getBottomAscent())
                .bold(style.isBold())
                .italic(style.isItalics())
                .strikethrough(style.isStrikeThrough())
                .underline(style.isUnderline())
                .shadow(style.isShadow())
                .textColor(textColor)
                .shadowColor(shadowColor)
                .strikethroughColor(strikethroughColor)
                .underlineColor(underlineColor)
                .build();
    }

    private static int extractColorFromShader(kr.syeyoung.dungeonsguide.mod.gui.elements.richtext.shaders.Shader shader, int defaultColor) {
        if (shader == null) {
            return defaultColor;
        }

        // Extract from SingleColorShader
        if (shader instanceof kr.syeyoung.dungeonsguide.mod.gui.elements.richtext.shaders.SingleColorShader) {
            return ((kr.syeyoung.dungeonsguide.mod.gui.elements.richtext.shaders.SingleColorShader) shader).getColor();
        }

        // Extract from ChromaShader - it calculates the chroma color
        if (shader instanceof kr.syeyoung.dungeonsguide.mod.gui.elements.richtext.shaders.ChromaShader) {
            return ((kr.syeyoung.dungeonsguide.mod.gui.elements.richtext.shaders.ChromaShader) shader).getColor();
        }

        return defaultColor;
    }
}



