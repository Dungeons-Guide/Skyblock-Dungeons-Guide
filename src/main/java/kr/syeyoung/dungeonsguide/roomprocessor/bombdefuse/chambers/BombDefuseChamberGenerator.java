package kr.syeyoung.dungeonsguide.roomprocessor.bombdefuse.chambers;

public interface BombDefuseChamberGenerator {
    public boolean match(BDChamber left, BDChamber right);

    public String getName();

    public ChamberProcessor createLeft(BDChamber left);
    public ChamberProcessor createRight(BDChamber right);
}