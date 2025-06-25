package kr.syeyoung.modapi.v1_8_9.entity;

import kr.syeyoung.modapi.data.Vector3D;
import kr.syeyoung.modapi.data.VectorI3D;
import kr.syeyoung.modapi.entity.UPlayerSelf;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.util.BlockPos;
import net.minecraft.util.Vec3;

public class UEntityPlayerSP implements UPlayerSelf {
    private EntityPlayerSP delegate;

    public UEntityPlayerSP(EntityPlayerSP delegate) {
        this.delegate = delegate;
    }

    @Override
    public Vector3D getPositionVector() {
        Vec3 vec3 = delegate.getPositionVector();
        return new Vector3D(vec3.xCoord, vec3.yCoord, vec3.zCoord);
    }

    // TODO: chagne to rendering context?
    @Override
    public Vector3D getPositionEyes(float partialTicks) {
        Vec3 vec3 = delegate.getPositionEyes(partialTicks);
        return new Vector3D(vec3.xCoord, vec3.yCoord, vec3.zCoord);
    }

    // TODO: is this the best place to be??
    public String getClientBrand() {
        return delegate.getClientBrand();
    }

    public String getName() {
        return delegate.getName();
    }

    // TODO: maybe consider changing this.
    public VectorI3D getPosition() {
        BlockPos pos = delegate.getPosition();
        return new VectorI3D(pos.getX(), pos.getY(), pos.getZ());
    }

    // TODO: should i let interpolation be done in rendering??? idk.
    public float getPrevRotationYawHead() {
        return delegate.prevRotationYawHead;
    }

    public float getRotationYawHead() {
        return delegate.rotationYawHead;
    }

    public Vector3D getLook(float partialTicks) {
        Vec3 vector3D = delegate.getLook(partialTicks);;
        return new Vector3D(vector3D.xCoord, vector3D.yCoord, vector3D.zCoord);
    }
}
