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

package kr.syeyoung.dungeonsguide.mod.discord.gamesdk.jna;

import com.sun.jna.Callback;
import com.sun.jna.Library;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.PointerByReference;
import kr.syeyoung.dungeonsguide.mod.discord.gamesdk.jna.datastruct.DiscordCreateParams;
import kr.syeyoung.dungeonsguide.mod.discord.gamesdk.jna.enumuration.EDiscordResult;
import kr.syeyoung.dungeonsguide.mod.discord.gamesdk.jna.typedef.DiscordVersion;

public interface NativeGameSDK extends Library {
    
    public static final int DISCORD_VERSION = 2;
    public static final int DISCORD_APPLICATION_MANAGER_VERSION = 1;
    public static final int DISCORD_USER_MANAGER_VERSION = 1;
    public static final int DISCORD_IMAGE_MANAGER_VERSION = 1;
    public static final int DISCORD_ACTIVITY_MANAGER_VERSION = 1;
    public static final int DISCORD_RELATIONSHIP_MANAGER_VERSION = 1;
    public static final int DISCORD_LOBBY_MANAGER_VERSION = 1;
    public static final int DISCORD_NETWORK_MANAGER_VERSION = 1;
    public static final int DISCORD_OVERLAY_MANAGER_VERSION = 1;
    public static final int DISCORD_STORAGE_MANAGER_VERSION = 1;
    public static final int DISCORD_STORE_MANAGER_VERSION = 1;
    public static final int DISCORD_VOICE_MANAGER_VERSION = 1;
    public static final int DISCORD_ACHIEVEMENT_MANAGER_VERSION = 1;

    EDiscordResult DiscordCreate(DiscordVersion version, DiscordCreateParams params, PointerByReference result); // result is double pointer of IDiscordCore

    interface DiscordCallback extends Callback {
        void callback(Pointer callbackData, EDiscordResult result);
    }
}
