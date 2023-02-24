/*
 *     Dungeons Guide - The most intelligent Hypixel Skyblock Dungeons Mod
 *     Copyright (C) 2021  cyoung06
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Affero General Public License as published
 *     by the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Affero General Public License for more details.
 *
 *     You should have received a copy of the GNU Affero General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package kr.syeyoung.dungeonsguide.mod.features.text;

import kr.syeyoung.dungeonsguide.mod.config.types.AColor;
import kr.syeyoung.dungeonsguide.mod.guiv2.elements.richtext.shaders.ChromaShader;
import kr.syeyoung.dungeonsguide.mod.guiv2.elements.richtext.shaders.SingleColorShader;
import kr.syeyoung.dungeonsguide.mod.guiv2.elements.richtext.styles.ParentDelegatingTextStyle;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TextStyle {
    private String groupName;
    private AColor color;
    private AColor background;
    private boolean shadow = false;

    public TextStyle(String groupName, AColor color, AColor background, boolean shadow) {
        this.groupName = groupName;
        this.color = color;
        this.background = background;
        this.shadow = shadow;
    }

    public void setColor(AColor color) {
        this.color = color;
        if (!color.isChroma())
        linked.textShader = new SingleColorShader(color.getRGB());
        else
        linked.textShader = new ChromaShader(color);
    }

    public void setBackground(AColor background) {
        this.background = background;
        if (!background.isChroma())
        linked.backgroundShader = new SingleColorShader(background.getRGB());
        else
        linked.backgroundShader = new ChromaShader(color);
    }

    private ParentDelegatingTextStyle linked = new ParentDelegatingTextStyle();
}
