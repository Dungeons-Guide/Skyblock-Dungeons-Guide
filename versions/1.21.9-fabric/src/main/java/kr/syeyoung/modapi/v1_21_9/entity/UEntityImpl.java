package kr.syeyoung.modapi.v1_21_9.entity;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import kr.syeyoung.modapi.data.Vector3D;
import kr.syeyoung.modapi.data.VectorI3D;
import kr.syeyoung.modapi.entity.EntityType;
import kr.syeyoung.modapi.entity.UEntity;
import lombok.Getter;
import net.minecraft.entity.Entity;
import net.minecraft.entity.FallingBlockEntity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.boss.WitherEntity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.decoration.ItemFrameEntity;
import net.minecraft.entity.mob.*;
import net.minecraft.entity.passive.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.Map;

public class UEntityImpl implements UEntity {
    @Getter
    protected Entity delegate;

    public UEntityImpl(Entity delegate) {
        this.delegate = delegate;
    }

    @Override
    public int getEntityId() {
        return delegate.getId();
    }

    @Override
    public Vector3D getPositionVector() {
        Vec3d vec3 = delegate.getEntityPos();
        return new Vector3D(vec3.x, vec3.y, vec3.z);
    }

    @Override
    public Vector3D getPositionEyes(float partialTicks) {
        Vec3d vec3 = delegate.getCameraPosVec(partialTicks);
        return new Vector3D(vec3.x, vec3.y, vec3.z);
    }

    public VectorI3D getPosition() {
        BlockPos pos = delegate.getBlockPos();
        return new VectorI3D(pos.getX(), pos.getY(), pos.getZ());
    }

    public Vector3D getLook(float partialTicks) {
        Vec3d vector3D = delegate.getRotationVec(partialTicks);
        return new Vector3D(vector3D.x, vector3D.y, vector3D.z);
    }

    @Override // TODO: to component.
    public String getName() {
        return delegate.getName().getString();
    }

    public boolean isInvisible() {
        return delegate.isInvisible();
    }

    public float getRotationPitch() {
        return delegate.getPitch();
    }

    public double getPrevPosX() {
        return delegate.lastX;
    }

    public double getPrevPosZ() {
        return delegate.lastZ;
    }

    public double getPosX() {
        return delegate.getX();
    }

    public double getPosZ() {
        return delegate.getZ();
    }

    public float getPrevRotationYaw() {
        return delegate.lastYaw;
    }

    public float getRotationYaw() {
        return delegate.getYaw();
    }

    public static final BiMap<Class<? extends Entity>, EntityType> bimap = HashBiMap.create();

    static {
        bimap.put(PlayerEntity.class, EntityType.PLAYER);
        bimap.put(BatEntity.class, EntityType.BAT);
        bimap.put(ItemEntity.class, EntityType.ITEM);
        bimap.put(SheepEntity.class, EntityType.SHEEP);
        bimap.put(CowEntity.class, EntityType.COW);
        bimap.put(WolfEntity.class, EntityType.WOLF);
        bimap.put(RabbitEntity.class, EntityType.RABBIT);
        bimap.put(ChickenEntity.class, EntityType.CHICKEN);
        bimap.put(GhastEntity.class, EntityType.GHAST);
        bimap.put(SkeletonEntity.class, EntityType.SKELETON);
        bimap.put(WitherEntity.class, EntityType.WITHER);
        bimap.put(GuardianEntity.class, EntityType.GUARDIAN);
        bimap.put(IronGolemEntity.class, EntityType.IRON_GOLEM);
        bimap.put(GiantEntity.class, EntityType.GIANT);
        bimap.put(EndCrystalEntity.class, EntityType.ENDER_CRYSTAL);
        bimap.put(FallingBlockEntity.class, EntityType.FALLING_BLOCK);
        bimap.put(ArmorStandEntity.class, EntityType.ARMOR_STAND);
        bimap.put(ItemFrameEntity.class, EntityType.ITEM_FRAME);
        bimap.put(BlazeEntity.class, EntityType.BLAZE);
        bimap.put(ArrowEntity.class, EntityType.ARROW);
        bimap.put(SilverfishEntity.class, EntityType.SILVERFISH);
    }

    @Override
    public EntityType getEntityType() {
        for (Map.Entry<Class<? extends Entity>, EntityType> classEntityTypeEntry : bimap.entrySet()) {
            if (classEntityTypeEntry.getKey().isAssignableFrom(delegate.getClass()))
                return classEntityTypeEntry.getValue();
        }
        return EntityType.UNKNOWN;
    }

    public boolean isDead() {
        return !delegate.isAlive();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof UEntityImpl)) return false;
        if (((UEntityImpl) obj).delegate == delegate) return true;
        return ((UEntityImpl) obj).delegate.equals(delegate);
    }

    public double getPosY() {
        return delegate.getY();
    }

    public double getPrevPosY() {
        return delegate.lastY;
    }

    @Override
    public double getHeight() {
        return delegate.getHeight();
    }
}

