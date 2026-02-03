package kr.syeyoung.modapi.v1_8_9.util;

import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.opengl.GL11;

import static org.lwjgl.opengl.GL11.GL_GREATER;

public class RenderUtils {
    public static void preRenderGui() {
        GlStateManager.disableDepth();
        GlStateManager.enableBlend();
        GlStateManager.enableAlpha();
        GlStateManager.alphaFunc(GL_GREATER, 0);
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0);
        GlStateManager.color(1, 1, 1, 1);
    }

    public static void postRenderGui() {
        GlStateManager.alphaFunc(GL_GREATER, 0.1f);
        GlStateManager.enableDepth();
        GL11.glDisable(GL11.GL_SCISSOR_TEST);
    }
}
