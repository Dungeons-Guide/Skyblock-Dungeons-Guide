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
import kr.syeyoung.dungeonsguide.launcher.exceptions.DungeonsGuideLoadingException;
import kr.syeyoung.dungeonsguide.launcher.exceptions.DungeonsGuideUnloadingException;
import net.minecraft.launchwrapper.LaunchClassLoader;
import org.apache.commons.io.IOUtils;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.PhantomReference;
import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;

public class LocalLoader implements IDGLoader {
    private DGInterface dgInterface;

    private ReferenceQueue<ClassLoader> refQueue = new ReferenceQueue<>();
    private PhantomReference<ClassLoader> phantomReference;


    public static class LocalClassLoader extends DGClassLoader {
        public LocalClassLoader(LaunchClassLoader parent) throws IOException {
            super(parent);
        }
        @Override
        public byte[] getClassBytes(String name) throws IOException { // . separated.
            if (name.startsWith("kr.syeyoung.dungeonsguide.launcher")) return null;
            InputStream in = convert(name.replace(".", "/")+".class");
            if (!(in instanceof BufferedInputStream)) return null;
            if (in == null) return null;
            return IOUtils.toByteArray(in);
        }

        @Override
        public InputStream convert(String name) { // / separated
            return LocalLoader.class.getResourceAsStream("/"+name);
        }
    }

    private LocalClassLoader classLoader;

    @Override
    public DGInterface loadDungeonsGuide() throws DungeonsGuideLoadingException {
        if (dgInterface != null) throw new IllegalStateException("Already loaded");

        try {
            classLoader = new LocalClassLoader((LaunchClassLoader) this.getClass().getClassLoader());
            phantomReference = new PhantomReference<>(classLoader, refQueue);
            dgInterface = (DGInterface) classLoader.loadClass("kr.syeyoung.dungeonsguide.mod.DungeonsGuide", true).newInstance();

            return dgInterface;
        } catch (Throwable e) {
            throw new DungeonsGuideLoadingException(e);
        }
    }

    @Override
    public DGInterface getInstance() {
        return dgInterface;
    }

    @Override
    public void unloadDungeonsGuide() throws DungeonsGuideUnloadingException {
        if (dgInterface == null && classLoader == null) return;
        try {
            if (dgInterface != null)
            dgInterface.unload();
        } catch (Throwable e) {
            dgInterface = null;
            throw new DungeonsGuideUnloadingException(e);
        }
        if (classLoader != null)
        classLoader.cleanup();
        classLoader = null;
        dgInterface = null;
        System.gc(); // pls do
        Reference<? extends ClassLoader> t = refQueue.poll();
        if (t == null) throw new DungeonsGuideUnloadingException("Reference Leaked"); // Why do you have to be that strict? Well, to tell them to actually listen on DungeonsGuideReloadListener. If it starts causing issues then I will remove check cus it's not really loaded (classes are loaded by child classloader)
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
        return "local";
    }

    @Override
    public String version() {
        return "unknown";
    }
}
