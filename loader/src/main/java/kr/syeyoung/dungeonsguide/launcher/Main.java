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
import kr.syeyoung.dungeonsguide.launcher.exceptions.DungeonsGuideLoadingException;
import kr.syeyoung.dungeonsguide.launcher.exceptions.NoSuitableLoaderFoundException;
import kr.syeyoung.dungeonsguide.launcher.exceptions.NoVersionFoundException;
import kr.syeyoung.dungeonsguide.launcher.exceptions.DungeonsGuideUnloadingException;
import kr.syeyoung.dungeonsguide.launcher.gui.screen.GuiDisplayer;
import kr.syeyoung.dungeonsguide.launcher.gui.screen.GuiLoadingError;
import kr.syeyoung.dungeonsguide.launcher.gui.screen.GuiUnloadingError;
import kr.syeyoung.dungeonsguide.launcher.gui.tooltip.Notification;
import kr.syeyoung.dungeonsguide.launcher.gui.tooltip.NotificationManager;
import kr.syeyoung.dungeonsguide.launcher.loader.*;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IReloadableResourceManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.ProgressManager;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.io.*;
import java.security.Security;
import java.util.*;

@Mod(modid = Main.MOD_ID, version = Main.VERSION)
public class Main
{
    public static final String MOD_ID = "dungeons_guide_wrapper";
    public static final String VERSION = "1.0";
    public static final String DOMAIN = "http://testmachine2/api";

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


    @Getter
    private IDGLoader currentLoader;

    private static final UUID dgUnloaded = UUID.randomUUID();

    @EventHandler
    public void initEvent(FMLInitializationEvent initializationEvent) throws ClassNotFoundException, InstantiationException, IllegalAccessException {
        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(GuiDisplayer.INSTANCE);
        MinecraftForge.EVENT_BUS.register(NotificationManager.INSTANCE);

        NotificationManager.INSTANCE.updateNotification(dgUnloaded, Notification.builder()
                        .title("Dungeons Guide Not Loaded")
                        .titleColor(0xFFFF0000)
                        .description("Click to try reloading....")
                        .onClick(() -> {
                            try {
                                File f = new File(configDir, "loader.cfg");
                                Configuration configuration = new Configuration(f);
                                IDGLoader idgLoader = obtainLoader(configuration);
                                load(idgLoader);
                            } catch (NoSuitableLoaderFoundException e) {
                                e.printStackTrace();
                                GuiDisplayer.INSTANCE.displayGui(new GuiLoadingError(e));
                            } catch (NoVersionFoundException e) {
                                e.printStackTrace();
                                GuiDisplayer.INSTANCE.displayGui(new GuiLoadingError(e));
                            } catch (DungeonsGuideLoadingException e) {
                                e.printStackTrace();
                                GuiDisplayer.INSTANCE.displayGui(new GuiLoadingError(e));
                            }
                        })
                        .unremovable(true)
                .build());

        try {
            File f = new File(configDir, "loader.cfg");
            Configuration configuration = new Configuration(f);
            IDGLoader idgLoader = obtainLoader(configuration);
            load(idgLoader);
        } catch (NoSuitableLoaderFoundException e) {
            e.printStackTrace();
            GuiDisplayer.INSTANCE.displayGui(new GuiLoadingError(e));
        } catch (NoVersionFoundException e) {
            e.printStackTrace();
            GuiDisplayer.INSTANCE.displayGui(new GuiLoadingError(e));
        } catch (DungeonsGuideLoadingException e) {
            e.printStackTrace();

            try {
                unload();
            } catch (Exception e2) {
                e2.printStackTrace();
                GuiDisplayer.INSTANCE.displayGui(new GuiUnloadingError(e2));
            }
            GuiDisplayer.INSTANCE.displayGui(new GuiLoadingError(e));

        }
    }

    public void unload() throws DungeonsGuideUnloadingException {
        if (currentLoader != null && !currentLoader.isUnloadable()) {
            throw new UnsupportedOperationException("Current version is not unloadable");
        }
        dgInterface = null;
        for (DungeonsGuideReloadListener listener : listeners) {
            listener.unloadReference();
        }

        NotificationManager.INSTANCE.updateNotification(dgUnloaded, Notification.builder()
                .title("Dungeons Guide Not Loaded")
                .titleColor(0xFFFF0000)
                .description("Click to try reloading....")
                .onClick(() -> {
                    try {
                        File f = new File(configDir, "loader.cfg");
                        Configuration configuration = new Configuration(f);
                        IDGLoader idgLoader = obtainLoader(configuration);
                        reload(idgLoader);
                    } catch (NoSuitableLoaderFoundException e) {
                        e.printStackTrace();
                        GuiDisplayer.INSTANCE.displayGui(new GuiLoadingError(e));
                    } catch (NoVersionFoundException e) {
                        e.printStackTrace();
                        GuiDisplayer.INSTANCE.displayGui(new GuiLoadingError(e));
                    } catch (DungeonsGuideLoadingException e) {
                        e.printStackTrace();
                        try {
                            unload();
                        } catch (Exception e2) {
                            e2.printStackTrace();
                            GuiDisplayer.INSTANCE.displayGui(new GuiUnloadingError(e2));
                        }
                        GuiDisplayer.INSTANCE.displayGui(new GuiLoadingError(e));
                    } catch (DungeonsGuideUnloadingException e) {
                        GuiDisplayer.INSTANCE.displayGui(new GuiUnloadingError(e));
                    }
                })
                .unremovable(true)
                .build());
        if (currentLoader != null) {
            currentLoader.unloadDungeonsGuide();
        }
        currentLoader = null;


    }
    private void load(IDGLoader newLoader) throws DungeonsGuideLoadingException {
        if (dgInterface != null) throw new IllegalStateException("DG is loaded");
        dgInterface = newLoader.loadDungeonsGuide();
        currentLoader = newLoader;
        try {
            dgInterface.init(configDir);
        } catch (Exception e) {
            throw new DungeonsGuideLoadingException("Exception occured while calling init", e);
        }
        for (DungeonsGuideReloadListener listener : listeners) {
            listener.onLoad(dgInterface);
        }


        NotificationManager.INSTANCE.updateNotification(UUID.randomUUID(), Notification.builder()
                    .title("Dungeons Guide Loaded!")
                    .description("Successfully Loaded Dungeons Guide!\nLoader: "+currentLoader.loaderName()+"\nVersion: "+currentLoader.version())
                    .titleColor(0xFF00FF00)
                    .build());


        NotificationManager.INSTANCE.removeNotification(dgUnloaded);
    }

