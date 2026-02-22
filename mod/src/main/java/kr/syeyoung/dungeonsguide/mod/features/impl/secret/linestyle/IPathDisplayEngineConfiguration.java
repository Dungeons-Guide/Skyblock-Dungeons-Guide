package kr.syeyoung.dungeonsguide.mod.features.impl.secret.linestyle;

public interface IPathDisplayEngineConfiguration {
    double getRefreshRate();
    boolean isPathfind();

    void setPathfind(boolean b);
    void setRefreshRate(int refreshRate);
}
