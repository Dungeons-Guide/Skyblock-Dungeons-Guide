package kr.syeyoung.dungeonsguide.mod.guiv2.renderer;

import kr.syeyoung.dungeonsguide.mod.guiv2.DomElement;
import net.minecraft.client.renderer.GlStateManager;

import java.awt.*;

public class SingleChildRenderer extends Renderer {
    public SingleChildRenderer(DomElement domElement) {
        super(domElement);
    }

    public void doRender(int absMouseX, int absMouseY, int relMouseX, int relMouseY, float partialTicks) {
         DomElement value = getDomElement().getChildren().get(0);
            Rectangle original = value.getRelativeBound();
            GlStateManager.pushMatrix();
            GlStateManager.translate(original.x, original.y, 0);

            double absXScale = getDomElement().getAbsBounds().getWidth() / getDomElement().getRelativeBound().width;
            double absYScale = getDomElement().getAbsBounds().getHeight() / getDomElement().getRelativeBound().height;

            Rectangle elementABSBound = new Rectangle(
                    (int) (getDomElement().getAbsBounds().x + original.x * absXScale),
                    (int) (getDomElement().getAbsBounds().y + original.y * absYScale),
                    (int) (original.width * absXScale),
                    (int) (original.height * absYScale)
            );
            value.setAbsBounds(elementABSBound);

            value.getRenderer().doRender(absMouseX, absMouseY,
                    relMouseX - original.x,
                    relMouseY - original.y, partialTicks);
            GlStateManager.popMatrix();
    }

    @Override
    public final Rectangle applyTransformation(DomElement target) {
        return target.getRelativeBound();
    }
}
