package v1_21_11.world.entities;

import com.mojang.authlib.properties.Property;
import kr.syeyoung.modapi.world.tileentities.UTileEntitySkull;
import net.minecraft.block.entity.SkullBlockEntity;
import net.minecraft.component.type.ProfileComponent;

public class UTileEntitySkullImpl extends UTileEntityImpl implements UTileEntitySkull {
    protected SkullBlockEntity delegate;
    public UTileEntitySkullImpl(SkullBlockEntity delegate) {
        super(delegate);
        this.delegate = delegate;
    }
    @Override
    public String getTexture() {
        ProfileComponent component = delegate.getOwner();
        if (component == null) return null;
        String texture = component.gameProfile()
                .getProperties()
                .get("textures")
                .stream().findFirst()
                .map(Property::value)
                .orElse(null);
        return texture;
    }
}
