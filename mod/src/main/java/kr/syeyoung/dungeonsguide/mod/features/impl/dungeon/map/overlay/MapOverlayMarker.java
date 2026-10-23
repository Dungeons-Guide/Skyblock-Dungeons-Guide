package kr.syeyoung.dungeonsguide.mod.features.impl.dungeon.map.overlay;

import kr.syeyoung.dungeonsguide.mod.dungeon.roomprocessor.bossfight.MarkerData;
import kr.syeyoung.dungeonsguide.mod.features.impl.dungeon.map.MapConfiguration;
import kr.syeyoung.dungeonsguide.mod.gui.DomElement;
import kr.syeyoung.dungeonsguide.mod.gui.renderer.RenderingContext;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;

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

    private final Identifier resourceLocation2 = Identifier.parse("dungeonsguide:map/bossfight/markers.png");
    @Override
    public void doRender(RenderingContext ctx, float rotation, float partialTicks, double scale, double relMouseX, double relMouseY) {
        double yaw = marker.getPrevYaw() + (marker.getCurrYaw() - marker.getPrevYaw()) * partialTicks;
        ctx.pose().rotate((float) Math.toRadians(yaw));

        ctx.pose().scale((float) (1 / scale), (float) (1 / scale));
        ctx.pose().scale((float) settings.getIconSize(), (float) settings.getIconSize());

        boolean flip = settings.getIconType() == MapConfiguration.PlayerHeadSettings.IconType.HEAD_FLIP;
        int tx = marker.getMarkerIndex() % 8;
        int ty = marker.getMarkerIndex() / 8;
        ctx.drawScaledCustomSizeModalRect(resourceLocation2, -4, -4, tx * 72,ty * 72 + (flip ? 8 : 0) , 72, 72, 8, flip ? -8 : 8, 576, 576);
    }

    @Override
    public boolean onClick(double relMouseX, double relMouseY, DomElement domElement) {
        return false;
    }
}
