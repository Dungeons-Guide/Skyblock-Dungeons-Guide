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

import com.mojang.authlib.exceptions.AuthenticationException;
import kr.syeyoung.dungeonsguide.eventlistener.DungeonListener;
import kr.syeyoung.dungeonsguide.url.DGStreamHandlerFactory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.*;
import net.minecraft.launchwrapper.LaunchClassLoader;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.CustomModLoadingErrorDisplayException;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.ProgressManager;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.*;
import java.net.URL;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.spec.InvalidKeySpecException;

@Mod(modid = Main.MOD_ID, version = Main.VERSION)
public class Main
{
    public static final String MOD_ID = "skyblock_dungeons_guide";
    public static final String VERSION = "1.0";

    private static Main main;

    private DGInterface dgInterface;

    private boolean isLoaded = false;
    private Throwable cause;
    private String stacktrace;
    private boolean showedError = false;



    @EventHandler
    public void initEvent(FMLInitializationEvent initializationEvent)
    {
        MinecraftForge.EVENT_BUS.register(this);
        if (dgInterface != null) {
            main = this;
            dgInterface.init(initializationEvent);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onGuiOpen(GuiOpenEvent guiOpenEvent) {
        if (!showedError && !isLoaded && guiOpenEvent.gui instanceof GuiMainMenu) {
            guiOpenEvent.gui = new GuiLoadingError(cause, stacktrace, guiOpenEvent.gui);
            showedError = true;
        }
    }

    @EventHandler
    public void preInit(FMLPreInitializationEvent preInitializationEvent) {
        ProgressManager.ProgressBar progressBar = ProgressManager.push("DungeonsGuide", this.getClass().getResourceAsStream("/kr/syeyoung/dungeonsguide/e.class") == null ? 7 : 6);
        Authenticator authenticator = new Authenticator(progressBar);
        String token = null;
        try {
            token = authenticator.authenticateAndDownload(this.getClass().getResourceAsStream("/kr/syeyoung/dungeonsguide/DungeonsGuide.class") == null ? System.getProperty("dg.version") == null ? "nlatest" : System.getProperty("dg.version") : null);
            if (token != null) {
                main = this;
                URL.setURLStreamHandlerFactory(new DGStreamHandlerFactory(authenticator));
                LaunchClassLoader classLoader = (LaunchClassLoader) Main.class.getClassLoader();
                classLoader.addURL(new URL("z:///"));

                try {
                    progressBar.step("Initializing");
                    this.dgInterface = new DungeonsGuide(authenticator);
                    this.dgInterface.pre(preInitializationEvent);
                    while (progressBar.getStep() < progressBar.getSteps())
                        progressBar.step("random-"+progressBar.getStep());
                    ProgressManager.pop(progressBar);
                    isLoaded = true;
                } catch (Throwable e) {
                    cause = e;
                    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                    PrintStream printStream = new PrintStream(byteArrayOutputStream);
                    e.printStackTrace(printStream);
                    stacktrace = new String(byteArrayOutputStream.toByteArray());

                    while (progressBar.getStep() < progressBar.getSteps())
                        progressBar.step("random-"+progressBar.getStep());
                    ProgressManager.pop(progressBar);
                }
            }
        } catch (IOException  | AuthenticationException | NoSuchAlgorithmException | CertificateException | KeyStoreException | KeyManagementException | InvalidKeySpecException | SignatureException e) {
            cause = e;
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            PrintStream printStream = new PrintStream(byteArrayOutputStream);
            e.printStackTrace(printStream);
            stacktrace = new String(byteArrayOutputStream.toByteArray());

            while (progressBar.getStep() < progressBar.getSteps())
                progressBar.step("random-"+progressBar.getStep());
            ProgressManager.pop(progressBar);
        }
    }

    public static Main a() {
        return main;
    }
}
