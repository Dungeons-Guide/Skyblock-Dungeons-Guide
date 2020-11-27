package kr.syeyoung.dungeonsguide.roomprocessor.waterpuzzle;

import kr.syeyoung.dungeonsguide.DungeonsGuide;
import kr.syeyoung.dungeonsguide.SkyblockStatus;
import kr.syeyoung.dungeonsguide.dungeon.roomfinder.DungeonRoom;
import lombok.AllArgsConstructor;
import lombok.Data;
import net.minecraft.block.BlockLever;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;

@Data
@AllArgsConstructor
public class SwitchData {
    private BlockPos switchLoc;
    private BlockPos blockLoc;

    private String blockId;

    public boolean getCurrentState() {
        BlockPos switch2 = getSwitchLoc();
        World w=  DungeonsGuide.getDungeonsGuide().getSkyblockStatus().getContext().getWorld();
        return w.getBlockState(switch2).getValue(BlockLever.POWERED);
    }
}
