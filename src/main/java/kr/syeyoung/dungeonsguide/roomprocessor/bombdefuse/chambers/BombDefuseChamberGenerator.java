package kr.syeyoung.dungeonsguide.roomprocessor.bombdefuse.chambers;

import kr.syeyoung.dungeonsguide.roomprocessor.bombdefuse.RoomProcessorBombDefuseSolver;

public interface BombDefuseChamberGenerator {
    public boolean match(BDChamber left, BDChamber right);

    public String getName();

    public ChamberProcessor createLeft(BDChamber left, RoomProcessorBombDefuseSolver solver);
    public ChamberProcessor createRight(BDChamber right, RoomProcessorBombDefuseSolver solver);
}