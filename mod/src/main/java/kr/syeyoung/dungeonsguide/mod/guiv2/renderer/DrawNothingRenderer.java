package kr.syeyoung.dungeonsguide.mod.guiv2.renderer;

import kr.syeyoung.dungeonsguide.mod.guiv2.DomElement;

import java.awt.*;

public class DrawNothingRenderer extends Renderer{
    public DrawNothingRenderer(DomElement domElement) {
        super(domElement);
    }

    @Override
    public void doRender(int absMouseX, int absMouseY, int relMouseX, int relMouseY, float partialTicks) {
    }

    @Override
    public Rectangle applyTransformation(DomElement target) {
        return target.getRelativeBound();
    }
}
