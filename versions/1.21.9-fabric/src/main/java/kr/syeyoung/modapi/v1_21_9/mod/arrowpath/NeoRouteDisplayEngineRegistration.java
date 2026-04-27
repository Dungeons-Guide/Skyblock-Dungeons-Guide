package kr.syeyoung.modapi.v1_21_9.mod.arrowpath;

import kr.syeyoung.dungeonsguide.mod.config.guiconfig.configv3.ParameterItem;
import kr.syeyoung.dungeonsguide.mod.config.types.AColor;
import kr.syeyoung.dungeonsguide.mod.config.types.TCAColor;
import kr.syeyoung.dungeonsguide.mod.config.types.TCBoolean;
import kr.syeyoung.dungeonsguide.mod.config.types.TCDouble;
import kr.syeyoung.dungeonsguide.mod.dungeon.actions.route.ActionRoute;
import kr.syeyoung.dungeonsguide.mod.features.FeatureParameter;
import kr.syeyoung.dungeonsguide.mod.features.impl.secret.linestyle.AbstractPathDisplayEngineSetting;
import kr.syeyoung.dungeonsguide.mod.features.impl.secret.linestyle.IPathDisplayEngine;
import kr.syeyoung.dungeonsguide.mod.features.impl.secret.linestyle.PathDisplayEngineSetting;
import kr.syeyoung.dungeonsguide.mod.features.impl.secret.linestyle.PathDisplayEngineSettingRegistration;
import kr.syeyoung.dungeonsguide.mod.gui.Widget;
import kr.syeyoung.dungeonsguide.mod.gui.elements.Placeholder;

public class NeoRouteDisplayEngineRegistration implements PathDisplayEngineSettingRegistration<NeoRouteDisplayEngineLineProperties> {
    public static final NeoRouteDisplayEngineRegistration INSTANCE = new NeoRouteDisplayEngineRegistration();
    private NeoRouteDisplayEngineRegistration() {}

    @Override
    public PathDisplayEngineSetting<NeoRouteDisplayEngineLineProperties> createConfiguration() {
        return new ArrowPathDisplayEngineSetting();
    }

    @Override
    public String getName() {
        return "Neo Route";
    }

    @Override
    public String getJsonName() {
        return "neoroute";
    }

    public static class ArrowPathDisplayEngineSetting extends AbstractPathDisplayEngineSetting<NeoRouteDisplayEngineLineProperties> {
        public ArrowPathDisplayEngineSetting() {
            addParameter("lineColor", new FeatureParameter<AColor>("lineColor", "Path Background Color", "Color of path background", new AColor(0x6400FFFF, true), TCAColor.INSTANCE));
            addParameter("arrowColor", new FeatureParameter<AColor>("arrowColor", "Path Arrow Color", "Color of path arrow", new AColor(0xFFFFFFFF, true), TCAColor.INSTANCE));
            addParameter("lineWidth", new FeatureParameter<Double>("lineWidth", "Path Thickness", "Thickness of the path",0.3, TCDouble.INSTANCE)
                    .setWidgetGenerator((param) -> new ParameterItem(param, new TCDouble.DoubleEditWidget(param, 0.01, Double.POSITIVE_INFINITY))));
            addParameter("lineSmooth", new FeatureParameter<Double>("lineSmooth", "Path Smoothness", "Smoothness of the path",1.0, TCDouble.INSTANCE)
                    .setWidgetGenerator((param) -> new ParameterItem(param, new TCDouble.DoubleEditWidget(param, 0.01, Double.POSITIVE_INFINITY))));
            addParameter("animationSpeed", new FeatureParameter<Double>("animationSpeed", "Path Arrow animation speed", "Speed of the arrow moving",1.0, TCDouble.INSTANCE)
                    .setWidgetGenerator((param) -> new ParameterItem(param, new TCDouble.DoubleEditWidget(param, 0, Double.POSITIVE_INFINITY))));
            addParameter("destinationSize", new FeatureParameter<Double>("destinationSize", "Size of the \"destination\" text", "N/A",1.0, TCDouble.INSTANCE)
                    .setWidgetGenerator((param) -> new ParameterItem(param, new TCDouble.DoubleEditWidget(param, 0, Double.POSITIVE_INFINITY))));


            addParameter("beacon", new FeatureParameter<Boolean>("beacon", "Enable Beacons", "Enable beacons for pathfind line targets",  true, TCBoolean.INSTANCE));
            addParameter("beamColor", new FeatureParameter<AColor>("beamColor", "Beam Color", "Color of the beacon beam", new AColor(0x77FF0000, true), TCAColor.INSTANCE));
            addParameter("beamTargetColor", new FeatureParameter<AColor>("beamTargetColor", "Target Color", "Color of the target", new AColor(0x33FF0000, true), TCAColor.INSTANCE));

            addParameter("etherwarpTracer", new FeatureParameter<Boolean>("etherwarpTracer", "Enable Etherwarp Tracer", "Enable tracer for etherwarps",  true, TCBoolean.INSTANCE));
            addParameter("etherwarpTracerColor", new FeatureParameter<AColor>("etherwarpTracerColor", "Etherwarp Tracer Color", "Color of the tracer line", new AColor(0xFFFFFFFF, true), TCAColor.INSTANCE));
            addParameter("etherwarpTracerWidth", new FeatureParameter<Double>("etherwarpTracerWidth", "Etherwarp Tracer Width", "Thickness of the tracer line",3.0, TCDouble.INSTANCE)
                    .setWidgetGenerator((param) -> new ParameterItem(param, new TCDouble.DoubleEditWidget(param, 1, Double.POSITIVE_INFINITY))));
            addParameter("etherwarpTracerDist", new FeatureParameter<Double>("etherwarpTracerDist", "Etherwarp Tracer Dist", "When to trigger tracer (sq dist)",3.0, TCDouble.INSTANCE)
                    .setWidgetGenerator((param) -> new ParameterItem(param, new TCDouble.DoubleEditWidget(param, 0.01, Double.POSITIVE_INFINITY))));
            addParameter("etherwarpTracerDisableEtherwarpRoute", new FeatureParameter<Boolean>("etherwarpTracerDisableEtherwarpRoute", "Disable etherwarp icon display when tracer is visible", "Disable etherwarp icon display when tracer is visible",  false, TCBoolean.INSTANCE));

        }

