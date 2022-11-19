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
import kr.syeyoung.dungeonsguide.launcher.branch.Update;
import kr.syeyoung.dungeonsguide.launcher.branch.UpdateRetrieverUtil;
import kr.syeyoung.dungeonsguide.launcher.exceptions.DungeonsGuideLoadingException;
import kr.syeyoung.dungeonsguide.launcher.exceptions.InvalidSignatureException;
import kr.syeyoung.dungeonsguide.launcher.exceptions.NoVersionFoundException;
import kr.syeyoung.dungeonsguide.launcher.exceptions.DungeonsGuideUnloadingException;
import net.minecraft.launchwrapper.LaunchClassLoader;
import org.apache.commons.io.IOUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.PhantomReference;
import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.util.HashMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class RemoteLoader implements IDGLoader {
    private DGInterface dgInterface;
    private ReferenceQueue<ClassLoader> refQueue = new ReferenceQueue<>();
    private PhantomReference<ClassLoader> phantomReference;

    public RemoteLoader(String friendlyBranchName, long branchId, long updateId) {
        this.friendlyBranchName = friendlyBranchName;
        this.branchId = branchId;
        this.updateId = updateId;
    }


    public static class JarClassLoader extends DGClassLoader {
        public JarClassLoader(LaunchClassLoader parent, ZipInputStream zipInputStream) throws IOException {
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
            if (this.loadedResources.containsKey(name))
                return new ByteArrayInputStream(this.loadedResources.get(name));
            return null;
        }
    }

    private JarClassLoader classLoader;



    @Override
    public DGInterface loadDungeonsGuide() throws DungeonsGuideLoadingException {
        if (dgInterface != null) throw new IllegalStateException("Already loaded");
        try {
            Update target;
            try {
                target = UpdateRetrieverUtil.getUpdate(branchId, updateId);
                friendlyVersionName = target.getName();
            } catch (Exception e) {
                throw new NoVersionFoundException(friendlyBranchName, friendlyVersionName, branchId+"@"+updateId, e);
            }

            InputStream in;
            byte[] mod = IOUtils.toByteArray(in = UpdateRetrieverUtil.downloadFile(target, "mod.jar"));
            in.close();
            byte[] signature = IOUtils.toByteArray(in = UpdateRetrieverUtil.downloadFile(target, "mod.jar.asc"));
            in.close();

            SignatureValidator.validateVersion1Signature(target, mod, signature);

            classLoader = new JarClassLoader((LaunchClassLoader) this.getClass().getClassLoader(), new ZipInputStream(new ByteArrayInputStream(mod)));
            phantomReference = new PhantomReference<>(classLoader, refQueue);
            dgInterface = (DGInterface) classLoader.loadClass("kr.syeyoung.dungeonsguide.mod.DungeonsGuide", true).newInstance();

            return dgInterface;
        } catch (Throwable e) { // the reason why I am catching throwable here: in case NoClassDefFoundError.
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
        System.gc();// pls do
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
        return "remote";
    }

    private long branchId = -1; // pre-retrieved
    private long updateId = -1; // pre-retrieved

    private String friendlyBranchName = "";
    private String friendlyVersionName = "";
    @Override
    public String version() {
        return friendlyBranchName+"("+branchId+")@"+friendlyVersionName+"("+updateId+")"; // maybe read the thing...
    }
}
