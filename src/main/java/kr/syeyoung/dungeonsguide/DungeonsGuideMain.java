package kr.syeyoung.dungeonsguide;

import com.mojang.authlib.exceptions.AuthenticationException;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiErrorScreen;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.IChatComponent;
import net.minecraftforge.fml.client.CustomModLoadingErrorDisplayException;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

import java.io.*;
import java.security.NoSuchAlgorithmException;

@Mod(modid = DungeonsGuideMain.MODID, version = DungeonsGuideMain.VERSION)
public class DungeonsGuideMain
{
    public static final String MODID = "skyblock_dungeons_guide";
    public static final String VERSION = "0.1";

    private static DungeonsGuideMain DungeonsGuideMain;

    private DungeonsGuideInterface dungeonsGuideInterface;

    @EventHandler
    public void init(FMLInitializationEvent event)
    {

        DungeonsGuideMain = this;

        dungeonsGuideInterface.init(event);
    }

    @Getter
    private Authenticator authenticator;
    private NetworkClassLoader classLoader;
    @EventHandler
    public void pre(FMLPreInitializationEvent event) {
        authenticator = new Authenticator();
        String token = null;
        try {
            token = authenticator.authenticate();
            if (token != null) {
                classLoader = new NetworkClassLoader(authenticator, DungeonsGuideMain.class.getClassLoader());

                Class skyblockStatusCls = null;
                try {
                    skyblockStatusCls = classLoader.findClass("kr.syeyoung.dungeonsguide.DungeonsGuide");
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();

                    error(new String[]{
                            "Couldn't load Dungeons Guide",
                            "Please contact developer if this problem persists after restart"
                    });
                    return;
                }
                try {
                    dungeonsGuideInterface = (DungeonsGuideInterface) skyblockStatusCls.newInstance();
                    dungeonsGuideInterface.pre(event);
                } catch (InstantiationException e) {
                    e.printStackTrace();

                    error(new String[]{
                            "Couldn't load Dungeons Guide",
                            "Please contact developer if this problem persists after restart"
                    });
                } catch (IllegalAccessException e) {
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
        return DungeonsGuideMain;
    }
}
