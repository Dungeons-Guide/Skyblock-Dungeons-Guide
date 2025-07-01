package kr.syeyoung.modapi.v1_21_5.entity;

import kr.syeyoung.modapi.entity.UEntityGuardian;
import lombok.Getter;
import net.minecraft.entity.monster.EntityGuardian;

public class UEntityGuardianImpl extends UEntityLivingImpl implements UEntityGuardian {
    @Getter
    protected EntityGuardian delegate;

    public UEntityGuardianImpl(EntityGuardian delegate) {
        super(delegate);
        this.delegate = delegate;
    }

    public boolean isElder() {
        return delegate.isElder();
    }
}
