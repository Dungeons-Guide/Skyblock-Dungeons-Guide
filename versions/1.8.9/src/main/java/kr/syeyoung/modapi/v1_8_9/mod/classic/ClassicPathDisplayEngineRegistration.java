package kr.syeyoung.modapi.v1_8_9.mod.classic;

import kr.syeyoung.dungeonsguide.mod.config.guiconfig.configv3.ParameterItem;
import kr.syeyoung.dungeonsguide.mod.config.types.*;
import kr.syeyoung.dungeonsguide.mod.dungeon.actions.route.ActionRoute;
import kr.syeyoung.dungeonsguide.mod.features.FeatureParameter;
import kr.syeyoung.dungeonsguide.mod.features.impl.secret.linestyle.AbstractPathDisplayEngineSetting;
import kr.syeyoung.dungeonsguide.mod.features.impl.secret.linestyle.IPathDisplayEngine;
import kr.syeyoung.dungeonsguide.mod.features.impl.secret.linestyle.PathDisplayEngineSetting;
import kr.syeyoung.dungeonsguide.mod.features.impl.secret.linestyle.PathDisplayEngineSettingRegistration;
import kr.syeyoung.dungeonsguide.mod.gui.Widget;
import kr.syeyoung.dungeonsguide.mod.gui.elements.Placeholder;

public class ClassicPathDisplayEngineRegistration implements PathDisplayEngineSettingRegistration<ClassicPathEngineLineProperties> {
    public static final ClassicPathDisplayEngineRegistration INSTANCE = new ClassicPathDisplayEngineRegistration();
    private ClassicPathDisplayEngineRegistration() {}

    @Override
    public PathDisplayEngineSetting<ClassicPathEngineLineProperties> createConfiguration() {
        return new ClassicPathDisplayEngineSetting();
    }

    @Override
    public String getName() {
        return "Classic";
    }

    @Override
    public String getJsonName() {
        return "classic";
    }

    public static class ClassicPathDisplayEngineSetting extends AbstractPathDisplayEngineSetting<ClassicPathEngineLineProperties> {
        public ClassicPathDisplayEngineSetting() {
            addParameter("pathfind", new FeatureParameter<Boolean>("pathfind", "Enable Pathfinding", "Enable pathfind for secrets", true, TCBoolean.INSTANCE));
            addParameter("lineColor", new FeatureParameter<AColor>("lineColor", "Line Color", "Color of the pathfind line", new AColor(0xFFFF0000, true), TCAColor.INSTANCE));
            addParameter("lineWidth", new FeatureParameter<Double>("lineWidth", "Line Thickness", "Thickness of the pathfind line",1.0, TCDouble.INSTANCE)
                    .setWidgetGenerator((param) -> new ParameterItem(param, new TCDouble.DoubleEditWidget(param, 0.1, Double.POSITIVE_INFINITY))));
            addParameter("linerefreshrate", new FeatureParameter<Integer>("linerefreshrate", "Line Refreshrate", "Ticks to wait per line refresh. Specify it to -1 to don't refresh line at all", 10, TCInteger.INSTANCE));
            addParameter("beacon", new FeatureParameter<Boolean>("beacon", "Enable Beacons", "Enable beacons for pathfind line targets",  true, TCBoolean.INSTANCE));
            addParameter("beamColor", new FeatureParameter<AColor>("beamColor", "Beam Color", "Color of the beacon beam", new AColor(0x77FF0000, true), TCAColor.INSTANCE));
            addParameter("beamTargetColor", new FeatureParameter<AColor>("beamTargetColor", "Target Color", "Color of the target", new AColor(0x33FF0000, true), TCAColor.INSTANCE));
        }

        @Override
        public Widget createSettingWidget() {
            return new WidgetLineParamEdit(this);
        }

        @Override
        public Widget createPreviewWidget() {
            return new Placeholder();
//            return new WidgetPreview(this);
        }

        @Override
        public PathDisplayEngineSettingRegistration getRegistration() {
            return INSTANCE;
        }

        public boolean isPathfind() {
            return this.<Boolean>getParameter("pathfind").getValue();
        }
        public AColor getLineColor() {
            return this.<AColor>getParameter("lineColor").getValue();
        }
        public double getLineWidth() {
            return this.<Double>getParameter("lineWidth").getValue();
        }
        public int getRefreshRate() {
            return this.<Integer>getParameter("linerefreshrate").getValue();
        }
        public boolean isBeacon() {
            return this.<Boolean>getParameter("beacon").getValue();
        }
        public AColor getBeamColor() {
            return this.<AColor>getParameter("beamColor").getValue();
        }
        public AColor getTargetColor() {
            return this.<AColor>getParameter("beamTargetColor").getValue();
        }

        public IPathDisplayEngine<ClassicPathEngineLineProperties> createPathDisplayEngine(ActionRoute route) {
            return new ClassicPathDisplayEngine(route, getRouteProperties());
        }

        public ClassicPathEngineLineProperties getRouteProperties() {
            ClassicPathEngineLineProperties classicPathEngineLineProperties = ClassicPathEngineLineProperties.builder().build();
            classicPathEngineLineProperties.setPathfind(isPathfind());
            classicPathEngineLineProperties.setLineColor(getLineColor());
            classicPathEngineLineProperties.setLineWidth(getLineWidth());
            classicPathEngineLineProperties.setLineRefreshRate(getRefreshRate());
            classicPathEngineLineProperties.setBeacon(isBeacon());
            classicPathEngineLineProperties.setBeaconBeamColor(getBeamColor());
            classicPathEngineLineProperties.setBeaconColor(getTargetColor());
            return classicPathEngineLineProperties;
        }
    }
}
