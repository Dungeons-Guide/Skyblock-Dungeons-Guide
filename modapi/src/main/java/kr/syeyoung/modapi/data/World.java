package kr.syeyoung.modapi.data;

import java.util.List;

public interface World {
    public BlockState getBlockState(VectorI3D position);
    public String getBlockId(VectorI3D position);
    public int getBlockData(VectorI3D position);
    public TileEntityData getTileEntity(VectorI3D position);


    public List<Entity> getEntities();
}
