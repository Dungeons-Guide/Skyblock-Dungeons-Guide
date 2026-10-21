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
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.vertex.VertexConsumer;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.gui.render.GuiItemAtlas;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.item.TrackingItemStackRenderState;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3x2f;

import java.awt.*;

/**
 * A 16x16 gui item (D100). DGGuiRenderer renders it into its item atlas and sets the slot before drawing.
 */
public class ItemState implements DGRenderState {
    private final Matrix3x2f pose;
    @Getter
    private final TrackingItemStackRenderState item;
    private final float x, y;
    private final Rectangle scissor;
    private final boolean stencilMask;
    @Getter @Setter
    private GuiItemAtlas.SlotView slot;

    public ItemState(Matrix3x2f pose, TrackingItemStackRenderState item, float x, float y, Rectangle scissor, boolean stencilMask) {
        this.pose = pose;
        this.item = item;
        this.x = x;
        this.y = y;
        this.scissor = scissor;
        this.stencilMask = stencilMask;
    }

    // the atlas holds premultiplied colors
    @Override
    public RenderPipeline pipeline() {
        return RenderPipelines.GUI_TEXTURED_PREMULTIPLIED_ALPHA;
    }

    @Override
    public TextureSetup textureSetup() {
        return TextureSetup.singleTexture(slot.textureView(), RenderSystem.getSamplerCache().getRepeat(FilterMode.NEAREST));
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
        consumer.addVertexWith2DPose(pose, x, y).setUv(slot.u0(), slot.v0()).setColor(-1);
        consumer.addVertexWith2DPose(pose, x, y + 16).setUv(slot.u0(), slot.v1()).setColor(-1);
        consumer.addVertexWith2DPose(pose, x + 16, y + 16).setUv(slot.u1(), slot.v1()).setColor(-1);
        consumer.addVertexWith2DPose(pose, x + 16, y).setUv(slot.u1(), slot.v0()).setColor(-1);
    }
}
