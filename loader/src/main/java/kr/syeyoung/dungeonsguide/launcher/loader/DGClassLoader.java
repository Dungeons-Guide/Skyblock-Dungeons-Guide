/*
 * Dungeons Guide - The most intelligent Hypixel Skyblock Dungeons Mod
 * Copyright (C) 2022  cyoung06 (syeyoung)
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

import kr.syeyoung.dungeonsguide.launcher.Main;
import kr.syeyoung.dungeonsguide.launcher.events.DGAwareEventSubscriptionTransformer;
import net.minecraft.launchwrapper.LaunchClassLoader;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public abstract class DGClassLoader extends ClassLoader implements ByteStreamURLHandler.InputStreamGenerator{

    DGAwareEventSubscriptionTransformer eventSubscriptionTransformer = new DGAwareEventSubscriptionTransformer(this);

    private Map<String, Class<?>> launchClassLoaderCacheMap;
    private Set<String> classesILoaded=  new HashSet<>();
    public DGClassLoader(LaunchClassLoader parent) {
        super(parent);

        this.launchClassLoaderCacheMap = ReflectionHelper.getPrivateValue(LaunchClassLoader.class, parent, "cachedClasses");
    }

    public Class<?> loadClass(String name, boolean resolve)
            throws ClassNotFoundException
    {
        synchronized (getClassLoadingLock(name)) {
            // First, check if the class has already been loaded
            Class<?> c = findLoadedClass(name);
            if (c == null) {
                try {
                    if (c == null) {
                        long t0 = System.nanoTime();
                        c = findClass(name);

                        if (c != null) {
                            launchClassLoaderCacheMap.put(name, c);
                            classesILoaded.add(name);
                        }

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

    public void cleanup() {
        for (String s : classesILoaded) {
            launchClassLoaderCacheMap.remove(s);
        }
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        byte[] res;
        try {
            res = getClassBytes(name);
        } catch (IOException e) {
            throw new ClassNotFoundException(name, e);
        }
        if (res != null) {
            res = eventSubscriptionTransformer.transform(name, name, res);
            return defineClass(name, res, 0, res.length, Main.class.getProtectionDomain());
        } else {
            throw new ClassNotFoundException(name);
        }
    }

    public abstract  byte[] getClassBytes(String name) throws IOException;

    public URL getResource(String name) {
        URL url = findResource(name);
        if (url == null && getParent() != null ) {
            url = getParent().getResource(name);
        }
        return url;
    }

    private ByteStreamURLHandler urlHandler = new ByteStreamURLHandler(this);
    @Override
    public URL findResource(String name) {
        try {
            return new URL("dungeonsguide", "",0, name, urlHandler);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }
}
