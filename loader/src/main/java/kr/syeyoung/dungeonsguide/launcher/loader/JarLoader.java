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
import kr.syeyoung.dungeonsguide.launcher.exceptions.DungeonsGuideLoadingException;
import kr.syeyoung.dungeonsguide.launcher.exceptions.ReferenceLeakedException;
import org.apache.commons.io.IOUtils;
import sun.misc.Resource;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.PhantomReference;
import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class JarLoader implements IDGLoader {
    private DGInterface dgInterface;
    private ReferenceQueue<ClassLoader> refQueue = new ReferenceQueue<>();
    private PhantomReference<ClassLoader> phantomReference;

    private boolean loaded;

    public static class JarClassLoader extends DGClassLoader {
        public JarClassLoader(ClassLoader parent, ZipInputStream zipInputStream) throws IOException {
            super(parent);

            ZipEntry zipEntry;
            while ((zipEntry=zipInputStream.getNextEntry()) != null) {
                this.loadedResources.put(zipEntry.getName(), IOUtils.toByteArray(zipInputStream));
            }

            zipInputStream.close();
        }
        private final HashMap<String, byte[]> loadedResources = new HashMap<String, byte[]>();
        @Override
        public byte[] getClassBytes(String name) throws IOException { // . separated.
            return this.loadedResources.get(name.replace(".", "/")+".class");
        }

        @Override
        public InputStream convert(String name) { // / separated
            if (this.loadedResources.containsKey(name.substring(1)))
                return new ByteArrayInputStream(this.loadedResources.get(name));
            return null;
        }
    }

    private JarClassLoader classLoader;

    @Override
    public DGInterface loadDungeonsGuide() throws DungeonsGuideLoadingException {
        if (dgInterface != null) throw new IllegalStateException("Already loaded");

        try {
            classLoader = new JarClassLoader(this.getClass().getClassLoader(), new ZipInputStream(JarLoader.class.getResourceAsStream("/mod.jar")));

            dgInterface = (DGInterface) classLoader.loadClass("kr.syeyoung.dungeonsguide.mod.DungeonsGuide", true).newInstance();
            phantomReference = new PhantomReference<>(classLoader, refQueue);
            return dgInterface;
        } catch (Exception e) {
            throw new DungeonsGuideLoadingException(e);
        }
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
    public String loaderName() {
        return "jar";
    }

    @Override
    public String version() {
        return "unknown"; // maybe read the thing...
    }
}
