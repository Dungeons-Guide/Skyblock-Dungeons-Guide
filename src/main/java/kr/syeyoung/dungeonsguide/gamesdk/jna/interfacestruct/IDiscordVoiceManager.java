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
import kr.syeyoung.dungeonsguide.gamesdk.jna.NativeGameSDK;
import kr.syeyoung.dungeonsguide.gamesdk.jna.datastruct.DiscordStruct;
import com.sun.jna.Pointer;
import kr.syeyoung.dungeonsguide.gamesdk.jna.enumuration.EDiscordResult;
import kr.syeyoung.dungeonsguide.gamesdk.jna.typedef.UInt8;
import kr.syeyoung.dungeonsguide.gamesdk.jna.datastruct.DiscordInputMode;
import kr.syeyoung.dungeonsguide.gamesdk.jna.typedef.DiscordSnowflake;

import java.util.Arrays;
import java.util.List;

public class IDiscordVoiceManager extends DiscordStruct { public IDiscordVoiceManager() {super();} public IDiscordVoiceManager(Pointer pointer) {super(pointer);}
    public interface GetInputModeCallback extends GameSDKCallback { EDiscordResult getInputMode(IDiscordVoiceManager manager, DiscordInputMode inputMode); }
    public GetInputModeCallback GetInputMode;

    public interface SetInputModeCallback extends GameSDKCallback { void setInputMode(IDiscordVoiceManager manager, DiscordInputMode.ByValue inputMode, Pointer callbackData, NativeGameSDK.DiscordCallback callback); }
    public SetInputModeCallback SetInputMode;

    public interface IsSelfMuteCallback extends GameSDKCallback { EDiscordResult isSelfMute(IDiscordVoiceManager manager, ByteByReference mute); }
    public IsSelfMuteCallback IsSelfMute;

    public interface SetSelfMuteCallback extends GameSDKCallback { EDiscordResult setSelfMute(IDiscordVoiceManager manager, boolean mute); }
    public SetSelfMuteCallback SetSelfMute;

    public interface IsSelfDeafCallback extends GameSDKCallback { EDiscordResult isSelfDeaf(IDiscordVoiceManager manager, ByteByReference deaf); }
    public IsSelfDeafCallback IsSelfDeaf;

    public interface SetSelfDeafCallback extends GameSDKCallback { EDiscordResult setSelfDeaf(IDiscordVoiceManager manager, boolean deaf); }
    public SetSelfDeafCallback SetSelfDeaf;

    public interface IsLocalMuteCallback extends GameSDKCallback { EDiscordResult isLocalMute(IDiscordVoiceManager manager, DiscordSnowflake userId, ByteByReference mute); }
    public IsLocalMuteCallback IsLocalMute;

    public interface SetLocalMuteCallback extends GameSDKCallback { EDiscordResult setLocalMute(IDiscordVoiceManager manager, DiscordSnowflake userId, boolean mute); }
    public SetLocalMuteCallback SetLocalMute;

    public interface GetLocalVolumeCallback extends GameSDKCallback { EDiscordResult getLocalVolume(IDiscordVoiceManager manager, DiscordSnowflake userId, ByteByReference volume); }
    public GetLocalVolumeCallback GetLocalVolume;

    public interface SetLocalVolumeCallback extends GameSDKCallback { EDiscordResult setLocalVolume(IDiscordVoiceManager manager, DiscordSnowflake userId, UInt8 volume); }
    public SetLocalVolumeCallback SetLocalVolume;



    public static class ByReference extends IDiscordVoiceManager implements Structure.ByReference { public ByReference() {super();} public ByReference(Pointer pointer) {super(pointer);}}
    public static class ByValue extends IDiscordVoiceManager implements Structure.ByValue { public ByValue() {super();} public ByValue(Pointer pointer) {super(pointer);}}


    @Override protected List getFieldOrder() { return Arrays.asList("GetInputMode", "SetInputMode", "IsSelfMute", "SetSelfMute", "IsSelfDeaf", "SetSelfDeaf", "IsLocalMute", "SetLocalMute", "GetLocalVolume", "SetLocalVolume"); }
}
