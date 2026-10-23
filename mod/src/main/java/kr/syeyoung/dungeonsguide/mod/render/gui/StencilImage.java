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

package kr.syeyoung.dungeonsguide.mod.render.gui;

import com.mojang.blaze3d.opengl.GlStateManager;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.state.gui.GuiElementRenderState;
import org.jetbrains.annotations.Nullable;
import com.mojang.blaze3d.opengl.GlTexture;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuTexture;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.textures.TextureFormat;
import lombok.Getter;
import org.lwjgl.opengl.GL30;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Map;

/**
 * Color texture through Blaze3D, plus DG's own GL framebuffer with a DEPTH24_STENCIL8 renderbuffer (D74).
 * GlCommandEncoderMixin binds this framebuffer for render passes that target the color view (D75).
 */
public class StencilImage implements AutoCloseable {
    private static final Map<GpuTextureView, StencilImage> IMAGES = Collections.synchronizedMap(new IdentityHashMap<>());

    private final String label;
    @Getter
    private GpuTexture colorTexture;
    @Getter
    private GpuTextureView colorView;
    @Getter
    private int fbo = -1;
    private int stencilRenderbuffer = -1;
    @Getter
    private int width, height;

    public StencilImage(String label) {
        this.label = label;
    }

    public static int getFboFor(GpuTextureView view) {
        StencilImage image = IMAGES.get(view);
        return image == null ? -1 : image.fbo;
    }

    public void ensureSize(int width, int height) {
        if (colorTexture != null && this.width == width && this.height == height) return;
        close();
        this.width = width;
        this.height = height;

        colorTexture = RenderSystem.getDevice().createTexture(() -> label, GpuTexture.USAGE_RENDER_ATTACHMENT | GpuTexture.USAGE_TEXTURE_BINDING, TextureFormat.RGBA8, width, height, 1, 1);
        colorView = RenderSystem.getDevice().createTextureView(colorTexture);

        // raw gl section (D74), the framebuffer binding goes through GlStateManager so its cache stays right
        fbo = GL30.glGenFramebuffers();
        GlStateManager._glBindFramebuffer(GL30.GL_FRAMEBUFFER, fbo);
        GL30.glFramebufferTexture2D(GL30.GL_FRAMEBUFFER, GL30.GL_COLOR_ATTACHMENT0, GL30.GL_TEXTURE_2D, ((GlTexture) colorTexture).glId(), 0);
        stencilRenderbuffer = GL30.glGenRenderbuffers();
        GL30.glBindRenderbuffer(GL30.GL_RENDERBUFFER, stencilRenderbuffer);
        GL30.glRenderbufferStorage(GL30.GL_RENDERBUFFER, GL30.GL_DEPTH24_STENCIL8, width, height);
        GL30.glBindRenderbuffer(GL30.GL_RENDERBUFFER, 0);
        GL30.glFramebufferRenderbuffer(GL30.GL_FRAMEBUFFER, GL30.GL_DEPTH_STENCIL_ATTACHMENT, GL30.GL_RENDERBUFFER, stencilRenderbuffer);
        int status = GL30.glCheckFramebufferStatus(GL30.GL_FRAMEBUFFER);
        GlStateManager._glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0);
        if (status != GL30.GL_FRAMEBUFFER_COMPLETE)
            throw new IllegalStateException("DG stencil framebuffer incomplete: " + status);

        IMAGES.put(colorView, this);
    }

    // D80: the finished image goes back into vanilla's gui as one textured element covering the window
    public void extractComposite(GuiGraphicsExtractor graphics) {
        float guiScale = Minecraft.getInstance().getWindow().getGuiScale();
        graphics.guiRenderState.addGuiElement(new Composite(colorView, width / guiScale, height / guiScale));
    }

    private record Composite(GpuTextureView view, float width, float height) implements GuiElementRenderState {
        @Override
        public void buildVertices(VertexConsumer consumer) {
            // gl textures start at the bottom row
            consumer.addVertex(0, 0, 0).setUv(0, 1).setColor(-1);
            consumer.addVertex(0, height, 0).setUv(0, 0).setColor(-1);
            consumer.addVertex(width, height, 0).setUv(1, 0).setColor(-1);
            consumer.addVertex(width, 0, 0).setUv(1, 1).setColor(-1);
        }

        // DG draws with translucent blending into a cleared image, so the image is premultiplied
        @Override
        public RenderPipeline pipeline() {
            return RenderPipelines.GUI_TEXTURED_PREMULTIPLIED_ALPHA;
        }

        @Override
        public TextureSetup textureSetup() {
            return TextureSetup.singleTexture(view, RenderSystem.getSamplerCache().getClampToEdge(FilterMode.NEAREST));
        }

        @Override
        public @Nullable ScreenRectangle scissorArea() {
            return null;
        }

        @Override
        public @Nullable ScreenRectangle bounds() {
            return null;
        }
    }

    @Override
    public void close() {
        if (colorView != null) {
            IMAGES.remove(colorView);
            colorView.close();
            colorView = null;
        }
        if (colorTexture != null) {
            colorTexture.close();
            colorTexture = null;
        }
        if (fbo != -1) {
            GlStateManager._glDeleteFramebuffers(fbo);
            fbo = -1;
        }
        if (stencilRenderbuffer != -1) {
            GL30.glDeleteRenderbuffers(stencilRenderbuffer);
            stencilRenderbuffer = -1;
        }
    }
}