        @Override
        public IPathDisplayEngine<NeoRouteDisplayEngineLineProperties> createPathDisplayEngine(ActionRoute route) {
            return new NeoRouteDisplayEngine(route, getSettings());
        }

        public NeoRouteDisplayEngineLineProperties getSettings() {
            return NeoRouteDisplayEngineLineProperties.builder()
                    .background(getLineColor())
                    .arrow(getArrowColor())
                    .width(getLineWidth())
                    .smooth(getLineSmoothness())
                    .animationSpeed(getAnimationSpeed())
                    .destinationSize(getDestinationSize())
                    .enableBeacon(isBeacon())
                    .beamColor(getBeamColor())
                    .beaconColor(getBeaconColor())
                    .etherwarpTracerWidth(getEtherwarpTracerWidth())
                    .etherwarpTracerColor(getEtherwarpTracerColor())
                    .etherwarpTracerDist(getEtherwarpTracerDist())
                    .etherwarpTracerDisableEtherwarpRoute(disableEtherwarpPath())
                    .enableEtherwarpTracer(isEtherwarpTracer())
                    .build();
        }

        public AColor getLineColor() {
            return this.<AColor>getParameter("lineColor").getValue();
        }

        public AColor getArrowColor() {
            return this.<AColor>getParameter("arrowColor").getValue();
        }

        public Double getLineWidth() {
            return this.<Double>getParameter("lineWidth").getValue();
        }

        public Double getLineSmoothness() {
            return this.<Double>getParameter("lineSmooth").getValue();
        }

        public Double getAnimationSpeed() {
            return this.<Double>getParameter("animationSpeed").getValue();
        }

        public Double getDestinationSize() {
            return this.<Double>getParameter("destinationSize").getValue();
        }

        public AColor getBeaconColor() {
            return this.<AColor>getParameter("beamTargetColor").getValue();
        }


        public boolean isBeacon() {
            return this.<Boolean>getParameter("beacon").getValue();
        }

        public AColor getBeamColor() {
            return this.<AColor>getParameter("beamColor").getValue();
        }

        public AColor getEtherwarpTracerColor() {
            return this.<AColor>getParameter("etherwarpTracerColor").getValue();
        }
        public float getEtherwarpTracerWidth() {return this.<Double>getParameter("etherwarpTracerWidth").getValue().floatValue(); }
        public double getEtherwarpTracerDist() {return this.<Double>getParameter("etherwarpTracerDist").getValue(); }
        public boolean isEtherwarpTracer() {return this.<Boolean>getParameter("etherwarpTracer").getValue(); }
        public boolean disableEtherwarpPath() {return this.<Boolean>getParameter("etherwarpTracerDisableEtherwarpRoute").getValue(); }

        @Override
        public PathDisplayEngineSettingRegistration<NeoRouteDisplayEngineLineProperties> getRegistration() {
            return INSTANCE;
        }

        @Override
        public Widget createSettingWidget() {
            return new WidgetNeoRouteParamEdit(this);
        }

        @Override
        public Widget createPreviewWidget() {
            return new Placeholder();
//            return new WidgetPreview(this);
        }
    }
}
