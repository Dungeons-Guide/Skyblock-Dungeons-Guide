package kr.syeyoung.modapi.v1_8_9.entity;

import kr.syeyoung.modapi.data.Vector3D;
import kr.syeyoung.modapi.data.VectorI3D;
import kr.syeyoung.modapi.entity.UEntity;
import net.minecraft.entity.Entity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.Vec3;

public class UEntityImpl implements UEntity {
    protected Entity delegate;

    public UEntityImpl(Entity delegate) {
        this.delegate = delegate;
    }

    @Override
    public Vector3D getPositionVector() {
        Vec3 vec3 = delegate.getPositionVector();
        return new Vector3D(vec3.xCoord, vec3.yCoord, vec3.zCoord);
    }

    @Override
    public Vector3D getPositionEyes(float partialTicks) {
        Vec3 vec3 = delegate.getPositionEyes(partialTicks);
        return new Vector3D(vec3.xCoord, vec3.yCoord, vec3.zCoord);
    }

    public VectorI3D getPosition() {
        BlockPos pos = delegate.getPosition();
        return new VectorI3D(pos.getX(), pos.getY(), pos.getZ());
    }

    public Vector3D getLook(float partialTicks) {
        Vec3 vector3D = delegate.getLook(partialTicks);
        return new Vector3D(vector3D.xCoord, vector3D.yCoord, vector3D.zCoord);
    }

    @Override
    public String getName() {
        return delegate.getName();
    }
}
