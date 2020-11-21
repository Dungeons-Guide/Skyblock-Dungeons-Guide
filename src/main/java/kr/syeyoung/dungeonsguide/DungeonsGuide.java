package kr.syeyoung.dungeonsguide;

import net.minecraft.init.Blocks;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;

@Mod(modid = DungeonsGuide.MODID, version = DungeonsGuide.VERSION)
public class DungeonsGuide
{
    public static final String MODID = "skyblock_dungeons_guide";
    public static final String VERSION = "0.1";
    
    @EventHandler
    public void init(FMLInitializationEvent event)
    {
		System.out.println("DIRT BLOCK >> "+Blocks.dirt.getUnlocalizedName());

    }
}
