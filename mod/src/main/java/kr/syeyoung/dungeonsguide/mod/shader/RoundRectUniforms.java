/*
 * Dungeons Guide - The most intelligent Hypixel Skyblock Dungeons Mod
 * Copyright (C) 2026  cyoung06 (syeyoung)
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

package kr.syeyoung.dungeonsguide.mod.shader;

import com.mojang.blaze3d.buffers.Std140Builder;
import com.mojang.blaze3d.buffers.Std140SizeCalculator;

import java.nio.ByteBuffer;

// roundrect and chromaroundrect
public record RoundRectUniforms(float radius, float halfSizeX, float halfSizeY, float centerPosX, float centerPosY, float smoothness) implements ShaderUniforms {
    private static final int SIZE = new Std140SizeCalculator().putFloat().putVec2().putVec2().putFloat().get();

    @Override
    public void write(ByteBuffer buffer) {
        Std140Builder.intoBuffer(buffer).putFloat(radius).putVec2(halfSizeX, halfSizeY).putVec2(centerPosX, centerPosY).putFloat(smoothness);
    }

    @Override
    public String blockName() {
        return "RoundRect";
    }

    @Override
    public int blockSize() {
        return SIZE;
    }
}
