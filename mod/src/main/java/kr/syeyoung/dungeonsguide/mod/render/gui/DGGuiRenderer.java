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

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.opengl.GlStateManager;
import com.mojang.blaze3d.pipeline.ColorTargetState;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.systems.CommandEncoder;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.VertexFormat;
import kr.syeyoung.dungeonsguide.mod.render.gui.state.DGRenderState;
import kr.syeyoung.dungeonsguide.mod.render.gui.state.ItemState;
import kr.syeyoung.dungeonsguide.mod.render.gui.state.ShaderShapeState;
import kr.syeyoung.dungeonsguide.mod.shader.ShaderUniforms;
import kr.syeyoung.dungeonsguide.mod.render.gui.state.StencilState;
import lombok.AllArgsConstructor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.render.GuiItemAtlas;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.renderer.DynamicUniformStorage;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MappableRingBuffer;
import net.minecraft.client.renderer.Projection;
import net.minecraft.client.renderer.ProjectionMatrixBuffer;
import net.minecraft.resources.Identifier;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.system.MemoryUtil;

import java.awt.*;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.List;

/**
 * Executes DG render states into StencilImages with DG's own render passes (D77).
 * States are recorded while vanilla extracts the gui, and drawn in the render phase right before vanilla's gui (D71).
 */
public class DGGuiRenderer implements AutoCloseable {
    public static final DGGuiRenderer INSTANCE = new DGGuiRenderer();

    private final List<Job> queue = new ArrayList<>();
    private final ByteBufferBuilder byteBufferBuilder = new ByteBufferBuilder(786432);
    private final Map<VertexFormat, MappableRingBuffer> vertexBuffers = new HashMap<>();
    private final Projection projection = new Projection();
    private final ProjectionMatrixBuffer projectionBuffer = new ProjectionMatrixBuffer("dungeonsguide gui");
    private final Map<RenderPipeline, RenderPipeline> maskPipelines = new HashMap<>();
    private final Map<Class<?>, DynamicUniformStorage<ShaderUniforms>> shaderUniforms = new HashMap<>();
    private GuiItemAtlas itemAtlas;
    private int itemAtlasGuiScale;

    public void submit(StencilImage target, List<DGRenderState> states) {
        queue.add(new Job(target, states));
    }

    // called by GameRendererMixin with vanilla's gui fog
    public void renderQueued(GpuBufferSlice fog) {
        try {
            for (Job job : queue) {
                renderInto(job.target, job.states, fog);
            }
        } finally {
            queue.clear();
            if (itemAtlas != null) itemAtlas.endFrame();
            for (DynamicUniformStorage<ShaderUniforms> storage : shaderUniforms.values()) {
                storage.endFrame();
            }
        }
    }

