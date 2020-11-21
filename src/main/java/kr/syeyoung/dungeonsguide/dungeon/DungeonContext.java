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

    private byte[] lastMapData;

    public DungeonContext(World world) {
        this.world = world;
    }


    public void tick() {
        mapTick();
    }

    private void buildMap(byte[] mapData) {

    }
    private void processMap(byte[] mapData) {

    }

    private void mapTick() {
        ItemStack stack = Minecraft.getMinecraft().thePlayer.inventory.getStackInSlot(8);
        byte[] mapData;
        if (stack == null || !(stack.getItem() instanceof ItemMap)) {
            mapData = lastMapData;
        } else {
            MapData mapData1 = ((ItemMap)stack.getItem()).getMapData(stack, world);
            mapData = mapData1.colors;
        }

        if (lastMapData == null && mapData != null) buildMap(mapData);
        processMap(mapData);

        lastMapData = mapData;
    }
}
