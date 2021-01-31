package kr.syeyoung.dungeonsguide.roomprocessor.bombdefuse.chambers.number;

import kr.syeyoung.dungeonsguide.roomprocessor.bombdefuse.RoomProcessorBombDefuseSolver;
import kr.syeyoung.dungeonsguide.roomprocessor.bombdefuse.chambers.BDChamber;
import kr.syeyoung.dungeonsguide.roomprocessor.bombdefuse.chambers.BombDefuseChamberGenerator;
import kr.syeyoung.dungeonsguide.roomprocessor.bombdefuse.chambers.ChamberProcessor;
import net.minecraft.init.Blocks;

public class NumberProcessorMatcher implements BombDefuseChamberGenerator {
    @Override
    public boolean match(BDChamber left, BDChamber right) {
        return left.getBlock(1,1,4).getBlock() == Blocks.stone &&
                left.getBlock(2,1,4).getBlock() == Blocks.stone &&
                left.getBlock(6,1,4).getBlock() == Blocks.stone &&
                left.getBlock(7,1,4).getBlock() == Blocks.stone &&
                right.getBlock(1,1,4).getBlock() == Blocks.stone &&
                right.getBlock(2,1,4).getBlock() == Blocks.stone &&
                right.getBlock(6,1,4).getBlock() == Blocks.stone &&
                right.getBlock(7,1,4).getBlock() == Blocks.stone;
    }

    @Override
    public String getName() {
        return "numberMatch";
    }
    @Override
    public ChamberProcessor createLeft(BDChamber left, RoomProcessorBombDefuseSolver solver) {
        return new NumberLeftProcessor(solver, left);
    }

    @Override
    public ChamberProcessor createRight(BDChamber right, RoomProcessorBombDefuseSolver solver) {
        return new NumberRightProcessor(solver, right);
    }
}
