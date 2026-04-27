package kr.syeyoung.modapi.v1_21_9.entity;

import kr.syeyoung.modapi.entity.UEntity;
import kr.syeyoung.modapi.entity.URenderManager;
import net.minecraft.client.render.entity.EntityRenderManager;

public class URenderManagerImpl implements URenderManager {
    private EntityRenderManager delegate;

    public URenderManagerImpl(EntityRenderManager entityRenderDispatcher) {
        this.delegate = entityRenderDispatcher;
    }

    public boolean doRenderEntity(UEntity entity, double x, double y, double z, float entityYaw, float partialTicks, boolean p_147939_10_) {
//        return delegate.render(((UEntityImpl)entity).getDelegate(), x, y, z, entityYaw, partialTicks, p_147939_10_);
        return true; // TODO : FIX
    }
}