    // executes states into an image now: the gui queue above, and DGWorldRenderer's prepare for world images
    public void renderInto(StencilImage target, List<DGRenderState> states, GpuBufferSlice fog) {
        prepareItems(states);

        List<Step> steps = new ArrayList<>();
        BufferBuilder bufferBuilder = null;
        RenderPipeline batchPipeline = null;
        TextureSetup batchTextures = null;
        Rectangle batchScissor = null;

        for (DGRenderState state : states) {
            if (state instanceof StencilState) {
                if (bufferBuilder != null) recordMesh(steps, bufferBuilder, batchPipeline, batchTextures, batchScissor);
                bufferBuilder = null;
                steps.add(new Step(null, null, null, null, (StencilState) state, 0, null, null, null));
                continue;
            }
            // each shape has its own uniforms, so it's its own draw
            if (state instanceof ShaderShapeState) {
                if (bufferBuilder != null) recordMesh(steps, bufferBuilder, batchPipeline, batchTextures, batchScissor);
                bufferBuilder = null;
                RenderPipeline pipeline = state.stencilMask() ? maskVariant(state.pipeline()) : state.pipeline();
                BufferBuilder shapeBuilder = new BufferBuilder(byteBufferBuilder, pipeline.getVertexFormatMode(), pipeline.getVertexFormat());
                state.buildVertices(shapeBuilder);
                MeshData mesh = shapeBuilder.build();
                if (mesh != null) {
                    Step step = new Step(mesh, pipeline, state.textureSetup(), state.scissor(), null, 0, null, null, null);
                    step.uniforms = ((ShaderShapeState) state).getUniforms();
                    steps.add(step);
                }
                continue;
            }

            // items that didn't fit the atlas are skipped, like vanilla does
            if (state instanceof ItemState && ((ItemState) state).getSlot() == null) continue;

            RenderPipeline pipeline = state.stencilMask() ? maskVariant(state.pipeline()) : state.pipeline();
            TextureSetup textures = state.textureSetup();
            Rectangle scissor = state.scissor();
            if (bufferBuilder == null || pipeline != batchPipeline || !textures.equals(batchTextures) || !Objects.equals(scissor, batchScissor)) {
                if (bufferBuilder != null) recordMesh(steps, bufferBuilder, batchPipeline, batchTextures, batchScissor);
                bufferBuilder = new BufferBuilder(byteBufferBuilder, pipeline.getVertexFormatMode(), pipeline.getVertexFormat());
                batchPipeline = pipeline;
                batchTextures = textures;
                batchScissor = scissor;
            }
            state.buildVertices(bufferBuilder);
        }
        if (bufferBuilder != null) recordMesh(steps, bufferBuilder, batchPipeline, batchTextures, batchScissor);

        upload(steps);

        projection.setupOrtho(1000.0F, 11000.0F, target.getWidth(), target.getHeight(), true);
        // written before the pass opens: buffer writes aren't allowed while a render pass is open
        GpuBufferSlice projectionSlice = projectionBuffer.getBuffer(projection);
        GpuBufferSlice dynamicTransforms = RenderSystem.getDynamicUniforms()
                .writeTransform(new Matrix4f().setTranslation(0.0F, 0.0F, -11000.0F), new Vector4f(1.0F, 1.0F, 1.0F, 1.0F), new Vector3f(), new Matrix4f());

        CommandEncoder encoder = RenderSystem.getDevice().createCommandEncoder();
        try (RenderPass renderPass = encoder.createRenderPass(() -> "Dungeons Guide gui", target.getColorView(), OptionalInt.of(0))) {
            RenderSystem.bindDefaultUniforms(renderPass);
            renderPass.setUniform("Projection", projectionSlice);
            renderPass.setUniform("Fog", fog);
            renderPass.setUniform("DynamicTransforms", dynamicTransforms);

            // raw gl stencil (D74/D75), DG's framebuffer is bound now
            GlStateManager._disableScissorTest();
            GL11.glStencilMask(0xFF);
            GL11.glClearStencil(0);
            GL11.glClear(GL11.GL_STENCIL_BUFFER_BIT);

            for (Step step : steps) {
                if (step.stencil != null) {
                    applyStencil(step.stencil);
                } else {
                    draw(renderPass, step);
                }
            }
        } finally {
            GL11.glDisable(GL11.GL_STENCIL_TEST);
            for (Step step : steps) {
                if (step.mesh != null) step.mesh.close();
            }
            for (MappableRingBuffer buffer : vertexBuffers.values()) {
                buffer.rotate();
            }
        }
    }

    // what vanilla GuiRenderer.prepareItemElements does, with DG's own atlas (D100)
    private void prepareItems(List<DGRenderState> states) {
        Set<Object> itemsInFrame = new HashSet<>();
        for (DGRenderState state : states) {
            if (state instanceof ItemState) itemsInFrame.add(((ItemState) state).getItem().getModelIdentity());
        }
        if (itemsInFrame.isEmpty()) return;

        int guiScale = Minecraft.getInstance().getWindow().getGuiScale();
        if (itemAtlas != null && itemAtlasGuiScale != guiScale) {
            itemAtlas.close();
            itemAtlas = null;
        }
        int slotTextureSize = 16 * guiScale;
        if (itemAtlas == null || !itemAtlas.tryPrepareFor(itemsInFrame)) {
            int textureSize = GuiItemAtlas.computeTextureSizeFor(slotTextureSize, itemsInFrame.size());
            if (itemAtlas == null || itemAtlas.textureSize() != textureSize) {
                if (itemAtlas != null) itemAtlas.close();
                GameRenderer gameRenderer = Minecraft.getInstance().gameRenderer;
                itemAtlas = new GuiItemAtlas(gameRenderer.getSubmitNodeStorage(), gameRenderer.getFeatureRenderDispatcher(),
                        Minecraft.getInstance().renderBuffers().bufferSource(), textureSize, slotTextureSize);
                itemAtlasGuiScale = guiScale;
            }
        }
        for (DGRenderState state : states) {
            if (state instanceof ItemState) {
                ItemState itemState = (ItemState) state;
                itemState.setSlot(itemAtlas.getOrUpdate(itemState.getItem()));
            }
        }
    }

