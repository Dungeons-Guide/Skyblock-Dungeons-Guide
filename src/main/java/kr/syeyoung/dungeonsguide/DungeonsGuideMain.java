package kr.syeyoung.dungeonsguide;

import com.mojang.authlib.exceptions.AuthenticationException;
import kr.syeyoung.dungeonsguide.customurl.DGURLStreamHandlerFactory;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiErrorScreen;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.launchwrapper.LaunchClassLoader;
import net.minecraftforge.fml.client.CustomModLoadingErrorDisplayException;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

import java.io.*;
import java.net.URL;
import java.security.NoSuchAlgorithmException;

@Mod(modid = DungeonsGuideMain.MODID, version = DungeonsGuideMain.VERSION)
public class DungeonsGuideMain
{
    public static final String MODID = "skyblock_dungeons_guide";
    public static final String VERSION = "0.1";

    private static DungeonsGuideMain dungeonsGuideMain;

    private DungeonsGuideInterface dungeonsGuideInterface;

    @EventHandler
    public void init(FMLInitializationEvent event)
    {

        dungeonsGuideMain = this;

        dungeonsGuideInterface.init(event);
    }

    @Getter
    private Authenticator authenticator;
    @EventHandler
    public void pre(FMLPreInitializationEvent event) {
        authenticator = new Authenticator();
        String token = null;
        try {
            token = authenticator.authenticate();
            if (token != null) {
                URL.setURLStreamHandlerFactory(new DGURLStreamHandlerFactory());
                LaunchClassLoader launchClassLoader = (LaunchClassLoader) DungeonsGuideMain.class.getClassLoader();
                launchClassLoader.addURL(new URL("dungeonsguide://"+token+"@/"));

                try {
                    dungeonsGuideInterface = new DungeonsGuide();
                    dungeonsGuideInterface.pre(event);
                } catch (Exception e) {
                    e.printStackTrace();

                    error(new String[]{
                            "Couldn't load Dungeons Guide",
                            "Please contact developer if this problem persists after restart"
                    });
                }
                return;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }  catch (AuthenticationException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        error(new String[]{
                "Can't validate current installation of Dungeons Guide",
                "Please contact mod author if you purchased this mod and getting this error",
                "And if you haven't purchased the mod, please consider doing so"
        });
    }

    public void error(final String[] s_msg) {
        final GuiScreen errorGui = new GuiErrorScreen(null, null) {

            @Override
            public void handleMouseInput() {
            }

            @Override
            public void handleKeyboardInput() {
            }

            @Override
            public void drawScreen(int par1, int par2, float par3) {
                drawDefaultBackground();
                for (int i = 0; i < s_msg.length; ++i) {
                    drawCenteredString(fontRendererObj, s_msg[i], width / 2, height / 3 + 12 * i, 0xFFFFFFFF);
                }
            }
        };
        @SuppressWarnings("serial") CustomModLoadingErrorDisplayException e = new CustomModLoadingErrorDisplayException() {

            @Override
            public void initGui(GuiErrorScreen errorScreen, FontRenderer fontRenderer) {
                Minecraft.getMinecraft().displayGuiScreen(errorGui);
            }

            @Override
            public void drawScreen(GuiErrorScreen errorScreen, FontRenderer fontRenderer, int mouseRelX, int mouseRelY, float tickTime) {
            }
        };
        throw e;
    }
    public static DungeonsGuideMain getDungeonsGuideMain() {
        return dungeonsGuideMain;
    }
}
