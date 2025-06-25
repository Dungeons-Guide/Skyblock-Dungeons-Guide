package kr.syeyoung.modapi.v1_8_9.entity;

import kr.syeyoung.modapi.entity.UEntityPlayer;
import net.minecraft.entity.player.EntityPlayer;

public class UEntityPlayerImpl extends UEntityLivingImpl implements UEntityPlayer {
    protected EntityPlayer delegate;

    public UEntityPlayerImpl(EntityPlayer delegate) {
        super(delegate);
        this.delegate = delegate;
    }
}
