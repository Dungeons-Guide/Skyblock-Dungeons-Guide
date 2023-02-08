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

package kr.syeyoung.dungeonsguide.mod.features.text;

import kr.syeyoung.dungeonsguide.mod.config.types.AColor;
import kr.syeyoung.dungeonsguide.mod.guiv2.elements.richtext.fonts.DefaultFontRenderer;
import kr.syeyoung.dungeonsguide.mod.guiv2.elements.richtext.fonts.FontRenderer;
import kr.syeyoung.dungeonsguide.mod.guiv2.elements.richtext.shaders.Shader;
import kr.syeyoung.dungeonsguide.mod.guiv2.elements.richtext.styles.ITextStyle;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.function.Supplier;

@Setter @Accessors(chain = true)
public class DefaultingDelegatingTextStyle implements ITextStyle, Cloneable {
    public Double size;
    public Double topAscent;
    public Double bottomAscent;



    public Boolean bold;
    public Boolean italics;
    public Boolean strikeThrough;
    public Boolean underline;
    public Boolean outline;
    public Boolean shadow;

    public AColor backgroundShader;
    public AColor textShader;
    public AColor strikeThroughShader;
    public AColor underlineShader;
    public AColor outlineShader;
    public AColor shadowShader;


    @Getter @Setter
    public Supplier<DefaultingDelegatingTextStyle> parent;
    public FontRenderer fontRenderer;

    public static DefaultingDelegatingTextStyle derive(Supplier<DefaultingDelegatingTextStyle> parent) {
        DefaultingDelegatingTextStyle defaultTextHUDFeatureStyleFeature = new DefaultingDelegatingTextStyle();
        defaultTextHUDFeatureStyleFeature.setParent(parent);
        return defaultTextHUDFeatureStyleFeature;
    }

    public static DefaultingDelegatingTextStyle ofDefault() {
        DefaultingDelegatingTextStyle parentDelegatingTextStyle = new DefaultingDelegatingTextStyle();
        parentDelegatingTextStyle.size = 8.0;
        parentDelegatingTextStyle.topAscent = 0.0;
        parentDelegatingTextStyle.bottomAscent = 1 / 8.0;
        parentDelegatingTextStyle.bold = false;
        parentDelegatingTextStyle.italics = false;
        parentDelegatingTextStyle.strikeThrough = false;
        parentDelegatingTextStyle.underline = false;
        parentDelegatingTextStyle.shadow = false;
        parentDelegatingTextStyle.outline = false;

        parentDelegatingTextStyle.backgroundShader = null;
        parentDelegatingTextStyle.textShader = new AColor(0xFFFFFFFF, true);
        parentDelegatingTextStyle.strikeThroughShader = new AColor(0xFF000000, true);
        parentDelegatingTextStyle.underlineShader = new AColor(0xFF000000, true);
        parentDelegatingTextStyle.outlineShader = new AColor(0xFF000000, true);
        parentDelegatingTextStyle.shadowShader = new AColor(0xFF000000, true);

        parentDelegatingTextStyle.fontRenderer = DefaultFontRenderer.DEFAULT_RENDERER;
        return parentDelegatingTextStyle;
    }
    
    public DefaultingDelegatingTextStyle getParent() {
        return parent.get();
    }

    @Override
    public Double getSize() {
        return parent != null && size == null ? getParent().getSize() : size;
    }

    @Override
    public Double getTopAscent() {
        return parent != null && topAscent == null ? getParent().getTopAscent() : topAscent;
    }

    @Override
    public Double getBottomAscent() {
        return parent != null && bottomAscent == null ? getParent().getBottomAscent() : bottomAscent;
    }

    @Override
    public Boolean isBold() {
        return parent != null && bold == null ? getParent().isBold() : bold;
    }

    @Override
    public Boolean isItalics() {
        return parent != null && italics == null ? getParent().isItalics() : italics;
    }

    @Override
    public Boolean isOutline() {
        return parent != null && outline == null ? getParent().isOutline() : outline;
    }

    @Override
    public Boolean isShadow() {
        return parent != null && shadow == null ? getParent().isShadow() : shadow;
    }

    @Override
    public Boolean isStrikeThrough() {
        return parent != null && strikeThrough == null ? getParent().isStrikeThrough() : strikeThrough;
    }

    @Override
    public Boolean isUnderline() {
        return parent != null && underline == null ? getParent().isUnderline() : underline;
    }

    @Override
    public FontRenderer getFontRenderer() {
        return parent != null && fontRenderer == null ? getParent().getFontRenderer() : fontRenderer;
    }

    @Override
    public Shader getShadowShader() {
        return parent != null && shadowShader == null ? getParent().getShadowShader() : shadowShader == null ? null : shadowShader.getShader();
    }

    @Override
    public Shader getBackgroundShader() {
        return parent != null && backgroundShader == null ? getParent().getBackgroundShader() : backgroundShader == null ? null : backgroundShader.getShader();
    }

    @Override
    public Shader getOutlineShader() {
        return parent != null && outlineShader == null ? getParent().getOutlineShader() : outlineShader == null ? null : outlineShader.getShader();
    }

    @Override
    public Shader getStrikeThroughShader() {
        return parent != null && strikeThroughShader == null ? getParent().getStrikeThroughShader() : strikeThroughShader == null ? null : strikeThroughShader.getShader();
    }

    @Override
    public Shader getTextShader() {
        return parent != null && textShader == null ? getParent().getTextShader() : textShader == null ? null : textShader.getShader();
    }

    @Override
    public Shader getUnderlineShader() {
        return parent != null && underlineShader == null ? getParent().getUnderlineShader() : underlineShader == null ? null : underlineShader.getShader();
    }

    public DefaultingDelegatingTextStyle clone() {
        try {
            return (DefaultingDelegatingTextStyle) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }
}
