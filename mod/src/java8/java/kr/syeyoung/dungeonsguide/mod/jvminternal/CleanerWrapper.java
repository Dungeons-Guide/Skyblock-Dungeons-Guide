package kr.syeyoung.dungeonsguide.mod.jvminternal;

import sun.misc.Cleaner;

public class CleanerWrapper {
    public static void registerCleaner(Object obj, Runnable clean) {
        Cleaner.create(obj, clean);
    }
}
