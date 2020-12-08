package kr.syeyoung.dungeonsguide;

import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

public interface DungeonsGuideInterface {
    public void init(FMLInitializationEvent event);

    public void pre(FMLPreInitializationEvent event);
}
