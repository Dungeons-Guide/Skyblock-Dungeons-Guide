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

import kr.syeyoung.dungeonsguide.mod.guiv2.elements.richtext.fonts.DefaultFontRenderer;
import kr.syeyoung.dungeonsguide.mod.guiv2.elements.richtext.fonts.FontRenderer;
import kr.syeyoung.dungeonsguide.mod.guiv2.elements.richtext.shaders.Shader;
import kr.syeyoung.dungeonsguide.mod.guiv2.elements.richtext.shaders.SingleColorShader;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Setter @Accessors(chain = true)
public class ParentDelegatingTextStyle implements ITextStyle {
    public Double size;
    public Double topAscent;
    public Double bottomAscent;


    public Boolean background;
    public Boolean bold;
    public Boolean italics;
    public Boolean strikeThrough;
    public Boolean underline;
    public Boolean outline;
    public Boolean shadow;

    public Shader backgroundShader;
    public Shader textShader;
    public Shader strikeThroughShader;
    public Shader underlineShader;
    public Shader outlineShader;
    public Shader shadowShader;


    @Getter @Setter
    public ITextStyle parent;
    public FontRenderer fontRenderer;

    public static ParentDelegatingTextStyle ofDefault() {
        ParentDelegatingTextStyle parentDelegatingTextStyle = new ParentDelegatingTextStyle();
        parentDelegatingTextStyle.size = 8.0;
        parentDelegatingTextStyle.topAscent = 0.0;
        parentDelegatingTextStyle.bottomAscent = 1 / 8.0;
        parentDelegatingTextStyle.bold = false;
        parentDelegatingTextStyle.italics = false;
        parentDelegatingTextStyle.strikeThrough = false;
        parentDelegatingTextStyle.underline = false;
        parentDelegatingTextStyle.shadow = false;
        parentDelegatingTextStyle.outline = false;
        parentDelegatingTextStyle.background = false;

        parentDelegatingTextStyle.backgroundShader = new SingleColorShader(0xFFFFFF00);
        parentDelegatingTextStyle.textShader = new SingleColorShader(0xFFFFFFFF);
        parentDelegatingTextStyle.strikeThroughShader = new SingleColorShader(0xFF000000);
        parentDelegatingTextStyle.underlineShader = new SingleColorShader(0xFF000000);
        parentDelegatingTextStyle.outlineShader = new SingleColorShader(0xFF000000);
        parentDelegatingTextStyle.shadowShader = new SingleColorShader(0xFF000000);

        parentDelegatingTextStyle.fontRenderer = DefaultFontRenderer.DEFAULT_RENDERER;
        return parentDelegatingTextStyle;
    }

    @Override
    public Double getSize() {
        return parent != null && size == null ? parent.getSize() : size;
    }

    @Override
    public Double getTopAscent() {
        return parent != null && topAscent == null ? parent.getTopAscent() : topAscent;
    }

    @Override
    public Double getBottomAscent() {
        return parent != null && bottomAscent == null ? parent.getBottomAscent() : bottomAscent;
    }

    @Override
    public Boolean hasBackground() {
        return parent != null && background == null ? parent.hasBackground() : background;
    }

    @Override
    public Boolean isBold() {
        return parent != null && bold == null ? parent.isBold() : bold;
    }

    @Override
    public Boolean isItalics() {
        return parent != null && italics == null ? parent.isItalics() : italics;
    }

    @Override
    public Boolean isOutline() {
        return parent != null && outline == null ? parent.isOutline() : outline;
    }

    @Override
    public Boolean isShadow() {
        return parent != null && shadow == null ? parent.isShadow() : shadow;
    }

    @Override
    public Boolean isStrikeThrough() {
        return parent != null && strikeThrough == null ? parent.isStrikeThrough() : strikeThrough;
    }

    @Override
    public Boolean isUnderline() {
        return parent != null && underline == null ? parent.isUnderline() : underline;
    }

    @Override
    public FontRenderer getFontRenderer() {
        return parent != null && fontRenderer == null ? parent.getFontRenderer() : fontRenderer;
    }

    @Override
    public Shader getShadowShader() {
        return parent != null && shadowShader == null ? parent.getShadowShader() : shadowShader;
    }

    @Override
    public Shader getBackgroundShader() {
        return parent != null && backgroundShader == null ? parent.getBackgroundShader() : backgroundShader;
    }

    @Override
    public Shader getOutlineShader() {
        return parent != null && outlineShader == null ? parent.getOutlineShader() : outlineShader;
    }

    @Override
    public Shader getStrikeThroughShader() {
        return parent != null && strikeThroughShader == null ? parent.getStrikeThroughShader() : strikeThroughShader;
    }

    @Override
    public Shader getTextShader() {
        return parent != null && textShader == null ? parent.getTextShader() : textShader;
    }

    @Override
    public Shader getUnderlineShader() {
        return parent != null && underlineShader == null ? parent.getUnderlineShader() : underlineShader;
    }


}
