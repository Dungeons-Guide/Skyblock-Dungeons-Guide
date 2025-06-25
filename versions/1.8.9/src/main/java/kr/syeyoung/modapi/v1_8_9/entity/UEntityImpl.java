package kr.syeyoung.modapi.v1_8_9.entity;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import kr.syeyoung.modapi.data.Vector3D;
import kr.syeyoung.modapi.data.VectorI3D;
import kr.syeyoung.modapi.entity.EntityType;
import kr.syeyoung.modapi.entity.UEntity;
import lombok.Getter;
import net.minecraft.entity.Entity;
import net.minecraft.entity.boss.EntityWither;
import net.minecraft.entity.item.*;
import net.minecraft.entity.monster.*;
import net.minecraft.entity.passive.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.util.BlockPos;
import net.minecraft.util.Vec3;

import java.util.Map;

public class UEntityImpl implements UEntity {
    @Getter
    protected Entity delegate;

    public UEntityImpl(Entity delegate) {
        this.delegate = delegate;
    }

    @Override
    public int getEntityId() {
        return delegate.getEntityId();
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

    public boolean isInvisible() {
        return delegate.isInvisible();
    }

    public float getRotationPitch() {
        return delegate.rotationPitch;
    }

    public double getPrevPosX() {
        return delegate.prevPosX;
    }

    public double getPrevPosZ() {
        return delegate.prevPosZ;
    }

    public double getPosX() {
        return delegate.posX;
    }

    public double getPosZ() {
        return delegate.posZ;
    }

    public float getPrevRotationYaw() {
        return delegate.prevRotationYaw;
    }

    public float getRotationYaw() {
        return delegate.rotationYaw;
    }

    public static final BiMap<Class<? extends Entity>, EntityType> bimap = HashBiMap.create();

    static {
        bimap.put(EntityPlayer.class, EntityType.PLAYER);
        bimap.put(EntityBat.class, EntityType.BAT);
        bimap.put(EntityItem.class, EntityType.ITEM);
        bimap.put(EntitySheep.class, EntityType.SHEEP);
        bimap.put(EntityCow.class, EntityType.COW);
        bimap.put(EntityWolf.class, EntityType.WOLF);
        bimap.put(EntityRabbit.class, EntityType.RABBIT);
        bimap.put(EntityChicken.class, EntityType.CHICKEN);
        bimap.put(EntityGhast.class, EntityType.GHAST);
        bimap.put(EntitySkeleton.class, EntityType.SKELETON);
        bimap.put(EntityWither.class, EntityType.WITHER);
        bimap.put(EntityGuardian.class, EntityType.GUARDIAN);
        bimap.put(EntityIronGolem.class, EntityType.IRON_GOLEM);
        bimap.put(EntityGiantZombie.class, EntityType.GIANT);
        bimap.put(EntityEnderCrystal.class, EntityType.ENDER_CRYSTAL);
        bimap.put(EntityFallingBlock.class, EntityType.FALLING_BLOCK);
        bimap.put(EntityArmorStand.class, EntityType.ARMOR_STAND);
        bimap.put(EntityItemFrame.class, EntityType.ITEM_FRAME);
        bimap.put(EntityBlaze.class, EntityType.BLAZE);
        bimap.put(EntityArrow.class, EntityType.ARROW);
        bimap.put(EntitySilverfish.class, EntityType.SILVERFISH);
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
        return delegate.isDead;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof UEntityImpl)) return false;
        if (((UEntityImpl) obj).delegate == delegate) return true;
        return ((UEntityImpl) obj).delegate.equals(delegate);
    }

    public double getPosY() {
        return delegate.posY;
    }

    public double getPrevPosY() {
        return delegate.prevPosY;
    }

    @Override
    public double getHeight() {
        return delegate.height;
    }
}

