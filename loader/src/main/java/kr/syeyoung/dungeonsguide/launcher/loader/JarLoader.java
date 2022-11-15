/*
 * Dungeons Guide - The most intelligent Hypixel Skyblock Dungeons Mod
 * Copyright (C) 2021  cyoung06
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package kr.syeyoung.dungeonsguide.launcher.loader;

import kr.syeyoung.dungeonsguide.launcher.DGInterface;
import kr.syeyoung.dungeonsguide.launcher.Main;
import kr.syeyoung.dungeonsguide.launcher.exceptions.ReferenceLeakedException;

import java.io.InputStream;
import java.lang.ref.PhantomReference;
import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.net.URLClassLoader;

public class JarLoader implements IDGLoader {
    private DGInterface dgInterface;
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

    private JarClassLoader classLoader;

    @Override
    public DGInterface loadDungeonsGuide() throws ClassNotFoundException, InstantiationException, IllegalAccessException {
        if (dgInterface != null) throw new IllegalStateException("Already loaded");

        classLoader = new JarClassLoader(new URL[] {
                Main.class.getResource("/mod.jar")
        }, this.getClass().getClassLoader());

        dgInterface = (DGInterface) classLoader.loadClassResolve("kr.syeyoung.dungeonsguide.DungeonsGuide", true).newInstance();
        phantomReference = new PhantomReference<>(classLoader, refQueue);
        return dgInterface;
    }

    @Override
    public DGInterface getInstance() {
        return dgInterface;
    }

    @Override
    public void unloadDungeonsGuide() throws ReferenceLeakedException {
        classLoader = null;
        dgInterface.unload();
        dgInterface = null;
        System.gc();// pls do
        Reference<? extends ClassLoader> t = refQueue.poll();
        if (t == null) throw new ReferenceLeakedException(); // Why do you have to be that strict? Well, to tell them to actually listen on DungeonsGuideReloadListener. If it starts causing issues then I will remove check cus it's not really loaded (classes are loaded by child classloader)
        t.clear();
        phantomReference = null;
    }

    @Override
    public boolean isUnloadable() {
        return true;
    }

    @Override
    public boolean isLoaded() {
        return dgInterface != null;
    }

    @Override
    public String branchName() {
        return "jar";
    }

    @Override
    public String version() {
        return "unknown"; // maybe read the thing...
    }
}
