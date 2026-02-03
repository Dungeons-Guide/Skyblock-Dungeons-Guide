package kr.syeyoung.dungeonsguide.mod.features.impl.dungeon.map.overlay;

import kr.syeyoung.dungeonsguide.mod.gui.DomElement;
import kr.syeyoung.dungeonsguide.mod.gui.renderer.RenderingContext;

public interface MapOverlay {
    double getX(float partialTicks);
    double getZ(float partialTicks);
    int priority();

    void doRender(RenderingContext context, float rotation, float partialTicks, double scale, double relMouseX, double relMouseY);

    boolean onClick(double relMouseX, double relMouseY, DomElement domElement);
}