    private void recordMesh(List<Step> steps, BufferBuilder bufferBuilder, RenderPipeline pipeline, TextureSetup textures, Rectangle scissor) {
        MeshData mesh = bufferBuilder.build();
        if (mesh != null)
            steps.add(new Step(mesh, pipeline, textures, scissor, null, 0, null, null, null));
    }

    private void upload(List<Step> steps) {
        Map<VertexFormat, Integer> requiredSizes = new HashMap<>();
        for (Step step : steps) {
            if (step.mesh == null) continue;
            MeshData.DrawState drawState = step.mesh.drawState();
            requiredSizes.merge(drawState.format(), drawState.vertexCount() * drawState.format().getVertexSize(), Integer::sum);
        }
        for (Map.Entry<VertexFormat, Integer> entry : requiredSizes.entrySet()) {
            MappableRingBuffer vertexBuffer = vertexBuffers.get(entry.getKey());
            if (vertexBuffer == null || vertexBuffer.size() < entry.getValue()) {
                if (vertexBuffer != null) vertexBuffer.close();
                VertexFormat format = entry.getKey();
                vertexBuffers.put(format, new MappableRingBuffer(() -> "Dungeons Guide gui vertex buffer for " + format, GpuBuffer.USAGE_VERTEX | GpuBuffer.USAGE_MAP_WRITE, entry.getValue()));
            }
        }

        CommandEncoder encoder = RenderSystem.getDevice().createCommandEncoder();
        Map<VertexFormat, Integer> offsets = new HashMap<>();
        for (Step step : steps) {
            if (step.mesh == null) continue;
            VertexFormat format = step.mesh.drawState().format();
            MappableRingBuffer vertexBuffer = vertexBuffers.get(format);
            int offset = offsets.getOrDefault(format, 0);
            ByteBuffer meshVertexBuffer = step.mesh.vertexBuffer();
            int size = meshVertexBuffer.remaining();
            try (GpuBuffer.MappedView mappedView = encoder.mapBuffer(vertexBuffer.currentBuffer().slice(offset, size), false, true)) {
                MemoryUtil.memCopy(meshVertexBuffer, mappedView.data());
            }
            offsets.put(format, offset + size);
            step.baseVertex = offset / format.getVertexSize();
            step.vertexBuffer = vertexBuffer.currentBuffer();

            if (step.uniforms != null) {
                ShaderUniforms uniforms = step.uniforms;
                step.uniformSlice = shaderUniforms
                        .computeIfAbsent(uniforms.getClass(), c -> new DynamicUniformStorage<>("Dungeons Guide " + uniforms.blockName() + " UBO", uniforms.blockSize(), 16))
                        .writeUniform(uniforms);
            }
        }
    }

    private void draw(RenderPass renderPass, Step step) {
        MeshData.DrawState drawState = step.mesh.drawState();
        renderPass.setPipeline(step.pipeline);
        if (step.uniforms != null) renderPass.setUniform(step.uniforms.blockName(), step.uniformSlice);
        renderPass.setVertexBuffer(0, step.vertexBuffer);
        if (step.scissor != null) {
            renderPass.enableScissor(step.scissor.x, step.scissor.y, step.scissor.width, step.scissor.height);
        } else {
            renderPass.disableScissor();
        }
        if (step.textures.texure0() != null) renderPass.bindTexture("Sampler0", step.textures.texure0(), step.textures.sampler0());
        if (step.textures.texure1() != null) renderPass.bindTexture("Sampler1", step.textures.texure1(), step.textures.sampler1());
        if (step.textures.texure2() != null) renderPass.bindTexture("Sampler2", step.textures.texure2(), step.textures.sampler2());

        RenderSystem.AutoStorageIndexBuffer indices = RenderSystem.getSequentialBuffer(drawState.mode());
        renderPass.setIndexBuffer(indices.getBuffer(drawState.indexCount()), indices.type());
        renderPass.drawIndexed(step.baseVertex, 0, drawState.indexCount(), 1);
    }

