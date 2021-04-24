package kr.syeyoung.dungeonsguide.features.impl.party.api;

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
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class SkinFetchur {

    private static Map<String, CachedData<SkinSet>> skinSetMap = new ConcurrentHashMap<>();

    private static Map<String, CompletableFuture<SkinSet>> currentReq = new HashMap<>();

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
