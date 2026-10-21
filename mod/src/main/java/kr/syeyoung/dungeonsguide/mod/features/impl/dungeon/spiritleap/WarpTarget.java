package kr.syeyoung.dungeonsguide.mod.features.impl.dungeon.spiritleap;

import lombok.AllArgsConstructor;
import lombok.Data;
import net.minecraft.world.item.ItemStack;

@Data @AllArgsConstructor
public class WarpTarget {
    private ItemStack itemStack;
    private int slotId;
}
