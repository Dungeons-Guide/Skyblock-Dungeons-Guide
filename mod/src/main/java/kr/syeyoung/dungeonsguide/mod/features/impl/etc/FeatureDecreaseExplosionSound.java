/*
 * Dungeons Guide - The most intelligent Hypixel Skyblock Dungeons Mod
 * Copyright (C) 2021  cyoung06
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package kr.syeyoung.dungeonsguide.mod.features.impl.etc;

<<<<<<<< HEAD:mod/src/main/java/kr/syeyoung/dungeonsguide/features/impl/etc/FeatureDecreaseExplosionSound.java
import kr.syeyoung.dungeonsguide.DungeonsGuide;
import kr.syeyoung.dungeonsguide.SkyblockStatus;
import kr.syeyoung.dungeonsguide.features.listener.SoundListener;
import kr.syeyoung.dungeonsguide.features.FeatureParameter;
import kr.syeyoung.dungeonsguide.features.SimpleFeature;
========
import kr.syeyoung.dungeonsguide.mod.SkyblockStatus;
import kr.syeyoung.dungeonsguide.mod.features.FeatureParameter;
import kr.syeyoung.dungeonsguide.mod.features.SimpleFeature;
import kr.syeyoung.dungeonsguide.mod.features.listener.SoundListener;
>>>>>>>> origin/breaking-changes-just-working-im-not-putting-all-of-these-into-3.0-but-for-the-sake-of-beta-release-this-thing-exists:mod/src/main/java/kr/syeyoung/dungeonsguide/mod/features/impl/etc/FeatureDecreaseExplosionSound.java
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraftforge.client.event.sound.PlaySoundEvent;

public class FeatureDecreaseExplosionSound extends SimpleFeature implements SoundListener {
    public FeatureDecreaseExplosionSound() {
       super("Misc", "Decrease Explosion sound effect", "Decreases volume of explosions while on skyblock", "qol.explosionsound");
        addParameter("sound", new FeatureParameter<Float>("sound", "Sound Multiplier %", "The volume of explosion effect will be multiplied by this value. 0~100", 10.0f, "float"));
    }

    @Override
    public void onSound(PlaySoundEvent soundEvent) {
        if (!SkyblockStatus.isOnSkyblock()) return;

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
