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

package kr.syeyoung.dungeonsguide.mod.guiv2.elements.richtext.shaders;

import kr.syeyoung.dungeonsguide.mod.config.types.AColor;
import kr.syeyoung.dungeonsguide.mod.utils.RenderUtils;
import net.minecraft.client.renderer.GlStateManager;

public class ChromaShader implements Shader {
    private AColor aColor;
    public ChromaShader(AColor aColor) {
        this.aColor = aColor;
    }


    // argb=
    @Override
    public void useShader() {
        int color = RenderUtils.getColorAt(0,0,aColor);
        float r = ((color >> 16) & 0xFF) / 255.0f;
        float g = ((color >> 8) & 0xFF) / 255.0f;
        float b = ((color) & 0xFF) / 255.0f;
        float a = ((color >> 24) & 0xFF) / 255.0f;
        GlStateManager.color(r,g,b,a);
    }

    @Override
    public void freeShader() {}
}
