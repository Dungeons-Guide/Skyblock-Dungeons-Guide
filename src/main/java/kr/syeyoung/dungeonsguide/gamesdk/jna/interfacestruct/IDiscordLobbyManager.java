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

package kr.syeyoung.dungeonsguide.gamesdk.jna.interfacestruct;

import com.sun.jna.Callback;
import com.sun.jna.Structure;
import com.sun.jna.ptr.ByteByReference;
import com.sun.jna.ptr.IntByReference;
import kr.syeyoung.dungeonsguide.gamesdk.jna.NativeGameSDK;
import kr.syeyoung.dungeonsguide.gamesdk.jna.datastruct.DiscordStruct;
import com.sun.jna.Pointer;
import kr.syeyoung.dungeonsguide.gamesdk.jna.enumuration.EDiscordResult;
import kr.syeyoung.dungeonsguide.gamesdk.jna.datastruct.DiscordLobby;
import kr.syeyoung.dungeonsguide.gamesdk.jna.typedef.Int32;
import kr.syeyoung.dungeonsguide.gamesdk.jna.typedef.UInt8;
import kr.syeyoung.dungeonsguide.gamesdk.jna.typedef.UInt32;
import kr.syeyoung.dungeonsguide.gamesdk.jna.typedef.DiscordSnowflake;
import kr.syeyoung.dungeonsguide.gamesdk.jna.datastruct.DiscordUser;

public class IDiscordLobbyManager extends DiscordStruct { public IDiscordLobbyManager() {super();} public IDiscordLobbyManager(Pointer pointer) {super(pointer);}
    public interface GetLobbyCreateTransactionCallback extends GameSDKCallback { EDiscordResult getLobbyCreateTransaction(IDiscordLobbyManager manager, IDiscordLobbyTransaction transaction); }
    public GetLobbyCreateTransactionCallback GetLobbyCreateTransaction;

    public interface GetLobbyUpdateTransactionCallback extends GameSDKCallback { EDiscordResult getLobbyUpdateTransaction(IDiscordLobbyManager manager, DiscordSnowflake lobbyId, IDiscordLobbyTransaction transaction); }
    public GetLobbyUpdateTransactionCallback GetLobbyUpdateTransaction;

    public interface GetMemberUpdateTransactionCallback extends GameSDKCallback { EDiscordResult getMemberUpdateTransaction(IDiscordLobbyManager manager, DiscordSnowflake lobbyId, DiscordSnowflake userId, IDiscordLobbyMemberTransaction transaction); }
    public GetMemberUpdateTransactionCallback GetMemberUpdateTransaction;

    public interface DiscordLobbyManagerCallback extends GameSDKCallback {
        void callback(Pointer callbackData, EDiscordResult result, DiscordLobby lobby);
    }

    public interface CreateLobbyCallback extends GameSDKCallback { void createLobby(IDiscordLobbyManager manager, IDiscordLobbyTransaction transaction, Pointer callbackData,DiscordLobbyManagerCallback callback); }
    public CreateLobbyCallback CreateLobby;

    public interface UpdateLobbyCallback extends GameSDKCallback { void updateLobby(IDiscordLobbyManager manager, DiscordSnowflake lobbyId, IDiscordLobbyTransaction transaction, Pointer callbackData, NativeGameSDK.DiscordCallback callback); }
    public UpdateLobbyCallback UpdateLobby;

    public interface DeleteLobbyCallback extends GameSDKCallback { void deleteLobby(IDiscordLobbyManager manager, DiscordSnowflake lobbyId, Pointer callbackData, NativeGameSDK.DiscordCallback callback); }
    public DeleteLobbyCallback DeleteLobby;

    public interface ConnectLobbyCallback extends GameSDKCallback { void connectLobby(IDiscordLobbyManager manager, DiscordSnowflake lobbyId, Pointer secret, Pointer callbackData, DiscordLobbyManagerCallback callback); }// secret 128 byte long str
    public ConnectLobbyCallback ConnectLobby;

