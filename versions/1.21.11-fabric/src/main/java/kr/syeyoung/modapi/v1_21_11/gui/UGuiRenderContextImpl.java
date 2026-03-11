package kr.syeyoung.modapi.v1_21_11.gui;

import kr.syeyoung.modapi.data.URect;
import kr.syeyoung.modapi.data.USize;
import kr.syeyoung.modapi.rendering.UGuiRenderContext;
import kr.syeyoung.modapi.rendering.URenderContext;
import net.minecraft.client.gui.DrawContext;
import org.joml.Matrix3x2f;
import org.joml.Matrix3x2fStack;

import java.awt.*;

public class UGuiRenderContextImpl implements UGuiRenderContext {
    private DrawContext context;
    private Matrix3x2fStack stack;
    private Matrix3x2f current;
    public UGuiRenderContextImpl(DrawContext context) {
        this.context = context;
        stack = context.getMatrices();
    }


    @Override
    public void pushMatrix() {
        stack = stack.pushMatrix();
    }

    @Override
    public void popMatrix() {
        stack = stack.popMatrix();
    }

    @Override
    public void translate(double x, double y, double z) {
        stack.translate((float) x, (float) y);
    }

    @Override
    public void scale(double x, double y, double z) {
        stack.scale((float) x, (float) y);
    }

    @Override
    public void rotate(float angle, float x, float y, float z) {
        stack.rotateAbout(angle, x, y);
    }


    @Override
    public Rectangle currentClip() {
        return context.scissorStack.peekLast();
    }

    @Override
    public void pushClip(URect absBounds, USize size, double x, double y, double width, double height) {

    }

    @Override
    public void popClip() {
        context.scissorStack.pop();
    }
}
