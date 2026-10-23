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
import com.mojang.blaze3d.textures.GpuSampler;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.vertex.VertexConsumer;
import lombok.AllArgsConstructor;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.renderer.RenderPipelines;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3x2f;

import java.awt.*;
import java.util.function.Supplier;

@AllArgsConstructor
public class TexturedRectState implements DGRenderState {
    private final Matrix3x2f pose;
    private final float x0, y0, x1, y1;
    private final float u0, u1, v0, v1;
    private final int color;
    // textures can be (re)loaded between recording and executing
    private final Supplier<GpuTextureView> texture;
    private final Supplier<GpuSampler> sampler;
    private final Rectangle scissor;
    private final boolean stencilMask;

    @Override
    public RenderPipeline pipeline() {
        return RenderPipelines.GUI_TEXTURED;
    }

    @Override
    public TextureSetup textureSetup() {
        return TextureSetup.singleTexture(texture.get(), sampler.get());
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
        consumer.addVertexWith2DPose(pose, x0, y0).setUv(u0, v0).setColor(color);
        consumer.addVertexWith2DPose(pose, x0, y1).setUv(u0, v1).setColor(color);
        consumer.addVertexWith2DPose(pose, x1, y1).setUv(u1, v1).setColor(color);
        consumer.addVertexWith2DPose(pose, x1, y0).setUv(u1, v0).setColor(color);
    }
}
