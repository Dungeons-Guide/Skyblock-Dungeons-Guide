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
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.ptr.ByteByReference;
import com.sun.jna.ptr.LongByReference;
import kr.syeyoung.dungeonsguide.gamesdk.jna.datastruct.DiscordStruct;
import kr.syeyoung.dungeonsguide.gamesdk.jna.enumuration.EDiscordResult;
import kr.syeyoung.dungeonsguide.gamesdk.jna.typedef.DiscordNetworkChannelId;
import kr.syeyoung.dungeonsguide.gamesdk.jna.typedef.DiscordNetworkPeerId;
import kr.syeyoung.dungeonsguide.gamesdk.jna.typedef.UInt32;

public class IDiscordNetworkManager extends DiscordStruct { public IDiscordNetworkManager() {super();} public IDiscordNetworkManager(Pointer pointer) {super(pointer);}
    public interface GetPeerIdCallback extends GameSDKCallback { void getPeerId(IDiscordNetworkManager manager, LongByReference peerId); }
    public GetPeerIdCallback GetPeerId;

    public interface FlushCallback extends GameSDKCallback { EDiscordResult flush(IDiscordNetworkManager manager); }
    public FlushCallback Flush;

    public interface OpenPeerCallback extends GameSDKCallback { EDiscordResult openPeer(IDiscordNetworkManager manager, DiscordNetworkPeerId peerId, String routeData); }
    public OpenPeerCallback OpenPeer;

    public interface UpdatePeerCallback extends GameSDKCallback { EDiscordResult updatePeer(IDiscordNetworkManager manager, DiscordNetworkPeerId peerId, String routeData); }
    public UpdatePeerCallback UpdatePeer;

    public interface ClosePeerCallback extends GameSDKCallback { EDiscordResult closePeer(IDiscordNetworkManager manager, DiscordNetworkPeerId peerId); }
    public ClosePeerCallback ClosePeer;

    public interface OpenChannelCallback extends GameSDKCallback { EDiscordResult openChannel(IDiscordNetworkManager manager, DiscordNetworkPeerId peerId, DiscordNetworkChannelId channelId, boolean reliable); }
    public OpenChannelCallback OpenChannel;

    public interface CloseChannelCallback extends GameSDKCallback { EDiscordResult closeChannel(IDiscordNetworkManager manager, DiscordNetworkPeerId peerId, DiscordNetworkChannelId channelId); }
    public CloseChannelCallback CloseChannel;

    public interface SendMessageCallback extends GameSDKCallback { EDiscordResult sendMessage(IDiscordNetworkManager manager, DiscordNetworkPeerId peerId, DiscordNetworkChannelId channelId, ByteByReference data, UInt32 dataLength); }
    public SendMessageCallback SendMessage;



    public static class ByReference extends IDiscordNetworkManager implements Structure.ByReference { public ByReference() {super();} public ByReference(Pointer pointer) {super(pointer);}}
    public static class ByValue extends IDiscordNetworkManager implements Structure.ByValue { public ByValue() {super();} public ByValue(Pointer pointer) {super(pointer);}}
}
