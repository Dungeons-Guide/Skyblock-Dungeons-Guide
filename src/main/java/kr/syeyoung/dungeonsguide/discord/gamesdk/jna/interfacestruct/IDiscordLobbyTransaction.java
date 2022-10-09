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

package kr.syeyoung.dungeonsguide.discord.gamesdk.jna.interfacestruct;

import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import kr.syeyoung.dungeonsguide.discord.gamesdk.jna.datastruct.DiscordStruct;
import kr.syeyoung.dungeonsguide.discord.gamesdk.jna.enumuration.EDiscordLobbyType;
import kr.syeyoung.dungeonsguide.discord.gamesdk.jna.enumuration.EDiscordResult;
import kr.syeyoung.dungeonsguide.discord.gamesdk.jna.typedef.UInt32;
import kr.syeyoung.dungeonsguide.discord.gamesdk.jna.typedef.DiscordSnowflake;

import java.util.Arrays;
import java.util.List;

public class IDiscordLobbyTransaction extends DiscordStruct { public IDiscordLobbyTransaction() {super();} public IDiscordLobbyTransaction(Pointer pointer) {super(pointer);}
    public interface SetTypeCallback extends GameSDKCallback { EDiscordResult setType(IDiscordLobbyTransaction lobbyTransaction, EDiscordLobbyType type); }
    public SetTypeCallback SetType;

    public interface SetOwnerCallback extends GameSDKCallback { EDiscordResult setOwner(IDiscordLobbyTransaction lobbyTransaction, DiscordSnowflake ownerId); }
    public SetOwnerCallback SetOwner;

    public interface SetCapacityCallback extends GameSDKCallback { EDiscordResult setCapacity(IDiscordLobbyTransaction lobbyTransaction, UInt32 capacity); }
    public SetCapacityCallback SetCapacity;

    public interface SetMetadataCallback extends GameSDKCallback { EDiscordResult setMetadata(IDiscordLobbyTransaction lobbyTransaction, Pointer key, Pointer value); }
    public SetMetadataCallback SetMetadata;

    public interface DeleteMetadataCallback extends GameSDKCallback { EDiscordResult deleteMetadata(IDiscordLobbyTransaction lobbyTransaction, Pointer key); }
    public DeleteMetadataCallback DeleteMetadata;

    public interface SetLockedCallback extends GameSDKCallback { EDiscordResult setLocked(IDiscordLobbyTransaction lobbyTransaction, boolean locked); }
    public SetLockedCallback SetLocked;



    public static class ByReference extends IDiscordLobbyTransaction implements Structure.ByReference { public ByReference() {super();} public ByReference(Pointer pointer) {super(pointer);}}
    public static class ByValue extends IDiscordLobbyTransaction implements Structure.ByValue { public ByValue() {super();} public ByValue(Pointer pointer) {super(pointer);}}

    @Override protected List getFieldOrder() { return Arrays.asList("SetType", "SetOwner", "SetCapacity", "SetMetadata", "DeleteMetadata", "SetLocked"); }
}
