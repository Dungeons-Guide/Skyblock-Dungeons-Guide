package kr.syeyoung.dungeonsguide.roomprocessor.bombdefuse.chambers.arrow;

import kr.syeyoung.dungeonsguide.roomprocessor.bombdefuse.RoomProcessorBombDefuseSolver;
import kr.syeyoung.dungeonsguide.roomprocessor.bombdefuse.chambers.BDChamber;
import kr.syeyoung.dungeonsguide.roomprocessor.bombdefuse.chambers.BombDefuseChamberGenerator;
import kr.syeyoung.dungeonsguide.roomprocessor.bombdefuse.chambers.ChamberProcessor;
import net.minecraft.init.Blocks;

public class ArrowProcessorMatcher implements BombDefuseChamberGenerator {
    @Override
    public boolean match(BDChamber left, BDChamber right) {
        return left.getBlock(8,1,1).getBlock() == Blocks.planks &&
                left.getBlock(8,1,2).getBlock() == Blocks.planks &&
                left.getBlock(8,1,3).getBlock() == Blocks.planks &&
                right.getBlock(0,1,1).getBlock() == Blocks.planks &&
                right.getBlock(0,1,2).getBlock() == Blocks.planks &&
                right.getBlock(0,1,3).getBlock() == Blocks.planks;
    }

    @Override
    public String getName() {
        return "arrowMatch";
    }

    @Override
    public ChamberProcessor createLeft(BDChamber left, RoomProcessorBombDefuseSolver solver) {
        return null;
    }

    @Override
    public ChamberProcessor createRight(BDChamber right, RoomProcessorBombDefuseSolver solver) {
        return null;
    }
}
