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

package kr.syeyoung.dungeonsguide.mod.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.textures.GpuTextureView;
import kr.syeyoung.dungeonsguide.mod.render.gui.StencilImage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

// D75: render passes that target a DG StencilImage use DG's framebuffer, which has the stencil attachment
@Mixin(targets = "com.mojang.blaze3d.opengl.GlCommandEncoder")
public class GlCommandEncoderMixin {
    @ModifyExpressionValue(
            method = "createRenderPass(Ljava/util/function/Supplier;Lcom/mojang/blaze3d/textures/GpuTextureView;Ljava/util/OptionalInt;Lcom/mojang/blaze3d/textures/GpuTextureView;Ljava/util/OptionalDouble;)Lcom/mojang/blaze3d/systems/RenderPassBackend;",
            at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/opengl/GlTextureView;getFbo(Lcom/mojang/blaze3d/opengl/DirectStateAccess;Lcom/mojang/blaze3d/textures/GpuTexture;)I")
    )
    private int dungeonsguide$useStencilFramebuffer(int fbo, @Local(argsOnly = true, ordinal = 0) GpuTextureView colorTexture) {
        int dgFbo = StencilImage.getFboFor(colorTexture);
        return dgFbo == -1 ? fbo : dgFbo;
    }
}
