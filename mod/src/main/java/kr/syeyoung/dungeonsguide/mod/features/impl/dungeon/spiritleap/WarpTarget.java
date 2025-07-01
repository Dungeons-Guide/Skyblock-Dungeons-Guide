package kr.syeyoung.dungeonsguide.mod.features.impl.dungeon.spiritleap;

import kr.syeyoung.modapi.item.UItemStack;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data @AllArgsConstructor
public class WarpTarget {
    private UItemStack itemStack;
    private int slotId;
}
