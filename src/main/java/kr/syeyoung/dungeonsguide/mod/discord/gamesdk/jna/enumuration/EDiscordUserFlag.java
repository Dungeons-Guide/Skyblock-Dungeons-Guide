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

package kr.syeyoung.dungeonsguide.mod.discord.gamesdk.jna.enumuration;

import com.sun.jna.FromNativeContext;
import com.sun.jna.ToNativeContext;
import com.sun.jna.TypeConverter;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

public enum EDiscordUserFlag {
    DiscordUserFlag_Partner(2),
    DiscordUserFlag_HypeSquadEvents(4),
    DiscordUserFlag_HypeSquadHouse1(64),
    DiscordUserFlag_HypeSquadHouse2(128),
    DiscordUserFlag_HypeSquadHouse3(256);

    @Getter
    private final int value;
    private EDiscordUserFlag(int value) {
        this.value = value;
    }

    private static final Map<Integer,EDiscordUserFlag> valueMap = new HashMap<>();
    static {
        for (EDiscordUserFlag value : values()) {
            valueMap.put(value.value, value);
        }
    }

    public static EDiscordUserFlag fromValue(int value) {
        return valueMap.get(value);
    }
    
    public static class EDiscordUserFlagTypeConverter implements TypeConverter {
        @Override
        public Object fromNative(Object nativeValue, FromNativeContext context) {
            return EDiscordUserFlag.fromValue((Integer)nativeValue);
        }

        @Override
        public Object toNative(Object value, ToNativeContext context) {
            if (value == null) return 0;
            return ((EDiscordUserFlag)value).getValue();
        }

        @Override
        public Class nativeType() {
            return Integer.class;
        }
    }
}
