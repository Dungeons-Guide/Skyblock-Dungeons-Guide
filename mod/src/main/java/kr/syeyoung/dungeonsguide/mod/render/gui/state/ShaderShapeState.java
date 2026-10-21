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

package kr.syeyoung.dungeonsguide.mod.render.gui.state;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.VertexConsumer;
import kr.syeyoung.dungeonsguide.mod.shader.ShaderProgram;
import kr.syeyoung.dungeonsguide.mod.shader.ShaderUniforms;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.client.gui.render.TextureSetup;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3x2f;

import java.awt.*;

/**
 * A quad drawn with one of DG's shaders (D78) and that shader's uniforms (D103). Corners and colors in order.
 */
@AllArgsConstructor
public class ShaderShapeState implements DGRenderState {
    private final Matrix3x2f pose;
    private final ShaderProgram shader;
    @Getter
    private final ShaderUniforms uniforms;
    private final float[] corners; // x0, y0, x1, y1, x2, y2, x3, y3
    private final int[] colors;
    private final Rectangle scissor;
    private final boolean stencilMask;

    @Override
    public RenderPipeline pipeline() {
        return shader.getPipeline();
    }

    @Override
    public TextureSetup textureSetup() {
        return TextureSetup.noTexture();
    }

    @Override
    public @Nullable Rectangle scissor() {
        return scissor;
    }

    @Override
    public boolean stencilMask() {
        return stencilMask;
    }

    @Override
    public void buildVertices(VertexConsumer consumer) {
        for (int i = 0; i < 4; i++)
            consumer.addVertexWith2DPose(pose, corners[i * 2], corners[i * 2 + 1]).setColor(colors[i]);
    }
}
