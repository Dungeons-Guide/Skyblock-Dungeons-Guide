package kr.syeyoung.dungeonsguide.launcher.loader;

import kr.syeyoung.dungeonsguide.launcher.DGInterface;
import kr.syeyoung.dungeonsguide.launcher.exceptions.ReferenceLeakedException;

import java.lang.ref.PhantomReference;
import java.lang.ref.ReferenceQueue;
import java.net.URL;
import java.net.URLClassLoader;

public class RemoteLoader implements IDGLoader {
    private String branch;
    private String version;


    private ReferenceQueue<ClassLoader> refQueue = new ReferenceQueue<>();
    private PhantomReference<ClassLoader> phantomReference;

    private boolean loaded;

    public static class JarClassLoader extends URLClassLoader {
        public JarClassLoader(URL[] urls, ClassLoader parent) {
            super(urls, parent);
        }

        @Override
        protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {

            synchronized (getClassLoadingLock(name)) {
                Class<?> c = findLoadedClass(name);
                if (c == null) {

                    try {
                        if (c == null) {
                            long t0 = System.nanoTime();
                            c = findClass(name);

                            sun.misc.PerfCounter.getFindClassTime().addElapsedTimeFrom(t0);
                            sun.misc.PerfCounter.getFindClasses().increment();
                        }
                    } catch (ClassNotFoundException e) {
                        // ClassNotFoundException thrown if class not found
                        // from the non-null parent class loader
                    }
                    if (getParent() != null && c == null) {
                        long t0 = System.nanoTime();
                        c = getParent().loadClass(name);
                        long t1 = System.nanoTime();
                        sun.misc.PerfCounter.getParentDelegationTime().addTime(t1 - t0);
                    }
                }
                if (resolve) {
                    resolveClass(c);
                }
                return c;
            }
        }

        public Class<?> loadClassResolve(String name, boolean resolve) throws ClassNotFoundException {
            return this.loadClass(name, resolve);
        }
    }

    private JarLoader.JarClassLoader classLoader;
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
