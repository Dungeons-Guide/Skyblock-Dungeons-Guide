package kr.syeyoung.modapi;

import java.util.Iterator;
import java.util.ServiceLoader;

public class ModAPIHolder {
    public static ModAPI modAPI;

    static {
        Iterator<ModAPI> it = ServiceLoader.load(ModAPI.class).iterator();

        if (!it.hasNext()) throw new ExceptionInInitializerError("ModAPI Not found");
        modAPI = it.next();
        if (it.hasNext()) throw new ExceptionInInitializerError("More than two mod apis found");
    }
}
