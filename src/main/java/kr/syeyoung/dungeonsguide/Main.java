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

import kr.syeyoung.dungeonsguide.auth.AuthManager;
import kr.syeyoung.dungeonsguide.mod.DungeonsGuide;
import lombok.Getter;
import net.minecraft.client.gui.GuiMainMenu;
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
import org.jetbrains.annotations.NotNull;

import java.io.*;

@Mod(modid = Main.MOD_ID, version = Main.VERSION)
public class Main {

    public static final String MOD_ID = "skyblock_dungeons_guide";
    public static final String VERSION = "3.8.0";
    Logger logger = LogManager.getLogger("DG-main");


    IDungeonGuide dgInstance;

    private boolean isLoaded = false;


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
    public void initEvent(final FMLInitializationEvent initializationEvent) {
        MinecraftForge.EVENT_BUS.register(this);
        try {
            logger.info("init-ing DungeonsGuide");
            dgInstance.init();
        } catch (Exception e) {
            handleException(e, null);
        }
    }



    private boolean showedError = false;
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onGuiOpen(GuiOpenEvent guiOpenEvent) {
        if (!showedError && !isLoaded && guiOpenEvent.gui instanceof GuiMainMenu) {
            guiOpenEvent.gui = new GuiLoadingError(guiOpenEvent.gui);
            showedError = true;
        }

    }


    @Getter
    static File configDir;

    @EventHandler
    public void preInit(final FMLPreInitializationEvent preInitializationEvent) {
        MinecraftForge.EVENT_BUS.register(new YoMamaOutdated());

        ProgressManager.ProgressBar progressBar = null;

        try {
            try (InputStream premiumControlClass = this.getClass().getResourceAsStream("/kr/syeyoung/dungeonsguide/e.class")) {
                progressBar = ProgressManager.push("DungeonsGuide", premiumControlClass == null ? 7 : 6);
            }

            AuthManager.getInstance().setBaseserverurl(SERVER_URL);

            AuthManager.getInstance().init();

            dgInstance = new DungeonsGuide();

            configDir = new File(preInitializationEvent.getModConfigurationDirectory(), "dungeonsguide");


            dgInstance.preinit();

            progressBar.step("Initializing");

            finishUpProgressBar(progressBar);
            isLoaded = true;

        } catch (IOException e) {
            handleException(e, progressBar);
        }
    }



    public void handleException(@NotNull final Throwable e, ProgressManager.ProgressBar progressBar) {
        GuiLoadingError.cause = e;

        finishUpProgressBar(progressBar);

        e.printStackTrace();
    }

    public static void finishUpProgressBar(final ProgressManager.ProgressBar progressBar) {
        if(progressBar == null) return;
        while (progressBar.getStep() < progressBar.getSteps())
            progressBar.step("random-" + progressBar.getStep());
        ProgressManager.pop(progressBar);
    }
}
