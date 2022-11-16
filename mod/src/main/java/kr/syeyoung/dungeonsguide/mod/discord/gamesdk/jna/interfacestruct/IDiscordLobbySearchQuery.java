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
import kr.syeyoung.dungeonsguide.mod.discord.gamesdk.jna.datastruct.DiscordStruct;
import kr.syeyoung.dungeonsguide.mod.discord.gamesdk.jna.enumuration.EDiscordLobbySearchCast;
import kr.syeyoung.dungeonsguide.mod.discord.gamesdk.jna.enumuration.EDiscordLobbySearchComparison;
import kr.syeyoung.dungeonsguide.mod.discord.gamesdk.jna.enumuration.EDiscordLobbySearchDistance;
import kr.syeyoung.dungeonsguide.mod.discord.gamesdk.jna.enumuration.EDiscordResult;
import kr.syeyoung.dungeonsguide.mod.discord.gamesdk.jna.typedef.UInt32;

import java.util.Arrays;
import java.util.List;

public class IDiscordLobbySearchQuery extends DiscordStruct { public IDiscordLobbySearchQuery() {super();} public IDiscordLobbySearchQuery(Pointer pointer) {super(pointer);}
    public interface FilterCallback extends GameSDKCallback { EDiscordResult filter(IDiscordLobbySearchQuery lobbySearchQuery, Pointer key, EDiscordLobbySearchComparison comparison, EDiscordLobbySearchCast cast, Pointer value); }
    public FilterCallback Filter;

    public interface SortCallback extends GameSDKCallback { EDiscordResult sort(IDiscordLobbySearchQuery lobbySearchQuery, Pointer key, EDiscordLobbySearchCast cast, Pointer value); }
    public SortCallback Sort;

    public interface LimitCallback extends GameSDKCallback { EDiscordResult limit(IDiscordLobbySearchQuery lobbySearchQuery, UInt32 limit); }
    public LimitCallback Limit;

    public interface DistanceCallback extends GameSDKCallback { EDiscordResult distance(IDiscordLobbySearchQuery lobbySearchQuery, EDiscordLobbySearchDistance distance); }
    public DistanceCallback Distance;



    public static class ByReference extends IDiscordLobbySearchQuery implements Structure.ByReference { public ByReference() {super();} public ByReference(Pointer pointer) {super(pointer);}}
    public static class ByValue extends IDiscordLobbySearchQuery implements Structure.ByValue { public ByValue() {super();} public ByValue(Pointer pointer) {super(pointer);}}

    @Override protected List getFieldOrder() { return Arrays.asList("Filter", "Sort", "Limit", "Distance"); }
}
