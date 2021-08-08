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
import com.sun.jna.ptr.IntByReference;
import kr.syeyoung.dungeonsguide.gamesdk.jna.datastruct.DiscordStruct;
import com.sun.jna.Pointer;
import kr.syeyoung.dungeonsguide.gamesdk.jna.datastruct.DiscordRelationship;
import kr.syeyoung.dungeonsguide.gamesdk.jna.enumuration.EDiscordResult;
import kr.syeyoung.dungeonsguide.gamesdk.jna.typedef.UInt32;
import kr.syeyoung.dungeonsguide.gamesdk.jna.typedef.DiscordSnowflake;

import java.util.Arrays;
import java.util.List;

public class IDiscordRelationshipManager extends DiscordStruct { public IDiscordRelationshipManager() {super();} public IDiscordRelationshipManager(Pointer pointer) {super(pointer);}
    public interface FilterCallback extends GameSDKCallback { void filter(IDiscordRelationshipManager manager, Pointer filterData, FilterCallback_Callback filter); }
    public interface FilterCallback_Callback extends GameSDKCallback { boolean filter(Pointer filterData, Structure relationShip);}
    public FilterCallback Filter;

    public interface CountCallback extends GameSDKCallback { EDiscordResult count(IDiscordRelationshipManager manager, IntByReference count); }
    public CountCallback Count;

    public interface GetCallback extends GameSDKCallback { EDiscordResult get(IDiscordRelationshipManager manager, DiscordSnowflake userId, DiscordRelationship relationship); }
    public GetCallback Get;

    public interface GetAtCallback extends GameSDKCallback { EDiscordResult getAt(IDiscordRelationshipManager manager, UInt32 index, DiscordRelationship relationship); }
    public GetAtCallback GetAt;



    public static class ByReference extends IDiscordRelationshipManager implements Structure.ByReference { public ByReference() {super();} public ByReference(Pointer pointer) {super(pointer);}}
    public static class ByValue extends IDiscordRelationshipManager implements Structure.ByValue { public ByValue() {super();} public ByValue(Pointer pointer) {super(pointer);}}

    @Override protected List getFieldOrder() { return Arrays.asList("Filter", "Count", "Get", "GetAt"); }
}
