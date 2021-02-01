package kr.syeyoung.dungeonsguide.roomprocessor.bombdefuse.chambers.maze;

import kr.syeyoung.dungeonsguide.roomprocessor.bombdefuse.RoomProcessorBombDefuseSolver;
import kr.syeyoung.dungeonsguide.roomprocessor.bombdefuse.chambers.BDChamber;
import kr.syeyoung.dungeonsguide.roomprocessor.bombdefuse.chambers.BombDefuseChamberGenerator;
import kr.syeyoung.dungeonsguide.roomprocessor.bombdefuse.chambers.ChamberProcessor;
import kr.syeyoung.dungeonsguide.roomprocessor.bombdefuse.chambers.goldenpath.GoldenPathLeftProcessor;
import kr.syeyoung.dungeonsguide.roomprocessor.bombdefuse.chambers.goldenpath.GoldenPathRightProcessor;
import net.minecraft.init.Blocks;

public class MazeProcessorMatcher implements BombDefuseChamberGenerator {
    @Override
    public boolean match(BDChamber left, BDChamber right) {
        return right.getBlock(1,0,1).getBlock() != Blocks.double_stone_slab &&
                left.getBlock(1,1,1).getBlock() != Blocks.barrier;
    }

    @Override
    public String getName() {
        return "mazeMatch";
    }
    @Override
    public ChamberProcessor createLeft(BDChamber left, RoomProcessorBombDefuseSolver solver) {
        return new MazeLeftProcessor(solver, left);
    }

    @Override
    public ChamberProcessor createRight(BDChamber right, RoomProcessorBombDefuseSolver solver) {
        return new MazeRightProcessor(solver, right);
    }
}
