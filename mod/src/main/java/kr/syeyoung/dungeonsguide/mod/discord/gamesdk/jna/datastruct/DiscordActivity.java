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
import kr.syeyoung.dungeonsguide.mod.discord.gamesdk.jna.enumuration.EDiscordActivityType;
import kr.syeyoung.dungeonsguide.mod.discord.gamesdk.jna.typedef.Int64;
import kr.syeyoung.dungeonsguide.mod.discord.gamesdk.jna.typedef.UInt32;

import java.util.Arrays;
import java.util.List;


public class DiscordActivity extends DiscordStruct {
    public EDiscordActivityType activityType = EDiscordActivityType.DiscordActivityType_Playing;
    public Int64 applicationId = new Int64();
    public byte[] name = new byte[128];
    public byte[] state = new byte[128];
    public byte[] details = new byte[128];
    public DiscordActivityTimestamps timestamps;
    public DiscordActivityAssets assets;
    public DiscordActivityParty party;
    public DiscordActivitySecrets secrets;
    public boolean instance;
    public UInt32 supported_platform;

    public DiscordActivity() {super();} public DiscordActivity(Pointer pointer) {super(pointer);}

    public static class ByReference extends DiscordActivity implements Structure.ByReference { public ByReference() {super();} public ByReference(Pointer pointer) {super(pointer);}}
    public static class ByValue extends DiscordActivity implements Structure.ByValue { public ByValue() {super();} public ByValue(Pointer pointer) {super(pointer);}}

    @Override
    protected List getFieldOrder() {
        return Arrays.asList("activityType", "applicationId", "name", "state", "details", "timestamps", "assets", "party", "secrets", "instance", "supported_platform");
    }
}
