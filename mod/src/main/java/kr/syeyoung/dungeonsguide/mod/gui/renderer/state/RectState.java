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

package kr.syeyoung.dungeonsguide.mod.gui.renderer.state;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.VertexConsumer;
import lombok.AllArgsConstructor;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.renderer.RenderPipelines;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3x2f;

import java.awt.*;

/**
 * Solid colored quad. Corners in order, transformed by pose.
 */
@AllArgsConstructor
public class RectState implements DGRenderState {
    private final Matrix3x2f pose;
    private final float[] corners; // x0, y0, x1, y1, x2, y2, x3, y3
    private final int color;
    private final Rectangle scissor;
    private final boolean stencilMask;

    @Override
    public RenderPipeline pipeline() {
        return RenderPipelines.GUI;
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
            consumer.addVertexWith2DPose(pose, corners[i * 2], corners[i * 2 + 1]).setColor(color);
    }
}
