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

package kr.syeyoung.dungeonsguide.mod.discord.gamesdk.jna.interfacestruct;

import com.sun.jna.Structure;
import kr.syeyoung.dungeonsguide.mod.discord.gamesdk.jna.datastruct.DiscordStruct;
import com.sun.jna.Pointer;
import kr.syeyoung.dungeonsguide.mod.discord.gamesdk.jna.enumuration.EDiscordActivityActionType;
import kr.syeyoung.dungeonsguide.mod.discord.gamesdk.jna.datastruct.DiscordActivity;
import kr.syeyoung.dungeonsguide.mod.discord.gamesdk.jna.datastruct.DiscordUser;

import java.util.Arrays;
import java.util.List;

public class IDiscordActivityEvents extends DiscordStruct { public IDiscordActivityEvents() {super();} public IDiscordActivityEvents(Pointer pointer) {super(pointer);}
    public interface OnActivityJoinCallback extends GameSDKCallback { void onActivityJoin(Pointer eventData, String secret); }
    public OnActivityJoinCallback OnActivityJoin;

    public interface OnActivitySpectateCallback extends GameSDKCallback { void onActivitySpectate(Pointer eventData, String secret); }
    public OnActivitySpectateCallback OnActivitySpectate;

    public interface OnActivityJoinRequestCallback extends GameSDKCallback { void onActivityJoinRequest(Pointer eventData, DiscordUser user); }
    public OnActivityJoinRequestCallback OnActivityJoinRequest;

    public interface OnActivityInviteCallback extends GameSDKCallback { void onActivityInvite(Pointer eventData, EDiscordActivityActionType type, DiscordUser user, DiscordActivity activity); }
    public OnActivityInviteCallback OnActivityInvite;



    public static class ByReference extends IDiscordActivityEvents implements Structure.ByReference { public ByReference() {super();} public ByReference(Pointer pointer) {super(pointer);}}
    public static class ByValue extends IDiscordActivityEvents implements Structure.ByValue { public ByValue() {super();} public ByValue(Pointer pointer) {super(pointer);}}

    @Override protected List getFieldOrder() { return Arrays.asList("OnActivityJoin", "OnActivitySpectate", "OnActivityJoinRequest", "OnActivityInvite"); }
}
