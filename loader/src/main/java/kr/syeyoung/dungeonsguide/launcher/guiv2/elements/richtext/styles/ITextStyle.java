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

package kr.syeyoung.dungeonsguide.launcher.guiv2.elements.richtext.styles;

public interface ITextStyle {
    Double getSize();

    Double getTopAscent();
    Double getBottomAscent();

    Boolean isBold();

    Boolean isItalics();

    Boolean isStrikeThrough();

    Boolean isUnderline();

    Boolean isOutline();

    Boolean isShadow();

    kr.syeyoung.dungeonsguide.mod.guiv2.elements.richtext.shaders.Shader getBackgroundShader();

    kr.syeyoung.dungeonsguide.mod.guiv2.elements.richtext.shaders.Shader getTextShader();

    kr.syeyoung.dungeonsguide.mod.guiv2.elements.richtext.shaders.Shader getStrikeThroughShader();

    kr.syeyoung.dungeonsguide.mod.guiv2.elements.richtext.shaders.Shader getUnderlineShader();

    kr.syeyoung.dungeonsguide.mod.guiv2.elements.richtext.shaders.Shader getOutlineShader();

    kr.syeyoung.dungeonsguide.mod.guiv2.elements.richtext.shaders.Shader getShadowShader();

    kr.syeyoung.dungeonsguide.mod.guiv2.elements.richtext.fonts.FontRenderer getFontRenderer();
}
