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
import kr.syeyoung.dungeonsguide.gamesdk.jna.interfacestruct.*;
import kr.syeyoung.dungeonsguide.gamesdk.jna.typedef.DiscordClientID;
import kr.syeyoung.dungeonsguide.gamesdk.jna.typedef.DiscordVersion;
import kr.syeyoung.dungeonsguide.gamesdk.jna.typedef.UInt64;

public class DiscordCreateParams extends DiscordStruct { public DiscordCreateParams() {super();} public DiscordCreateParams(Pointer pointer) {super(pointer);}
    public DiscordClientID client_id = new DiscordClientID();
    public UInt64 flags = new UInt64();
    public Pointer events; // void*
    public Pointer event_data; // void*
    public Pointer application_events;
    public DiscordVersion application_version = new DiscordVersion();
    public IDiscordUserEvents.ByReference user_events;
    public DiscordVersion user_version= new DiscordVersion();
    public Pointer image_events;// void*
    public DiscordVersion image_version= new DiscordVersion();
    public IDiscordActivityEvents.ByReference activity_events;
    public DiscordVersion activity_version= new DiscordVersion();
    public IDiscordRelationshipEvents.ByReference relationship_events;
    public DiscordVersion relationship_version= new DiscordVersion();
    public IDiscordLobbyEvents.ByReference lobby_events;
    public DiscordVersion lobby_version= new DiscordVersion();
    public IDiscordNetworkEvents.ByReference network_events;
    public DiscordVersion network_version= new DiscordVersion();
    public IDiscordOverlayEvents.ByReference overlay_events;
    public DiscordVersion overlay_version= new DiscordVersion();
    public Pointer storage_events;// void*
    public DiscordVersion storage_version= new DiscordVersion();
    public IDiscordStoreEvents.ByReference store_events;
    public DiscordVersion store_version= new DiscordVersion();
    public IDiscordVoiceEvents.ByReference voice_events;
    public DiscordVersion voice_version= new DiscordVersion();
    public IDiscordAchievementEvents.ByReference achievement_events;
    public DiscordVersion achievement_version= new DiscordVersion();

    public static class ByReference extends DiscordCreateParams implements Structure.ByReference { public ByReference() {super();} public ByReference(Pointer pointer) {super(pointer);}}
    public static class ByValue extends DiscordCreateParams implements Structure.ByValue { public ByValue() {super();} public ByValue(Pointer pointer) {super(pointer);}}
}
