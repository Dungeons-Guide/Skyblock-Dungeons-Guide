package kr.syeyoung.modapi.v1_8_9.mod.arrowpath;

import kr.syeyoung.dungeonsguide.mod.config.types.AColor;
import lombok.Builder;
import lombok.Data;

@Data @Builder(toBuilder = true)
public class NeoRouteDisplayEngineLineProperties {
    private double width;
    private double smooth;
    private AColor background;
    private AColor arrow;
    private double animationSpeed;

    private double destinationSize;

    private boolean enableBeacon;
    private AColor beaconColor;
    private AColor beamColor;

    private boolean enableEtherwarpTracer;
    private AColor etherwarpTracerColor;
    private float etherwarpTracerWidth;
    private double etherwarpTracerDist;
    private boolean etherwarpTracerDisableEtherwarpRoute;
}
