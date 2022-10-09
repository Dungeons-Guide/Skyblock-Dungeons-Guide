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

package kr.syeyoung.dungeonsguide.discord.gamesdk.jna.enumuration;

import com.sun.jna.FromNativeContext;
import com.sun.jna.ToNativeContext;
import com.sun.jna.TypeConverter;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

public enum EDiscordResult {
    DiscordResult_Ok(0),
    DiscordResult_ServiceUnavailable(1),
    DiscordResult_InvalidVersion(2),
    DiscordResult_LockFailed(3),
    DiscordResult_InternalError(4),
    DiscordResult_InvalidPayload(5),
    DiscordResult_InvalidCommand(6),
    DiscordResult_InvalidPermissions(7),
    DiscordResult_NotFetched(8),
    DiscordResult_NotFound(9),
    DiscordResult_Conflict(10),
    DiscordResult_InvalidSecret(11),
    DiscordResult_InvalidJoinSecret(12),
    DiscordResult_NoEligibleActivity(13),
    DiscordResult_InvalidInvite(14),
    DiscordResult_NotAuthenticated(15),
    DiscordResult_InvalidAccessToken(16),
    DiscordResult_ApplicationMismatch(17),
    DiscordResult_InvalidDataUrl(18),
    DiscordResult_InvalidBase64(19),
    DiscordResult_NotFiltered(20),
    DiscordResult_LobbyFull(21),
    DiscordResult_InvalidLobbySecret(22),
    DiscordResult_InvalidFilename(23),
    DiscordResult_InvalidFileSize(24),
    DiscordResult_InvalidEntitlement(25),
    DiscordResult_NotInstalled(26),
    DiscordResult_NotRunning(27),
    DiscordResult_InsufficientBuffer(28),
    DiscordResult_PurchaseCanceled(29),
    DiscordResult_InvalidGuild(30),
    DiscordResult_InvalidEvent(31),
    DiscordResult_InvalidChannel(32),
    DiscordResult_InvalidOrigin(33),
    DiscordResult_RateLimited(34),
    DiscordResult_OAuth2Error(35),
    DiscordResult_SelectChannelTimeout(36),
    DiscordResult_GetGuildTimeout(37),
    DiscordResult_SelectVoiceForceRequired(38),
    DiscordResult_CaptureShortcutAlreadyListening(39),
    DiscordResult_UnauthorizedForAchievement(40),
    DiscordResult_InvalidGiftCode(41),
    DiscordResult_PurchaseError(42),
    DiscordResult_TransactionAborted(43);

    @Getter
    private final int value;
    private EDiscordResult(int value) {
        this.value = value;
    }

    private static final Map<Integer,EDiscordResult> valueMap = new HashMap<>();
    static {
        for (EDiscordResult value : values()) {
            valueMap.put(value.value, value);
        }
    }

    public static EDiscordResult fromValue(int value) {
        return valueMap.get(value);
    }
    
    public static class EDiscordResultTypeConverter implements TypeConverter {
        @Override
        public Object fromNative(Object nativeValue, FromNativeContext context) {
            return EDiscordResult.fromValue((Integer)nativeValue);
        }

        @Override
        public Object toNative(Object value, ToNativeContext context) {
            if (value == null) return 0;
            return ((EDiscordResult)value).getValue();
        }

        @Override
        public Class nativeType() {
            return Integer.class;
        }
    }
}
