/*
 *     Dungeons Guide - The most intelligent Hypixel Skyblock Dungeons Mod
 *     Copyright (C) 2021  cyoung06
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Affero General Public License as published
 *     by the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Affero General Public License for more details.
 *
 *     You should have received a copy of the GNU Affero General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package kr.syeyoung.dungeonsguide.mod.features.impl.party.playerpreview.api;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.authlib.GameProfile;
import kr.syeyoung.dungeonsguide.launcher.Main;
import kr.syeyoung.dungeonsguide.launcher.auth.AuthManager;
import kr.syeyoung.dungeonsguide.launcher.auth.DgAuthUtil;
import kr.syeyoung.dungeonsguide.mod.DungeonsGuide;
import kr.syeyoung.dungeonsguide.mod.VersionInfo;
import kr.syeyoung.dungeonsguide.mod.features.impl.party.playerpreview.api.playerprofile.PlayerProfile;
import kr.syeyoung.dungeonsguide.mod.features.impl.party.playerpreview.api.playerprofile.PlayerProfileParser;
import kr.syeyoung.dungeonsguide.mod.utils.TextUtils;
import net.minecraft.client.Minecraft;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ApiFetcher {

    private ApiFetcher(){}

    private static final Gson gson = new Gson();

    private static final Map<String, CachedData<PlayerSkyblockData>> playerProfileCache = new ConcurrentHashMap<>();
    private static final Map<String, CachedData<String>> nicknameToUID = new ConcurrentHashMap<>();
    private static final Map<String, CachedData<String>> UIDtoNickname = new ConcurrentHashMap<>();
    private static final Map<String, CachedData<GameProfile>> UIDtoGameProfile = new ConcurrentHashMap<>();

    public static final ExecutorService ex = DungeonsGuide.getDungeonsGuide().registerExecutorService(Executors.newFixedThreadPool(4, new ThreadFactoryBuilder()
            .setThreadFactory(DungeonsGuide.THREAD_FACTORY)
            .setNameFormat("DG-APIFetcher-%d").build()));


    public static void purgeCache() {
        playerProfileCache.clear();
        nicknameToUID.clear();
        UIDtoNickname.clear();
        UIDtoGameProfile.clear();

        completableFutureMap.clear();
        completableFutureMap2.clear();
        completableFutureMap3.clear();
        completableFutureMap4.clear();
        PlayerProfileParser.constants = null;

        ex.submit(PlayerProfileParser::getLilyWeightConstants);
    }

    static {
        ex.submit(PlayerProfileParser::getLilyWeightConstants);
    }

    public static JsonObject getJson(String url) throws IOException {
        URLConnection connection = new URL(url).openConnection();
        connection.setRequestProperty("User-Agent", "DungeonsGuide/"+ VersionInfo.VERSION);
        connection.setConnectTimeout(10000);
        connection.setReadTimeout(10000);
        InputStreamReader inputStreamReader = new InputStreamReader(connection.getInputStream());
        String servers = IOUtils.toString(inputStreamReader);
        return gson.fromJson(servers, JsonObject.class);
    }
    public static JsonObject getJsonWithAuth(String url, String token) throws IOException {
        URLConnection connection = new URL(url).openConnection();
        connection.setRequestProperty("User-Agent", "DungeonsGuide/"+ VersionInfo.VERSION);
        connection.addRequestProperty("Authorization", "Bearer "+token);
        connection.setConnectTimeout(10000);
        connection.setReadTimeout(10000);
        InputStreamReader inputStreamReader = new InputStreamReader(connection.getInputStream());
        String servers = IOUtils.toString(inputStreamReader);
        return gson.fromJson(servers, JsonObject.class);
    }

    public static JsonArray getJsonArr(String url) throws IOException {
        URLConnection connection = new URL(url).openConnection();
        connection.setRequestProperty("User-Agent", "DungeonsGuide/"+ VersionInfo.VERSION);
        connection.setConnectTimeout(10000);
        connection.setReadTimeout(10000);
        return gson.fromJson(new InputStreamReader(connection.getInputStream()), JsonArray.class);
    }

    private static final Map<String, CompletableFuture<Optional<GameProfile>>> completableFutureMap4 = new ConcurrentHashMap<>();

    public static CompletableFuture<Optional<GameProfile>> getSkinGameProfileByUUIDAsync(String uid) {
        if (UIDtoGameProfile.containsKey(uid)) {
            CachedData<GameProfile> cachedData = UIDtoGameProfile.get(uid);
            if (cachedData.getExpire() > System.currentTimeMillis()) {
                return CompletableFuture.completedFuture(Optional.ofNullable(cachedData.getData()));
            }
            UIDtoGameProfile.remove(uid);
        }
        if (completableFutureMap4.containsKey(uid)) return completableFutureMap4.get(uid);

        CompletableFuture<Optional<GameProfile>> completableFuture = new CompletableFuture<>();
        fetchNicknameAsync(uid).thenAccept(nick -> {
            if (!nick.isPresent()) {
                completableFuture.complete(Optional.empty());
                return;
            }
            ex.submit(() -> {
                try {
                    Optional<GameProfile> playerProfile = getSkinGameProfileByUUID(uid, nick.get());
                    UIDtoGameProfile.put(uid, new CachedData<GameProfile>(System.currentTimeMillis() + 1000 * 60 * 30, playerProfile.orElse(null)));
                    completableFuture.complete(playerProfile);
                    completableFutureMap4.remove(uid);
                    return;
                } catch (IOException e) {
                    e.printStackTrace();
                }
                completableFuture.complete(Optional.empty());
                completableFutureMap4.remove(uid);
            });
        });
        completableFutureMap4.put(uid, completableFuture);
        return completableFuture;
    }

    public static Optional<GameProfile> getSkinGameProfileByUUID(String uid, String nickname) throws IOException {
        GameProfile gameProfile = new GameProfile(UUID.fromString(uid), nickname);
        GameProfile newProf = Minecraft.getMinecraft().getSessionService().fillProfileProperties(gameProfile, true);
        return newProf == gameProfile ? Optional.empty() : Optional.of(newProf);
    }


    private static final Map<String, CompletableFuture<Optional<PlayerSkyblockData>>> completableFutureMap = new ConcurrentHashMap<>();

    public static CompletableFuture<Optional<PlayerSkyblockData>> fetchMostRecentProfileAsync(String uid) {
        if (playerProfileCache.containsKey(uid)) {
            CachedData<PlayerSkyblockData> cachedData = playerProfileCache.get(uid);
            if (cachedData.getExpire() > System.currentTimeMillis()) {
                return CompletableFuture.completedFuture(Optional.ofNullable(cachedData.getData()));
            }
            playerProfileCache.remove(uid);
        }
        if (completableFutureMap.containsKey(uid)) {
            return completableFutureMap.get(uid);
        }
        CompletableFuture<Optional<PlayerSkyblockData>> completableFuture = new CompletableFuture<>();
        ex.submit(() -> {
            try {
                Optional<PlayerSkyblockData> playerProfile = fetchPlayerProfiles(uid);
                playerProfileCache.put(uid, new CachedData<>(System.currentTimeMillis() + 1000 * 60 * 30, playerProfile.orElse(null)));
                completableFuture.complete(playerProfile);
                completableFutureMap.remove(uid);
            } catch (IOException e) {
                if (e.getMessage().contains("403 for URL")) {
                    completableFuture.completeExceptionally(e);
                    completableFutureMap.remove(uid);
                } else {
                    completableFuture.completeExceptionally(e);
                    completableFutureMap.remove(uid);
                }
                e.printStackTrace();
            }
        });
        completableFutureMap.put(uid, completableFuture);
        return completableFuture;
    }

    private static final Map<String, CompletableFuture<Optional<String>>> completableFutureMap3 = new ConcurrentHashMap<>();

    public static CompletableFuture<Optional<String>> fetchNicknameAsync(String uid) {
        if (UIDtoNickname.containsKey(uid)) {
            CachedData<String> cachedData = UIDtoNickname.get(uid);
            if (cachedData.getExpire() > System.currentTimeMillis()) {
                return CompletableFuture.completedFuture(Optional.ofNullable(cachedData.getData()));
            }
            UIDtoNickname.remove(uid);
        }
        if (completableFutureMap3.containsKey(uid)) return completableFutureMap3.get(uid);


        CompletableFuture<Optional<String>> completableFuture = new CompletableFuture<>();

        ex.submit(() -> {
            try {
                Optional<String> playerProfile = fetchNickname(uid);
                UIDtoNickname.put(uid, new CachedData<String>(System.currentTimeMillis() + 1000 * 60 * 60 * 12, playerProfile.orElse(null)));
                if (playerProfile.isPresent())
                    nicknameToUID.put(playerProfile.orElse(null), new CachedData<>(System.currentTimeMillis() + 1000 * 60 * 60 * 12, uid));
                completableFuture.complete(playerProfile);
                completableFutureMap3.remove(uid);
                return;
            } catch (IOException e) {
                e.printStackTrace();
            }
            completableFuture.complete(Optional.empty());
            completableFutureMap3.remove(uid);
        });
        completableFutureMap3.put(uid, completableFuture);

        return completableFuture;
    }

    private static final Map<String, CompletableFuture<Optional<String>>> completableFutureMap2 = new ConcurrentHashMap<>();

    public static CompletableFuture<Optional<String>> fetchUUIDAsync(String nickname) {
        if (nicknameToUID.containsKey(nickname)) {
            CachedData<String> cachedData = nicknameToUID.get(nickname);
            if (cachedData.getExpire() > System.currentTimeMillis()) {
                return CompletableFuture.completedFuture(Optional.ofNullable(cachedData.getData()));
            }
            nicknameToUID.remove(nickname);
        }
        if (completableFutureMap2.containsKey(nickname)) return completableFutureMap2.get(nickname);


        CompletableFuture<Optional<String>> completableFuture = new CompletableFuture<>();

        ex.submit(() -> {
            try {
                Optional<String> playerProfile = fetchUUID(nickname);
                nicknameToUID.put(nickname, new CachedData<String>(System.currentTimeMillis() + 1000 * 60 * 60 * 12, playerProfile.orElse(null)));
                if (playerProfile.isPresent())
                    UIDtoNickname.put(playerProfile.orElse(null), new CachedData<>(System.currentTimeMillis() + 1000 * 60 * 60 * 12, nickname));

                completableFuture.complete(playerProfile);
                completableFutureMap2.remove(nickname);
                return;
            } catch (IOException e) {
                e.printStackTrace();
            }
            completableFuture.complete(Optional.empty());
            completableFutureMap2.remove(nickname);
        });
        completableFutureMap2.put(nickname, completableFuture);

        return completableFuture;
    }

    public static Optional<String> fetchUUID(String nickname) throws IOException {
        JsonObject json = getJson("https://api.mojang.com/users/profiles/minecraft/" + nickname);
        if (json.has("error")) return Optional.empty();
        return Optional.of(TextUtils.insertDashUUID(json.get("id").getAsString()));
    }

    public static Optional<String> fetchNickname(String uuid) throws IOException {
        try {
            JsonArray json = getJsonArr("https://api.mojang.com/user/profiles/" + uuid.replace("-", "") + "/names");
            return Optional.of(json.get(json.size() - 1).getAsJsonObject().get("name").getAsString());
        } catch (Exception e) {
            return Optional.empty();
        }
    }


    public static int getArrayIndex(Object[] arr,Object value) {
        int k=0;
        for(int i=0;i<arr.length;i++){
            if(arr[i]==value){
                k=i;
                break;
            }
        }
        return k;
    }

    public static Optional<PlayerSkyblockData> fetchPlayerProfiles(String uid) throws IOException {
        String dgAPIToken = AuthManager.getInstance().getWorkingTokenOrThrow();

        System.out.println("Fetching player profiles");
        JsonObject json = getJsonWithAuth(Main.DOMAIN+"/skyblock/player/v2/"+uid, dgAPIToken);


        System.out.println("Downloaded data from api");
        JsonArray profiles = json.getAsJsonArray("profiles");
        String dashTrimmed = uid.replace("-", "");


        PlayerSkyblockData pp = new PlayerSkyblockData();
        ArrayList<PlayerProfile> playerProfiles = new ArrayList<>();
        System.out.println("Saving and parsing data");
        float lastSave = Long.MIN_VALUE;
        PlayerProfile latest = null;
        for (JsonElement jsonElement : profiles) {
            JsonObject semiProfile = jsonElement.getAsJsonObject();
            if (!semiProfile.get("members").getAsJsonObject().has(dashTrimmed)) {
                System.out.println("Profile does not appear to have the player???");
                continue;
            }

            System.out.println("Parsing profile");
            PlayerProfile e = PlayerProfileParser.parseProfile(semiProfile, dashTrimmed);
            System.out.println("Finished Parsing Profile");

            System.out.println("Getting selected profile");
            if (e.isSelected()) {
                latest = e;
            }
            playerProfiles.add(e);
        }
        System.out.println("THE AMOUNT OF PLAYER PROFILES: " + playerProfiles.size());
        PlayerProfile[] p = new PlayerProfile[playerProfiles.size()];
        pp.setPlayerProfiles(playerProfiles.toArray(p));
        pp.setLatestProfileArrayIndex(getArrayIndex(p, latest));
        return Optional.of(pp);
    }


}
