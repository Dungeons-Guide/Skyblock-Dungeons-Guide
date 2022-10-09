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

package kr.syeyoung.dungeonsguide.discord.gamesdk.jna;

import com.sun.jna.DefaultTypeMapper;
import kr.syeyoung.dungeonsguide.discord.gamesdk.jna.enumuration.*;

public class GameSDKTypeMapper extends DefaultTypeMapper {
    public static final GameSDKTypeMapper INSTANCE = new GameSDKTypeMapper();
    private GameSDKTypeMapper() {
        addTypeConverter(EDiscordResult.class, new EDiscordResult.EDiscordResultTypeConverter());
        addTypeConverter(EDiscordCreateFlags.class, new EDiscordCreateFlags.EDiscordCreateFlagsTypeConverter());
        addTypeConverter(EDiscordLogLevel.class, new EDiscordLogLevel.EDiscordLogLevelTypeConverter());
        addTypeConverter(EDiscordUserFlag.class, new EDiscordUserFlag.EDiscordUserFlagTypeConverter());
        addTypeConverter(EDiscordPremiumType.class, new EDiscordPremiumType.EDiscordPremiumTypeTypeConverter());
        addTypeConverter(EDiscordImageType.class, new EDiscordImageType.EDiscordImageTypeTypeConverter());
        addTypeConverter(EDiscordActivityType.class, new EDiscordActivityType.EDiscordActivityTypeTypeConverter());
        addTypeConverter(EDiscordActivityActionType.class, new EDiscordActivityActionType.EDiscordActivityActionTypeTypeConverter());
        addTypeConverter(EDiscordActivityJoinRequestReply.class, new EDiscordActivityJoinRequestReply.EDiscordActivityJoinRequestReplyTypeConverter());
        addTypeConverter(EDiscordStatus.class, new EDiscordStatus.EDiscordStatusTypeConverter());
        addTypeConverter(EDiscordRelationshipType.class, new EDiscordRelationshipType.EDiscordRelationshipTypeTypeConverter());
        addTypeConverter(EDiscordLobbyType.class, new EDiscordLobbyType.EDiscordLobbyTypeTypeConverter());
        addTypeConverter(EDiscordLobbySearchComparison.class, new EDiscordLobbySearchComparison.EDiscordLobbySearchComparisonTypeConverter());
        addTypeConverter(EDiscordLobbySearchCast.class, new EDiscordLobbySearchCast.EDiscordLobbySearchCastTypeConverter());
        addTypeConverter(EDiscordLobbySearchDistance.class, new EDiscordLobbySearchDistance.EDiscordLobbySearchDistanceTypeConverter());
        addTypeConverter(EDiscordEntitlementType.class, new EDiscordEntitlementType.EDiscordEntitlementTypeTypeConverter());
        addTypeConverter(EDiscordSkuType.class, new EDiscordSkuType.EDiscordSkuTypeTypeConverter());
        addTypeConverter(EDiscordInputModeType.class, new EDiscordInputModeType.EDiscordInputModeTypeTypeConverter());
    }
}
