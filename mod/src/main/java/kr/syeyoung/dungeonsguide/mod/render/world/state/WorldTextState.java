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
import lombok.AllArgsConstructor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.phys.Vec3;

/**
 * Text facing the camera, centered on pos. 1.8.9's legacy formatting codes in the string still apply.
 */
@AllArgsConstructor
public class WorldTextState implements DGWorldRenderState {
    private final String text;
    private final Vec3 pos;
    private final int color;
    private final float scale;
    // scale grows with distance, so the text keeps its size on screen
    private final boolean increase;
    private final boolean blackBox;
    private final boolean depth;

    @Override
    public void submit(PoseStack poseStack, MultiBufferSource bufferSource, Vec3 camera) {
        Minecraft mc = Minecraft.getInstance();
        Font font = mc.font;
        Vec3 renderPos = pos.subtract(camera);

        float lScale = scale;
        if (increase) {
            double distance = renderPos.length();
            double multiplier = distance / 120f; //mobs only render ~120 blocks away
            lScale *= 0.45f * multiplier;
        }

        poseStack.pushPose();
        poseStack.translate(renderPos.x, renderPos.y, renderPos.z);
        // 1.8.9 rotated by the player's view yaw / pitch and scaled by (-s, -s, s), 26.1 name tags use the camera rotation
        poseStack.mulPose(mc.gameRenderer.getMainCamera().rotation());
        poseStack.scale(lScale, -lScale, lScale);

        int textWidth = font.width(text);
        // 1.8.9's black box was black at 0.25 alpha
        font.drawInBatch(text, -textWidth / 2, 0, color, false, poseStack.last().pose(), bufferSource,
                depth ? Font.DisplayMode.NORMAL : Font.DisplayMode.SEE_THROUGH, blackBox ? 0x40000000 : 0, LightCoordsUtil.FULL_BRIGHT);
        poseStack.popPose();
    }
}
