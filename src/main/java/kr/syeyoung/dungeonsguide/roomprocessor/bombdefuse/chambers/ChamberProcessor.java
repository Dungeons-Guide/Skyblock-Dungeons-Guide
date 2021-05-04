package kr.syeyoung.dungeonsguide.roomprocessor.bombdefuse.chambers;

import kr.syeyoung.dungeonsguide.roomprocessor.RoomProcessor;
import net.minecraft.nbt.NBTTagCompound;

public interface ChamberProcessor extends RoomProcessor {
    void onDataRecieve(NBTTagCompound compound);
    String getName();
}
