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
package kr.syeyoung.dungeonsguide.mod.render.world.state;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.PoseStack;
import kr.syeyoung.dungeonsguide.mod.shader.ShaderProgram;
import kr.syeyoung.dungeonsguide.mod.shader.ShaderUniforms;
import lombok.AllArgsConstructor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;
import java.util.OptionalDouble;
import java.util.OptionalInt;

/**
 * A quad drawn with a DG shader and its own uniform block, so DGWorldRenderer's world pass draws it with its own
 * render pass instead of the buffer source (render types can't carry per-draw uniforms).
 */
@AllArgsConstructor
public class ShaderQuadState implements DGWorldRenderState {
    private final ShaderProgram shader;
    private final ShaderUniforms uniforms;
    private final Identifier texture;
    private final Vec3[] corners;
    private final float[][] uvs;
    private final int color;
    private final boolean depth;

    public void draw(RenderTarget target, RenderPipeline pipeline, Vec3 camera) {
        MeshData mesh;
        try (ByteBufferBuilder byteBufferBuilder = new ByteBufferBuilder(pipeline.getVertexFormat().getVertexSize() * 4)) {
            BufferBuilder builder = new BufferBuilder(byteBufferBuilder, pipeline.getVertexFormatMode(), pipeline.getVertexFormat());
            for (int i = 0; i < 4; i++) {
                builder.addVertex((float) (corners[i].x - camera.x), (float) (corners[i].y - camera.y), (float) (corners[i].z - camera.z))
                        .setUv(uvs[i][0], uvs[i][1]).setColor(color);
            }
            mesh = builder.build();
            if (mesh == null) return;

            ByteBuffer uniformData = MemoryUtil.memAlloc(uniforms.blockSize());
            try (GpuBuffer vertexBuffer = RenderSystem.getDevice().createBuffer(() -> "Dungeons Guide world shader quad", GpuBuffer.USAGE_VERTEX, mesh.vertexBuffer());
                 GpuBuffer uniformBuffer = RenderSystem.getDevice().createBuffer(() -> "Dungeons Guide " + uniforms.blockName() + " UBO", GpuBuffer.USAGE_UNIFORM, writeUniforms(uniformData))) {
                AbstractTexture abstractTexture = Minecraft.getInstance().getTextureManager().getTexture(texture);
                RenderSystem.AutoStorageIndexBuffer indices = RenderSystem.getSequentialBuffer(mesh.drawState().mode());
                try (RenderPass renderPass = RenderSystem.getDevice().createCommandEncoder().createRenderPass(() -> "Dungeons Guide world shader quad",
                        target.getColorTextureView(), OptionalInt.empty(), target.getDepthTextureView(), OptionalDouble.empty())) {
                    RenderSystem.bindDefaultUniforms(renderPass);
                    renderPass.setUniform("DynamicTransforms", RenderSystem.getDynamicUniforms()
                            .writeTransform(RenderSystem.getModelViewMatrix(), new Vector4f(1, 1, 1, 1), new Vector3f(), new Matrix4f()));
                    renderPass.setUniform(uniforms.blockName(), uniformBuffer);
                    renderPass.setPipeline(pipeline);
                    renderPass.bindTexture("Sampler0", abstractTexture.getTextureView(), abstractTexture.getSampler());
                    renderPass.setVertexBuffer(0, vertexBuffer);
                    renderPass.setIndexBuffer(indices.getBuffer(mesh.drawState().indexCount()), indices.type());
                    renderPass.drawIndexed(0, 0, mesh.drawState().indexCount(), 1);
                }
            } finally {
                MemoryUtil.memFree(uniformData);
                mesh.close();
            }
        }
    }

    // not through the buffer source: DGWorldRenderer's world pass calls draw at this state's place in the order
    @Override
    public void submit(PoseStack poseStack, MultiBufferSource bufferSource, Vec3 camera) {
    }

    private ByteBuffer writeUniforms(ByteBuffer buffer) {
        uniforms.write(buffer);
        buffer.rewind();
        return buffer;
    }

    public ShaderProgram getShader() {
        return shader;
    }

    public boolean isDepth() {
        return depth;
    }
}
