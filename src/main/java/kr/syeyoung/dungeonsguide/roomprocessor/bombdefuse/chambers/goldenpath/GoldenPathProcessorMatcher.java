package kr.syeyoung.dungeonsguide.roomprocessor.bombdefuse.chambers.goldenpath;

import kr.syeyoung.dungeonsguide.roomprocessor.bombdefuse.RoomProcessorBombDefuseSolver;
import kr.syeyoung.dungeonsguide.roomprocessor.bombdefuse.chambers.BDChamber;
import kr.syeyoung.dungeonsguide.roomprocessor.bombdefuse.chambers.BombDefuseChamberGenerator;
import kr.syeyoung.dungeonsguide.roomprocessor.bombdefuse.chambers.ChamberProcessor;
import kr.syeyoung.dungeonsguide.roomprocessor.bombdefuse.chambers.creeper.CreeperLeftProcessor;
import kr.syeyoung.dungeonsguide.roomprocessor.bombdefuse.chambers.creeper.CreeperRightProcessor;
import net.minecraft.init.Blocks;

public class GoldenPathProcessorMatcher implements BombDefuseChamberGenerator {
    @Override
    public boolean match(BDChamber left, BDChamber right) {
        return left.getBlock(4,0,0).getBlock() == Blocks.hardened_clay
                || left.getBlock(4,0,0).getBlock() == Blocks.stained_hardened_clay;
    }

    @Override
    public String getName() {
        return "goldPath";
    }
    @Override
    public ChamberProcessor createLeft(BDChamber left, RoomProcessorBombDefuseSolver solver) {
        return new GoldenPathLeftProcessor(solver, left);
    }

    @Override
    public ChamberProcessor createRight(BDChamber right, RoomProcessorBombDefuseSolver solver) {
        return new GoldenPathRightProcessor(solver, right);
    }
}
