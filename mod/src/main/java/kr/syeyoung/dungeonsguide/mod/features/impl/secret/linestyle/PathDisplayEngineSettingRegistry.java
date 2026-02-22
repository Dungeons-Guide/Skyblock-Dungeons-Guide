package kr.syeyoung.dungeonsguide.mod.features.impl.secret.linestyle;

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

}
