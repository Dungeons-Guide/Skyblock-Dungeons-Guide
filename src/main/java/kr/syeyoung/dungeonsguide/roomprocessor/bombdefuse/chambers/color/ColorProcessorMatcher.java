package kr.syeyoung.dungeonsguide.roomprocessor.bombdefuse.chambers.color;

import kr.syeyoung.dungeonsguide.roomprocessor.bombdefuse.RoomProcessorBombDefuseSolver;
import kr.syeyoung.dungeonsguide.roomprocessor.bombdefuse.chambers.BDChamber;
import kr.syeyoung.dungeonsguide.roomprocessor.bombdefuse.chambers.BombDefuseChamberGenerator;
import kr.syeyoung.dungeonsguide.roomprocessor.bombdefuse.chambers.ChamberProcessor;
import net.minecraft.init.Blocks;

public class ColorProcessorMatcher implements BombDefuseChamberGenerator {
    @Override
    public boolean match(BDChamber left, BDChamber right) {
        return
                right.getBlock(0,1,1).getBlock() == Blocks.stonebrick &&
                right.getBlock(0,1,2).getBlock() == Blocks.stonebrick &&
                right.getBlock(0,1,3).getBlock() == Blocks.stonebrick;
    }

    @Override
    public String getName() {
        return "colorMatch";
    }
    @Override
    public ChamberProcessor createLeft(BDChamber left, RoomProcessorBombDefuseSolver solver) {
        return new ColorLeftProcessor(solver,left);
    }

    @Override
    public ChamberProcessor createRight(BDChamber right, RoomProcessorBombDefuseSolver solver) {
        return new ColorRightProcessor(solver,right);
    }
}
