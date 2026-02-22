package v1_21_11.entity;

import kr.syeyoung.modapi.entity.UEntityGuardian;
import kr.syeyoung.modapi.v1_21_5.entity.UEntityLivingImpl;
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
