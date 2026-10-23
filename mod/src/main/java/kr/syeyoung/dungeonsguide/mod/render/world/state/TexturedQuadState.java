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

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import kr.syeyoung.dungeonsguide.mod.render.world.DGRenderTypes;
import lombok.AllArgsConstructor;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

/**
 * One textured quad (straight alpha), corners in order with a uv per corner, tinted by color.
 */
@AllArgsConstructor
public class TexturedQuadState implements DGWorldRenderState {
    private final Identifier texture;
    private final Vec3[] corners;
    private final float[][] uvs;
    private final int color;
    private final boolean depth;

    @Override
    public void submit(PoseStack poseStack, MultiBufferSource bufferSource, Vec3 camera) {
        VertexConsumer consumer = bufferSource.getBuffer(depth ? DGRenderTypes.texturedQuad(texture) : DGRenderTypes.texturedQuadSeeThrough(texture));
        Matrix4f pose = poseStack.last().pose();
        for (int i = 0; i < 4; i++) {
            Vec3 corner = corners[i];
            consumer.addVertex(pose, (float) (corner.x - camera.x), (float) (corner.y - camera.y), (float) (corner.z - camera.z))
                    .setUv(uvs[i][0], uvs[i][1]).setColor(color);
        }
    }
}
