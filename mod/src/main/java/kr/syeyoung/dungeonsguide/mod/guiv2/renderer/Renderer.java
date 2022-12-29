package kr.syeyoung.dungeonsguide.mod.guiv2.renderer;

import kr.syeyoung.dungeonsguide.mod.guiv2.DomElement;
import lombok.Getter;
import net.minecraft.client.renderer.GlStateManager;

import java.awt.*;

public abstract class Renderer {
    @Getter
    private DomElement domElement;
    public Renderer(DomElement domElement) {
        this.domElement = domElement;
    }

    public void doRender(int absMouseX, int absMouseY, int relMouseX, int relMouseY, float partialTicks) {
        for (DomElement value : domElement.getChildren()) {
            Rectangle original = value.getRelativeBound();
            Rectangle boundaryBox = applyTransformation(value);
            GlStateManager.pushMatrix();
            GlStateManager.translate(boundaryBox.x, boundaryBox.y, 0);
            GlStateManager.scale(boundaryBox.getWidth() / original.width, boundaryBox.getHeight() / original.height, 1.0);


            double absXScale = domElement.getAbsBounds().getWidth() / domElement.getRelativeBound().width;
            double absYScale = domElement.getAbsBounds().getHeight() / domElement.getRelativeBound().height;

            Rectangle elementABSBound = new Rectangle(
                    (int) (domElement.getAbsBounds().x + boundaryBox.x * absXScale),
                    (int) (domElement.getAbsBounds().y + boundaryBox.y * absYScale),
                    (int) (boundaryBox.width * absXScale),
                    (int) (boundaryBox.height * absYScale)
            );
            value.setAbsBounds(elementABSBound);

            value.getRenderer().doRender(absMouseX, absMouseY,
                        (relMouseX - boundaryBox.x) * original.width / boundaryBox.height,
                        (relMouseY - boundaryBox.y) * original.height / boundaryBox.height, partialTicks);
            GlStateManager.popMatrix();
        }
    }

    public abstract Rectangle applyTransformation(DomElement target);
}
