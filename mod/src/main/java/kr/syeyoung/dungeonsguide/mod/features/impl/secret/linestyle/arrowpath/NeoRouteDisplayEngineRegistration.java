package kr.syeyoung.dungeonsguide.mod.features.impl.secret.linestyle.arrowpath;

import kr.syeyoung.dungeonsguide.mod.dungeon.actions.route.ActionRoute;
import kr.syeyoung.dungeonsguide.mod.features.impl.secret.linestyle.AbstractPathDisplayEngineSetting;
import kr.syeyoung.dungeonsguide.mod.features.impl.secret.linestyle.IPathDisplayEngine;
import kr.syeyoung.dungeonsguide.mod.features.impl.secret.linestyle.PathDisplayEngineSetting;
import kr.syeyoung.dungeonsguide.mod.features.impl.secret.linestyle.PathDisplayEngineSettingRegistration;
import kr.syeyoung.dungeonsguide.mod.gui.Widget;
import kr.syeyoung.dungeonsguide.mod.gui.elements.Placeholder;

public class NeoRouteDisplayEngineRegistration implements PathDisplayEngineSettingRegistration<Object> {
    public static final NeoRouteDisplayEngineRegistration INSTANCE = new NeoRouteDisplayEngineRegistration();
    private NeoRouteDisplayEngineRegistration() {}

    @Override
    public PathDisplayEngineSetting<Object> createConfiguration() {
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

    public static class ArrowPathDisplayEngineSetting extends AbstractPathDisplayEngineSetting<Object> {

        @Override
        public IPathDisplayEngine<Object> createPathDisplayEngine(ActionRoute route) {
            return new NeoRouteDisplayEngine(route);
        }

        @Override
        public PathDisplayEngineSettingRegistration<Object> getRegistration() {
            return INSTANCE;
        }

        @Override
        public Widget createSettingWidget() {
            return null;
        }

        @Override
        public Widget createPreviewWidget() {
            return new Placeholder();
        }
    }
}
