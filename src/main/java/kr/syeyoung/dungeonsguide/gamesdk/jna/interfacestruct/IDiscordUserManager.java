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
import kr.syeyoung.dungeonsguide.gamesdk.jna.datastruct.DiscordStruct;
import com.sun.jna.Pointer;
import kr.syeyoung.dungeonsguide.gamesdk.jna.enumuration.EDiscordResult;
import kr.syeyoung.dungeonsguide.gamesdk.jna.enumuration.EDiscordUserFlag;
import kr.syeyoung.dungeonsguide.gamesdk.jna.typedef.DiscordSnowflake;
import kr.syeyoung.dungeonsguide.gamesdk.jna.datastruct.DiscordUser;

public class IDiscordUserManager extends DiscordStruct { public IDiscordUserManager() {super();} public IDiscordUserManager(Pointer pointer) {super(pointer);}
    public interface GetCurrentUserCallback extends GameSDKCallback { EDiscordResult getCurrentUser(IDiscordUserManager manager, DiscordUser currentUser); }
    public GetCurrentUserCallback GetCurrentUser;

    public interface GetUserCallback extends GameSDKCallback { void getUser(IDiscordUserManager manager, DiscordSnowflake userId, Pointer callbackData, GetUserCallback_Callback callback); }
    public interface GetUserCallback_Callback extends GameSDKCallback { void callback(Pointer callbackData, EDiscordResult result, DiscordUser user);}
    public GetUserCallback GetUser;

    public interface GetCurrentUserPremiumTypeCallback extends GameSDKCallback { EDiscordResult getCurrentUserPremiumType(IDiscordUserManager manager, IntByReference premiumType); } // EDiscordPremiumType ptr
    public GetCurrentUserPremiumTypeCallback GetCurrentUserPremiumType;

    public interface CurrentUserHasFlagCallback extends GameSDKCallback { EDiscordResult currentUserHasFlag(IDiscordUserManager manager, EDiscordUserFlag flag, ByteByReference hasFlag); } // hasFlag bool ptr
    public CurrentUserHasFlagCallback CurrentUserHasFlag;



    public static class ByReference extends IDiscordUserManager implements Structure.ByReference { public ByReference() {super();} public ByReference(Pointer pointer) {super(pointer);}}
    public static class ByValue extends IDiscordUserManager implements Structure.ByValue { public ByValue() {super();} public ByValue(Pointer pointer) {super(pointer);}}
}
