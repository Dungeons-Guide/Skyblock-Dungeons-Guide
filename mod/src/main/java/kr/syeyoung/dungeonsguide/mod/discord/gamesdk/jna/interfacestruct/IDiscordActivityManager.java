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
import kr.syeyoung.dungeonsguide.mod.discord.gamesdk.jna.NativeGameSDK;
import kr.syeyoung.dungeonsguide.mod.discord.gamesdk.jna.datastruct.DiscordStruct;
import com.sun.jna.Pointer;
import kr.syeyoung.dungeonsguide.mod.discord.gamesdk.jna.enumuration.EDiscordActivityActionType;
import kr.syeyoung.dungeonsguide.mod.discord.gamesdk.jna.enumuration.EDiscordResult;
import kr.syeyoung.dungeonsguide.mod.discord.gamesdk.jna.typedef.UInt32;
import kr.syeyoung.dungeonsguide.mod.discord.gamesdk.jna.datastruct.DiscordActivity;
import kr.syeyoung.dungeonsguide.mod.discord.gamesdk.jna.typedef.DiscordSnowflake;
import kr.syeyoung.dungeonsguide.mod.discord.gamesdk.jna.enumuration.EDiscordActivityJoinRequestReply;

import java.util.Arrays;
import java.util.List;

public class IDiscordActivityManager extends DiscordStruct { public IDiscordActivityManager() {super();} public IDiscordActivityManager(Pointer pointer) {super(pointer);}
    public interface RegisterCommandCallback extends GameSDKCallback { EDiscordResult registerCommand(IDiscordActivityManager manager, String command); }
    public RegisterCommandCallback RegisterCommand;

    public interface RegisterSteamCallback extends GameSDKCallback { EDiscordResult registerSteam(IDiscordActivityManager manager, UInt32 steamId); }
    public RegisterSteamCallback RegisterSteam;

    public interface UpdateActivityCallback extends GameSDKCallback { void updateActivity(IDiscordActivityManager manager, DiscordActivity activity, Pointer callbackData, NativeGameSDK.DiscordCallback callback); }
    public UpdateActivityCallback UpdateActivity;

    public interface ClearActivityCallback extends GameSDKCallback { void clearActivity(IDiscordActivityManager manager, Pointer callbackData, NativeGameSDK.DiscordCallback callback); }
    public ClearActivityCallback ClearActivity;

    public interface SendRequestReplyCallback extends GameSDKCallback { void sendRequestReply(IDiscordActivityManager manager, DiscordSnowflake userId, EDiscordActivityJoinRequestReply reply, Pointer callbackData, NativeGameSDK.DiscordCallback callback); }
    public SendRequestReplyCallback SendRequestReply;

    public interface SendInviteCallback extends GameSDKCallback { void sendInvite(IDiscordActivityManager manager, DiscordSnowflake userId, EDiscordActivityActionType type, String content, Pointer callbackData, NativeGameSDK.DiscordCallback callback); }
    public SendInviteCallback SendInvite;

    public interface AcceptInviteCallback extends GameSDKCallback { void acceptInvite(IDiscordActivityManager manager, DiscordSnowflake userId, Pointer callbackData, NativeGameSDK.DiscordCallback callback); }
    public AcceptInviteCallback AcceptInvite;



    public static class ByReference extends IDiscordActivityManager implements Structure.ByReference { public ByReference() {super();} public ByReference(Pointer pointer) {super(pointer);}}
    public static class ByValue extends IDiscordActivityManager implements Structure.ByValue { public ByValue() {super();} public ByValue(Pointer pointer) {super(pointer);}}

    @Override protected List getFieldOrder() { return Arrays.asList("RegisterCommand", "RegisterSteam", "UpdateActivity", "ClearActivity", "SendRequestReply", "SendInvite", "AcceptInvite"); }
}
