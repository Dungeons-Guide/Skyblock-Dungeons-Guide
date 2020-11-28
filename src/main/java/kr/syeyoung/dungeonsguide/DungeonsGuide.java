package kr.syeyoung.dungeonsguide;

import kr.syeyoung.dungeonsguide.commands.CommandEditRoom;
import kr.syeyoung.dungeonsguide.commands.CommandLoadData;
import kr.syeyoung.dungeonsguide.commands.CommandSaveData;
import kr.syeyoung.dungeonsguide.commands.CommandToggleDebug;
import kr.syeyoung.dungeonsguide.dungeon.roomfinder.DungeonRoomInfoRegistry;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.command.CommandDebug;
import net.minecraft.init.Blocks;
import net.minecraft.util.IChatComponent;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

import java.io.File;

@Mod(modid = DungeonsGuide.MODID, version = DungeonsGuide.VERSION)
public class DungeonsGuide
{
    public static final String MODID = "skyblock_dungeons_guide";
    public static final String VERSION = "0.1";

    private SkyblockStatus skyblockStatus;
    
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

        //noinspection ResultOfMethodCallIgnored
        configDir.mkdirs();
        DungeonRoomInfoRegistry.loadAll(configDir);

        Keybinds.register();
    }

    @Getter
    private File configDir;

    @EventHandler
    public void pre(FMLPreInitializationEvent event) {
        configDir = new File(event.getModConfigurationDirectory(),"pog");
    }

    public SkyblockStatus getSkyblockStatus() {
        return skyblockStatus;
    }

    public static DungeonsGuide getDungeonsGuide() {
        return dungeonsGuide;
    }
}
