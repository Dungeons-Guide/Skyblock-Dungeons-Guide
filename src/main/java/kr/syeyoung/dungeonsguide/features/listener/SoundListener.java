package kr.syeyoung.dungeonsguide.features.listener;

import net.minecraftforge.client.event.sound.PlaySoundEvent;

public interface SoundListener {
    void onSound(PlaySoundEvent playSoundEvent);
}
