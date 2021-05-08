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
import kr.syeyoung.dungeonsguide.url.DGStreamHandlerFactory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiErrorScreen;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.launchwrapper.LaunchClassLoader;
import net.minecraftforge.fml.client.CustomModLoadingErrorDisplayException;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.ProgressManager;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

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

    @EventHandler
    public void initEvent(FMLInitializationEvent initializationEvent)
    {

        main = this;
        dgInterface.init(initializationEvent);
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
                } catch (Exception e) {
                    e.printStackTrace();

                    throwError(new String[]{
                            "Couldn't load Dungeons Guide",
                            "Please contact developer if this problem persists after restart"
                    });
                }
                return;
            }
        } catch (IOException  | AuthenticationException | NoSuchAlgorithmException | CertificateException | KeyStoreException | KeyManagementException | InvalidKeySpecException | SignatureException e) {
            e.printStackTrace();
        }

        throwError(new String[]{
                "Can't validate current installation of Dungeons Guide",
                "Steps to fix",
                "1. check if other people can't join minecraft servers.",
                "2. restart minecraft launcher",
                "3. make sure you're on the right account",
                "4. restart your computer",
                "If the problem persists after following these steps, please contact developer",
                "If you haven't purchased the mod, please consider doing so"
        });
    }

    public void throwError(final String[] a) {
        final GuiScreen b = new GuiErrorScreen(null, null) {
            @Override
            public void drawScreen(int par1, int par2, float par3) {
                super.drawScreen(par1, par2, par3);
                for (int i = 0; i < a.length; ++i) {
                    drawCenteredString(fontRendererObj, a[i], width / 2, height / 3 + 12 * i, 0xFFFFFFFF);
                }
            }

            @Override
            public void initGui() {
                super.initGui();
                this.buttonList.clear();
                this.buttonList.add(new GuiButton(0, width / 2 - 50, height - 50, 100,20, "close"));
            }

            @Override
            protected void actionPerformed(GuiButton button) throws IOException {
                System.exit(-1);
            }
        };
        @SuppressWarnings("serial") CustomModLoadingErrorDisplayException e = new CustomModLoadingErrorDisplayException() {

            @Override
            public void initGui(GuiErrorScreen errorScreen, FontRenderer fontRenderer) {
                Minecraft.getMinecraft().displayGuiScreen(b);
            }

            @Override
            public void drawScreen(GuiErrorScreen errorScreen, FontRenderer fontRenderer, int mouseRelX, int mouseRelY, float tickTime) {
            }
        };
        throw e;
    }
    public static Main a() {
        return main;
    }
}
