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

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.*;
import java.net.URL;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
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

    @EventHandler
    public void pre(FMLPreInitializationEvent event) {
        Authenticator authenticator = new Authenticator();
        String token = null;
        try {
            token = authenticator.authenticate();
            if (token != null) {
                dungeonsGuideMain = this;
                URL.setURLStreamHandlerFactory(new DGURLStreamHandlerFactory(authenticator));
                LaunchClassLoader launchClassLoader = (LaunchClassLoader) DungeonsGuideMain.class.getClassLoader();
                launchClassLoader.addURL(new URL("dungeonsguide:///"));

                try {
                    dungeonsGuideInterface = new DungeonsGuide(authenticator);
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
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (InvalidAlgorithmParameterException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        }

        error(new String[]{
                "Can't validate current installation of Dungeons Guide",
                "Steps to fix",
                "1. check if other people can't join minecraft servers. If they can't it's impossible to validate",
                "2. restart minecraft launcher",
                "3. make sure you're on the right account",
                "4. restart your computer",
                "If the problem persists after following these steps, please contact developer",
                "If you haven't purchased the mod, please consider doing so"
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
