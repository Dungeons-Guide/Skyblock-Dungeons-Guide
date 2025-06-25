package kr.syeyoung.modapi.v1_8_9.audio;

import kr.syeyoung.modapi.audio.USoundHandler;
import kr.syeyoung.modapi.data.ResourceIdentifier;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.util.ResourceLocation;

public class USoundHandlerImpl implements USoundHandler {
    private SoundHandler delegate;

    public USoundHandlerImpl(SoundHandler delegate) {
        this.delegate = delegate;
    }


    @Override
    public void playSoundAtPlayer(ResourceIdentifier identifier, float pitch) {
        delegate.playSound(PositionedSoundRecord.create(new ResourceLocation(identifier.getMod(), identifier.getLocation()), pitch));
    }
}
