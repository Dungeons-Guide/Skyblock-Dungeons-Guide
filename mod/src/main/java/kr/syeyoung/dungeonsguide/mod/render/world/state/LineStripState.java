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
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/**
 * 1.8.9's GL_LINE_STRIP / GL_LINE_LOOP: a color per point. 26.1 only draws line segments, so each pair of points is one.
 */
@AllArgsConstructor
public class LineStripState implements DGWorldRenderState {
    private final List<Vec3> points;
    private final int[] colors;
    private final float width;
    private final boolean loop;
    private final boolean depth;

    @Override
    public void submit(PoseStack poseStack, MultiBufferSource bufferSource, Vec3 camera) {
        VertexConsumer consumer = bufferSource.getBuffer(depth ? RenderTypes.lines() : DGRenderTypes.linesSeeThrough());
        PoseStack.Pose pose = poseStack.last();
        int segments = loop ? points.size() : points.size() - 1;
        for (int i = 0; i < segments; i++) {
            int j = (i + 1) % points.size();
            Vec3 from = points.get(i), to = points.get(j);
            Vec3 normal = to.subtract(from).normalize();
            consumer.addVertex(pose, (float) (from.x - camera.x), (float) (from.y - camera.y), (float) (from.z - camera.z))
                    .setColor(colors[i]).setNormal(pose, (float) normal.x, (float) normal.y, (float) normal.z).setLineWidth(width);
            consumer.addVertex(pose, (float) (to.x - camera.x), (float) (to.y - camera.y), (float) (to.z - camera.z))
                    .setColor(colors[j]).setNormal(pose, (float) normal.x, (float) normal.y, (float) normal.z).setLineWidth(width);
        }
    }
}
