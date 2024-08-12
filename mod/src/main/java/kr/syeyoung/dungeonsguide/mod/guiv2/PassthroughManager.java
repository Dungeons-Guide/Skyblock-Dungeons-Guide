/*
 * Dungeons Guide - The most intelligent Hypixel Skyblock Dungeons Mod
 * Copyright (C) 2024  cyoung06 (syeyoung)
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

package kr.syeyoung.dungeonsguide.mod.guiv2;

import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.profiler.Profiler;
import net.minecraftforge.client.event.EntityViewRenderEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;

public class PassthroughManager {

    public static PassthroughManager INSTANCE = new PassthroughManager();
    @Getter
    private Framebuffer framebuffer;

    @Getter
    private int fogColor = 0;

    @SubscribeEvent
    public void onFogColour(EntityViewRenderEvent.FogColors event) {
        fogColor = 0xff000000
                    | ((int) (event.red * 255) & 0xFF) << 16
                    | ((int) (event.green * 255) & 0xFF) << 8
                    | (int) (event.blue * 255) & 0xFF;
    }


    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onGameRenderOverlay(RenderGameOverlayEvent.Pre event) {
        if (event.type != RenderGameOverlayEvent.ElementType.ALL) return;
        Profiler profiler = Minecraft.getMinecraft().mcProfiler;
        profiler.startSection("Dungeons Guide - RenderGameOverlayEvent :: Passthrough");

        int width = Minecraft.getMinecraft().displayWidth;
        int height = Minecraft.getMinecraft().displayHeight;

        GlStateManager.matrixMode(GL11.GL_PROJECTION);
        GlStateManager.loadIdentity();
        GlStateManager.ortho(0.0D, width, height, 0.0D, 1, 10);
        GlStateManager.matrixMode(GL11.GL_MODELVIEW);
        GlStateManager.loadIdentity();
        GlStateManager.translate(0.0F, 0.0F, 0);

        if (framebuffer == null) {
            framebuffer = new Framebuffer(width, height, false);
            framebuffer.setFramebufferFilter(GL11.GL_NEAREST);
        }
        if (framebuffer.framebufferWidth != width || framebuffer.framebufferHeight != height) {
            framebuffer.createBindFramebuffer(width, height);
            Minecraft.getMinecraft().getFramebuffer().bindFramebuffer(false);
        }


        GL11.glPushMatrix();
        GL30.glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, Minecraft.getMinecraft().getFramebuffer().framebufferObject);
        GL30.glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, framebuffer.framebufferObject);
        GL30.glBlitFramebuffer(0, 0, width, height,
                0, 0, framebuffer.framebufferWidth, framebuffer.framebufferHeight,
                GL11.GL_COLOR_BUFFER_BIT, GL11.GL_NEAREST
        );

        GlStateManager.enableDepth();
        GL11.glPopMatrix();

        Minecraft.getMinecraft().getFramebuffer().bindFramebuffer(false);

        profiler.endSection();
    }
}
