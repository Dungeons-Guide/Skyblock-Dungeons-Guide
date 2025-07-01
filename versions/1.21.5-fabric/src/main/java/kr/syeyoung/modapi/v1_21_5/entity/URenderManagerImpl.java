package kr.syeyoung.modapi.v1_21_5.entity;

import kr.syeyoung.modapi.entity.UEntity;
import kr.syeyoung.modapi.entity.URenderManager;
import net.minecraft.client.renderer.entity.RenderManager;

public class URenderManagerImpl implements URenderManager {
    private RenderManager delegate;

    public URenderManagerImpl(RenderManager delegate) {
        this.delegate = delegate;
    }

    public boolean doRenderEntity(UEntity entity, double x, double y, double z, float entityYaw, float partialTicks, boolean p_147939_10_) {
        return delegate.doRenderEntity(((UEntityImpl)entity).getDelegate(), x, y, z, entityYaw, partialTicks, p_147939_10_);
    }
}
