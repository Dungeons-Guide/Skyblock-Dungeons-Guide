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
import com.mojang.authlib.yggdrasil.ProfileResult;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.world.entity.player.PlayerModelType;
import net.minecraft.world.entity.player.PlayerSkin;
import net.minecraft.resources.Identifier;

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
            return CompletableFuture.completedFuture(new SkinSet(DefaultPlayerSkin.getDefaultTexture(), null, "default"));
        }
        if (skinSetMap.containsKey(gameProfile.id().toString())) {
            CachedData<SkinSet> ss = skinSetMap.get(gameProfile.id().toString());
            if (ss.getExpire() > System.currentTimeMillis())
                CompletableFuture.completedFuture(skinSetMap.get(gameProfile.id().toString()).getData());
            skinSetMap.remove(gameProfile.id().toString());
        }
        if (currentReq.containsKey(gameProfile.id().toString()))
            return currentReq.get(gameProfile.id().toString());

        SkinSet skinSet = new SkinSet();
        CompletableFuture<SkinSet> skinSet2 = new CompletableFuture<>();
        currentReq.put(gameProfile.id().toString(), skinSet2);
            ApiFetcher.ex.submit(() -> {
                // 1.8.9's fillProfileProperties kept the profile as is when it failed
                ProfileResult profileResult = Minecraft.getInstance().services().sessionService().fetchProfile(gameProfile.id(), true);
                GameProfile gameProfile1 = profileResult == null ? gameProfile : profileResult.profile();

                // 1.8.9's loadProfileTextures callback (only called when there was a skin), the skin manager's PlayerSkin on 26.1
                Minecraft.getInstance().getSkinManager().get(gameProfile1).thenAccept(playerSkin -> playerSkin.ifPresent(skin -> {
                    if (skin.cape() != null)
                        skinSet.setCapeLoc(skin.cape().texturePath());
                    skinSet.setSkinLoc(skin.body().texturePath());
                    skinSet.setSkinType(skin.model() == PlayerModelType.SLIM ? "slim" : "default");
                    skinSet2.complete(skinSet);
                    skinSetMap.put(gameProfile.id().toString(), new CachedData<>(System.currentTimeMillis() + 1000*60*60*3, skinSet));
                    currentReq.get(gameProfile.id().toString());
                }));
            });
        return skinSet2;
    }


    public static void purgeCache() {
        skinSetMap.clear();
        currentReq.clear();
    }


    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SkinSet {
        private Identifier skinLoc;
        private Identifier capeLoc;
        private String skinType;
    }
}
