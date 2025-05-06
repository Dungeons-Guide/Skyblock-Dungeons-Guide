package kr.syeyoung.dungeonsguide.mod.features.impl.secret.linestyle;

import kr.syeyoung.dungeonsguide.mod.features.impl.secret.linestyle.arrowpath.NeoRouteDisplayEngineRegistration;
import kr.syeyoung.dungeonsguide.mod.features.impl.secret.linestyle.classic.ClassicPathDisplayEngineRegistration;

import java.util.HashMap;
import java.util.Map;

public class PathDisplayEngineSettingRegistry {
    private static final Map<String, PathDisplayEngineSettingRegistration<?>> registrationList = new HashMap<>();

    public static Map<String, PathDisplayEngineSettingRegistration<?>> getRegistrationList() {
        return registrationList;
    }

    public static PathDisplayEngineSettingRegistration<?> getRegistration(String name) {
        return registrationList.get(name);
    }

    public static void register(PathDisplayEngineSettingRegistration<?> registration) {
        registrationList.put(registration.getJsonName(), registration);
    }
    static {
        register(ClassicPathDisplayEngineRegistration.INSTANCE);
        register(NeoRouteDisplayEngineRegistration.INSTANCE);
    }

}
