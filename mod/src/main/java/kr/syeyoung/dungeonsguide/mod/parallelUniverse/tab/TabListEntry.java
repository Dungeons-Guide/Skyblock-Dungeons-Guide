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

package kr.syeyoung.dungeonsguide.mod.parallelUniverse.tab;

import com.google.common.base.Objects;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import kr.syeyoung.dungeonsguide.mod.parallelUniverse.teams.Team;
import kr.syeyoung.dungeonsguide.mod.parallelUniverse.teams.TeamManager;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.client.resources.SkinManager;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.WorldSettings.GameType;

import java.util.UUID;

@RequiredArgsConstructor
public class TabListEntry {
    @Getter
    private final GameProfile gameProfile;
    // properties?
    @Getter @Setter
    private final GameType gamemode;
    @Getter @Setter
    private int ping;
    @Getter
    private IChatComponent displayName;

    @Getter
    private String formatted;

    public void setDisplayName(IChatComponent displayName) {
        this.displayName = displayName;
        formatted = displayName == null ? null : displayName.getFormattedText();
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


    private boolean playerTexturesLoaded = false;
    private ResourceLocation locationSkin;
    private ResourceLocation locationCape;
    private String skinType;


    public boolean hasLocationSkin() {
        return this.locationSkin != null;
    }

    public String getSkinType() {
        return this.skinType == null ? DefaultPlayerSkin.getSkinType(this.gameProfile.getId()) : this.skinType;
    }

    public ResourceLocation getLocationSkin() {
        if (this.locationSkin == null) {
            this.loadPlayerTextures();
        }

        return (ResourceLocation) Objects.firstNonNull(this.locationSkin, DefaultPlayerSkin.getDefaultSkin(this.gameProfile.getId()));
    }

    public ResourceLocation getLocationCape() {
        if (this.locationCape == null) {
            this.loadPlayerTextures();
        }

        return this.locationCape;
    }
    protected void loadPlayerTextures() {
        synchronized(this) {
            if (!this.playerTexturesLoaded) {
                this.playerTexturesLoaded = true;
                Minecraft.getMinecraft().getSkinManager().loadProfileTextures(this.gameProfile, new SkinManager.SkinAvailableCallback() {
                    public void skinAvailable(MinecraftProfileTexture.Type type, ResourceLocation location, MinecraftProfileTexture profileTexture) {
                        switch (type) {
                            case SKIN:
                                TabListEntry.this.locationSkin = location;
                                TabListEntry.this.skinType = profileTexture.getMetadata("model");
                                if (TabListEntry.this.skinType == null) {
                                    TabListEntry.this.skinType = "default";
                                }
                                break;
                            case CAPE:
                                TabListEntry.this.locationCape = location;
                        }

                    }
                }, true);
            }

        }
    }


}
