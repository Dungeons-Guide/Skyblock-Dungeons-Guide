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
import kr.syeyoung.dungeonsguide.mod.discord.gamesdk.jna.NativeGameSDK;
import kr.syeyoung.dungeonsguide.mod.discord.gamesdk.jna.datastruct.DiscordStruct;
import kr.syeyoung.dungeonsguide.mod.discord.gamesdk.jna.enumuration.EDiscordActivityActionType;

import java.util.Arrays;
import java.util.List;

public class IDiscordOverlayManager extends DiscordStruct { public IDiscordOverlayManager() {super();} public IDiscordOverlayManager(Pointer pointer) {super(pointer);}
    public interface IsEnabledCallback extends GameSDKCallback { void isEnabled(IDiscordOverlayManager manager, ByteByReference enabled); }
    public IsEnabledCallback IsEnabled;

    public interface IsLockedCallback extends GameSDKCallback { void isLocked(IDiscordOverlayManager manager, ByteByReference locked); }
    public IsLockedCallback IsLocked;

    public interface SetLockedCallback extends GameSDKCallback { void setLocked(IDiscordOverlayManager manager, boolean locked, Pointer callbackData, NativeGameSDK.DiscordCallback callback); }
    public SetLockedCallback SetLocked;

    public interface OpenActivityInviteCallback extends GameSDKCallback { void openActivityInvite(IDiscordOverlayManager manager, EDiscordActivityActionType type, Pointer callbackData, NativeGameSDK.DiscordCallback callback); }
    public OpenActivityInviteCallback OpenActivityInvite;

    public interface OpenGuildInviteCallback extends GameSDKCallback { void openGuildInvite(IDiscordOverlayManager manager, String code, Pointer callbackData, NativeGameSDK.DiscordCallback callback); }
    public OpenGuildInviteCallback OpenGuildInvite;

    public interface OpenVoiceSettingsCallback extends GameSDKCallback { void openVoiceSettings(IDiscordOverlayManager manager, Pointer callbackData, NativeGameSDK.DiscordCallback callback); }
    public OpenVoiceSettingsCallback OpenVoiceSettings;



    public static class ByReference extends IDiscordOverlayManager implements Structure.ByReference { public ByReference() {super();} public ByReference(Pointer pointer) {super(pointer);}}
    public static class ByValue extends IDiscordOverlayManager implements Structure.ByValue { public ByValue() {super();} public ByValue(Pointer pointer) {super(pointer);}}

    @Override protected List getFieldOrder() { return Arrays.asList("IsEnabled", "IsLocked", "SetLocked", "OpenActivityInvite", "OpenGuildInvite", "OpenVoiceSettings"); }
}
