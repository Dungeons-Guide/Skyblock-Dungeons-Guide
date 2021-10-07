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

package kr.syeyoung.dungeonsguide.features.impl.party.api;

import com.google.gson.*;
import com.mojang.authlib.GameProfile;
import kr.syeyoung.dungeonsguide.utils.TextUtils;
import kr.syeyoung.dungeonsguide.utils.XPUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import org.json.JSONObject;
import scala.tools.cmd.Opt;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ApiFetchur {
    private static final Gson gson = new Gson();

    private static final Map<String, CachedData<PlayerProfile>> playerProfileCache = new ConcurrentHashMap<>();
    private static final Map<String, CachedData<String>> nicknameToUID = new ConcurrentHashMap<>();
    private static final Map<String, CachedData<String>> UIDtoNickname = new ConcurrentHashMap<>();
    private static final Map<String, CachedData<GameProfile>> UIDtoGameProfile = new ConcurrentHashMap<>();

    private static final ExecutorService ex = Executors.newFixedThreadPool(4);

    private static final Set<String> invalidKeys = new HashSet<>();
    public static void purgeCache() {
        playerProfileCache.clear();
        nicknameToUID.clear();
        UIDtoNickname.clear();
        UIDtoGameProfile.clear();

        completableFutureMap.clear();
        completableFutureMap2.clear();
        completableFutureMap3.clear();
        completableFutureMap4.clear();
        invalidKeys.clear();
        completableFutureMapLily= null;
        constants = null;
    }

    public static JsonObject getJson(String url) throws IOException {
        URLConnection connection = new URL(url).openConnection();
        connection.setConnectTimeout(10000);
        connection.setReadTimeout(10000);
        return gson.fromJson(new InputStreamReader(connection.getInputStream()), JsonObject.class);
    }
    public static JsonArray getJsonArr(String url) throws IOException {
        URLConnection connection = new URL(url).openConnection();
        connection.setConnectTimeout(10000);
        connection.setReadTimeout(10000);
        return gson.fromJson(new InputStreamReader(connection.getInputStream()), JsonArray.class);
    }

    private static JsonObject constants;
    private static CompletableFuture<JsonObject> completableFutureMapLily;
    public static CompletableFuture<JsonObject> getLilyWeightConstants() {
        if (constants != null) return CompletableFuture.completedFuture(constants);
        if (completableFutureMapLily != null) return completableFutureMapLily;
        CompletableFuture<JsonObject> completableFuture = new CompletableFuture<>();
        completableFutureMapLily = completableFuture;
        ex.submit(() -> {
            try {
                JsonObject jsonObject = getJson("https://raw.githubusercontent.com/Antonio32A/lilyweight/master/lib/constants.json");
                constants = jsonObject;
                completableFuture.complete(jsonObject);
                completableFutureMapLily= null;
            } catch (Exception e) {
                completableFuture.completeExceptionally(e);
                completableFutureMapLily= null;
            }
        });
        return completableFuture;
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
                    Optional<GameProfile> playerProfile = getSkinGameProfileByUUID(uid,nick.get());
                    UIDtoGameProfile.put(uid, new CachedData<GameProfile>(System.currentTimeMillis()+1000*60*30, playerProfile.orElse(null)));
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


    private static final Map<String, CompletableFuture<Optional<PlayerProfile>>> completableFutureMap = new ConcurrentHashMap<>();
    public static CompletableFuture<Optional<PlayerProfile>> fetchMostRecentProfileAsync(String uid, String apiKey) {
        if (playerProfileCache.containsKey(uid)) {
            CachedData<PlayerProfile> cachedData = playerProfileCache.get(uid);
            if (cachedData.getExpire() > System.currentTimeMillis()) {
                return CompletableFuture.completedFuture(Optional.ofNullable(cachedData.getData()));
            }
            playerProfileCache.remove(uid);
        }
        if (completableFutureMap.containsKey(uid)) return completableFutureMap.get(uid);
        if (invalidKeys.contains(apiKey)) {
            CompletableFuture cf = new CompletableFuture();
            cf.completeExceptionally(new IOException("403 for url"));
            return cf;
        }

        CompletableFuture<Optional<PlayerProfile>> completableFuture = new CompletableFuture<>();
        ex.submit(() -> {
            try {
                Optional<PlayerProfile> playerProfile = fetchMostRecentProfile(uid, apiKey);
                playerProfileCache.put(uid, new CachedData<PlayerProfile>(System.currentTimeMillis()+1000*60*30, playerProfile.orElse(null)));
                completableFuture.complete(playerProfile);
                completableFutureMap.remove(uid);
                return;
            } catch (IOException e) {
                if (e.getMessage().contains("403 for URL")) {
                    completableFuture.completeExceptionally(e);
                    completableFutureMap.remove(uid);
                    invalidKeys.add(apiKey);
                } else {
                    completableFuture.complete(Optional.empty());
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
                UIDtoNickname.put(uid, new CachedData<String>(System.currentTimeMillis()+1000*60*60*12,playerProfile.orElse(null)));
                if (playerProfile.isPresent())
                    nicknameToUID.put(playerProfile.orElse(null), new CachedData<>(System.currentTimeMillis()+1000*60*60*12, uid));
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
                nicknameToUID.put(nickname, new CachedData<String>(System.currentTimeMillis()+1000*60*60*12,playerProfile.orElse(null)));
                if (playerProfile.isPresent())
                    UIDtoNickname.put(playerProfile.orElse(null), new CachedData<>(System.currentTimeMillis()+1000*60*60*12, nickname));

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
        JsonObject json = getJson("https://api.mojang.com/users/profiles/minecraft/"+nickname);
        if (json.has("error")) return Optional.empty();
        return Optional.of(TextUtils.insertDashUUID(json.get("id").getAsString()));
    }
    public static Optional<String> fetchNickname(String uuid) throws IOException {
        try {
            JsonArray json = getJsonArr("https://api.mojang.com/user/profiles/" + uuid.replace("-", "") + "/names");
            return Optional.of(json.get(json.size()-1).getAsJsonObject().get("name").getAsString());
        } catch (Exception e) {return Optional.empty();}
    }

    public static List<PlayerProfile> fetchPlayerProfiles(String uid, String apiKey) throws IOException {
        JsonObject json = getJson("https://api.hypixel.net/skyblock/profiles?uuid="+uid+"&key="+apiKey);
        if (!json.get("success").getAsBoolean()) return new ArrayList<>();
        JsonArray profiles = json.getAsJsonArray("profiles");
        String dashTrimmed = uid.replace("-", "");

        ArrayList<PlayerProfile> playerProfiles = new ArrayList<>();
        for (JsonElement jsonElement : profiles) {
            JsonObject semiProfile = jsonElement.getAsJsonObject();
            if (!semiProfile.has(dashTrimmed)) continue;
            playerProfiles.add(parseProfile(semiProfile, dashTrimmed));
        }
        return playerProfiles;
    }

    public static Optional<PlayerProfile> fetchMostRecentProfile(String uid, String apiKey) throws IOException {
        JsonObject json = getJson("https://api.hypixel.net/skyblock/profiles?uuid="+uid+"&key="+apiKey);
        if (!json.get("success").getAsBoolean()) return Optional.empty();
        JsonArray profiles = json.getAsJsonArray("profiles");
        String dashTrimmed = uid.replace("-", "");

        JsonObject profile = null;
        long lastSave = Long.MIN_VALUE;
        for (JsonElement jsonElement : profiles) {
            JsonObject semiProfile = jsonElement.getAsJsonObject();
            if (!semiProfile.getAsJsonObject("members").has(dashTrimmed)) continue;
            long lastSave2 = semiProfile.getAsJsonObject("members").getAsJsonObject(dashTrimmed).get("last_save").getAsLong();
            if (lastSave2 > lastSave) {
                profile = semiProfile;
                lastSave = lastSave2;
            }
        }

        if (profile == null) return Optional.empty();
        PlayerProfile pp = parseProfile(profile, dashTrimmed);
        json = getJson("https://api.hypixel.net/player?uuid="+uid+"&key="+apiKey);
        if (json.has("player")) {
            JsonObject treasures = json.getAsJsonObject("player");
            if (treasures.has("achievements")) {
                treasures = treasures.getAsJsonObject("achievements");
                if (treasures.has("skyblock_treasure_hunter")) {
                    pp.setTotalSecrets(treasures.get("skyblock_treasure_hunter").getAsInt());
                }
            }
        }

        return Optional.of(pp);
    }

    public static int getOrDefault(JsonObject jsonObject, String key, int value) {
        if (jsonObject == null || !jsonObject.has(key) || jsonObject.get(key) instanceof JsonNull) return value;
        return jsonObject.get(key).getAsInt();
    }
    public static long getOrDefault(JsonObject jsonObject, String key, long value) {
        if (jsonObject == null || !jsonObject.has(key) || jsonObject.get(key) instanceof JsonNull) return value;
        return jsonObject.get(key).getAsLong();
    }
    public static double getOrDefault(JsonObject jsonObject, String key, double value) {
        if (jsonObject == null || !jsonObject.has(key) || jsonObject.get(key) instanceof JsonNull) return value;
        return jsonObject.get(key).getAsDouble();
    }
    public static String getOrDefault(JsonObject jsonObject, String key, String value) {
        if (jsonObject == null || !jsonObject.has(key) || jsonObject.get(key) instanceof JsonNull) return value;
        return jsonObject.get(key).getAsString();
    }
    public static Double getOrDefaultNullable(JsonObject jsonObject, String key, Double value) {
        if (jsonObject == null || !jsonObject.has(key) || jsonObject.get(key) instanceof JsonNull) return value;
        return jsonObject.get(key).getAsDouble();
    }
    public static NBTTagCompound parseBase64NBT(String nbt) throws IOException {
        return CompressedStreamTools.readCompressed(new ByteArrayInputStream(Base64.getDecoder().decode(nbt)));
    }

    public static ItemStack deserializeNBT(NBTTagCompound nbtTagCompound) {
        if (nbtTagCompound.hasNoTags()) return null;
        ItemStack itemStack = new ItemStack(Blocks.stone);
        itemStack.deserializeNBT(nbtTagCompound);
        return itemStack;
    }

    public static PlayerProfile parseProfile(JsonObject profile, String dashTrimmed) throws IOException {
        PlayerProfile playerProfile = new PlayerProfile();
        playerProfile.setProfileUID(getOrDefault(profile, "profile_id", ""));
        playerProfile.setMemberUID(dashTrimmed);
        playerProfile.setProfileName(getOrDefault(profile, "cute_name", ""));

        JsonObject playerData = profile.getAsJsonObject("members").getAsJsonObject(dashTrimmed);
        playerProfile.setLastSave(getOrDefault(playerData, "last_save", 0L));
        playerProfile.setFairySouls(getOrDefault(playerData, "fairy_souls_collected", 0));
        playerProfile.setFairyExchanges(getOrDefault(playerData,  "fairy_exchanges", 0));

        if (playerData.has("inv_armor")) {
            playerProfile.setCurrentArmor(new PlayerProfile.Armor());
            NBTTagCompound armor = parseBase64NBT(playerData.getAsJsonObject("inv_armor")
                    .get("data")
                    .getAsString());
            NBTTagList array = armor.getTagList("i", 10);
            for (int i = 0; i < 4; i++) {
                NBTTagCompound item = array.getCompoundTagAt(i);
                playerProfile.getCurrentArmor().getArmorSlots()[i] = deserializeNBT(item);
            }
        }

        if (playerData.has("wardrobe_contents")) {
            NBTTagCompound armor = parseBase64NBT(playerData.getAsJsonObject("wardrobe_contents").get("data").getAsString());
            NBTTagList array = armor.getTagList("i", 10);
            for (int i = 0; i < array.tagCount(); i++) {
                if (i % 4 == 0) playerProfile.getWardrobe().add(new PlayerProfile.Armor());
                NBTTagCompound item = array.getCompoundTagAt(i);
                playerProfile.getWardrobe().get(i/4).getArmorSlots()[i%4] = deserializeNBT(item);
            }

        }
        playerProfile.setSelectedWardrobe(getOrDefault(playerData, "wardrobe_equipped_slot", -1));

        if (playerData.has("inv_contents")) {
            NBTTagCompound armor = parseBase64NBT(playerData.getAsJsonObject("inv_contents").get("data").getAsString());
            NBTTagList array = armor.getTagList("i", 10);
            playerProfile.setInventory(new ItemStack[array.tagCount()]);
            for (int i = 0; i < array.tagCount(); i++) {
                NBTTagCompound item = array.getCompoundTagAt(i);
                playerProfile.getInventory()[i] = deserializeNBT(item);
            }
        }
        if (playerData.has("ender_chest_contents")) {
            NBTTagCompound armor = parseBase64NBT(playerData.getAsJsonObject("ender_chest_contents").get("data").getAsString());
            NBTTagList array = armor.getTagList("i", 10);
            playerProfile.setEnderchest(new ItemStack[array.tagCount()]);
            for (int i = 0; i < array.tagCount(); i++) {
                NBTTagCompound item = array.getCompoundTagAt(i);
                playerProfile.getEnderchest()[i] = deserializeNBT(item);
            }
        }
        if (playerData.has("talisman_bag")) {
            NBTTagCompound armor = parseBase64NBT(playerData.getAsJsonObject("talisman_bag").get("data").getAsString());
            NBTTagList array = armor.getTagList("i", 10);
            playerProfile.setTalismans(new ItemStack[array.tagCount()]);
            for (int i = 0; i < array.tagCount(); i++) {
                NBTTagCompound item = array.getCompoundTagAt(i);
                playerProfile.getTalismans()[i] = deserializeNBT(item);
            }
        }

        playerProfile.setSkillXp(new HashMap<>());
        for (Skill value : Skill.values()) {
            playerProfile.getSkillXp().put(value, getOrDefaultNullable(playerData, "experience_skill_"+value.getJsonName(), null));
        }

        if (playerData.has("pets")) {
            for (JsonElement pets : playerData.getAsJsonArray("pets")) {
                JsonObject pet = pets.getAsJsonObject();
                Pet petObj = new Pet();
                petObj.setActive(pet.get("active").getAsBoolean());
                petObj.setExp(getOrDefault(pet, "exp", 0.0));
                petObj.setHeldItem(getOrDefault(pet, "heldItem", null));
                petObj.setSkin(getOrDefault(pet, "skin", null));
                petObj.setType(getOrDefault(pet, "type", null));
                petObj.setUuid(getOrDefault(pet, "uuid", null));

                playerProfile.getPets().add(petObj);
            }
        }

        if (playerData.has("dungeons") && playerData.getAsJsonObject("dungeons").has("dungeon_types")) {
            JsonObject types = playerData.getAsJsonObject("dungeons")
                    .getAsJsonObject("dungeon_types");
            for (DungeonType value : DungeonType.values()) {
                DungeonStat dungeonStat = new DungeonStat();
                DungeonSpecificData<DungeonStat> dungeonSpecificData = new DungeonSpecificData<>(value, dungeonStat);
                playerProfile.getDungeonStats().put(value, dungeonSpecificData);

                if (!types.has(value.getJsonName())) continue;

                JsonObject dungeonObj = types.getAsJsonObject(value.getJsonName());

                dungeonStat.setHighestCompleted(getOrDefault(dungeonObj, "highest_tier_completed", -1));

                for (Integer validFloor : value.getValidFloors()) {
                    DungeonStat.PlayedFloor playedFloor = new DungeonStat.PlayedFloor();
                    playedFloor.setBestScore(getOrDefault(dungeonObj.getAsJsonObject("best_score"), ""+validFloor, 0));
                    playedFloor.setCompletions(getOrDefault(dungeonObj.getAsJsonObject("tier_completions"), ""+validFloor, 0));
                    playedFloor.setFastestTime(getOrDefault(dungeonObj.getAsJsonObject("fastest_time"), ""+validFloor, -1));
                    playedFloor.setFastestTimeS(getOrDefault(dungeonObj.getAsJsonObject("fastest_time_s"), ""+validFloor, -1));
                    playedFloor.setFastestTimeSPlus(getOrDefault(dungeonObj.getAsJsonObject("fastest_time_s_plus"), ""+validFloor, -1));
                    playedFloor.setMobsKilled(getOrDefault(dungeonObj.getAsJsonObject("mobs_killed"), ""+validFloor, 0));
                    playedFloor.setMostMobsKilled(getOrDefault(dungeonObj.getAsJsonObject("most_mobs_killed"), ""+validFloor, 0));
                    playedFloor.setMostHealing(getOrDefault(dungeonObj.getAsJsonObject("most_healing"), ""+validFloor, 0));
                    playedFloor.setTimes_played(getOrDefault(dungeonObj.getAsJsonObject("times_played"), ""+validFloor, 0));
                    playedFloor.setWatcherKills(getOrDefault(dungeonObj.getAsJsonObject("watcher_kills"), ""+validFloor, 0));

                    for (DungeonClass dungeonClass : DungeonClass.values()) {
                        DungeonStat.PlayedFloor.ClassStatistics classStatistics = new DungeonStat.PlayedFloor.ClassStatistics();
                        classStatistics.setMostDamage(getOrDefault(dungeonObj.getAsJsonObject("most_damage_"+dungeonClass.getJsonName()), ""+validFloor, 0));
                        ClassSpecificData<DungeonStat.PlayedFloor.ClassStatistics> classStatisticsClassSpecificData = new ClassSpecificData<>(dungeonClass, classStatistics);

                        playedFloor.getClassStatistics().put(dungeonClass, classStatisticsClassSpecificData);
                    }

                    FloorSpecificData<DungeonStat.PlayedFloor> playedFloorFloorSpecificData = new FloorSpecificData<>(validFloor, playedFloor);
                    dungeonStat.getPlays().put(validFloor, playedFloorFloorSpecificData);
                }

                dungeonStat.setExperience(getOrDefault(dungeonObj, "experience", 0));


            }
        }
        if (playerData.has("dungeons") && playerData.getAsJsonObject("dungeons").has("player_classes")) {
            JsonObject classes = playerData.getAsJsonObject("dungeons")
                    .getAsJsonObject("player_classes");
            for (DungeonClass dungeonClass : DungeonClass.values()) {
                PlayerProfile.PlayerClassData classStatistics = new PlayerProfile.PlayerClassData();
                classStatistics.setExperience(getOrDefault(classes.getAsJsonObject(dungeonClass.getJsonName()), "experience", 0));
                ClassSpecificData<PlayerProfile.PlayerClassData> classStatisticsClassSpecificData = new ClassSpecificData<>(dungeonClass, classStatistics);

                playerProfile.getPlayerClassData().put(dungeonClass, classStatisticsClassSpecificData);
            }
        }
        if (playerData.has("dungeons")) {
            String id = getOrDefault(playerData.getAsJsonObject("dungeons"), "selected_dungeon_class", null);
            DungeonClass dungeonClass = DungeonClass.getClassByJsonName(id);
            playerProfile.setSelectedClass(dungeonClass);
        }
        try {
            calculateLilyWeight(playerProfile, playerData);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return playerProfile;
    }

    private static void calculateLilyWeight(PlayerProfile playerProfile, JsonObject playerData) throws ExecutionException, InterruptedException {
        JsonObject constants = getLilyWeightConstants().get();
        double[] slayerXP = new double[4];
        if (playerData.has("slayer_bosses")) {
            slayerXP[0] = getOrDefault(playerData.getAsJsonObject("slayer_bosses").getAsJsonObject("zombie"), "xp", 0);
            slayerXP[1] = getOrDefault(playerData.getAsJsonObject("slayer_bosses").getAsJsonObject("spider"), "xp", 0);
            slayerXP[2] = getOrDefault(playerData.getAsJsonObject("slayer_bosses").getAsJsonObject("wolf"), "xp", 0);
            slayerXP[3] = getOrDefault(playerData.getAsJsonObject("slayer_bosses").getAsJsonObject("enderman"), "xp", 0);
        }
        double skillWeight = 0;
        double overflowWeight = 0;
        {
            JsonObject srw = constants.getAsJsonObject("skillRatioWeight");
            int skillMaxXP = constants.get("skillMaxXP").getAsInt();
            JsonArray overflowMultiplier = constants.getAsJsonArray("skillOverflowMultipliers");
            JsonArray skillFactor = constants.getAsJsonArray("skillFactors");

            double skillAvg = playerProfile.getSkillXp().entrySet().stream()
                    .filter(a -> a.getValue() != null)
                    .filter(a -> srw.has(a.getKey().getJsonName()))
                    .map(a ->  XPUtils.getSkillXp(a.getKey(), a.getValue()).getLevel()).collect(Collectors.averagingInt(a -> a));

            double n = 12 * (skillAvg/60)*(skillAvg/60);
            double r2 = Math.sqrt(2);

            for (Map.Entry<Skill, Double> skillDoubleEntry : playerProfile.getSkillXp().entrySet()) {
                String jsonName = skillDoubleEntry.getKey().getJsonName();
                JsonArray temp_srw = srw.getAsJsonArray(jsonName);
                if (temp_srw == null) continue;
                int lv = XPUtils.getSkillXp(skillDoubleEntry.getKey(), skillDoubleEntry.getValue()).getLevel();
                skillWeight += n * temp_srw.get(lv).getAsDouble()
                            * temp_srw.get(temp_srw.size() - 1).getAsDouble();
                skillWeight += temp_srw.get(temp_srw.size() - 1).getAsDouble() * Math.pow(lv/60.0, r2);
            }

            int cnt = 0;
            for (Map.Entry<String, JsonElement> skillNames : constants.getAsJsonObject("skillNames").entrySet()) {
                Skill s = getSkillByLilyName(skillNames.getKey());
                double factor = skillFactor.get(cnt).getAsDouble();
                double effectiveOver;
                Double xp = playerProfile.getSkillXp().get(s);
                if (xp == null) continue; xp -= skillMaxXP;
                {
                    if (xp < skillMaxXP) effectiveOver = xp;
                    else {
                        double remainingXP = xp;
                        double z = 0;
                        for (int i = 0; i<= xp/skillMaxXP; i++) {
                            if (remainingXP >= skillMaxXP) {
                                remainingXP -= skillMaxXP;
                                z += Math.pow(factor, i);
                            }
                        }
                        effectiveOver = z * skillMaxXP;
                    }
                }
                double rating = effectiveOver / skillMaxXP;
                double overflowMult = overflowMultiplier.get(cnt).getAsDouble();
                double t = rating * overflowMult;
                if (t > 0) overflowWeight += t;
                cnt++;
            }
        }
        double cataCompWeight, masterCompWeight;
        {
            JsonArray completionFactor = constants.getAsJsonArray("dungeonCompletionWorth");
            JsonObject dungeonCompletionBuffs = constants.getAsJsonObject("dungeonCompletionBuffs");
            double max1000 = 0;
            double mMax1000 = 0;
            for (int i = 0; i < completionFactor.size(); i++) {
                if (i < 8) max1000 += completionFactor.get(i).getAsDouble();
                else mMax1000 += completionFactor.get(i).getAsDouble();
            }

            max1000 *= 1000;
            mMax1000 *= 1000;

            double upperBound = 1500; double score = 0;

            DungeonStat dStat = playerProfile.getDungeonStats().get(DungeonType.CATACOMBS).getData();
            for (FloorSpecificData<DungeonStat.PlayedFloor> value : dStat.getPlays().values()) {
                int runs = value.getData().getCompletions();
                int excess = 0; if (runs > 1000) {
                    excess = runs - 1000; runs = 1000;
                }

                double floorScore = runs * completionFactor.get(value.getFloor()).getAsDouble();
                if (excess > 0)
                    floorScore *= Math.log10(excess / 1000.0 + 1) / Math.log10(7.5) + 1;
                score += floorScore;
            }
            cataCompWeight = (score / max1000 * upperBound);

            dStat = playerProfile.getDungeonStats().get(DungeonType.MASTER_CATACOMBS).getData();
            for (FloorSpecificData<DungeonStat.PlayedFloor> value : dStat.getPlays().values()) {
                if (dungeonCompletionBuffs.has(value.getFloor()+"")) {
                    double threshold = 20;
                    if (value.getData().getCompletions() >= threshold) upperBound += dungeonCompletionBuffs.get(value.getFloor()+"").getAsDouble();
                    else upperBound += dungeonCompletionBuffs.get(value.getFloor()+"").getAsDouble() * Math.pow(value.getData().getCompletions()/threshold, 1.840896416);
                }
            }
            score = 0;
            for (FloorSpecificData<DungeonStat.PlayedFloor> value : dStat.getPlays().values()) {
                int runs = value.getData().getCompletions();
                int excess = 0; if (runs > 1000) {
                    excess = runs - 1000; runs = 1000;
                }

                double floorScore = runs * completionFactor.get(value.getFloor()+7).getAsDouble();
                if (excess > 0)
                    floorScore *= Math.log10(excess / 1000.0 + 1) / Math.log10(5) + 1;
                score += floorScore;
            }

            masterCompWeight = score / mMax1000 * upperBound;
        }
        double dungeonXPWeight = 0;
        {
            double dungeonOverall = constants.get("dungeonOverall").getAsDouble();
            double dungeonMaxXP = constants.get("dungeonMaxXP").getAsDouble();

            double cataXP = playerProfile.getDungeonStats().get(DungeonType.CATACOMBS).getData().getExperience();
            XPUtils.XPCalcResult xpCalcResult = XPUtils.getCataXp(cataXP);
            double level = xpCalcResult.getLevel();
            if (xpCalcResult.getLevel() != 50) {
                double progress = Math.floor(xpCalcResult.getRemainingXp() / xpCalcResult.getNextLvXp() * 1000) / 1000.0;
                level += progress;
            }

            double n; double tempLevel = 0;
            if (cataXP < dungeonMaxXP)
                n = 0.2 * Math.pow(level / 50.0, 2.967355422);
            else {
                double part = 142452410;
                tempLevel = 50 + (cataXP - dungeonMaxXP) / part;
                n = 0.2 * Math.pow(1 + ((tempLevel - 50) / 50), 2.967355422);
            }
            if (level != 0) {
                if (cataXP < 569809640) dungeonXPWeight = dungeonOverall * (Math.pow(1.18340401286164044, (level + 1)) - 1.05994990217254) * (1 + n);
                else  dungeonXPWeight =4000 * (n / 0.15465244570598540);
            } else dungeonXPWeight = 0;
        }
        double slayerWeight = 0;
        {
            JsonArray slayerDeprecationScaling = constants.getAsJsonArray("slayerDeprecationScaling");
            BiFunction<Double, Integer, Double> getSlayerWeight = (xp, type) -> {
                double score = 0;
                {
                    double d = xp / 100000;
                    if (xp >= 6416) {
                        double D = (d - Math.pow(3, (-5 / 2.0))) * (d + Math.pow(3, -5 / 2.0));
                        double u = Math.cbrt(3 * (d + Math.sqrt(D)));
                        double v = Math.cbrt(3 * (d - Math.sqrt(D)));
                        score = u + v - 1;
                    } else {
                        score = Math.sqrt(4 / 3.0) * Math.cos(Math.acos(d * Math.pow(3, 5 / 2.0)) / 3) - 1;
                    }
                }
                score = Math.floor(score);
                double scaling = slayerDeprecationScaling.get(type).getAsDouble();
                double effectiveXP = 0;
                for (int i = 1; i <= score; i++)
                    effectiveXP += (i * i + i) * Math.pow(scaling, i);
                effectiveXP = Math.round((1000000 * effectiveXP * (0.05 / scaling)) * 100) / 100.0;
                double actualXP = ((score*score*score / 6) + (score*score / 2) + (score / 3)) * 100000;
                double distance = xp - actualXP;
                double effectiveDistance = distance * Math.pow(scaling, score);
                return effectiveXP + effectiveDistance;
            };

            double zombie = getSlayerWeight.apply(slayerXP[0], 0);
            double spider = getSlayerWeight.apply(slayerXP[1], 1);
            double wolf = getSlayerWeight.apply(slayerXP[2], 2);
            double enderman = getSlayerWeight.apply(slayerXP[3], 3);
            double individual = zombie / 7000 + spider / 4800 + wolf / 2200 + enderman / 1000;
            double extra = (slayerXP[0] + 1.6 * slayerXP[1] + 3.6 * slayerXP[2] + 10 * slayerXP[3]) / 900000;
            slayerWeight = (individual + extra);
        }

        PlayerProfile.LilyWeight lilyWeight = new PlayerProfile.LilyWeight();
        lilyWeight.setCatacombs_base(cataCompWeight);
        lilyWeight.setCatacombs_master(masterCompWeight);
        lilyWeight.setCatacombs_exp(dungeonXPWeight);
        lilyWeight.setSkill_base(skillWeight);
        lilyWeight.setSkill_overflow(overflowWeight);
        lilyWeight.setSlayer(slayerWeight);

        playerProfile.setLilyWeight(lilyWeight);
    }

    private static Skill getSkillByLilyName(String lilyName) {
        switch(lilyName) {
            case "experience_skill_enchanting": return Skill.ENCHANTING;
            case "experience_skill_taming": return Skill.TAMING;
            case "experience_skill_alchemy": return Skill.ALCHEMY;
            case "experience_skill_mining": return Skill.MINING;
            case "experience_skill_farming": return Skill.FARMING;
            case "experience_skill_foraging": return Skill.FORAGING;
            case "experience_skill_combat": return Skill.COMBAT;
            case "experience_skill_fishing": return Skill.FISHING;
            default: return null;
        }
    }
}
