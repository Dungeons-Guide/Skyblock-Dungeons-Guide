/*
 * Dungeons Guide - The most intelligent Hypixel Skyblock Dungeons Mod
 * Copyright (C) 2026  cyoung06 (syeyoung)
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

package kr.syeyoung.dungeonsguide.mod.events.impl;

import kr.syeyoung.dungeonsguide.mod.events.DGEvent;
import net.minecraft.client.resources.sounds.SoundInstance;

// set result to play a different sound, or null to not play it. like 1.8.9 PlaySoundEvent
public class PlaySoundEvent extends DGEvent {
    // path of the sound identifier
    public final String name;
    public final SoundInstance sound;
    public SoundInstance result;

    public PlaySoundEvent(String name, SoundInstance sound) {
        this.name = name;
        this.sound = sound;
        this.result = sound;
    }
}
