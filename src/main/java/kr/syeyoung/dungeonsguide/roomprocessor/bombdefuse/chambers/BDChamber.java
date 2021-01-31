package kr.syeyoung.dungeonsguide.roomprocessor.bombdefuse.chambers;

import kr.syeyoung.dungeonsguide.dungeon.data.OffsetPoint;
import kr.syeyoung.dungeonsguide.dungeon.data.OffsetPointSet;
import kr.syeyoung.dungeonsguide.dungeon.roomfinder.DungeonRoom;
import lombok.AllArgsConstructor;
import lombok.Data;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockPos;

@AllArgsConstructor
@Data
public class BDChamber {
    private DungeonRoom room;
    private OffsetPointSet chamberBlocks;

    private boolean isLeft;
    private int level;

    private ChamberProcessor processor;

    // for whatever's sake, 6: z, 9: x. Starts from botoom right, left, then up

    public OffsetPoint getOffsetPoint(int x, int z) {
        return chamberBlocks.getOffsetPointList().get(z * 9 + x);
    }

    public BlockPos getBlockPos(int x, int y, int z) {
        return getOffsetPoint(x,z).getBlockPos(room).add(0,y,0);
    }

    public IBlockState getBlock(int x, int y, int z) {
        BlockPos pos = getBlockPos(x,y,z);
        return room.getContext().getWorld().getBlockState(pos);
    }

    public boolean isWithinAbsolute(int x, int y, int z) {
        return isWithinAbsolute(new BlockPos(x,y,z));
    }
    public boolean isWithinAbsolute(BlockPos pos) {
        return chamberBlocks.getOffsetPointList().contains(new OffsetPoint(room, pos));
    }
}