    public interface ConnectLobbyWithActivitySecretCallback extends GameSDKCallback { void connectLobbyWithActivitySecret(IDiscordLobbyManager manager, Pointer activitySecret, Pointer callbackData, DiscordLobbyManagerCallback callback); } // secret 128 byte long str
    public ConnectLobbyWithActivitySecretCallback ConnectLobbyWithActivitySecret;

    public interface DisconnectLobbyCallback extends GameSDKCallback { void disconnectLobby(IDiscordLobbyManager manager, DiscordSnowflake lobbyId, Pointer callbackData, NativeGameSDK.DiscordCallback callback); }
    public DisconnectLobbyCallback DisconnectLobby;

    public interface GetLobbyCallback extends GameSDKCallback { EDiscordResult getLobby(IDiscordLobbyManager manager, DiscordSnowflake lobbyId, DiscordLobby lobby); }
    public GetLobbyCallback GetLobby;

    public interface GetLobbyActivitySecretCallback extends GameSDKCallback { EDiscordResult getLobbyActivitySecret(IDiscordLobbyManager manager, DiscordSnowflake lobbyId, Pointer secret); } // pointer to char[128]
    public GetLobbyActivitySecretCallback GetLobbyActivitySecret;

    public interface GetLobbyMetadataValueCallback extends GameSDKCallback { EDiscordResult getLobbyMetadataValue(IDiscordLobbyManager manager, DiscordSnowflake lobbyId, Pointer key, Pointer value); } // key: char[256] value: pointer to char[4096]
    public GetLobbyMetadataValueCallback GetLobbyMetadataValue;

    public interface GetLobbyMetadataKeyCallback extends GameSDKCallback { EDiscordResult getLobbyMetadataKey(IDiscordLobbyManager manager, DiscordSnowflake lobbyId, Int32 index, Pointer key); }// key: pointer to char[256]
    public GetLobbyMetadataKeyCallback GetLobbyMetadataKey;

    public interface LobbyMetadataCountCallback extends GameSDKCallback { EDiscordResult lobbyMetadataCount(IDiscordLobbyManager manager, DiscordSnowflake lobbyId, IntByReference count); }
    public LobbyMetadataCountCallback LobbyMetadataCount;

    public interface MemberCountCallback extends GameSDKCallback { EDiscordResult memberCount(IDiscordLobbyManager manager, DiscordSnowflake lobbyId, IntByReference count); }
    public MemberCountCallback MemberCount;

    public interface GetMemberUserIdCallback extends GameSDKCallback { EDiscordResult getMemberUserId(IDiscordLobbyManager manager, DiscordSnowflake lobbyId, Int32 index, Pointer userId); } // userID: pointer to DiscordUserID
    public GetMemberUserIdCallback GetMemberUserId;

    public interface GetMemberUserCallback extends GameSDKCallback { EDiscordResult getMemberUser(IDiscordLobbyManager manager, DiscordSnowflake lobbyId, DiscordSnowflake userId, DiscordUser user); }
    public GetMemberUserCallback GetMemberUser;

    public interface GetMemberMetadataValueCallback extends GameSDKCallback { EDiscordResult getMemberMetadataValue(IDiscordLobbyManager manager, DiscordSnowflake lobbyId, DiscordSnowflake userId, Pointer key, Pointer value); }// key: char[256] value: pointer to char[4096]
    public GetMemberMetadataValueCallback GetMemberMetadataValue;

    public interface GetMemberMetadataKeyCallback extends GameSDKCallback { EDiscordResult getMemberMetadataKey(IDiscordLobbyManager manager, DiscordSnowflake lobbyId, DiscordSnowflake userId, Int32 index, Pointer key); }// key: pointer to char[256]
    public GetMemberMetadataKeyCallback GetMemberMetadataKey;

    public interface MemberMetadataCountCallback extends GameSDKCallback { EDiscordResult memberMetadataCount(IDiscordLobbyManager manager, DiscordSnowflake lobbyId, DiscordSnowflake userId, IntByReference count); }
    public MemberMetadataCountCallback MemberMetadataCount;

