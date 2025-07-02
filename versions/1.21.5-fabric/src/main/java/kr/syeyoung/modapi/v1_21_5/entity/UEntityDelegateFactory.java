package kr.syeyoung.modapi.v1_21_5.entity;

import kr.syeyoung.modapi.entity.UEntity;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.decoration.ItemFrameEntity;
import net.minecraft.entity.mob.AbstractSkeletonEntity;
import net.minecraft.entity.mob.GuardianEntity;
import net.minecraft.entity.player.PlayerEntity;

public class UEntityDelegateFactory {
    public static UEntity createEntityFor(Entity entity) {
        if (entity instanceof ClientPlayerEntity) {
            return new UEntityPlayerSP((ClientPlayerEntity) entity);
        }
        if (entity instanceof PlayerEntity) {
            return new UEntityPlayerImpl((PlayerEntity) entity);
        }
        if (entity instanceof ItemEntity) {
            return new UEntityItemImpl((ItemEntity) entity);
        }
        if (entity instanceof ItemFrameEntity) {
            return new UEntityItemFrameImpl((ItemFrameEntity) entity);
        }
        if (entity instanceof AbstractSkeletonEntity) {
            return new UEntitySkeletonImpl((AbstractSkeletonEntity) entity);
        }
        if (entity instanceof GuardianEntity) {
            return new UEntityGuardianImpl((GuardianEntity) entity);
        }
        if (entity instanceof ArmorStandEntity) {
            return new UEntityArmorStandImpl((ArmorStandEntity) entity);
        }
        if (entity instanceof LivingEntity) {
            return new UEntityLivingImpl((LivingEntity) entity);
        }
        return new UEntityImpl(entity);
    }
}
