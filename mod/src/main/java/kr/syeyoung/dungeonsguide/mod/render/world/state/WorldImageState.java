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

import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import kr.syeyoung.dungeonsguide.mod.render.gui.DGGuiRenderer;
import kr.syeyoung.dungeonsguide.mod.render.gui.StencilImage;
import kr.syeyoung.dungeonsguide.mod.render.gui.state.DGRenderState;
import kr.syeyoung.dungeonsguide.mod.render.world.DGRenderTypes;
import lombok.AllArgsConstructor;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

import java.util.List;

/**
 * 2D DG content on a quad in the world: the gui render states are rendered into the image first
 * (DGWorldRenderer's prepare, like vanilla's picture in picture in reverse), then the quad samples it by texture id.
 */
@AllArgsConstructor
public class WorldImageState implements DGWorldRenderState {
    private final StencilImage image;
    // a StencilImageTexture of image
    private final Identifier texture;
    private final List<DGRenderState> states;
    private final int width, height;
    // top left, bottom left, bottom right, top right of the image
    private final Vec3[] corners;
    private final boolean depth;

    public void prepare(GpuBufferSlice fog) {
        image.ensureSize(width, height);
        DGGuiRenderer.INSTANCE.renderInto(image, states, fog);
    }

    @Override
    public void submit(PoseStack poseStack, MultiBufferSource bufferSource, Vec3 camera) {
        VertexConsumer consumer = bufferSource.getBuffer(depth ? DGRenderTypes.image(texture) : DGRenderTypes.imageSeeThrough(texture));
        Matrix4f pose = poseStack.last().pose();
        // gl textures start at the bottom row
        float[][] uvs = {{0, 1}, {0, 0}, {1, 0}, {1, 1}};
        for (int i = 0; i < 4; i++) {
            Vec3 corner = corners[i];
            consumer.addVertex(pose, (float) (corner.x - camera.x), (float) (corner.y - camera.y), (float) (corner.z - camera.z))
                    .setUv(uvs[i][0], uvs[i][1]).setColor(-1);
        }
    }
}
