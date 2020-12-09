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
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.CustomModLoadingErrorDisplayException;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.File;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

public class DungeonsGuide implements DungeonsGuideInterface {

    private SkyblockStatus skyblockStatus;

    private static DungeonsGuide dungeonsGuide;

    public static boolean DEBUG = false;

    @Getter
    private Authenticator authenticator;
    public DungeonsGuide(Authenticator authenticator) {
        this.authenticator = authenticator;
    }

    public static void sendDebugChat(IChatComponent iChatComponent) {
        if (DEBUG)
            Minecraft.getMinecraft().thePlayer.addChatMessage(iChatComponent);
    }

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
    public void pre(FMLPreInitializationEvent event) {
        configDir = new File(event.getModConfigurationDirectory(),"dungeonsguide");
    }

    @Getter
    private File configDir;


    public Object getSkyblockStatus() {
        return (SkyblockStatus) skyblockStatus;
    }

    public static DungeonsGuide getDungeonsGuide() {
        return dungeonsGuide;
    }
}
