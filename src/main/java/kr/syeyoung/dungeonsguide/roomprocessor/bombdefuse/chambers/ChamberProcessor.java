package kr.syeyoung.dungeonsguide.roomprocessor.bombdefuse.chambers;

import kr.syeyoung.dungeonsguide.roomprocessor.RoomProcessor;
import net.minecraft.nbt.NBTTagCompound;

public interface ChamberProcessor extends RoomProcessor {
    public void onDataRecieve(NBTTagCompound compound);
    public String getName();
}
