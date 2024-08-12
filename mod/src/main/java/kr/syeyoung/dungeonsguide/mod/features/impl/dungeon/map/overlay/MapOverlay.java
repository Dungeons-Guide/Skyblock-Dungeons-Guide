package kr.syeyoung.dungeonsguide.mod.features.impl.dungeon.map.overlay;

public interface MapOverlay {
    double getX(float partialTicks);
    double getZ(float partialTicks);
    int priority();

    void doRender(float rotation, float partialTicks, float scale, double relPreScaleMouseX, double relPreScaleMouseY);

    void onClick(int relPreScaleMouseX, int relPreScaleMouseY);
}
