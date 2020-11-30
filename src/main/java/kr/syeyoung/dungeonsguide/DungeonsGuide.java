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
import org.apache.commons.io.IOUtils;

import java.io.*;

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
        DungeonRoomInfoRegistry.loadAll(configDir);

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

    @EventHandler
    public void pre(FMLPreInitializationEvent event) {
        configDir = new File(event.getModConfigurationDirectory(),"dungeonsguide");
    }

    public SkyblockStatus getSkyblockStatus() {
        return skyblockStatus;
    }

    public static DungeonsGuide getDungeonsGuide() {
        return dungeonsGuide;
    }
}
