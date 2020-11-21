package kr.syeyoung.dungeonsguide;

import net.minecraft.init.Blocks;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;

@Mod(modid = DungeonsGuide.MODID, version = DungeonsGuide.VERSION)
public class DungeonsGuide
{
    public static final String MODID = "skyblock_dungeons_guide";
    public static final String VERSION = "0.1";

    private SkyblockStatus skyblockStatus;
    
    private static DungeonsGuide dungeonsGuide;

    @EventHandler
    public void init(FMLInitializationEvent event)
    {
        dungeonsGuide = this;
        skyblockStatus = new SkyblockStatus();
        MinecraftForge.EVENT_BUS.register(new EventListener());
    }

    public SkyblockStatus getSkyblockStatus() {
        return skyblockStatus;
    }

    public static DungeonsGuide getDungeonsGuide() {
        return dungeonsGuide;
    }
}
