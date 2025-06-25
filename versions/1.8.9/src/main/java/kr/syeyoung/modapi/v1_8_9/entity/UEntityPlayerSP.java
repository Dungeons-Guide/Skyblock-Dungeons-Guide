package kr.syeyoung.modapi.v1_8_9.entity;

import kr.syeyoung.modapi.entity.UPlayerSelf;
import lombok.Getter;
import net.minecraft.client.entity.EntityPlayerSP;

public class UEntityPlayerSP extends UEntityPlayerImpl implements UPlayerSelf {
    @Getter
    protected EntityPlayerSP delegate;

    public UEntityPlayerSP(EntityPlayerSP delegate) {
        super(delegate);
        this.delegate = delegate;
    }

    // TODO: is this the best place to be??
    public String getClientBrand() {
        return delegate.getClientBrand();
    }
}
