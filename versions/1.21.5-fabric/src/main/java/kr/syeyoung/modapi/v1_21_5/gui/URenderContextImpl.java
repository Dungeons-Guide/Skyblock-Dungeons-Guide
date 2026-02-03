package kr.syeyoung.modapi.v1_21_5.gui;

import kr.syeyoung.modapi.rendering.UGuiRenderContext;
import kr.syeyoung.modapi.rendering.URenderContext;
import net.minecraft.client.gui.DrawContext;

public class URenderContextImpl implements URenderContext {
    private DrawContext context;
    public URenderContextImpl(DrawContext context) {
        this.context = context;
    }

    @Override
    public void pushMatrix() {

    }

    @Override
    public void popMatrix() {

    }

    @Override
    public void translate(double x, double y, double z) {

    }

    @Override
    public void scale(double x, double y, double z) {

    }

    @Override
    public void rotate(float angle, int x, int y, int z) {

    }

    @Override
    public UGuiRenderContext getContext() {
        return null;
    }
}
