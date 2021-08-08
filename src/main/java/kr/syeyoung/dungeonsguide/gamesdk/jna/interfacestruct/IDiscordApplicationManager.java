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
import kr.syeyoung.dungeonsguide.gamesdk.jna.NativeGameSDK;
import kr.syeyoung.dungeonsguide.gamesdk.jna.datastruct.DiscordStruct;
import com.sun.jna.Pointer;
import kr.syeyoung.dungeonsguide.gamesdk.jna.enumuration.EDiscordResult;
import kr.syeyoung.dungeonsguide.gamesdk.jna.datastruct.DiscordOAuth2Token;

import java.util.Arrays;
import java.util.List;

public class IDiscordApplicationManager extends DiscordStruct { public IDiscordApplicationManager() {super();} public IDiscordApplicationManager(Pointer pointer) {super(pointer);}
    public interface ValidateOrExitCallback extends GameSDKCallback { void validateOrExit(IDiscordApplicationManager manager, Pointer callbackData, NativeGameSDK.DiscordCallback callback); }
    public ValidateOrExitCallback ValidateOrExit;

    public interface GetCurrentLocaleCallback extends GameSDKCallback { void getCurrentLocale(IDiscordApplicationManager manager, Pointer locale); } // len 128 memory
    public GetCurrentLocaleCallback GetCurrentLocale;

    public interface GetCurrentBranchCallback extends GameSDKCallback { void getCurrentBranch(IDiscordApplicationManager manager, Pointer branch); } // len 4096 mem
    public GetCurrentBranchCallback GetCurrentBranch;

    public interface GetOauth2TokenCallback extends GameSDKCallback { void getOauth2Token(IDiscordApplicationManager manager, Pointer callbackData, GetOauth2TokenCallback_Callback callback); }
    public interface GetOauth2TokenCallback_Callback extends GameSDKCallback {
        void callback(Pointer callbackData, EDiscordResult result, DiscordOAuth2Token oauthToken);
    }

    public GetOauth2TokenCallback GetOauth2Token;

    public interface GetTicketCallback extends GameSDKCallback { void getTicket(IDiscordApplicationManager manager, Pointer callbackData, GetTicketCallback_Callback callback); }
    public interface GetTicketCallback_Callback extends GameSDKCallback {
        void callback(Pointer callbackData, EDiscordResult result, String data);
    }
    public GetTicketCallback GetTicket;



    public static class ByReference extends IDiscordApplicationManager implements Structure.ByReference { public ByReference() {super();} public ByReference(Pointer pointer) {super(pointer);}}
    public static class ByValue extends IDiscordApplicationManager implements Structure.ByValue { public ByValue() {super();} public ByValue(Pointer pointer) {super(pointer);}}

    @Override protected List getFieldOrder() { return Arrays.asList("ValidateOrExit", "GetCurrentLocale", "GetCurrentBranch", "GetOauth2Token", "GetTicket"); }
}
