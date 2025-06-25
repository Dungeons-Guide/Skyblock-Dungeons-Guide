package kr.syeyoung.modapi.v1_8_9.entity;

import com.mojang.authlib.properties.Property;
import kr.syeyoung.modapi.entity.EntityType;
import kr.syeyoung.modapi.entity.UEntityPlayer;
import lombok.Getter;
import net.minecraft.entity.player.EntityPlayer;

import java.util.Collection;

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
}
