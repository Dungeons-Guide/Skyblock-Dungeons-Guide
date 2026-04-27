package kr.syeyoung.modapi.v1_21_9.entity;

import kr.syeyoung.modapi.entity.UEntityGuardian;
import lombok.Getter;
import net.minecraft.entity.mob.ElderGuardianEntity;
import net.minecraft.entity.mob.GuardianEntity;

public class UEntityGuardianImpl extends UEntityLivingImpl implements UEntityGuardian {
    @Getter
    protected GuardianEntity delegate;

    public UEntityGuardianImpl(GuardianEntity delegate) {
        super(delegate);
        this.delegate = delegate;
    }

    public boolean isElder() {
        return delegate instanceof ElderGuardianEntity;
    }
}
