package kr.syeyoung.dungeonsguide.roomprocessor.bombdefuse.chambers.creeper;

import kr.syeyoung.dungeonsguide.roomprocessor.bombdefuse.RoomProcessorBombDefuseSolver;
import kr.syeyoung.dungeonsguide.roomprocessor.bombdefuse.chambers.BDChamber;
import kr.syeyoung.dungeonsguide.roomprocessor.bombdefuse.chambers.BombDefuseChamberGenerator;
import kr.syeyoung.dungeonsguide.roomprocessor.bombdefuse.chambers.ChamberProcessor;
import net.minecraft.init.Blocks;

public class CreeperProcessorMatcher implements BombDefuseChamberGenerator {
    @Override
    public boolean match(BDChamber left, BDChamber right) {
        int airs = 0;
        for (int x = 0; x < 3; x ++) {
            for (int y = 0; y < 3; y++) {
                if (right.getBlock(3 + x, 1, y + 1).getBlock() != Blocks.stone) return false;
            }
        }
        return true;
    }

    @Override
    public String getName() {
        return "creeperMatch";
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