    public interface UpdateMemberCallback extends GameSDKCallback { void updateMember(IDiscordLobbyManager manager, DiscordSnowflake lobbyId, DiscordSnowflake userId, IDiscordLobbyMemberTransaction transaction, Pointer callbackData, NativeGameSDK.DiscordCallback callback); }
    public UpdateMemberCallback UpdateMember;

    public interface SendLobbyMessageCallback extends GameSDKCallback { void sendLobbyMessage(IDiscordLobbyManager manager, DiscordSnowflake lobbyId, ByteByReference data, UInt32 dataLength, Pointer callbackData, NativeGameSDK.DiscordCallback callback); }
    public SendLobbyMessageCallback SendLobbyMessage;

    public interface GetSearchQueryCallback extends GameSDKCallback { EDiscordResult getSearchQuery(IDiscordLobbyManager manager, IDiscordLobbySearchQuery query); }
    public GetSearchQueryCallback GetSearchQuery;

    public interface SearchCallback extends GameSDKCallback { void search(IDiscordLobbyManager manager, IDiscordLobbySearchQuery query, Pointer callbackData, NativeGameSDK.DiscordCallback callback); }
    public SearchCallback Search;

    public interface LobbyCountCallback extends GameSDKCallback { void lobbyCount(IDiscordLobbyManager manager, IntByReference count); }
    public LobbyCountCallback LobbyCount;

    public interface GetLobbyIdCallback extends GameSDKCallback { EDiscordResult getLobbyId(IDiscordLobbyManager manager, Int32 index, Pointer lobbyId); } // lobbyID: Pointer to DiscordSnowflake
    public GetLobbyIdCallback GetLobbyId;

    public interface ConnectVoiceCallback extends GameSDKCallback { void connectVoice(IDiscordLobbyManager manager, DiscordSnowflake lobbyId, Pointer callbackData, NativeGameSDK.DiscordCallback callback); }
    public ConnectVoiceCallback ConnectVoice;

    public interface DisconnectVoiceCallback extends GameSDKCallback { void disconnectVoice(IDiscordLobbyManager manager, DiscordSnowflake lobbyId, Pointer callbackData, NativeGameSDK.DiscordCallback callback); }
    public DisconnectVoiceCallback DisconnectVoice;

    public interface ConnectNetworkCallback extends GameSDKCallback { EDiscordResult connectNetwork(IDiscordLobbyManager manager, DiscordSnowflake lobbyId); }
    public ConnectNetworkCallback ConnectNetwork;

    public interface DisconnectNetworkCallback extends GameSDKCallback { EDiscordResult disconnectNetwork(IDiscordLobbyManager manager, DiscordSnowflake lobbyId); }
    public DisconnectNetworkCallback DisconnectNetwork;

    public interface FlushNetworkCallback extends GameSDKCallback { EDiscordResult flushNetwork(IDiscordLobbyManager manager); }
    public FlushNetworkCallback FlushNetwork;

    public interface OpenNetworkChannelCallback extends GameSDKCallback { EDiscordResult openNetworkChannel(IDiscordLobbyManager manager, DiscordSnowflake lobbyId, UInt8 channelId, boolean reliable); }
    public OpenNetworkChannelCallback OpenNetworkChannel;

    public interface SendNetworkMessageCallback extends GameSDKCallback { EDiscordResult sendNetworkMessage(IDiscordLobbyManager manager, DiscordSnowflake lobbyId, DiscordSnowflake userId, UInt8 channelId, ByteByReference data, UInt32 dataLength); }
    public SendNetworkMessageCallback SendNetworkMessage;



    public static class ByReference extends IDiscordLobbyManager implements Structure.ByReference { public ByReference() {super();} public ByReference(Pointer pointer) {super(pointer);}}
    public static class ByValue extends IDiscordLobbyManager implements Structure.ByValue { public ByValue() {super();} public ByValue(Pointer pointer) {super(pointer);}}
}
