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
import kr.syeyoung.dungeonsguide.gamesdk.jna.typedef.Int32;
import kr.syeyoung.dungeonsguide.gamesdk.jna.datastruct.DiscordFileStat;
import kr.syeyoung.dungeonsguide.gamesdk.jna.typedef.UInt32;
import kr.syeyoung.dungeonsguide.gamesdk.jna.typedef.UInt64;

public class IDiscordStorageManager extends DiscordStruct { public IDiscordStorageManager() {super();} public IDiscordStorageManager(Pointer pointer) {super(pointer);}
    public interface ReadCallback extends GameSDKCallback { EDiscordResult read(IDiscordStorageManager manager, String name, ByteByReference data, UInt32 dataLength, IntByReference read); }
    public ReadCallback Read;

    public interface DiscordStorageManagerCallback extends GameSDKCallback {
        void callback(Pointer callbackData, EDiscordResult result, ByteByReference data, IntByReference data_length);
    }

    public interface ReadAsyncCallback extends GameSDKCallback { void readAsync(IDiscordStorageManager manager, String name, Pointer callbackData, DiscordStorageManagerCallback callback); }
    public ReadAsyncCallback ReadAsync;

    public interface ReadAsyncPartialCallback extends GameSDKCallback { void readAsyncPartial(IDiscordStorageManager manager, String name, UInt64 offset, UInt64 length, Pointer callbackData,  DiscordStorageManagerCallback callback); }
    public ReadAsyncPartialCallback ReadAsyncPartial;

    public interface WriteCallback extends GameSDKCallback { EDiscordResult write(IDiscordStorageManager manager, String name, ByteByReference data, UInt32 dataLength); }
    public WriteCallback Write;

    public interface WriteAsyncCallback extends GameSDKCallback { void writeAsync(IDiscordStorageManager manager, String name, ByteByReference data, UInt32 dataLength, Pointer callbackData, NativeGameSDK.DiscordCallback callback); }
    public WriteAsyncCallback WriteAsync;

    public interface DeleteCallback extends GameSDKCallback { EDiscordResult delete(IDiscordStorageManager manager, String name); }
    public DeleteCallback Delete;

    public interface ExistsCallback extends GameSDKCallback { EDiscordResult exists(IDiscordStorageManager manager, String name, ByteByReference exists); } // exists actually boolean
    public ExistsCallback Exists;

    public interface CountCallback extends GameSDKCallback { void count(IDiscordStorageManager manager, IntByReference count); }
    public CountCallback Count;

    public interface StatCallback extends GameSDKCallback { EDiscordResult stat(IDiscordStorageManager manager, String name, DiscordFileStat stat); }
    public StatCallback Stat;

    public interface StatAtCallback extends GameSDKCallback { EDiscordResult statAt(IDiscordStorageManager manager, Int32 index, DiscordFileStat stat); }
    public StatAtCallback StatAt;

    public interface GetPathCallback extends GameSDKCallback { EDiscordResult getPath(IDiscordStorageManager manager, Pointer path); } // path str len 4096
    public GetPathCallback GetPath;



    public static class ByReference extends IDiscordStorageManager implements Structure.ByReference { public ByReference() {super();} public ByReference(Pointer pointer) {super(pointer);}}
    public static class ByValue extends IDiscordStorageManager implements Structure.ByValue { public ByValue() {super();} public ByValue(Pointer pointer) {super(pointer);}}
}
