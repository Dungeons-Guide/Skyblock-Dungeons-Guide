package kr.syeyoung.modapi.v1_21_9.entity;

import com.mojang.authlib.properties.Property;
import kr.syeyoung.modapi.entity.EntityType;
import kr.syeyoung.modapi.entity.UEntityPlayer;
import lombok.Getter;
import net.minecraft.entity.player.PlayerEntity;

import java.util.Collection;
import java.util.UUID;

public class UEntityPlayerImpl extends UEntityLivingImpl implements UEntityPlayer {
    @Getter
    protected PlayerEntity delegate;

    public UEntityPlayerImpl(PlayerEntity delegate) {
        super(delegate);
        this.delegate = delegate;
    }

    @Override
    public EntityType getEntityType() {
        return EntityType.PLAYER;
    }

    @Override
    public String getSkinTexture() {
        Collection<Property> obj = delegate.getGameProfile().properties().get("textures");
        return obj.stream().findFirst().map(Property::value).orElse(null);
    }

    @Override
    public UUID getUUID() {
        return delegate.getGameProfile().id();
    }

    public void refreshDisplayName() {
//        delegate.refreshDisplayName(); // TODO: need to mixin
    }
}
