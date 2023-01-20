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

package kr.syeyoung.dungeonsguide.mod.guiv2.elements.richtext.styles;

import kr.syeyoung.dungeonsguide.mod.guiv2.elements.richtext.fonts.FontRenderer;
import kr.syeyoung.dungeonsguide.mod.guiv2.elements.richtext.shaders.Shader;
import lombok.Getter;
import lombok.Setter;

public class CompiledTextStyle implements ITextStyle {
    public Double size;
    public Double topAscent;
    public Double bottomAscent;

    public boolean bold;
    public boolean italics;
    public boolean strikeThrough;
    public boolean underline;
    public boolean outline;
    public boolean shadow;

    public Shader backgroundShader;
    public Shader textShader;
    public Shader strikeThroughShader;
    public Shader underlineShader;
    public Shader outlineShader;
    public Shader shadowShader;


    public FontRenderer fontRenderer;

    public CompiledTextStyle(ITextStyle from) {
        this.size = from.getSize();
        this.topAscent = from.getTopAscent();
        this.bottomAscent = from.getBottomAscent();

        this.bold = from.isBold();
        this.italics = from.isItalics();
        this.strikeThrough = from.isStrikeThrough();
        this.underline = from.isUnderline();
        this.outline = from.isOutline();
        this.shadow = from.isShadow();

        this.backgroundShader = from.getBackgroundShader();
        this.textShader = from.getTextShader();
        this.strikeThroughShader = from.getStrikeThroughShader();
        this.underlineShader = from.getUnderlineShader();
        this.outlineShader = from.getOutlineShader();
        this.shadowShader = from.getShadowShader();
        this.fontRenderer = from.getFontRenderer();
    }

    @Override
    public Double getSize() {
        return size;
    }

    @Override
    public Double getTopAscent() {
        return topAscent;
    }

    @Override
    public Double getBottomAscent() {
        return bottomAscent;
    }

    @Override
    public Boolean isBold() {
        return bold;
    }

    @Override
    public Boolean isItalics() {
        return italics;
    }

    @Override
    public Boolean isStrikeThrough() {
        return strikeThrough;
    }

    @Override
    public Boolean isUnderline() {
        return underline;
    }

    @Override
    public Boolean isOutline() {
        return outline;
    }

    @Override
    public Boolean isShadow() {
        return shadow;
    }

    @Override
    public Shader getBackgroundShader() {
        return backgroundShader;
    }

    @Override
    public Shader getTextShader() {
        return textShader;
    }

    @Override
    public Shader getStrikeThroughShader() {
        return strikeThroughShader;
    }

    @Override
    public Shader getUnderlineShader() {
        return underlineShader;
    }

    @Override
    public Shader getOutlineShader() {
        return outlineShader;
    }

    @Override
    public Shader getShadowShader() {
        return shadowShader;
    }

    @Override
    public FontRenderer getFontRenderer() {
        return fontRenderer;
    }
}
