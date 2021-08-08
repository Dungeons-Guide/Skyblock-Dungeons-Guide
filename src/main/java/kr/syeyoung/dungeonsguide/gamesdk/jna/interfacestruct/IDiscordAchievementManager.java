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
import kr.syeyoung.dungeonsguide.gamesdk.jna.NativeGameSDK;
import kr.syeyoung.dungeonsguide.gamesdk.jna.datastruct.DiscordStruct;
import com.sun.jna.Pointer;
import kr.syeyoung.dungeonsguide.gamesdk.jna.enumuration.EDiscordResult;
import kr.syeyoung.dungeonsguide.gamesdk.jna.typedef.Int32;
import kr.syeyoung.dungeonsguide.gamesdk.jna.typedef.UInt8;
import kr.syeyoung.dungeonsguide.gamesdk.jna.typedef.DiscordSnowflake;
import kr.syeyoung.dungeonsguide.gamesdk.jna.datastruct.DiscordUserAchievement;

public class IDiscordAchievementManager extends DiscordStruct { public IDiscordAchievementManager() {super();} public IDiscordAchievementManager(Pointer pointer) {super(pointer);}
    public interface SetUserAchievementCallback extends GameSDKCallback { void setUserAchievement(IDiscordAchievementManager manager, DiscordSnowflake achievementId, UInt8 percentComplete, Pointer callbackData, NativeGameSDK.DiscordCallback callback); }
    public SetUserAchievementCallback SetUserAchievement;

    public interface FetchUserAchievementsCallback extends GameSDKCallback { void fetchUserAchievements(IDiscordAchievementManager manager, Pointer callbackData, NativeGameSDK.DiscordCallback callback); }
    public FetchUserAchievementsCallback FetchUserAchievements;

    public interface CountUserAchievementsCallback extends GameSDKCallback { void countUserAchievements(IDiscordAchievementManager manager, IntByReference count); }
    public CountUserAchievementsCallback CountUserAchievements;

    public interface GetUserAchievementCallback extends GameSDKCallback { EDiscordResult getUserAchievement(IDiscordAchievementManager manager, DiscordSnowflake userAchievementId, DiscordUserAchievement userAchievement); }
    public GetUserAchievementCallback GetUserAchievement;

    public interface GetUserAchievementAtCallback extends GameSDKCallback { EDiscordResult getUserAchievementAt(IDiscordAchievementManager manager, Int32 index, DiscordUserAchievement userAchievement); }
    public GetUserAchievementAtCallback GetUserAchievementAt;



    public static class ByReference extends IDiscordAchievementManager implements Structure.ByReference { public ByReference() {super();} public ByReference(Pointer pointer) {super(pointer);}}
    public static class ByValue extends IDiscordAchievementManager implements Structure.ByValue { public ByValue() {super();} public ByValue(Pointer pointer) {super(pointer);}}
}
