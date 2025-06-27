package kr.syeyoung.modapi.v1_8_9.world;

import kr.syeyoung.modapi.world.UBlock;
import kr.syeyoung.modapi.world.UBlockState;
import lombok.Getter;
import net.minecraft.block.Block;

public class UBlockImpl implements UBlock {
    @Getter
    private Block delegate;
    private String id;

    public UBlockImpl(Block block) {
        this.delegate = block;
        this.id = block.getRegistryName();
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getLocalizedName() {
        return delegate.getLocalizedName();
    }
}
