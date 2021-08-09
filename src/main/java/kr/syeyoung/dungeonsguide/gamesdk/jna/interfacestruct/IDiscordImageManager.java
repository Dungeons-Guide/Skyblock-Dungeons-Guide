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
import kr.syeyoung.dungeonsguide.gamesdk.jna.datastruct.DiscordStruct;
import com.sun.jna.Pointer;
import kr.syeyoung.dungeonsguide.gamesdk.jna.enumuration.EDiscordResult;
import kr.syeyoung.dungeonsguide.gamesdk.jna.datastruct.DiscordImageDimensions;
import kr.syeyoung.dungeonsguide.gamesdk.jna.typedef.UInt32;
import kr.syeyoung.dungeonsguide.gamesdk.jna.datastruct.DiscordImageHandle;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;

public class IDiscordImageManager extends DiscordStruct { public IDiscordImageManager() {super();} public IDiscordImageManager(Pointer pointer) {super(pointer);}
    public interface FetchCallback_Callback extends GameSDKCallback {
        void callback(Pointer callbackData, EDiscordResult result, DiscordImageHandle.ByValue handleResult);
    }
    public interface FetchCallback extends GameSDKCallback { void fetch(IDiscordImageManager manager, DiscordImageHandle.ByValue handle, boolean refresh, Pointer callbackData, FetchCallback_Callback callback); }
    public FetchCallback Fetch;

    public interface GetDimensionsCallback extends GameSDKCallback { EDiscordResult getDimensions(IDiscordImageManager manager, DiscordImageHandle.ByValue handle, DiscordImageDimensions dimensions); }
    public GetDimensionsCallback GetDimensions;

    public interface GetDataCallback extends GameSDKCallback { EDiscordResult getData(IDiscordImageManager manager, DiscordImageHandle.ByValue handle, ByteBuffer data, UInt32 dataLength); }
    public GetDataCallback GetData;



    public static class ByReference extends IDiscordImageManager implements Structure.ByReference { public ByReference() {super();} public ByReference(Pointer pointer) {super(pointer);}}
    public static class ByValue extends IDiscordImageManager implements Structure.ByValue { public ByValue() {super();} public ByValue(Pointer pointer) {super(pointer);}}

    @Override protected List getFieldOrder() { return Arrays.asList("Fetch", "GetDimensions", "GetData"); }
}
