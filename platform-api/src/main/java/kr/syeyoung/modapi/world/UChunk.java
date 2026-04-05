package kr.syeyoung.modapi.world;

public interface UChunk extends IBlockAccessible {
    public int getChunkX();

    public int getChunkZ();

    public UBlockState getRelativeBlockAt(int x, int y, int z);

    boolean isEmpty();

    public int getLenX();
    public int getLenY();
    public int getLenZ();
    public int getMinX();
    public int getMinY();
    public int getMinZ();
}
