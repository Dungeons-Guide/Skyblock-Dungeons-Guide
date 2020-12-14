package kr.syeyoung.dungeonsguide;

import kr.syeyoung.dungeonsguide.commands.*;
import kr.syeyoung.dungeonsguide.dungeon.roomfinder.DungeonRoomInfoRegistry;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.util.IChatComponent;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.common.MinecraftForge;
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

public class e implements c {

    private SkyblockStatus skyblockStatus;

    private static e dungeonsGuide;

    public static final boolean DEBUG = true;

    @Getter
    private b authenticator;
    public e(b authenticator) {
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
        ClientCommandHandler.instance.registerCommand(new CommandLoadData());
        ClientCommandHandler.instance.registerCommand(new CommandSaveData());

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
        configDir = new File(event.getModConfigurationDirectory(),"z");
    }

    @Getter
    private File configDir;


    public SkyblockStatus getSkyblockStatus() {
        return (SkyblockStatus) skyblockStatus;
    }

    public static e getDungeonsGuide() {
        return dungeonsGuide;
    }
}
