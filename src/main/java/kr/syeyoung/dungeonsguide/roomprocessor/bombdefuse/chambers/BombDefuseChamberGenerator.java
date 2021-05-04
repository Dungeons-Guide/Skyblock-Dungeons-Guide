package kr.syeyoung.dungeonsguide.roomprocessor.bombdefuse.chambers;

import kr.syeyoung.dungeonsguide.roomprocessor.bombdefuse.RoomProcessorBombDefuseSolver;

public interface BombDefuseChamberGenerator {
    boolean match(BDChamber left, BDChamber right);

    String getName();

    ChamberProcessor createLeft(BDChamber left, RoomProcessorBombDefuseSolver solver);
    ChamberProcessor createRight(BDChamber right, RoomProcessorBombDefuseSolver solver);
}