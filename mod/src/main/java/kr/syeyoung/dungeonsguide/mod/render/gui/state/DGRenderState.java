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
import net.minecraft.client.gui.render.TextureSetup;
import org.jetbrains.annotations.Nullable;

import java.awt.*;

/**
 * Something DG draws into a StencilImage. Recorded by RenderingContext, executed by DGGuiRenderer.
 */
public interface DGRenderState {
    RenderPipeline pipeline();

    // resolved when DGGuiRenderer executes, so the lightmap is the ui one
    TextureSetup textureSetup();

    // gl coordinates (origin bottom left) in image pixels, null for no scissor
    @Nullable Rectangle scissor();

    // drawn while a stencil mask is being written: no color output
    boolean stencilMask();

    void buildVertices(VertexConsumer consumer);
}
