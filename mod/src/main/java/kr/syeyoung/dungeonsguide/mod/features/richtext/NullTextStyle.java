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

import kr.syeyoung.dungeonsguide.mod.guiv2.elements.richtext.fonts.DefaultFontRenderer;
import kr.syeyoung.dungeonsguide.mod.guiv2.elements.richtext.fonts.FontRenderer;
import kr.syeyoung.dungeonsguide.mod.guiv2.elements.richtext.shaders.Shader;
import kr.syeyoung.dungeonsguide.mod.guiv2.elements.richtext.shaders.SingleColorShader;
import kr.syeyoung.dungeonsguide.mod.guiv2.elements.richtext.styles.ITextStyle;
import lombok.Setter;
import lombok.experimental.Accessors;

@Setter @Accessors(chain = true)
public class NullTextStyle implements ITextStyle, Cloneable {
    @Override
    public Double getSize() {
        return 0.0;
    }

    @Override
    public Boolean hasBackground() {
        return false;
    }

    @Override
    public Double getTopAscent() {
        return 0.0;
    }

    @Override
    public Double getBottomAscent() {
        return 0.0;
    }

    @Override
    public Boolean isBold() {
        return false;
    }

    @Override
    public Boolean isItalics() {
        return false;
    }

    @Override
    public Boolean isStrikeThrough() {
        return false;
    }

    @Override
    public Boolean isUnderline() {
        return false;
    }

    @Override
    public Boolean isOutline() {
        return false;
    }

    @Override
    public Boolean isShadow() {
        return false;
    }

    @Override
    public Shader getBackgroundShader() {
        return null;
    }

    @Override
    public Shader getTextShader() {
        return new SingleColorShader(0);
    }

    @Override
    public Shader getStrikeThroughShader() {
        return new SingleColorShader(0);
    }

    @Override
    public Shader getUnderlineShader() {
        return new SingleColorShader(0);
    }

    @Override
    public Shader getOutlineShader() {
        return new SingleColorShader(0);
    }

    @Override
    public Shader getShadowShader() {
        return new SingleColorShader(0);
    }

    @Override
    public FontRenderer getFontRenderer() {
        return DefaultFontRenderer.DEFAULT_RENDERER;
    }
}
