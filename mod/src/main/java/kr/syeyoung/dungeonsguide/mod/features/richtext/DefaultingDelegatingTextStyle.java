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

package kr.syeyoung.dungeonsguide.mod.features.richtext;

import kr.syeyoung.dungeonsguide.mod.config.types.AColor;
import kr.syeyoung.dungeonsguide.mod.guiv2.elements.richtext.fonts.DefaultFontRenderer;
import kr.syeyoung.dungeonsguide.mod.guiv2.elements.richtext.fonts.FontRenderer;
import kr.syeyoung.dungeonsguide.mod.guiv2.elements.richtext.shaders.Shader;
import kr.syeyoung.dungeonsguide.mod.guiv2.elements.richtext.styles.ITextStyle;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.Optional;
import java.util.function.Supplier;

@Setter @Accessors(chain = true) @RequiredArgsConstructor
public class DefaultingDelegatingTextStyle implements ITextStyle, Cloneable {
    public String name;

    public Double size;
    public Double topAscent;
    public Double bottomAscent;



    public Boolean bold;
    public Boolean italics;
    public Boolean strikeThrough;
    public Boolean underline;
    public Boolean outline;
    public Boolean shadow;
    public Boolean background;
    public AColor backgroundShader;
    public AColor textShader;
    // field null: use parent
    // optional null: default color derived from text
    public Optional<AColor> strikeThroughShader;
    public Optional<AColor> underlineShader;
    public Optional<AColor> outlineShader;
    public Optional<AColor> shadowShader;


    @Getter @Setter
    public Supplier<ITextStyle> parent;
    public FontRenderer fontRenderer;

    public static DefaultingDelegatingTextStyle derive(String name, Supplier<ITextStyle> parent) {
        DefaultingDelegatingTextStyle defaultTextHUDFeatureStyleFeature = new DefaultingDelegatingTextStyle();
        defaultTextHUDFeatureStyleFeature.setName(name);
        defaultTextHUDFeatureStyleFeature.setParent(parent);
        return defaultTextHUDFeatureStyleFeature;
    }

    public static DefaultingDelegatingTextStyle ofDefault(String name) {
        DefaultingDelegatingTextStyle parentDelegatingTextStyle = new DefaultingDelegatingTextStyle();
        parentDelegatingTextStyle.setName(name);
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

        parentDelegatingTextStyle.backgroundShader = new AColor(0XFF000000, true);
        parentDelegatingTextStyle.textShader = new AColor(0xFFFFFFFF, true);
        parentDelegatingTextStyle.strikeThroughShader = Optional.empty();
        parentDelegatingTextStyle.underlineShader = Optional.empty();
        parentDelegatingTextStyle.outlineShader = Optional.empty();
        parentDelegatingTextStyle.shadowShader = Optional.empty();

        parentDelegatingTextStyle.fontRenderer = DefaultFontRenderer.DEFAULT_RENDERER;
        return parentDelegatingTextStyle;
    }
    
    public ITextStyle getParent() {
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
    public Boolean hasBackground() {
        return parent != null && background == null ? getParent().hasBackground() : background;
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
        Optional<AColor> color;
        if (shadowShader != null) color = shadowShader;
        else if (parent != null && parent.get() instanceof DefaultingDelegatingTextStyle) color = ((DefaultingDelegatingTextStyle) parent.get()).getShadowShaderColor();
        else return parent.get().getShadowShader();
        if (color.isPresent()) return color.get().getShader();
        AColor textShader = getTextShaderColor();
        AColor aColor = new AColor(textShader.getRed()/4, textShader.getGreen()/4, textShader.getBlue()/4, textShader.getAlpha());
        aColor.setChroma(textShader.isChroma());
        aColor.setChromaSpeed(textShader.getChromaSpeed());
        return aColor.getShader();
    }

    @Override
    public Shader getBackgroundShader() {
        return parent != null && backgroundShader == null ? getParent().getBackgroundShader() : backgroundShader == null ? null : backgroundShader.getShader();
    }

    @Override
    public Shader getOutlineShader() {
        Optional<AColor> color;
        if (outlineShader != null) color = outlineShader;
        else if (parent != null && parent.get() instanceof DefaultingDelegatingTextStyle) color = ((DefaultingDelegatingTextStyle) parent.get()).getOutlineShaderColor();
        else return parent.get().getOutlineShader();
        if (color.isPresent()) return color.get().getShader();
        return getTextShaderColor().getShader();
    }

    @Override
    public Shader getStrikeThroughShader() {
        Optional<AColor> color;
        if (strikeThroughShader != null) color = strikeThroughShader;
        else if (parent != null && parent.get() instanceof DefaultingDelegatingTextStyle) color = ((DefaultingDelegatingTextStyle) parent.get()).getStrikeThroughShaderColor();
        else return parent.get().getStrikeThroughShader();
        if (color.isPresent()) return color.get().getShader();
        return getTextShaderColor().getShader();
    }

    @Override
    public Shader getTextShader() {
        return parent != null && textShader == null ? getParent().getTextShader() : textShader == null ? null : textShader.getShader();
    }

    @Override
    public Shader getUnderlineShader() {
        Optional<AColor> color;
        if (underlineShader != null) color = underlineShader;
        else if (parent != null && parent.get() instanceof DefaultingDelegatingTextStyle) color = ((DefaultingDelegatingTextStyle) parent.get()).getUnderlineShaderColor();
        else return parent.get().getUnderlineShader();
        if (color.isPresent()) return color.get().getShader();
        return getTextShaderColor().getShader();
    }


    public Optional<AColor> getShadowShaderColor() {
        return parent != null && shadowShader == null && getParent() instanceof DefaultingDelegatingTextStyle ? ((DefaultingDelegatingTextStyle) getParent()).getShadowShaderColor() : shadowShader;
    }

    public AColor getBackgroundShaderColor() {
        return parent != null && backgroundShader == null && getParent() instanceof DefaultingDelegatingTextStyle ? ((DefaultingDelegatingTextStyle) getParent()).getBackgroundShaderColor() : backgroundShader;
    }

    public Optional<AColor> getOutlineShaderColor() {
        return parent != null && outlineShader == null && getParent() instanceof DefaultingDelegatingTextStyle ? ((DefaultingDelegatingTextStyle) getParent()).getOutlineShaderColor() : outlineShader;
    }

    public Optional<AColor> getStrikeThroughShaderColor() {
        return parent != null && strikeThroughShader == null && getParent() instanceof DefaultingDelegatingTextStyle ? ((DefaultingDelegatingTextStyle) getParent()).getStrikeThroughShaderColor() : strikeThroughShader;
    }

    public AColor getTextShaderColor() {
        return parent != null && textShader == null && getParent() instanceof DefaultingDelegatingTextStyle ? ((DefaultingDelegatingTextStyle) getParent()).getTextShaderColor() : textShader;
    }

    public Optional<AColor> getUnderlineShaderColor() {
        return parent != null && underlineShader == null && getParent() instanceof DefaultingDelegatingTextStyle ? ((DefaultingDelegatingTextStyle) getParent()).getUnderlineShaderColor() : underlineShader;
    }

    public DefaultingDelegatingTextStyle clone() {
        try {
            return (DefaultingDelegatingTextStyle) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }
}
