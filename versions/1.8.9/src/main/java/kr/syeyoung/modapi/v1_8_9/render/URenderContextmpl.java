package kr.syeyoung.modapi.v1_8_9.render;

import kr.syeyoung.modapi.rendering.URenderContext;
import net.minecraft.client.renderer.GlStateManager;

public class URenderContextmpl implements URenderContext {
    public static final URenderContextmpl INSTANCE = new URenderContextmpl();

    @Override
    public void pushMatrix() {
        GlStateManager.pushMatrix();
    }

    @Override
    public void popMatrix() {
        GlStateManager.popMatrix();
    }

    @Override
    public void translate(double x, double y, double z) {
        GlStateManager.translate(x,y,z);
    }

    @Override
    public void scale(double x, double y, double z) {
        GlStateManager.scale(x,y,z);
    }

    @Override
    public void rotate(float angle, int x, int y, int z) {
        GlStateManager.rotate(angle, x, y, z);
    }
}
