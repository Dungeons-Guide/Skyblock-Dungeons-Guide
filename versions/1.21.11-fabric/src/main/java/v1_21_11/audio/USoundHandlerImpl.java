package v1_21_11.audio;

import kr.syeyoung.modapi.audio.USoundHandler;
import kr.syeyoung.modapi.data.ResourceIdentifier;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.sound.SoundManager;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;

public class USoundHandlerImpl implements USoundHandler {
    private SoundManager delegate;

    public USoundHandlerImpl(SoundManager delegate) {
        this.delegate = delegate;
    }


    @Override
    public void playSoundAtPlayer(ResourceIdentifier identifier, float pitch) {
        delegate.play(PositionedSoundInstance.master(
                SoundEvent.of(Identifier.of(identifier.getMod(), identifier.getLocation())),
                pitch
        ));
    }
}
