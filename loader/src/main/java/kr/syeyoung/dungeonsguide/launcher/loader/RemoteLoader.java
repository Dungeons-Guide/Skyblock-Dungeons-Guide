package kr.syeyoung.dungeonsguide.launcher.loader;

import kr.syeyoung.dungeonsguide.launcher.DGInterface;
import kr.syeyoung.dungeonsguide.launcher.exceptions.ReferenceLeakedException;

public class RemoteLoader implements IDGLoader {
    private String branch;
    private String version;

    @Override
    public DGInterface loadDungeonsGuide() throws InstantiationException, IllegalAccessException, ClassNotFoundException {
        return null;
    }

    @Override
    public DGInterface getInstance() {
        return null;
    }

    @Override
    public void unloadDungeonsGuide() throws ReferenceLeakedException {

    }

    @Override
    public boolean isUnloadable() {
        return false;
    }

    @Override
    public boolean isLoaded() {
        return false;
    }

    @Override
    public String loaderName() {
        return branch;
    }

    @Override
    public String version() {
        return version;
    }
}
