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

package kr.syeyoung.dungeonsguide.mod.discord.gamesdk.jna.datastruct;

import com.sun.jna.Library;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import kr.syeyoung.dungeonsguide.mod.discord.gamesdk.jna.GameSDKTypeMapper;

import java.util.Collections;
import java.util.Map;

public abstract class DiscordStruct extends Structure {
    public static final Map<String, Object> OPTIONS = Collections.singletonMap(Library.OPTION_TYPE_MAPPER, GameSDKTypeMapper.INSTANCE);
    protected DiscordStruct() {
        super(GameSDKTypeMapper.INSTANCE);
    }
    protected DiscordStruct(Pointer p) {super(p); read();}
}
