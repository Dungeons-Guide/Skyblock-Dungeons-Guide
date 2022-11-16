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

import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import kr.syeyoung.dungeonsguide.mod.discord.gamesdk.jna.NativeGameSDK;
import kr.syeyoung.dungeonsguide.mod.discord.gamesdk.jna.interfacestruct.*;
import kr.syeyoung.dungeonsguide.mod.discord.gamesdk.jna.typedef.DiscordClientID;
import kr.syeyoung.dungeonsguide.mod.discord.gamesdk.jna.typedef.DiscordVersion;
import kr.syeyoung.dungeonsguide.mod.discord.gamesdk.jna.typedef.UInt64;

import java.util.Arrays;
import java.util.List;

public class DiscordCreateParams extends DiscordStruct { public DiscordCreateParams() {super();} public DiscordCreateParams(Pointer pointer) {super(pointer);}
    public DiscordClientID client_id = new DiscordClientID();
    public UInt64 flags = new UInt64();
    public Pointer events; // void*
    public Pointer event_data; // void*
    public Pointer application_events;
    public DiscordVersion application_version = new DiscordVersion(NativeGameSDK.DISCORD_APPLICATION_MANAGER_VERSION);
    public IDiscordUserEvents.ByReference user_events;
    public DiscordVersion user_version= new DiscordVersion(NativeGameSDK.DISCORD_USER_MANAGER_VERSION);
    public Pointer image_events;// void*
    public DiscordVersion image_version= new DiscordVersion(NativeGameSDK.DISCORD_IMAGE_MANAGER_VERSION);
    public IDiscordActivityEvents.ByReference activity_events;
    public DiscordVersion activity_version= new DiscordVersion(NativeGameSDK.DISCORD_ACTIVITY_MANAGER_VERSION);
    public IDiscordRelationshipEvents.ByReference relationship_events;
    public DiscordVersion relationship_version= new DiscordVersion(NativeGameSDK.DISCORD_RELATIONSHIP_MANAGER_VERSION);
    public IDiscordLobbyEvents.ByReference lobby_events;
    public DiscordVersion lobby_version= new DiscordVersion(NativeGameSDK.DISCORD_LOBBY_MANAGER_VERSION);
    public IDiscordNetworkEvents.ByReference network_events;
    public DiscordVersion network_version= new DiscordVersion(NativeGameSDK.DISCORD_NETWORK_MANAGER_VERSION);
    public IDiscordOverlayEvents.ByReference overlay_events;
    public DiscordVersion overlay_version= new DiscordVersion(NativeGameSDK.DISCORD_OVERLAY_MANAGER_VERSION);
    public Pointer storage_events;// void*
    public DiscordVersion storage_version= new DiscordVersion(NativeGameSDK.DISCORD_STORAGE_MANAGER_VERSION);
    public IDiscordStoreEvents.ByReference store_events;
    public DiscordVersion store_version= new DiscordVersion(NativeGameSDK.DISCORD_STORE_MANAGER_VERSION);
    public IDiscordVoiceEvents.ByReference voice_events;
    public DiscordVersion voice_version= new DiscordVersion(NativeGameSDK.DISCORD_VOICE_MANAGER_VERSION);
    public IDiscordAchievementEvents.ByReference achievement_events;
    public DiscordVersion achievement_version= new DiscordVersion(NativeGameSDK.DISCORD_ACHIEVEMENT_MANAGER_VERSION);

    public static class ByReference extends DiscordCreateParams implements Structure.ByReference { public ByReference() {super();} public ByReference(Pointer pointer) {super(pointer);}}
    public static class ByValue extends DiscordCreateParams implements Structure.ByValue { public ByValue() {super();} public ByValue(Pointer pointer) {super(pointer);}}

    @Override
    protected List getFieldOrder() {
        return Arrays.asList("client_id", "flags", "events", "event_data", "application_events", "application_version", "user_events",
                "user_version", "image_events", "image_version", "activity_events", "activity_version", "lobby_events", "lobby_version",
                "network_events", "network_version", "overlay_events", "overlay_version", "storage_events", "storage_version", "store_events",
                "store_version", "voice_events", "voice_version", "achievement_events", "achievement_version");
    }
}
