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
import kr.syeyoung.modapi.event.events.PlaySoundEvent;

public class FeatureDecreaseExplosionSound extends SimpleFeature {
    public FeatureDecreaseExplosionSound() {
       super("Misc", "Decrease Explosion sound effect", "Decreases volume of explosions while on skyblock", "qol.explosionsound");
        addParameter("sound", new FeatureParameter<Double>("sound", "Sound Multiplier %", "The volume of explosion effect will be multiplied by this value. 0~100", 10.0, TCDouble.INSTANCE)
                .setWidgetGenerator((param) -> new ParameterItem(param, new TCDouble.DoubleEditWidget(param, 0, 100))));
    }

    @DGEventHandler
    public void onSound(PlaySoundEvent soundEvent) {
        if (!SkyblockStatus.isOnSkyblock()) return;

        if (soundEvent.getSound().getSoundName().getLocation().equals("random.explode")) {
            PlaySoundEvent.Sound positionedSoundRecord = soundEvent.getResult();
            PlaySoundEvent.Sound neweff = new PlaySoundEvent.Sound(
                    positionedSoundRecord.getSoundName(),
                    (float) (positionedSoundRecord.getVolume() * (this.<Double>getParameter("sound").getValue() / 100)),
                    positionedSoundRecord.getPitch(),
                    positionedSoundRecord.getXPos(),
                    positionedSoundRecord.getYPos(),
                    positionedSoundRecord.getZPos()
            );

            soundEvent.setResult(neweff);
        }
    }
}
