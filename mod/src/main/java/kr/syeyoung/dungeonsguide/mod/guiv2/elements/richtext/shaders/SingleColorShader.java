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

import net.minecraft.client.renderer.GlStateManager;

public class SingleColorShader implements Shader{
    private float r, g, b, a;

    // argb
    public SingleColorShader(int color) {
        r = ((color >> 16) & 0xFF) / 255.0f;
        g = ((color >> 8) & 0xFF) / 255.0f;
        b = ((color) & 0xFF) / 255.0f;
        r = ((color >> 24) & 0xFF) / 255.0f;
    }
    @Override
    public void useShader() {
        GlStateManager.color(r,g,b,a);
    }

    @Override
    public void freeShader() {}
}