    // same gl calls the 1.8.9 Stencil / NegativeStencil elements made
    private void applyStencil(StencilState state) {
        switch (state.getAction()) {
            case BEGIN_MASK:
                Rectangle scissor = state.scissor();
                if (scissor != null) {
                    GlStateManager._enableScissorTest();
                    GlStateManager._scissorBox(scissor.x, scissor.y, scissor.width, scissor.height);
                } else {
                    GlStateManager._disableScissorTest();
                }
                GL11.glEnable(GL11.GL_STENCIL_TEST);
                GL11.glClearStencil(0);
                GL11.glStencilMask(0xFF);
                GL11.glClear(GL11.GL_STENCIL_BUFFER_BIT);
                GL11.glStencilFunc(GL11.GL_ALWAYS, 1, 0xFF);
                GL11.glStencilOp(GL11.GL_KEEP, GL11.GL_REPLACE, GL11.GL_REPLACE);
                break;
            case BEGIN_MASKED:
                GL11.glStencilFunc(GL11.GL_EQUAL, state.getRef(), 0xFF);
                GL11.glStencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_KEEP);
                break;
            case END:
                GL11.glDisable(GL11.GL_STENCIL_TEST);
                break;
        }
    }

    // same pipeline without color output, for draws that only write the stencil mask
    private RenderPipeline maskVariant(RenderPipeline base) {
        return maskPipelines.computeIfAbsent(base, pipeline -> {
            RenderPipeline.Builder builder = RenderPipeline.builder()
                    .withLocation(Identifier.fromNamespaceAndPath("dungeonsguide", "pipeline/stencil_mask/" + pipeline.getLocation().getNamespace() + "/" + pipeline.getLocation().getPath()))
                    .withVertexShader(pipeline.getVertexShader())
                    .withFragmentShader(pipeline.getFragmentShader())
                    .withVertexFormat(pipeline.getVertexFormat(), pipeline.getVertexFormatMode())
                    .withCull(pipeline.isCull())
                    .withPolygonMode(pipeline.getPolygonMode())
                    .withDepthStencilState(Optional.ofNullable(pipeline.getDepthStencilState()))
                    .withColorTargetState(new ColorTargetState(Optional.empty(), ColorTargetState.WRITE_NONE));
            for (String sampler : pipeline.getSamplers()) {
                builder.withSampler(sampler);
            }
            for (RenderPipeline.UniformDescription uniform : pipeline.getUniforms()) {
                if (uniform.textureFormat() == null) builder.withUniform(uniform.name(), uniform.type());
                else builder.withUniform(uniform.name(), uniform.type(), uniform.textureFormat());
            }
            for (String flag : pipeline.getShaderDefines().flags()) {
                builder.withShaderDefine(flag);
            }
            for (Map.Entry<String, String> define : pipeline.getShaderDefines().values().entrySet()) {
                try {
                    builder.withShaderDefine(define.getKey(), Integer.parseInt(define.getValue()));
                } catch (NumberFormatException e) {
                    builder.withShaderDefine(define.getKey(), Float.parseFloat(define.getValue()));
                }
            }
            return builder.build();
        });
    }

    @Override
    public void close() {
        if (itemAtlas != null) itemAtlas.close();
        for (DynamicUniformStorage<ShaderUniforms> storage : shaderUniforms.values()) {
            storage.close();
        }
        byteBufferBuilder.close();
        projectionBuffer.close();
        for (MappableRingBuffer buffer : vertexBuffers.values()) {
            buffer.close();
        }
    }

    @AllArgsConstructor
    private static class Job {
        private final StencilImage target;
        private final List<DGRenderState> states;
    }

    @AllArgsConstructor
    private static class Step {
        private final MeshData mesh;
        private final RenderPipeline pipeline;
        private final TextureSetup textures;
        private final Rectangle scissor;
        private final StencilState stencil;
        private int baseVertex;
        private GpuBuffer vertexBuffer;
        private ShaderUniforms uniforms;
        private GpuBufferSlice uniformSlice;
    }
}
