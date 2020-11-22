package kr.syeyoung.dungeonsguide.dungeon;

import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.init.Items;
import net.minecraft.item.ItemMap;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraft.world.storage.MapData;

public class DungeonContext {
    @Getter
    private World world;
    @Getter
    private MapProcessor mapProcessor;

    public DungeonContext(World world) {
        this.world = world;
        mapProcessor = new MapProcessor(this);
    }


    public void tick() {
        mapProcessor.tick();
    }

}
