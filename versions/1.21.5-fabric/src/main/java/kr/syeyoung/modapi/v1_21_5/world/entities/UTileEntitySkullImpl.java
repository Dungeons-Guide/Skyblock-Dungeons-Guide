package kr.syeyoung.modapi.v1_21_5.world.entities;

import kr.syeyoung.modapi.world.tileentities.UTileEntitySkull;
import net.minecraft.tileentity.TileEntitySkull;

import java.util.Optional;

public class UTileEntitySkullImpl extends UTileEntityImpl implements UTileEntitySkull {
    protected TileEntitySkull delegate;
    public UTileEntitySkullImpl(TileEntitySkull delegate) {
        super(delegate);
        this.delegate = delegate;
    }
    @Override
    public String getTexture() {
        String texture = Optional.ofNullable(((TileEntitySkull) delegate).getPlayerProfile())
                .map(a -> a.getProperties())
                .map(a -> a.get("textures"))
                .flatMap(a -> a.stream().findFirst())
                .map(a -> a.getValue()).orElse(null);
        return texture;
    }
}
