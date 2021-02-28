package kr.syeyoung.dungeonsguide.features.impl.etc;

import kr.syeyoung.dungeonsguide.SkyblockStatus;
import kr.syeyoung.dungeonsguide.e;
import kr.syeyoung.dungeonsguide.features.FeatureParameter;
import kr.syeyoung.dungeonsguide.features.SimpleFeature;
import kr.syeyoung.dungeonsguide.features.listener.SoundListener;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraftforge.client.event.sound.PlaySoundEvent;

public class FeatureDecreaseExplosionSound extends SimpleFeature implements SoundListener {
    public FeatureDecreaseExplosionSound() {
       super("ETC", "Decrease Explosion sound effect", "Decreases volume of explosions while on skyblock", "qol.reparty");
       parameters.put("sound", new FeatureParameter<Float>("sound", "Sound Multiplier %", "The volume of explosion effect will be multiplied by this value. 0~100", 10.0f, "float"));
    }

    SkyblockStatus skyblockStatus = e.getDungeonsGuide().getSkyblockStatus();

    @Override
    public void onSound(PlaySoundEvent soundEvent) {
        if (!skyblockStatus.isOnSkyblock()) return;

        if (soundEvent.name.equalsIgnoreCase("random.explode") && soundEvent.result instanceof PositionedSoundRecord) {
            PositionedSoundRecord positionedSoundRecord = (PositionedSoundRecord) soundEvent.result;
            PositionedSoundRecord neweff = new PositionedSoundRecord(
                    positionedSoundRecord.getSoundLocation(),
                    positionedSoundRecord.getVolume() * (this.<Float>getParameter("sound").getValue() / 100),
                    positionedSoundRecord.getPitch(),
                    positionedSoundRecord.getXPosF(),
                    positionedSoundRecord.getYPosF(),
                    positionedSoundRecord.getZPosF()
            );

            soundEvent.result = neweff;
        }
    }
}
