package kr.syeyoung.modapi.v1_21_5.entity;

import com.mojang.authlib.properties.Property;
import kr.syeyoung.modapi.entity.EntityType;
import kr.syeyoung.modapi.entity.UEntityPlayer;
import kr.syeyoung.modapi.item.UItemStack;
import kr.syeyoung.modapi.v1_8_9.item.UItemStackImpl;
import lombok.Getter;
import net.minecraft.entity.player.EntityPlayer;

import java.util.Collection;
import java.util.UUID;

public class UEntityPlayerImpl extends UEntityLivingImpl implements UEntityPlayer {
    @Getter
    protected EntityPlayer delegate;

    public UEntityPlayerImpl(EntityPlayer delegate) {
        super(delegate);
        this.delegate = delegate;
    }

    @Override
    public EntityType getEntityType() {
        return EntityType.PLAYER;
    }

    @Override
    public String getSkinTexture() {
        Collection<Property> obj = delegate.getGameProfile().getProperties().get("textures");
        return obj.stream().findFirst().map(Property::getValue).orElse(null);
    }

    public UItemStack getHeldItem() {
        return delegate.getHeldItem() == null ? null : new UItemStackImpl(delegate.getHeldItem());
    }

    @Override
    public UUID getUUID() {
        return delegate.getGameProfile().getId();
    }

    public void refreshDisplayName() {
        delegate.refreshDisplayName();
    }
}
