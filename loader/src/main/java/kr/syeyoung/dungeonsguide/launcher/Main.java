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

package kr.syeyoung.dungeonsguide.launcher;

import kr.syeyoung.dungeonsguide.launcher.auth.AuthManager;
import kr.syeyoung.dungeonsguide.launcher.branch.UpdateRetrieverUtil;
import kr.syeyoung.dungeonsguide.launcher.exceptions.NoSuitableLoaderFoundException;
import kr.syeyoung.dungeonsguide.launcher.exceptions.NoVersionFoundException;
import kr.syeyoung.dungeonsguide.launcher.exceptions.ReferenceLeakedException;
import kr.syeyoung.dungeonsguide.launcher.gui.screen.GuiDisplayer;
import kr.syeyoung.dungeonsguide.launcher.gui.screen.GuiLoadingError;
import kr.syeyoung.dungeonsguide.launcher.gui.screen.GuiReferenceLeak;
import kr.syeyoung.dungeonsguide.launcher.gui.screen.SpecialGuiScreen;
import kr.syeyoung.dungeonsguide.launcher.loader.IDGLoader;
import kr.syeyoung.dungeonsguide.launcher.loader.JarLoader;
import kr.syeyoung.dungeonsguide.launcher.loader.LocalLoader;
import kr.syeyoung.dungeonsguide.launcher.loader.RemoteLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IReloadableResourceManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.ProgressManager;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

import java.io.*;
import java.util.*;

@Mod(modid = Main.MOD_ID, version = Main.VERSION)
public class Main
{
    public static final String MOD_ID = "dungeons_guide_wrapper";
    public static final String VERSION = "1.0";
    public static final String DOMAIN = "http://testmachine:8080/api";

    private static Main main;

    private static File configDir;

    private DGInterface dgInterface;

    private final List<DungeonsGuideReloadListener> listeners = new ArrayList<>();

    public static File getConfigDir() {
        return configDir;
    }

    public void addDGReloadListener(DungeonsGuideReloadListener dungeonsGuideReloadListener) {
        listeners.add(Objects.requireNonNull(dungeonsGuideReloadListener));
    }
    public void removeDGReloadListener(DungeonsGuideReloadListener dungeonsGuideReloadListener) {
        listeners.remove(dungeonsGuideReloadListener);
    }

    private IDGLoader currentLoader;
    @EventHandler
    public void initEvent(FMLInitializationEvent initializationEvent) throws ClassNotFoundException, InstantiationException, IllegalAccessException {
        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(GuiDisplayer.INSTANCE);

        try {
            File f = new File(configDir, "loader.cfg");
            Configuration configuration = new Configuration(f);
            IDGLoader idgLoader = obtainLoader(configuration);
            load(idgLoader);
        } catch (Throwable e) {
            GuiDisplayer.INSTANCE.displayGui(obtainErrorGUI(e));
        }
    }

    public void unload() throws ReferenceLeakedException {
        if (currentLoader != null && !currentLoader.isUnloadable()) {
            throw new UnsupportedOperationException("Current version is not unloadable");
        }
        dgInterface = null;
        for (DungeonsGuideReloadListener listener : listeners) {
            listener.unloadReference();
        }
        if (currentLoader != null) {
            currentLoader.unloadDungeonsGuide();
        }
        currentLoader = null;
    }
    private void load(IDGLoader newLoader) throws ClassNotFoundException, InstantiationException, IllegalAccessException, IOException {
        if (dgInterface != null) throw new IllegalStateException("DG is loaded");
        dgInterface = newLoader.loadDungeonsGuide();
        currentLoader = newLoader;

        dgInterface.init(configDir);

        for (DungeonsGuideReloadListener listener : listeners) {
            listener.onLoad(dgInterface);
        }
    }
    public void reload(IDGLoader newLoader) {
        try {
            unload();
            load(newLoader);
        } catch (Exception e) {
            dgInterface = null;
            currentLoader = null;

            e.printStackTrace();

            GuiDisplayer.INSTANCE.displayGui(obtainErrorGUI(e));
        }
    }

    public SpecialGuiScreen obtainErrorGUI(Throwable lastError) {
        if (lastError instanceof kr.syeyoung.dungeonsguide.launcher.exceptions.AuthenticationUnavailableException) {
            return null;
        } else if (lastError instanceof NoSuitableLoaderFoundException) {
            return new GuiLoadingError(lastError);
        } else if (lastError instanceof ReferenceLeakedException) {
            return new GuiReferenceLeak(lastError);
        } else if (lastError != null){
            return new GuiLoadingError(lastError);
        }
        // when gets called init and stuff remove thing
        return null;
    }


    public String getLoaderName(Configuration configuration) {
        String loader = System.getProperty("dg.loader");
        if (loader == null) {
            loader = configuration.get("loader", "modsource", "auto").getString();
        }
        if (loader == null) loader = "auto";
        return loader;
    }


    public IDGLoader obtainLoader(Configuration configuration) {
        String loader = getLoaderName(configuration);

        if ("local".equals(loader) ||
                (loader.equals("auto") && this.getClass().getResourceAsStream("/kr/syeyoung/dungeonsguide/DungeonsGuide.class") == null)) {
            return new LocalLoader();
        } else if ("jar".equals(loader) ||
                (loader.equals("auto") && this.getClass().getResourceAsStream("/mod.jar") == null)) {
            return new JarLoader();
        } else if (loader.equals("auto") ){
                // remote load
            String branch =  System.getProperty("branch") == null ? configuration.get("loader", "remoteBranch", "$default").getString() : System.getProperty("branch");
            String version = System.getProperty("version") == null ? configuration.get("loader", "remoteVersion", "latest").getString() : System.getProperty("version");
            try {
                UpdateRetrieverUtil.VersionInfo versionInfo = UpdateRetrieverUtil.getIds(
                       branch,
                        version
                );
                if (versionInfo == null) throw new NoVersionFoundException(branch, version);

                return new RemoteLoader(versionInfo.getFriendlyBranchName(), versionInfo.getBranchId(), versionInfo.getUpdateId());
            } catch (IOException e) {
                throw new NoVersionFoundException(branch, version, e);
            }
        } else {
            throw new NoSuitableLoaderFoundException(System.getProperty("dg.loader"), configuration.get("loader", "modsource", "auto").getString());
        }
    }

    @EventHandler
    public void preInit(FMLPreInitializationEvent preInitializationEvent) {
        // setup static variables
        main = this;
        configDir = preInitializationEvent.getModConfigurationDirectory();

        // setup preinit progress bar for well, progress bar!
        ProgressManager.ProgressBar bar = ProgressManager.push("DungeonsGuide", 1);
        try {
            // Try authenticate
            bar.step("Authenticating...");
            AuthManager.getInstance().init();


            // If authentication succeeds, obtain loader and partially load dungeons guide

            File f = new File(preInitializationEvent.getModConfigurationDirectory(), "loader.cfg");
            Configuration configuration = new Configuration(f);
            // Save config because... well to generate it
            configuration.save();
        } catch (Throwable t) {
            dgInterface = null;
            currentLoader = null;

            t.printStackTrace();
        } finally {
            while(bar.getStep() < bar.getSteps()) bar.step("");
            ProgressManager.pop(bar);
        }

        ((IReloadableResourceManager) Minecraft.getMinecraft().getResourceManager()).registerReloadListener(a -> {
            if (dgInterface != null) dgInterface.onResourceReload(a);
        });
    }

    public static Main getMain() {
        return main;
    }
}
