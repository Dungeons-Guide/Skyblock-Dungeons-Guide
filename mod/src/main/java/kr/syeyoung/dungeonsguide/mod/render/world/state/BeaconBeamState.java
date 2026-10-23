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
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

/**
 * Taken from NotEnoughUpdates under Creative Commons Attribution-NonCommercial 3.0
 * And modified to fit out need.
 * https://github.com/Moulberry/NotEnoughUpdates/blob/master/LICENSE
 * @author Moulberry
 *
 * The beam RenderUtils.renderBeaconBeam drew on 1.8.9, through walls like it did then.
 */
@AllArgsConstructor
public class BeaconBeamState implements DGWorldRenderState {
    private final double x, y, z;
    private final int color;
    private final float partialTicks;

    @Override
    public void submit(PoseStack poseStack, MultiBufferSource bufferSource, Vec3 camera) {
        int height = 300;
        int bottomOffset = 0;
        int topOffset = bottomOffset + height;

        VertexConsumer consumer = bufferSource.getBuffer(DGRenderTypes.beaconBeamSeeThrough());
        PoseStack.Pose pose = poseStack.last();
        double x = this.x - camera.x, y = this.y - camera.y, z = this.z - camera.z;

        double time = System.currentTimeMillis() / 50 + (double)partialTicks;
        // 1.8.9's MathHelper.func_181162_h: the fractional part
        double d1 = Mth.frac(-time * 0.2D - (double)Mth.floor(-time * 0.1D));

        int c = color;
        float alpha = ((c >> 24) & 0xFF) / 255.0f;

        float r = ((c >> 16) & 0xFF) / 255f;
        float g = ((c >> 8) & 0xFF) / 255f;
        float b = (c & 0xFF) / 255f;
        double d2 = time * 0.025D * -1.5D;
        double d4 = 0.5D + Math.cos(d2 + 2.356194490192345D) * 0.2D;
        double d5 = 0.5D + Math.sin(d2 + 2.356194490192345D) * 0.2D;
        double d6 = 0.5D + Math.cos(d2 + (Math.PI / 4D)) * 0.2D;
        double d7 = 0.5D + Math.sin(d2 + (Math.PI / 4D)) * 0.2D;
        double d8 = 0.5D + Math.cos(d2 + 3.9269908169872414D) * 0.2D;
        double d9 = 0.5D + Math.sin(d2 + 3.9269908169872414D) * 0.2D;
        double d10 = 0.5D + Math.cos(d2 + 5.497787143782138D) * 0.2D;
        double d11 = 0.5D + Math.sin(d2 + 5.497787143782138D) * 0.2D;
        double d14 = -1.0D + d1;
        double d15 = (double)(height) * 2.5D + d14;
        vertex(consumer, pose, x + d4, y + topOffset, z + d5, 1.0D, d15, r, g, b, alpha);
        vertex(consumer, pose, x + d4, y + bottomOffset, z + d5, 1.0D, d14, r, g, b, 1.0F);
        vertex(consumer, pose, x + d6, y + bottomOffset, z + d7, 0.0D, d14, r, g, b, 1.0F);
        vertex(consumer, pose, x + d6, y + topOffset, z + d7, 0.0D, d15, r, g, b, alpha);
        vertex(consumer, pose, x + d10, y + topOffset, z + d11, 1.0D, d15, r, g, b, alpha);
        vertex(consumer, pose, x + d10, y + bottomOffset, z + d11, 1.0D, d14, r, g, b, 1.0F);
        vertex(consumer, pose, x + d8, y + bottomOffset, z + d9, 0.0D, d14, r, g, b, 1.0F);
        vertex(consumer, pose, x + d8, y + topOffset, z + d9, 0.0D, d15, r, g, b, alpha);
        vertex(consumer, pose, x + d6, y + topOffset, z + d7, 1.0D, d15, r, g, b, alpha);
        vertex(consumer, pose, x + d6, y + bottomOffset, z + d7, 1.0D, d14, r, g, b, 1.0F);
        vertex(consumer, pose, x + d10, y + bottomOffset, z + d11, 0.0D, d14, r, g, b, 1.0F);
        vertex(consumer, pose, x + d10, y + topOffset, z + d11, 0.0D, d15, r, g, b, alpha);
        vertex(consumer, pose, x + d8, y + topOffset, z + d9, 1.0D, d15, r, g, b, alpha);
        vertex(consumer, pose, x + d8, y + bottomOffset, z + d9, 1.0D, d14, r, g, b, 1.0F);
        vertex(consumer, pose, x + d4, y + bottomOffset, z + d5, 0.0D, d14, r, g, b, 1.0F);
        vertex(consumer, pose, x + d4, y + topOffset, z + d5, 0.0D, d15, r, g, b, alpha);

        double d12 = -1.0D + d1;
        double d13 = height + d12;

        vertex(consumer, pose, x + 0.2D, y + topOffset, z + 0.2D, 1.0D, d13, r, g, b, 0.25F*alpha);
        vertex(consumer, pose, x + 0.2D, y + bottomOffset, z + 0.2D, 1.0D, d12, r, g, b, 0.25F);
        vertex(consumer, pose, x + 0.8D, y + bottomOffset, z + 0.2D, 0.0D, d12, r, g, b, 0.25F);
        vertex(consumer, pose, x + 0.8D, y + topOffset, z + 0.2D, 0.0D, d13, r, g, b, 0.25F*alpha);
        vertex(consumer, pose, x + 0.8D, y + topOffset, z + 0.8D, 1.0D, d13, r, g, b, 0.25F*alpha);
        vertex(consumer, pose, x + 0.8D, y + bottomOffset, z + 0.8D, 1.0D, d12, r, g, b, 0.25F);
        vertex(consumer, pose, x + 0.2D, y + bottomOffset, z + 0.8D, 0.0D, d12, r, g, b, 0.25F);
        vertex(consumer, pose, x + 0.2D, y + topOffset, z + 0.8D, 0.0D, d13, r, g, b, 0.25F*alpha);
        vertex(consumer, pose, x + 0.8D, y + topOffset, z + 0.2D, 1.0D, d13, r, g, b, 0.25F*alpha);
        vertex(consumer, pose, x + 0.8D, y + bottomOffset, z + 0.2D, 1.0D, d12, r, g, b, 0.25F);
        vertex(consumer, pose, x + 0.8D, y + bottomOffset, z + 0.8D, 0.0D, d12, r, g, b, 0.25F);
        vertex(consumer, pose, x + 0.8D, y + topOffset, z + 0.8D, 0.0D, d13, r, g, b, 0.25F*alpha);
        vertex(consumer, pose, x + 0.2D, y + topOffset, z + 0.8D, 1.0D, d13, r, g, b, 0.25F*alpha);
        vertex(consumer, pose, x + 0.2D, y + bottomOffset, z + 0.8D, 1.0D, d12, r, g, b, 0.25F);
        vertex(consumer, pose, x + 0.2D, y + bottomOffset, z + 0.2D, 0.0D, d12, r, g, b, 0.25F);
        vertex(consumer, pose, x + 0.2D, y + topOffset, z + 0.2D, 0.0D, d13, r, g, b, 0.25F*alpha);
    }

    private static void vertex(VertexConsumer consumer, PoseStack.Pose pose, double x, double y, double z, double u, double v, float r, float g, float b, float a) {
        consumer.addVertex(pose, (float) x, (float) y, (float) z).setColor(r, g, b, a).setUv((float) u, (float) v).setLight(LightCoordsUtil.FULL_BRIGHT);
    }
}