    private volatile IDGLoader reqLoader = null;
    public void reloadWithoutStacktraceReference(IDGLoader newLoader) {
        reqLoader = newLoader;
    }
    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent tickEvent) {
        if (reqLoader != null) {
            IDGLoader loader = reqLoader;
            reqLoader = null;

            try {
                reload(loader);
            } catch (DungeonsGuideLoadingException e) {
                try {
                    unload();
                } catch (Exception e2) {
                    GuiDisplayer.INSTANCE.displayGui(new GuiUnloadingError(e2));
                }
                GuiDisplayer.INSTANCE.displayGui(new GuiLoadingError(e));
            } catch (DungeonsGuideUnloadingException e) {
                GuiDisplayer.INSTANCE.displayGui(new GuiUnloadingError(e));
            }
        }
    }

    public void reload(IDGLoader newLoader) throws DungeonsGuideLoadingException, DungeonsGuideUnloadingException {
        try {
            unload();
            load(newLoader);
        } catch (DungeonsGuideLoadingException | DungeonsGuideUnloadingException e) {
            dgInterface = null;
//            currentLoader = null;

            e.printStackTrace();
            throw e;
        }
    }


    public String getLoaderName(Configuration configuration) {
        String loader = System.getProperty("dg.loader");
        if (loader == null) {
            loader = configuration.get("loader", "modsource", "auto").getString();
        }
        if (loader == null) loader = "auto";
        return loader;
    }


    public IDGLoader obtainLoader(Configuration configuration) throws NoVersionFoundException, NoSuitableLoaderFoundException {
        String loader = getLoaderName(configuration);

        if ("devenv".equals(loader)) {
            return new DevEnvLoader();
        } else if ("local".equals(loader) ||
                (loader.equals("auto") && this.getClass().getResourceAsStream("/kr/syeyoung/dungeonsguide/mod/DungeonsGuide.class") != null)) {
            return new LocalLoader();
        } else if ("jar".equals(loader) ||
                (loader.equals("auto") && this.getClass().getResourceAsStream("/mod.jar") != null)) {
            return new JarLoader();
        } else if (loader.equals("remote") || loader.equals("auto") ){
                // remote load
            String branch =  System.getProperty("branch") == null ? configuration.get("loader", "remoteBranch", "$default").getString() : System.getProperty("branch");
            String version = System.getProperty("version") == null ? configuration.get("loader", "remoteVersion", "latest").getString() : System.getProperty("version");
            try {
                UpdateRetrieverUtil.VersionInfo versionInfo = UpdateRetrieverUtil.getIds(
                       branch,
                        version
                );
                return new RemoteLoader(versionInfo.getFriendlyBranchName(), versionInfo.getBranchId(), versionInfo.getUpdateId());
            } catch (IOException e) {
                throw new NoVersionFoundException(branch, version, "IO err", e);
            }
        } else {
            throw new NoSuitableLoaderFoundException(System.getProperty("dg.loader"), configuration.get("loader", "modsource", "auto").getString());
        }
    }

    @EventHandler
    public void preInit(FMLPreInitializationEvent preInitializationEvent) {
        Security.addProvider(new BouncyCastleProvider());
        // setup static variables
        main = this;
        dgInterface = null;
        currentLoader = null;
        configDir = preInitializationEvent.getModConfigurationDirectory();

        // setup preinit progress bar for well, progress bar!
        ProgressManager.ProgressBar bar = ProgressManager.push("DungeonsGuide", 1);
        // Try authenticate
        bar.step("Authenticating...");

        try {
            AuthManager.getInstance().init();
        } catch (Exception e) {
            e.printStackTrace();
        }

        File f = new File(preInitializationEvent.getModConfigurationDirectory(), "loader.cfg");
        Configuration configuration = new Configuration(f);
        // Save config because... well to generate it
        configuration.save();

        while(bar.getStep() < bar.getSteps()) bar.step("");
        ProgressManager.pop(bar);

        ((IReloadableResourceManager) Minecraft.getMinecraft().getResourceManager()).registerReloadListener(a -> {
            if (dgInterface != null) dgInterface.onResourceReload(a);
        });
    }

    public static Main getMain() {
        return main;
    }
}
