package kr.syeyoung.dungeonsguide;

import com.mojang.authlib.exceptions.AuthenticationException;
import kr.syeyoung.dungeonsguide.commands.*;
import kr.syeyoung.dungeonsguide.dungeon.roomfinder.DungeonRoomInfoRegistry;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiErrorScreen;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.launchwrapper.LaunchClassLoader;
import net.minecraft.util.IChatComponent;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.CustomModLoadingErrorDisplayException;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.apache.commons.io.IOUtils;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.*;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

@Mod(modid = DungeonsGuide.MODID, version = DungeonsGuide.VERSION)
public class DungeonsGuide
{
    public static final String MODID = "skyblock_dungeons_guide";
    public static final String VERSION = "0.1";

    private Object skyblockStatus;
    
    private static DungeonsGuide dungeonsGuide;

    public static boolean DEBUG = false;

    public static void sendDebugChat(IChatComponent iChatComponent) {
        if (DEBUG)
            Minecraft.getMinecraft().thePlayer.addChatMessage(iChatComponent);
    }

    @EventHandler
    public void init(FMLInitializationEvent event)
    {
        Thread.currentThread().setContextClassLoader(classLoader);

        Class skyblockStatusCls = null;
        try {
            skyblockStatusCls = classLoader.findClass("kr.syeyoung.dungeonsguide.SkyblockStatus");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();

            error(new String[]{
                    "Couldn't load Dungeons Guide",
                    "Please contact developer if this problem persists after restart"
            });
            return;
        }

        dungeonsGuide = this;
        try {
            skyblockStatus = skyblockStatusCls.newInstance();
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
    }

    @Getter
    private File configDir;

    @Getter
    private Authenticator authenticator;
    private NetworkClassLoader classLoader;
    @EventHandler
    public void pre(FMLPreInitializationEvent event) {
        System.out.println(DungeonsGuide.class.getClassLoader());
        configDir = new File(event.getModConfigurationDirectory(),"dungeonsguide");

        authenticator = new Authenticator();
        String token = null;
        try {
            token = authenticator.authenticate();
            System.out.println(token);
            if (token != null) {
                classLoader = new NetworkClassLoader(authenticator, DungeonsGuide.class.getClassLoader());
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
    public Object getSkyblockStatus() {
        return (SkyblockStatus) skyblockStatus;
    }

    public static DungeonsGuide getDungeonsGuide() {
        return dungeonsGuide;
    }
}
