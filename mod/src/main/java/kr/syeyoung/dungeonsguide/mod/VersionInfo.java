package kr.syeyoung.dungeonsguide.mod;

import kr.syeyoung.dungeonsguide.launcher.Main;
import kr.syeyoung.dungeonsguide.launcher.loader.IDGLoader;

public class VersionInfo {
    public static final String VERSION = "4.0.0-beta-1.0.0";

    public static IDGLoader getCurrentLoader() {
        return Main.getMain().getCurrentLoader();
    }

    public static String getLoaderInfo() {
        return getCurrentLoader().loaderName();
    }
}
