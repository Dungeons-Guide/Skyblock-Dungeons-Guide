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


import kr.syeyoung.dungeonsguide.mod.SkyblockStatus;
import kr.syeyoung.dungeonsguide.mod.config.guiconfig.configv3.ParameterItem;
import kr.syeyoung.dungeonsguide.mod.config.types.TCDouble;
import kr.syeyoung.dungeonsguide.mod.events.annotations.DGEventHandler;
import kr.syeyoung.dungeonsguide.mod.features.FeatureParameter;
import kr.syeyoung.dungeonsguide.mod.features.SimpleFeature;
import net.minecraft.client.resources.sounds.AbstractSoundInstance;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import kr.syeyoung.dungeonsguide.mod.events.impl.PlaySoundEvent;

public class FeatureDecreaseExplosionSound extends SimpleFeature {
    public FeatureDecreaseExplosionSound() {
       super("Misc", "Decrease Explosion sound effect", "Decreases volume of explosions while on skyblock", "qol.explosionsound");
        addParameter("sound", new FeatureParameter<Double>("sound", "Sound Multiplier %", "The volume of explosion effect will be multiplied by this value. 0~100", 10.0, TCDouble.INSTANCE)
                .setWidgetGenerator((param) -> new ParameterItem(param, new TCDouble.DoubleEditWidget(param, 0, 100))));
    }

    @DGEventHandler
    public void onSound(PlaySoundEvent soundEvent) {
        if (!SkyblockStatus.isOnSkyblock()) return;

        // 1.8.9's random.explode; the instance isn't resolved yet, so the raw volume / pitch are read (class tweaker)
        if (soundEvent.name.equalsIgnoreCase(SoundEvents.GENERIC_EXPLODE.value().location().getPath()) && soundEvent.result instanceof AbstractSoundInstance) {
            AbstractSoundInstance positionedSoundRecord = (AbstractSoundInstance) soundEvent.result;
            SimpleSoundInstance neweff = new SimpleSoundInstance(
                    positionedSoundRecord.getIdentifier(),
                    positionedSoundRecord.getSource(),
                    (float) (positionedSoundRecord.volume * (this.<Double>getParameter("sound").getValue() / 100)),
                    positionedSoundRecord.pitch,
                    RandomSource.create(),
                    positionedSoundRecord.isLooping(),
                    positionedSoundRecord.getDelay(),
                    positionedSoundRecord.getAttenuation(),
                    positionedSoundRecord.getX(),
                    positionedSoundRecord.getY(),
                    positionedSoundRecord.getZ(),
                    positionedSoundRecord.isRelative()
            );

            soundEvent.result = neweff;
        }
    }
}
