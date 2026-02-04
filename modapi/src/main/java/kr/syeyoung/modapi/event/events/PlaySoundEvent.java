package kr.syeyoung.modapi.event.events;

import kr.syeyoung.modapi.data.ResourceIdentifier;
import kr.syeyoung.modapi.event.UEvent;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
public class PlaySoundEvent extends UEvent {
    @Getter
    private final Sound sound;
    @Getter @Setter
    private Sound result;

    @AllArgsConstructor @Getter
    public static class Sound {
        private final ResourceIdentifier soundName;
        private final float xPos;
        private final float yPos;
        private final float zPos;
        private final float pitch;
        private final float volume;
    }
}
