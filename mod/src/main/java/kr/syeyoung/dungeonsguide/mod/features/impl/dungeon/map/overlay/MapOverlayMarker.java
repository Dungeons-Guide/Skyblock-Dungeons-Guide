package kr.syeyoung.dungeonsguide.mod.features.impl.dungeon.map.overlay;

import kr.syeyoung.dungeonsguide.mod.dungeon.roomprocessor.bossfight.MarkerData;
import kr.syeyoung.dungeonsguide.mod.features.impl.dungeon.map.MapConfiguration;
import kr.syeyoung.dungeonsguide.mod.guiv2.DomElement;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;

public class MapOverlayMarker implements MapOverlay{
    private MarkerData marker;
    private MapConfiguration.PlayerHeadSettings settings;

    public MapOverlayMarker(MarkerData markerData, MapConfiguration.PlayerHeadSettings headSettings) {this.marker = markerData; this.settings = headSettings;}

    @Override
    public double getX(float partialTicks) {
        return marker.getPrevX() + (marker.getCurrX() - marker.getPrevX()) * partialTicks;
    }

    @Override
    public double getZ(float partialTicks) {
        return marker.getPrevZ() + (marker.getCurrZ() - marker.getPrevZ()) * partialTicks;
    }

    @Override
    public int priority() {
        return 50 - marker.getType().ordinal();
    }

    private final ResourceLocation resourceLocation2 = new ResourceLocation("dungeonsguide:map/bossfight/markers.png");
    @Override
    public void doRender(float rotation, float partialTicks, double scale, double relMouseX, double relMouseY) {
        double yaw = marker.getPrevYaw() + (marker.getCurrYaw() - marker.getPrevYaw()) * partialTicks;
        GlStateManager.rotate((float) yaw, 0, 0, 1);

        GlStateManager.scale(1 / scale, 1 / scale, 0);
        GlStateManager.scale(settings.getIconSize(), settings.getIconSize(), 1);

        boolean flip = settings.getIconType() == MapConfiguration.PlayerHeadSettings.IconType.HEAD_FLIP;
        int tx = marker.getMarkerIndex() % 8;
        int ty = marker.getMarkerIndex() / 8;
        Minecraft.getMinecraft().getTextureManager().bindTexture(resourceLocation2);
        Gui.drawScaledCustomSizeModalRect(-4, -4, tx * 72,ty * 72 + (flip ? 8 : 0) , 72, 72, 8, flip ? -8 : 8, 576, 576);
    }

    @Override
    public boolean onClick(double relMouseX, double relMouseY, DomElement domElement) {
        return false;
    }
}
