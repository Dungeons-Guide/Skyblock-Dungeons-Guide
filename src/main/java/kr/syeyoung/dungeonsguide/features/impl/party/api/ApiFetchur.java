package kr.syeyoung.dungeonsguide.features.impl.party.api;

import com.google.gson.*;
import kr.syeyoung.dungeonsguide.utils.TextUtils;
import lombok.AllArgsConstructor;
import lombok.Data;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import org.apache.commons.io.IOUtils;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.*;

public class ApiFetchur {
    private static final Gson gson = new Gson();

    private static final Map<String, CachedData<PlayerProfile>> playerProfileCache = new HashMap<>();
    private static final Map<String, CachedData<String>> nicknameToUID = new HashMap<>();
    private static final ExecutorService ex = Executors.newFixedThreadPool(4);

    public static void purgeCache() {
        playerProfileCache.clear();
        nicknameToUID.clear();
    }

    @Data
    @AllArgsConstructor
    public static class CachedData<T> {
        private final long expire;
        private final T data;
    }

    public static JsonObject getJson(String url) throws IOException {
        URLConnection connection = new URL(url).openConnection();
        connection.setConnectTimeout(10000);
        connection.setReadTimeout(10000);
        return gson.fromJson(new InputStreamReader(connection.getInputStream()), JsonObject.class);
    }

    public static CompletableFuture<Optional<PlayerProfile>> fetchMostRecentProfileAsync(String uid, String apiKey) {
        if (playerProfileCache.containsKey(uid)) {
            CachedData<PlayerProfile> cachedData = playerProfileCache.get(uid);
            if (cachedData.expire > System.currentTimeMillis()) {
                return CompletableFuture.completedFuture(Optional.ofNullable(cachedData.data));
            }
            playerProfileCache.remove(uid);
        }

        CompletableFuture<Optional<PlayerProfile>> completableFuture = new CompletableFuture<>();
        ex.submit(() -> {
            try {
                Optional<PlayerProfile> playerProfile = fetchMostRecentProfile(uid, apiKey);
                playerProfileCache.put(uid, new CachedData<PlayerProfile>(System.currentTimeMillis()+1000*60*30, playerProfile.orElse(null)));
                completableFuture.complete(playerProfile);
                return;
            } catch (IOException e) {
                e.printStackTrace();
            }
            playerProfileCache.put(uid, new CachedData<PlayerProfile>(System.currentTimeMillis()+1000*60*30, null));
            completableFuture.complete(Optional.empty());
        });
        return completableFuture;
    }

    public static CompletableFuture<Optional<String>> fetchUUIDAsync(String nickname) {
        if (nicknameToUID.containsKey(nickname)) {
            CachedData<String> cachedData = nicknameToUID.get(nickname);
            if (cachedData.expire > System.currentTimeMillis()) {
                return CompletableFuture.completedFuture(Optional.ofNullable(cachedData.data));
            }
            nicknameToUID.remove(nickname);
        }


        CompletableFuture<Optional<String>> completableFuture = new CompletableFuture<>();

        ex.submit(() -> {
            try {
                Optional<String> playerProfile = fetchUUID(nickname);
                nicknameToUID.put(nickname, new CachedData<String>(System.currentTimeMillis()+1000*60*60,playerProfile.orElse(null)));
                completableFuture.complete(playerProfile);
                return;
            } catch (IOException e) {
                e.printStackTrace();
            }
            nicknameToUID.put(nickname, new CachedData<String>(System.currentTimeMillis()+1000*60*30, null));
            completableFuture.complete(Optional.empty());
        });

        return completableFuture;
    }

    public static Optional<String> fetchUUID(String nickname) throws IOException {
        JsonObject json = getJson("https://api.mojang.com/users/profiles/minecraft/"+nickname);
        if (json.has("error")) return Optional.empty();
        return Optional.of(TextUtils.insertDashUUID(json.get("id").getAsString()));
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

        return Optional.of(parseProfile(profile, dashTrimmed));
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

        for (Skill value : Skill.values()) {
            playerProfile.getSkillXp().put(value, getOrDefault(playerData, "experience_skill_"+value.getJsonName(), 0.0));
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
            String id = getOrDefault(playerData, "selected_dungeon_class", null);
            DungeonClass dungeonClass = DungeonClass.getClassByJsonName(id);
            playerProfile.setSelectedClass(dungeonClass);
        }

        return playerProfile;
    }
}
