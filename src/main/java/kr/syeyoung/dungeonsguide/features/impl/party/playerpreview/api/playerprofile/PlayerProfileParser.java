package kr.syeyoung.dungeonsguide.features.impl.party.playerpreview.api.playerprofile;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import kr.syeyoung.dungeonsguide.features.impl.party.playerpreview.api.ApiFetcher;
import kr.syeyoung.dungeonsguide.features.impl.party.playerpreview.api.playerprofile.dataclasses.*;
import kr.syeyoung.dungeonsguide.utils.XPUtils;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

public class PlayerProfileParser {

    public static volatile JsonObject constants;

    public static JsonObject getLilyWeightConstants() {
        if (constants != null) return constants;
        try {
            JsonObject jsonObject = ApiFetcher.getJson("https://raw.githubusercontent.com/Antonio32A/lilyweight/master/lib/constants.json");
            constants = jsonObject;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return constants;
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
        playerProfile.setFairyExchanges(getOrDefault(playerData, "fairy_exchanges", 0));

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
                playerProfile.getWardrobe().get(i / 4).getArmorSlots()[i % 4] = deserializeNBT(item);
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
            playerProfile.getSkillXp().put(value, getOrDefaultNullable(playerData, "experience_skill_" + value.getJsonName(), null));
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
                    playedFloor.setBestScore(getOrDefault(dungeonObj.getAsJsonObject("best_score"), "" + validFloor, 0));
                    playedFloor.setCompletions(getOrDefault(dungeonObj.getAsJsonObject("tier_completions"), "" + validFloor, 0));
                    playedFloor.setFastestTime(getOrDefault(dungeonObj.getAsJsonObject("fastest_time"), "" + validFloor, -1));
                    playedFloor.setFastestTimeS(getOrDefault(dungeonObj.getAsJsonObject("fastest_time_s"), "" + validFloor, -1));
                    playedFloor.setFastestTimeSPlus(getOrDefault(dungeonObj.getAsJsonObject("fastest_time_s_plus"), "" + validFloor, -1));
                    playedFloor.setMobsKilled(getOrDefault(dungeonObj.getAsJsonObject("mobs_killed"), "" + validFloor, 0));
                    playedFloor.setMostMobsKilled(getOrDefault(dungeonObj.getAsJsonObject("most_mobs_killed"), "" + validFloor, 0));
                    playedFloor.setMostHealing(getOrDefault(dungeonObj.getAsJsonObject("most_healing"), "" + validFloor, 0));
                    playedFloor.setTimes_played(getOrDefault(dungeonObj.getAsJsonObject("times_played"), "" + validFloor, 0));
                    playedFloor.setWatcherKills(getOrDefault(dungeonObj.getAsJsonObject("watcher_kills"), "" + validFloor, 0));

                    for (DungeonClass dungeonClass : DungeonClass.values()) {
                        DungeonStat.PlayedFloor.ClassStatistics classStatistics = new DungeonStat.PlayedFloor.ClassStatistics();
                        classStatistics.setMostDamage(getOrDefault(dungeonObj.getAsJsonObject("most_damage_" + dungeonClass.getJsonName()), "" + validFloor, 0));
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
        JsonObject constants = getLilyWeightConstants();
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
                    .map(a -> XPUtils.getSkillXp(a.getKey(), a.getValue()).getLevel()).collect(Collectors.averagingInt(a -> a));

            double n = 12 * (skillAvg / 60) * (skillAvg / 60);
            double r2 = Math.sqrt(2);

            for (Map.Entry<Skill, Double> skillDoubleEntry : playerProfile.getSkillXp().entrySet()) {
                String jsonName = skillDoubleEntry.getKey().getJsonName();
                JsonArray temp_srw = srw.getAsJsonArray(jsonName);
                if (temp_srw == null) continue;
                int lv = XPUtils.getSkillXp(skillDoubleEntry.getKey(), skillDoubleEntry.getValue()).getLevel();
                skillWeight += n * temp_srw.get(lv).getAsDouble()
                        * temp_srw.get(temp_srw.size() - 1).getAsDouble();
                skillWeight += temp_srw.get(temp_srw.size() - 1).getAsDouble() * Math.pow(lv / 60.0, r2);
            }

            int cnt = 0;
            for (Map.Entry<String, JsonElement> skillNames : constants.getAsJsonObject("skillNames").entrySet()) {
                Skill s = getSkillByLilyName(skillNames.getKey());
                double factor = skillFactor.get(cnt).getAsDouble();
                double effectiveOver;
                Double xp = playerProfile.getSkillXp().get(s);
                if (xp == null) continue;
                xp -= skillMaxXP;
                {
                    if (xp < skillMaxXP) effectiveOver = xp;
                    else {
                        double remainingXP = xp;
                        double z = 0;
                        for (int i = 0; i <= xp / skillMaxXP; i++) {
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

            double upperBound = 1500;
            double score = 0;

            DungeonStat dStat = playerProfile.getDungeonStats().get(DungeonType.CATACOMBS).getData();
            for (FloorSpecificData<DungeonStat.PlayedFloor> value : dStat.getPlays().values()) {
                int runs = value.getData().getCompletions();
                int excess = 0;
                if (runs > 1000) {
                    excess = runs - 1000;
                    runs = 1000;
                }

                double floorScore = runs * completionFactor.get(value.getFloor()).getAsDouble();
                if (excess > 0)
                    floorScore *= Math.log10(excess / 1000.0 + 1) / Math.log10(7.5) + 1;
                score += floorScore;
            }
            cataCompWeight = (score / max1000 * upperBound);

            dStat = playerProfile.getDungeonStats().get(DungeonType.MASTER_CATACOMBS).getData();
            for (FloorSpecificData<DungeonStat.PlayedFloor> value : dStat.getPlays().values()) {
                if (dungeonCompletionBuffs.has(value.getFloor() + "")) {
                    double threshold = 20;
                    if (value.getData().getCompletions() >= threshold)
                        upperBound += dungeonCompletionBuffs.get(value.getFloor() + "").getAsDouble();
                    else
                        upperBound += dungeonCompletionBuffs.get(value.getFloor() + "").getAsDouble() * Math.pow(value.getData().getCompletions() / threshold, 1.840896416);
                }
            }
            score = 0;
            for (FloorSpecificData<DungeonStat.PlayedFloor> value : dStat.getPlays().values()) {
                int runs = value.getData().getCompletions();
                int excess = 0;
                if (runs > 1000) {
                    excess = runs - 1000;
                    runs = 1000;
                }

                double floorScore = runs * completionFactor.get(value.getFloor() + 7).getAsDouble();
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

            double n;
            double tempLevel = 0;
            if (cataXP < dungeonMaxXP)
                n = 0.2 * Math.pow(level / 50.0, 2.967355422);
            else {
                double part = 142452410;
                tempLevel = 50 + (cataXP - dungeonMaxXP) / part;
                n = 0.2 * Math.pow(1 + ((tempLevel - 50) / 50), 2.967355422);
            }
            if (level != 0) {
                if (cataXP < 569809640)
                    dungeonXPWeight = dungeonOverall * (Math.pow(1.18340401286164044, (level + 1)) - 1.05994990217254) * (1 + n);
                else dungeonXPWeight = 4000 * (n / 0.15465244570598540);
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
                double actualXP = ((score * score * score / 6) + (score * score / 2) + (score / 3)) * 100000;
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
        switch (lilyName) {
            case "experience_skill_enchanting":
                return Skill.ENCHANTING;
            case "experience_skill_taming":
                return Skill.TAMING;
            case "experience_skill_alchemy":
                return Skill.ALCHEMY;
            case "experience_skill_mining":
                return Skill.MINING;
            case "experience_skill_farming":
                return Skill.FARMING;
            case "experience_skill_foraging":
                return Skill.FORAGING;
            case "experience_skill_combat":
                return Skill.COMBAT;
            case "experience_skill_fishing":
                return Skill.FISHING;
            default:
                return null;
        }
    }
}
