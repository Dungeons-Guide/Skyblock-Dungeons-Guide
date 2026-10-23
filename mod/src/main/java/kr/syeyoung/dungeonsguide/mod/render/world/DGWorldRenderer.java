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
package kr.syeyoung.dungeonsguide.mod.render.world;

import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.framegraph.FrameGraphBuilder;
import com.mojang.blaze3d.framegraph.FramePass;
import com.mojang.blaze3d.pipeline.DepthStencilState;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.CompareOp;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.resource.ResourceHandle;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import kr.syeyoung.dungeonsguide.mod.render.world.state.BlockModelState;
import kr.syeyoung.dungeonsguide.mod.render.world.state.DGWorldRenderState;
import kr.syeyoung.dungeonsguide.mod.render.world.state.ShaderQuadState;
import kr.syeyoung.dungeonsguide.mod.render.world.state.WorldImageState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelTargetBundle;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.fog.FogRenderer;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * DG world rendering on vanilla's frame graph (D123). RenderWorldEvent fires during level extraction and only records
 * states here; LevelRendererMixin then adds DG's passes, which the graph orders by what they read and write:
 * world images first (their 2D content into their image), then block models, then everything else into main.
 */
public class DGWorldRenderer {
    private static final List<DGWorldRenderState> states = new ArrayList<>();
    private static final List<BlockModelState> blockModels = new ArrayList<>();
    private static Vec3 camera = Vec3.ZERO;

    // called right before RenderWorldEvent fires for this frame's extraction
    public static void beginExtraction(Vec3 cameraPosition) {
        states.clear();
        blockModels.clear();
        camera = cameraPosition;
    }

    public static void record(DGWorldRenderState state) {
        states.add(state);
    }

    public static void record(BlockModelState state) {
        blockModels.add(state);
    }

    private static final Map<RenderPipeline, RenderPipeline> seeThroughPipelines = new HashMap<>();

    // the same DG shader pipeline without the depth test, for draws 1.8.9 made with depth off
    private static RenderPipeline seeThroughVariant(RenderPipeline base) {
        return seeThroughPipelines.computeIfAbsent(base, pipeline -> {
            RenderPipeline.Builder builder = RenderPipeline.builder()
                    .withLocation(Identifier.fromNamespaceAndPath("dungeonsguide", "pipeline/see_through/" + pipeline.getLocation().getNamespace() + "/" + pipeline.getLocation().getPath()))
                    .withVertexShader(pipeline.getVertexShader())
                    .withFragmentShader(pipeline.getFragmentShader())
                    .withVertexFormat(pipeline.getVertexFormat(), pipeline.getVertexFormatMode())
                    .withCull(pipeline.isCull())
                    .withPolygonMode(pipeline.getPolygonMode())
                    .withColorTargetState(pipeline.getColorTargetState())
                    .withDepthStencilState(new DepthStencilState(CompareOp.ALWAYS_PASS, false));
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

    public static void addPasses(FrameGraphBuilder frame, LevelTargetBundle targets) {
        if (states.isEmpty() && blockModels.isEmpty()) return;
        List<DGWorldRenderState> frameStates = new ArrayList<>(states);
        List<BlockModelState> frameBlockModels = new ArrayList<>(blockModels);
        Vec3 frameCamera = camera;
        states.clear();
        blockModels.clear();

        // images aren't graph resources, the world pass requires this pass instead
        FramePass imagePass = frame.addPass("dungeonsguide_world_images");
        imagePass.disableCulling();
        imagePass.executes(() -> {
            // gui content has no fog, like vanilla's gui
            GpuBufferSlice noFog = Minecraft.getInstance().gameRenderer.fogRenderer.getBuffer(FogRenderer.FogMode.NONE);
            for (DGWorldRenderState state : frameStates) {
                if (state instanceof WorldImageState) ((WorldImageState) state).prepare(noFog);
            }
        });

        FramePass blockPass = frame.addPass("dungeonsguide_world_blocks");
        targets.main = blockPass.readsAndWrites(targets.main);
        ResourceHandle<RenderTarget> blockTarget = targets.main;
        blockPass.executes(() -> {
            if (frameBlockModels.isEmpty()) return;
            GameRenderer gameRenderer = Minecraft.getInstance().gameRenderer;
            RenderSystem.outputColorTextureOverride = blockTarget.get().getColorTextureView();
            RenderSystem.outputDepthTextureOverride = blockTarget.get().getDepthTextureView();
            gameRenderer.getLighting().setupFor(Lighting.Entry.LEVEL);
            PoseStack poseStack = new PoseStack();
            for (BlockModelState state : frameBlockModels) {
                state.submit(poseStack, gameRenderer.getSubmitNodeStorage(), frameCamera);
            }
            gameRenderer.getFeatureRenderDispatcher().renderAllFeatures();
            Minecraft.getInstance().renderBuffers().bufferSource().endBatch();
            RenderSystem.outputColorTextureOverride = null;
            RenderSystem.outputDepthTextureOverride = null;
        });

        FramePass worldPass = frame.addPass("dungeonsguide_world");
        worldPass.requires(imagePass);
        targets.main = worldPass.readsAndWrites(targets.main);
        ResourceHandle<RenderTarget> worldTarget = targets.main;
        worldPass.executes(() -> {
            // 1.8.9 drew DG's world overlays without fog
            GpuBufferSlice previousFog = RenderSystem.getShaderFog();
            RenderSystem.setShaderFog(Minecraft.getInstance().gameRenderer.fogRenderer.getBuffer(FogRenderer.FogMode.NONE));
            RenderSystem.outputColorTextureOverride = worldTarget.get().getColorTextureView();
            RenderSystem.outputDepthTextureOverride = worldTarget.get().getDepthTextureView();
            PoseStack poseStack = new PoseStack();
            MultiBufferSource.BufferSource bufferSource = Minecraft.getInstance().renderBuffers().bufferSource();
            for (DGWorldRenderState state : frameStates) {
                if (state instanceof ShaderQuadState) {
                    // its own render pass, after what's buffered so far
                    bufferSource.endBatch();
                    ShaderQuadState shaderQuad = (ShaderQuadState) state;
                    RenderPipeline pipeline = shaderQuad.getShader().getPipeline();
                    shaderQuad.draw(worldTarget.get(), shaderQuad.isDepth() ? pipeline : seeThroughVariant(pipeline), frameCamera);
                } else {
                    state.submit(poseStack, bufferSource, frameCamera);
                }
            }
            bufferSource.endBatch();
            RenderSystem.outputColorTextureOverride = null;
            RenderSystem.outputDepthTextureOverride = null;
            if (previousFog != null) RenderSystem.setShaderFog(previousFog);
        });
    }
}
