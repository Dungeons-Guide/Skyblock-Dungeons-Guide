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
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.client.gui.render.TextureSetup;
import org.jetbrains.annotations.Nullable;

import java.awt.*;

/**
 * Stencil step of the Stencil / NegativeStencil elements, executed as raw gl (D74/D75). Not a draw.
 */
@AllArgsConstructor
public class StencilState implements DGRenderState {
    public enum Action {
        // clear the stencil, following draws write 1 and no color
        BEGIN_MASK,
        // following draws only where stencil == ref
        BEGIN_MASKED,
        END
    }

    @Getter
    private final Action action;
    @Getter
    private final int ref;
    private final Rectangle scissor;

    @Override
    public RenderPipeline pipeline() {
        return null;
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
        return false;
    }

    @Override
    public void buildVertices(VertexConsumer consumer) {}
}
