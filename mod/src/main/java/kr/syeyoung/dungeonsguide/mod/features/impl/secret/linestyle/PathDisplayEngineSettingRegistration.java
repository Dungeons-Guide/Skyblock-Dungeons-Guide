package kr.syeyoung.dungeonsguide.mod.features.impl.secret.linestyle;

public interface PathDisplayEngineSettingRegistration<T> {
    PathDisplayEngineSetting<T> createConfiguration();
    String getName();
    String getJsonName();
}

