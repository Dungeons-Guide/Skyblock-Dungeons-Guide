package kr.syeyoung.dungeonsguide.dungeon;

import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.world.World;

import java.util.Collection;

public class MortDetector2000 {


    public static Collection<EntityArmorStand> getMorts(World w){
        return w.getEntities(EntityArmorStand.class, input -> input.getName().equals("§bMort"));
    }
}
