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

package kr.syeyoung.dungeonsguide.mod.features.impl.party.playerpreview.api;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.client.resources.SkinManager;
import net.minecraft.util.ResourceLocation;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class SkinFetcher {

    private SkinFetcher(){}

    private static final Map<String, CachedData<SkinSet>> skinSetMap = new ConcurrentHashMap<>();

    private static final Map<String, CompletableFuture<SkinSet>> currentReq = new HashMap<>();

    public static CompletableFuture<SkinSet> getSkinSet(GameProfile gameProfile) {
        if (gameProfile == null) {
            return CompletableFuture.completedFuture(new SkinSet(DefaultPlayerSkin.getDefaultSkinLegacy(), null, "default"));
        }
        if (skinSetMap.containsKey(gameProfile.getId().toString())) {
            CachedData<SkinSet> ss = skinSetMap.get(gameProfile.getId().toString());
            if (ss.getExpire() > System.currentTimeMillis())
                CompletableFuture.completedFuture(skinSetMap.get(gameProfile.getId().toString()).getData());
            skinSetMap.remove(gameProfile.getId().toString());
        }
        if (currentReq.containsKey(gameProfile.getId().toString()))
            return currentReq.get(gameProfile.getId().toString());

        SkinSet skinSet = new SkinSet();
        CompletableFuture<SkinSet> skinSet2 = new CompletableFuture<>();
        currentReq.put(gameProfile.getId().toString(), skinSet2);
        Minecraft.getMinecraft().getSkinManager().loadProfileTextures(gameProfile, new SkinManager.SkinAvailableCallback() {
            public void skinAvailable(MinecraftProfileTexture.Type p_180521_1_, ResourceLocation location, MinecraftProfileTexture profileTexture) {
                switch (p_180521_1_) {
                    case SKIN:
                        skinSet.setSkinLoc(location);
                        skinSet.setSkinType(profileTexture.getMetadata("model"));
                        if (skinSet.getSkinType() == null) {
                            skinSet.setSkinType("default");
                        }
                        skinSet2.complete(skinSet);
                        skinSetMap.put(gameProfile.getId().toString(), new CachedData<>(System.currentTimeMillis() + 1000*60*60*3, skinSet));
                        currentReq.get(gameProfile.getId().toString());
                        break;
                    case CAPE:
                        skinSet.setCapeLoc(location);
                }
            }
        }, true);
        return skinSet2;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SkinSet {
        private ResourceLocation skinLoc;
        private ResourceLocation capeLoc;
        private String skinType;
    }
}
