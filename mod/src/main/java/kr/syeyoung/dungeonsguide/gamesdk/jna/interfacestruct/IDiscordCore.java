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

import com.sun.jna.Structure;
import com.sun.jna.TypeMapper;
import kr.syeyoung.dungeonsguide.gamesdk.jna.GameSDKTypeMapper;
import kr.syeyoung.dungeonsguide.gamesdk.jna.datastruct.DiscordStruct;
import com.sun.jna.Pointer;
import kr.syeyoung.dungeonsguide.gamesdk.jna.enumuration.EDiscordLogLevel;
import kr.syeyoung.dungeonsguide.gamesdk.jna.enumuration.EDiscordResult;

import java.util.Arrays;
import java.util.List;

public class IDiscordCore extends DiscordStruct { public IDiscordCore() {super();} public IDiscordCore(Pointer pointer) {super(pointer);}

    public static final TypeMapper TYPE_MAPPER = GameSDKTypeMapper.INSTANCE;
    public interface DestroyCallback extends GameSDKCallback { void destroy(IDiscordCore core); }
    public DestroyCallback Destroy;

    public interface RunCallbacksCallback extends GameSDKCallback { EDiscordResult runCallbacks(IDiscordCore core); }
    public RunCallbacksCallback RunCallbacks;

    public interface SetLogHookCallback extends GameSDKCallback { void setLogHook(IDiscordCore core, EDiscordLogLevel minLevel, Pointer hookData, LogHook hook); }
    public interface LogHook extends GameSDKCallback {
        void hook(Pointer hookData, EDiscordLogLevel level, String message);
    }
    public SetLogHookCallback SetLogHook;

    public interface GetApplicationManagerCallback extends GameSDKCallback { IDiscordApplicationManager getApplicationManager(IDiscordCore core); }
    public GetApplicationManagerCallback GetApplicationManager;

    public interface GetUserManagerCallback extends GameSDKCallback { IDiscordUserManager getUserManager(IDiscordCore core); }
    public GetUserManagerCallback GetUserManager;

    public interface GetImageManagerCallback extends GameSDKCallback { IDiscordImageManager getImageManager(IDiscordCore core); }
    public GetImageManagerCallback GetImageManager;

    public interface GetActivityManagerCallback extends GameSDKCallback { IDiscordActivityManager getActivityManager(IDiscordCore core); }
    public GetActivityManagerCallback GetActivityManager;

    public interface GetRelationshipManagerCallback extends GameSDKCallback { IDiscordRelationshipManager getRelationshipManager(IDiscordCore core); }
    public GetRelationshipManagerCallback GetRelationshipManager;

    public interface GetLobbyManagerCallback extends GameSDKCallback { IDiscordLobbyManager getLobbyManager(IDiscordCore core); }
    public GetLobbyManagerCallback GetLobbyManager;

    public interface GetNetworkManagerCallback extends GameSDKCallback { IDiscordNetworkManager getNetworkManager(IDiscordCore core); }
    public GetNetworkManagerCallback GetNetworkManager;

    public interface GetOverlayManagerCallback extends GameSDKCallback { IDiscordOverlayManager getOverlayManager(IDiscordCore core); }
    public GetOverlayManagerCallback GetOverlayManager;

    public interface GetStorageManagerCallback extends GameSDKCallback { IDiscordStorageManager getStorageManager(IDiscordCore core); }
    public GetStorageManagerCallback GetStorageManager;

    public interface GetStoreManagerCallback extends GameSDKCallback { IDiscordStoreManager getStoreManager(IDiscordCore core); }
    public GetStoreManagerCallback GetStoreManager;

    public interface GetVoiceManagerCallback extends GameSDKCallback { IDiscordVoiceManager getVoiceManager(IDiscordCore core); }
    public GetVoiceManagerCallback GetVoiceManager;

    public interface GetAchievementManagerCallback extends GameSDKCallback { IDiscordAchievementManager getAchievementManager(IDiscordCore core); }
    public GetAchievementManagerCallback GetAchievementManager;



    public static class ByReference extends IDiscordCore implements Structure.ByReference { public ByReference() {super();} public ByReference(Pointer pointer) {super(pointer);}
    }
    public static class ByValue extends IDiscordCore implements Structure.ByValue { public ByValue() {super();} public ByValue(Pointer pointer) {super(pointer);}
    }

    @Override protected List getFieldOrder() { return Arrays.asList("Destroy", "RunCallbacks", "SetLogHook", "GetApplicationManager", "GetUserManager", "GetImageManager", "GetActivityManager", "GetRelationshipManager", "GetLobbyManager", "GetNetworkManager", "GetOverlayManager", "GetStorageManager", "GetStoreManager", "GetVoiceManager", "GetAchievementManager"); }
}
