package kr.syeyoung.dungeonsguide.roomprocessor.waterpuzzle;

import kr.syeyoung.dungeonsguide.DungeonsGuide;
import kr.syeyoung.dungeonsguide.SkyblockStatus;
import kr.syeyoung.dungeonsguide.dungeon.roomfinder.DungeonRoom;
import kr.syeyoung.dungeonsguide.roomprocessor.waterpuzzle.nodes.WaterNodeStart;
import kr.syeyoung.dungeonsguide.roomprocessor.waterpuzzle.nodes.WaterNodeToggleable;
import lombok.AllArgsConstructor;
import lombok.Data;
import net.minecraft.block.BlockLever;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;

@Data
@AllArgsConstructor
public class SwitchData {
    private WaterBoard waterBoard;

    private BlockPos switchLoc;
    private BlockPos blockLoc;

    private String blockId;

    public boolean getCurrentState(World w) {
        WaterNode waterNode = waterBoard.getToggleableMap().get(blockId);
        if (waterNode instanceof WaterNodeStart)
            return ((WaterNodeStart) waterNode).isTriggered(w);
        else if (waterNode instanceof WaterNodeToggleable)
            return ((WaterNodeToggleable) waterNode).isTriggered(w);
        return false;
    }
}
