package kr.syeyoung.modapi.v1_8_9.entity;

import kr.syeyoung.modapi.entity.UEntity;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityItemFrame;
import net.minecraft.entity.monster.EntityGuardian;
import net.minecraft.entity.monster.EntitySkeleton;
import net.minecraft.entity.player.EntityPlayer;

public class UEntityDelegateFactory {
    public static UEntity createEntityFor(Entity entity) {
        if (entity instanceof EntityPlayerSP) {
            return new UEntityPlayerSP((EntityPlayerSP) entity);
        }
        if (entity instanceof EntityPlayer) {
            return new UEntityPlayerImpl((EntityPlayer) entity);
        }
        if (entity instanceof EntityItem) {
            return new UEntityItemImpl((EntityItem) entity);
        }
        if (entity instanceof EntityItemFrame) {
            return new UEntityItemFrameImpl((EntityItemFrame) entity);
        }
        if (entity instanceof EntitySkeleton) {
            return new UEntitySkeletonImpl((EntitySkeleton) entity);
        }
        if (entity instanceof EntityGuardian) {
            return new UEntityGuardianImpl((EntityGuardian) entity);
        }
        if (entity instanceof EntityArmorStand) {
            return new UEntityArmorStandImpl((EntityArmorStand) entity);
        }
        if (entity instanceof EntityLivingBase) {
            return new UEntityLivingImpl((EntityLivingBase) entity);
        }
        return new UEntityImpl(entity);
    }
}
