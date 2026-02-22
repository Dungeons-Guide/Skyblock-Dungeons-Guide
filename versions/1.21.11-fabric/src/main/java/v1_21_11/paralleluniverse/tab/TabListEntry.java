/*
 * Dungeons Guide - The most intelligent Hypixel Skyblock Dungeons Mod
 * Copyright (C) 2023  cyoung06 (syeyoung)
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

package v1_21_11.paralleluniverse.tab;

import com.mojang.authlib.GameProfile;
import kr.syeyoung.modapi.data.ResourceIdentifier;
import kr.syeyoung.modapi.paralleluniverse.tablist.UTabListEntry;
import kr.syeyoung.modapi.util.GameMode;
import kr.syeyoung.modapi.v1_21_5.paralleluniverse.teams.Team;
import kr.syeyoung.modapi.v1_21_5.paralleluniverse.teams.TeamManager;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.PlayerSkinProvider;
import net.minecraft.client.util.DefaultSkinHelper;
import net.minecraft.client.util.SkinTextures;
import net.minecraft.text.Text;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@RequiredArgsConstructor
public class TabListEntry implements UTabListEntry {
    @Getter
    private final GameProfile gameProfile;
    @Getter
    // properties?
    private final GameMode gameMode;

    @Getter @Setter
    private int ping;
    @Getter
    private Text displayName;

    @Getter
    private String formatted;

    public void setDisplayName(Text displayName) {
        this.displayName = displayName;
        formatted = displayName == null ? null : displayName.getString();
    }

    public String getEffectiveName() {
        if (formatted != null) return formatted;

        Team team = TeamManager.INSTANCE.getPlayerTeam(gameProfile.getName());
        if (team != null) {
            return team.getPrefix() + gameProfile.getName() + team.getSuffix();
        }
        return gameProfile.getName();
    }
    public String getEffectiveWithoutName() {
        if (formatted != null) return formatted;

        Team team = TeamManager.INSTANCE.getPlayerTeam(gameProfile.getName());
        if (team != null) {
            return team.getPrefix() + team.getSuffix();
        }
        return gameProfile.getName();
    }

    private static java.util.function.Supplier<SkinTextures> texturesSupplier(GameProfile profile) {
        MinecraftClient minecraftClient = MinecraftClient.getInstance();
        PlayerSkinProvider playerSkinProvider = minecraftClient.getSkinProvider();
        CompletableFuture<Optional<SkinTextures>> completableFuture = playerSkinProvider.fetchSkinTextures(profile);
        boolean bl = !minecraftClient.uuidEquals(profile.getId());
        SkinTextures skinTextures = DefaultSkinHelper.getSkinTextures(profile);
        return () -> {
            SkinTextures skinTextures2 = completableFuture.getNow(Optional.empty()).orElse(skinTextures);
            if (bl && !skinTextures2.secure()) {
                return skinTextures;
            }
            return skinTextures2;
        };
    }

    public ResourceIdentifier getLocationSkin() {
//        if (this.locationSkin == null) {
//
//            this.loadPlayerTextures();
//        }
//
//        MinecraftClient.getInstance().getNetworkHandler().getp
////        if (this.lo)
        return new ResourceIdentifier("","");
//        return new ResourceIdentifier((this.locationSkin, DefaultPlayerSkin.getDefaultSkin(this.gameProfile.getId())).toString());
    }
    @Override
    public UUID getUUID() {
        return gameProfile.getId();
    }

    @Override
    public String getPlayerName() {
        return gameProfile.getName();
    }
}
