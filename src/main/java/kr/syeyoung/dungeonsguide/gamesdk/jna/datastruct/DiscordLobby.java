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

package kr.syeyoung.dungeonsguide.gamesdk.jna.datastruct;

import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import kr.syeyoung.dungeonsguide.gamesdk.jna.enumuration.EDiscordLobbyType;
import kr.syeyoung.dungeonsguide.gamesdk.jna.typedef.DiscordSnowflake;

import kr.syeyoung.dungeonsguide.gamesdk.jna.typedef.UInt32;

public class DiscordLobby extends DiscordStruct {
    public DiscordSnowflake id = new DiscordSnowflake();
    public EDiscordLobbyType type = EDiscordLobbyType.DiscordLobbyType_Private;
    public DiscordSnowflake owner_id = new DiscordSnowflake();
    public byte[] secret = new byte[128];
    public UInt32 capacity = new UInt32();
    public boolean locked;
    public DiscordLobby() {super();} public DiscordLobby(Pointer pointer) {super(pointer);}

    public static class ByReference extends DiscordLobby implements Structure.ByReference { public ByReference() {super();} public ByReference(Pointer pointer) {super(pointer);}}
    public static class ByValue extends DiscordLobby implements Structure.ByValue { public ByValue() {super();} public ByValue(Pointer pointer) {super(pointer);}}
}
