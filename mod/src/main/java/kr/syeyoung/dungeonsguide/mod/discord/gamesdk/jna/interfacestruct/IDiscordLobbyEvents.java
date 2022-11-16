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

import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.ptr.ByteByReference;
import kr.syeyoung.dungeonsguide.mod.discord.gamesdk.jna.datastruct.DiscordStruct;
import kr.syeyoung.dungeonsguide.mod.discord.gamesdk.jna.typedef.Int64;
import kr.syeyoung.dungeonsguide.mod.discord.gamesdk.jna.typedef.UInt32;
import kr.syeyoung.dungeonsguide.mod.discord.gamesdk.jna.typedef.UInt8;

import java.util.Arrays;
import java.util.List;

public class IDiscordLobbyEvents extends DiscordStruct { public IDiscordLobbyEvents() {super();} public IDiscordLobbyEvents(Pointer pointer) {super(pointer);}
    public interface OnLobbyUpdateCallback extends GameSDKCallback { void onLobbyUpdate(Pointer eventData, Int64 lobbyId); }
    public OnLobbyUpdateCallback OnLobbyUpdate;

    public interface OnLobbyDeleteCallback extends GameSDKCallback { void onLobbyDelete(Pointer eventData, Int64 lobbyId, UInt32 reason); }
    public OnLobbyDeleteCallback OnLobbyDelete;

    public interface OnMemberConnectCallback extends GameSDKCallback { void onMemberConnect(Pointer eventData, Int64 lobbyId, Int64 userId); }
    public OnMemberConnectCallback OnMemberConnect;

    public interface OnMemberUpdateCallback extends GameSDKCallback { void onMemberUpdate(Pointer eventData, Int64 lobbyId, Int64 userId); }
    public OnMemberUpdateCallback OnMemberUpdate;

    public interface OnMemberDisconnectCallback extends GameSDKCallback { void onMemberDisconnect(Pointer eventData, Int64 lobbyId, Int64 userId); }
    public OnMemberDisconnectCallback OnMemberDisconnect;

    public interface OnLobbyMessageCallback extends GameSDKCallback { void onLobbyMessage(Pointer eventData, Int64 lobbyId, Int64 userId, ByteByReference data, UInt32 dataLength); }
    public OnLobbyMessageCallback OnLobbyMessage;

    public interface OnSpeakingCallback extends GameSDKCallback { void onSpeaking(Pointer eventData, Int64 lobbyId, Int64 userId, boolean speaking); }
    public OnSpeakingCallback OnSpeaking;

    public interface OnNetworkMessageCallback extends GameSDKCallback { void onNetworkMessage(Pointer eventData, Int64 lobbyId, Int64 userId, UInt8 channelId, ByteByReference data, UInt32 dataLength); }
    public OnNetworkMessageCallback OnNetworkMessage;



    public static class ByReference extends IDiscordLobbyEvents implements Structure.ByReference { public ByReference() {super();} public ByReference(Pointer pointer) {super(pointer);}}
    public static class ByValue extends IDiscordLobbyEvents implements Structure.ByValue { public ByValue() {super();} public ByValue(Pointer pointer) {super(pointer);}}


    @Override protected List getFieldOrder() { return Arrays.asList("OnLobbyUpdate", "OnLobbyDelete", "OnMemberConnect", "OnMemberUpdate", "OnMemberDisconnect", "OnLobbyMessage", "OnSpeaking", "OnNetworkMessage"); }
}
