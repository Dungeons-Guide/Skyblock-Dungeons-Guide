package kr.syeyoung.dungeonsguide;

import com.mojang.authlib.exceptions.AuthenticationException;
import kr.syeyoung.dungeonsguide.commands.*;
import kr.syeyoung.dungeonsguide.dungeon.roomfinder.DungeonRoomInfoRegistry;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiErrorScreen;
import net.minecraft.client.gui.GuiScreen;
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
        dungeonsGuide = this;
        skyblockStatus = new SkyblockStatus();
        MinecraftForge.EVENT_BUS.register(new EventListener());
        CommandEditRoom cc = new CommandEditRoom();
        ClientCommandHandler.instance.registerCommand(cc);
        MinecraftForge.EVENT_BUS.register(cc);
        ClientCommandHandler.instance.registerCommand(new CommandLoadData());
        ClientCommandHandler.instance.registerCommand(new CommandSaveData());
        ClientCommandHandler.instance.registerCommand(new CommandToggleDebug());
        ClientCommandHandler.instance.registerCommand(new CommandWhatYearIsIt());

        if (!configDir.exists()) {
            //noinspection ResultOfMethodCallIgnored
            configDir.mkdirs();
            String[] files = {
                    "990f6e4c-f7cf-4d27-ae91-11219b85861f.roomdata",
                    "5000be9d-3081-4a5e-8563-dd826705663a.roomdata",
                    "9139cb1c-b6f3-4bac-92de-909b1eb73449.roomdata",
                    "11982f7f-703e-4d98-9d27-4e07ba3fef71.roomdata",
                    "a053f4fa-d6b2-4aef-ae3e-97c7eee0252e.roomdata",
                    "c2ea0a41-d495-437f-86cc-235a71c49f22.roomdata",
                    "cf6d49d3-4f1e-4ec9-836e-049573793ddd.roomdata",
                    "cf44c95c-950e-49e0-aa4c-82c2b18d0acc.roomdata",
                    "d3e61abf-4198-4520-a950-a03761a0eb6f.roomdata",
                    "ffd5411b-6ff4-4f60-b387-72f00510ec50.roomdata",
                    "b2dce4ed-2bda-4303-a4d7-3ebb914db318.roomdata"
            };
            for (String str:files) {
                try {
                    copy(DungeonsGuide.class.getResourceAsStream("/roomdata/"+str), new File(configDir, str));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        try {
            DungeonRoomInfoRegistry.loadAll();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (InvalidAlgorithmParameterException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        }

        Keybinds.register();
    }

    private void copy(InputStream inputStream, File f) throws IOException {
        FileOutputStream fos = new FileOutputStream(f);
        IOUtils.copy(inputStream, fos);
        fos.flush();
        fos.close();
        inputStream.close();
    }

    @Getter
    private File configDir;

    @Getter
    private Authenticator authenticator;
    private NetworkClassLoader classLoader;
    @EventHandler
    public void pre(FMLPreInitializationEvent event) {
        configDir = new File(event.getModConfigurationDirectory(),"dungeonsguide");

        authenticator = new Authenticator();
        String token = null;
        try {
            token = authenticator.authenticate();
            System.out.println(token);
            if (token != null) {
                classLoader = new NetworkClassLoader(authenticator);
                Thread.currentThread().setContextClassLoader(classLoader);
                return;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }  catch (AuthenticationException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

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
                String[] s_msg = new String[] {
                        "Can't validate current installation of Dungeons Guide",
                        "Please contact mod author if you purchased this mod and getting this error",
                        "And if you haven't purchased the mod, please consider doing so"
                };
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
