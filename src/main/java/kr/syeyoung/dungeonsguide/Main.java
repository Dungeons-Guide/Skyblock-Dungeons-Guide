/*
 *     Dungeons Guide - The most intelligent Hypixel Skyblock Dungeons Mod
 *     Copyright (C) 2021  cyoung06
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Affero General Public License as published
 *     by the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Affero General Public License for more details.
 *
 *     You should have received a copy of the GNU Affero General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package kr.syeyoung.dungeonsguide;

import com.google.common.base.Throwables;
import kr.syeyoung.dungeonsguide.auth.AuthManager;
import kr.syeyoung.dungeonsguide.auth.InvalidDungeonsGuideCredentialsException;
import kr.syeyoung.dungeonsguide.auth.ResourceManager;
import kr.syeyoung.dungeonsguide.url.DGStreamHandlerFactory;
import lombok.Getter;
import net.minecraft.client.gui.*;
import net.minecraft.launchwrapper.LaunchClassLoader;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.ProgressManager;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.net.URL;

@Mod(modid = Main.MOD_ID, version = Main.VERSION)
public class Main {
    public static final String MOD_ID = "skyblock_dungeons_guide";
    public static final String VERSION = "1.0";


    private boolean isLoaded = false;
    private Throwable cause;
    private String stacktrace;
    private boolean showedError = false;

    Logger logger = LogManager.getLogger("DG-main");
    @Getter
    private static boolean offlineMode = false;


    @EventHandler
    public void initEvent(final FMLInitializationEvent initializationEvent) {
        MinecraftForge.EVENT_BUS.register(this);
        try {
            logger.info("init-ing DungeonsGuide");
            DungeonsGuide.getDungeonsGuide().init();
        } catch (Exception e) {
            cause = e;
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            PrintStream printStream = new PrintStream(byteArrayOutputStream);
            e.printStackTrace(printStream);
            stacktrace = byteArrayOutputStream.toString();

            e.printStackTrace();
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onGuiOpen(GuiOpenEvent guiOpenEvent) {
        if (!showedError && !isLoaded && guiOpenEvent.gui instanceof GuiMainMenu) {
            guiOpenEvent.gui = new GuiLoadingError(cause, stacktrace, guiOpenEvent.gui);
            showedError = true;
        }
    }

    ProgressManager.ProgressBar progressBar;

    public static final String SERVER_URL = "https://dungeons.guide";

    public static final String SOME_FUNNY_KEY_THING = "MIICIjANBgkqhkiG9w0BAQEFAAOCAg8AMIICCgKCAgEAxO89qtwG67jNucQ9Y44c" +
            "IUs/B+5BeJPs7G+RG2gfs4/2+tzF/c1FLDc33M7yKw8aKk99vsBUY9Oo8gxxiEPB" +
            "JitP/qfon2THp94oM77ZTpHlmFoqbZMcKGZVI8yfvEL4laTM8Hw+qh5poQwtpEbK" +
            "Xo47AkxygxJasUnykER2+aSTZ6kWU2D4xiNtFA6lzqN+/oA+NaYfPS0amAvyVlHR" +
            "n/8IuGkxb5RrlqVssQstFnxsJuv88qdGSEqlcKq2tLeg9hb8eCnl2OFzvXmgbVER" +
            "0JaV+4Z02fVG1IlR3Xo1mSit7yIU6++3usRCjx2yfXpnGGJUW5pe6YETjNew3ax+" +
            "FAZ4GePWCdmS7FvBnbbABKo5pE06ZTfDUTCjQlAJQiUgoF6ntMJvQAXPu48Vr8q/" +
            "mTcuZWVnI6CDgyE7nNq3WNoq3397sBzxRohMxuqzl3T19zkfPKF05iV2Ju1HQMW5" +
            "I119bYrmVD240aGESZc20Sx/9g1BFpNzQbM5PGUlWJ0dhLjl2ge4ip2hHciY3OEY" +
            "p2Qy2k+xEdenpKdL+WMRimCQoO9gWe2Tp4NmP5dppDXZgPjXqjZpnGs0Uxs+fXqW" +
            "cwlg3MbX3rFl9so/fhVf4p9oXZK3ve7z5D6XSSDRYECvsKIa08WAxJ/U6n204E/4" +
            "xUF+3ZgFPdzZGn2PU7SsnOsCAwEAAQ==";

    @EventHandler
    public void preInit(final FMLPreInitializationEvent preInitializationEvent) {
        try {
            try (InputStream premiumControlClass = this.getClass().getResourceAsStream("/kr/syeyoung/dungeonsguide/e.class")) {
                progressBar = ProgressManager.push("DungeonsGuide", premiumControlClass == null ? 7 : 6);
            }



            AuthManager.getInstance().setBaseserverurl(SERVER_URL);


            String version = null;
            try (InputStream resourceAsStream = this.getClass().getResourceAsStream("/kr/syeyoung/dungeonsguide/DungeonsGuide.class")) {
                if (resourceAsStream == null) {
                    if (System.getProperty("dg.version") == null) {
                        version = "nlatest";
                    } else {
                        version = System.getProperty("dg.version");
                    }
                }
            }


            AuthManager.getInstance().init();


            ResourceManager.getInstance().setBaseUrl(SERVER_URL);
            ResourceManager.getInstance().setBASE64_X509ENCODEDKEYSPEC(SOME_FUNNY_KEY_THING);

            if(!AuthManager.getInstance().isPlebUser()){
                try {
                    ResourceManager.getInstance().downloadAssets(version);
                } catch (InvalidDungeonsGuideCredentialsException e) {
                    logger.error("Downloading assets failed with {}", String.valueOf(Throwables.getRootCause(e)));
                }
            }

            URL.setURLStreamHandlerFactory(new DGStreamHandlerFactory());
            LaunchClassLoader classLoader = (LaunchClassLoader) Main.class.getClassLoader();
            classLoader.addURL(new URL("z:///"));

            progressBar.step("Initializing");
            DungeonsGuide.getDungeonsGuide().pre(preInitializationEvent);
            finishUpProgressBar(progressBar);
            isLoaded = true;

        } catch (IOException e) {
            handleException(e);
        }
    }

    public void handleException(Throwable e) {
        cause = e;
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        PrintStream printStream = new PrintStream(byteArrayOutputStream);
        e.printStackTrace(printStream);
        stacktrace = byteArrayOutputStream.toString();

        finishUpProgressBar(progressBar);


        e.printStackTrace();
    }

    void finishUpProgressBar(ProgressManager.ProgressBar progressBar) {
        if(progressBar == null) return;
        while (progressBar.getStep() < progressBar.getSteps())
            progressBar.step("random-" + progressBar.getStep());
        ProgressManager.pop(progressBar);
    }
}
