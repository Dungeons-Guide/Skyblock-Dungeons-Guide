package kr.syeyoung.dungeonsguide.mod.features.impl.secret.linestyle;

public interface PathDisplayEngineSettingRegistration<T extends IPathDisplayEngineConfiguration> {
    PathDisplayEngineSetting<T> createConfiguration();
    String getName();
    String getJsonName();
}

