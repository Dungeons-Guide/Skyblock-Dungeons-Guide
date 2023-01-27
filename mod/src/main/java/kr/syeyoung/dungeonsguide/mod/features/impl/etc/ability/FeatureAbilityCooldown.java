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

package kr.syeyoung.dungeonsguide.mod.features.impl.etc.ability;


import kr.syeyoung.dungeonsguide.mod.SkyblockStatus;
import kr.syeyoung.dungeonsguide.mod.config.types.AColor;
import kr.syeyoung.dungeonsguide.mod.events.annotations.DGEventHandler;
import kr.syeyoung.dungeonsguide.mod.events.impl.DGTickEvent;
import kr.syeyoung.dungeonsguide.mod.features.FeatureParameter;
import kr.syeyoung.dungeonsguide.mod.features.text.StyledText;
import kr.syeyoung.dungeonsguide.mod.features.text.TextHUDFeature;
import kr.syeyoung.dungeonsguide.mod.features.text.TextStyle;
import kr.syeyoung.dungeonsguide.mod.utils.TextUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.client.event.ClientChatReceivedEvent;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FeatureAbilityCooldown extends TextHUDFeature {

    public FeatureAbilityCooldown() {
        super("Misc", "View Ability Cooldowns", "A handy hud for viewing cooldown abilities", "etc.abilitycd2");
        getStyles().add(new TextStyle("abilityname", new AColor(0x00, 0xAA,0xAA,255), new AColor(0, 0,0,0), false));
        getStyles().add(new TextStyle("separator", new AColor(0x55, 0x55,0x55,255), new AColor(0, 0,0,0), false));
        getStyles().add(new TextStyle("number", new AColor(0x55, 0xFF,0xFF,255), new AColor(0, 0,0,0), false));
        getStyles().add(new TextStyle("unit",new AColor(0x00, 0xAA,0xAA,255), new AColor(0, 0,0,0), false));
        getStyles().add(new TextStyle("ready",new AColor(0xDF, 0x00,0x67,255), new AColor(0, 0,0,0), false));
        addParameter("disable", new FeatureParameter<Boolean>("disable", "Disable outside of dungeon", "Disable the feature when out of dungeon", false, "boolean"));
        addParameter("decimal", new FeatureParameter<Integer>("decimal", "Decimal places", "ex) 2 -> Cooldown: 3.21 3-> Cooldown: 3.210", 0, "integer"));
    }

    @Override
    public boolean isHUDViewable() {
        return SkyblockStatus.isOnSkyblock() && (!this.<Boolean>getParameter("disable").getValue() || (this.<Boolean>getParameter("disable").getValue() && SkyblockStatus.isOnSkyblock()));
    }

    @Override
    public List<String> getUsedTextStyle() {
        return Arrays.asList("abilityname", "separator", "number", "unit", "ready");
    }

    private static final List<StyledText> dummy = new ArrayList<>();

    static {
        dummy.add(new StyledText("Random Ability", "abilityname"));
        dummy.add(new StyledText(": ", "separator"));
        dummy.add(new StyledText("10", "number"));
        dummy.add(new StyledText("s\n", "unit"));
        dummy.add(new StyledText("Random Ability2", "abilityname"));
        dummy.add(new StyledText(": ", "separator"));
        dummy.add(new StyledText("10", "number"));
        dummy.add(new StyledText("m ", "unit"));
        dummy.add(new StyledText("9", "number"));
        dummy.add(new StyledText("s\n", "unit"));
        dummy.add(new StyledText("Random Ability", "abilityname"));
        dummy.add(new StyledText(": ", "separator"));
        dummy.add(new StyledText("READY", "ready"));
    }

    @Override
    public List<StyledText> getDummyText() {
        return dummy;
    }

    private static final Map<String, SkyblockAbility> skyblockAbilities = new HashMap<>();
    private static final Map<String, List<SkyblockAbility>> skyblockAbilitiesByItemID = new HashMap<>();

    static {
        register(new SkyblockAbility("Disgusting Healing", -1, -1, "REAPER_MASK"));
        register(new SkyblockAbility("Spirit Leap", -1, 5, "SPIRIT_LEAP"));
        register(new SkyblockAbility("Farmer's Grace", -1, -1, "RANCHERS_BOOTS"));
        register(new SkyblockAbility("Raise Souls", -1, 1, "SUMMONING_RING"));
        register(new SkyblockAbility("Instant Heal", 70, -1, "FLORID_ZOMBIE_SWORD"));
        register(new SkyblockAbility("Phantom Impel", -1, -1, "PHANTOM_ROD"));
        register(new SkyblockAbility("Implosion", 300, 10, "IMPLOSION_SCROLL"));
        register(new SkyblockAbility("Parley", -1, 5, "ASPECT_OF_THE_JERRY"));
        register(new SkyblockAbility("Guided Bat", 250, -1, "BAT_WAND"));
        register(new SkyblockAbility("Bat Swarm", -1, -1, "WITCH_MASK"));
        register(new SkyblockAbility("Flay", -1, -1, "SOUL_WHIP"));
        register(new SkyblockAbility("Instant Heal", 70, -1, "ZOMBIE_SWORD"));
        register(new SkyblockAbility("Mithril's Protection", -1, -1, "MITHRIL_COAT"));
        register(new SkyblockAbility("Second Wind", -1, 30, "SPIRIT_MASK"));
        register(new SkyblockAbility("Love Tap", -1, -1, "ZOMBIE_SOLDIER_CUTLASS"));
        register(new SkyblockAbility("Spiky", -1, -1, "PUFFERFISH_HAT"));
        register(new SkyblockAbility("Jingle Bells", -1, 1, "JINGLE_BELLS"));
        register(new SkyblockAbility("Wither Shield", 150, 10, "WITHER_SHIELD_SCROLL"));
        register(new SkyblockAbility("Wither Impact", 150, 5, "WITHER_SHIELD_SCROLL"));
        register(new SkyblockAbility("Brute Force", -1, -1, "WARDEN_HELMET"));
        register(new SkyblockAbility("Growth", -1, 4, "GROWTH_LEGGINGS"));
        register(new SkyblockAbility("Shadowstep", -1, 60, "SILENT_DEATH"));
        register(new SkyblockAbility("Creeper Veil", -1, -1, "WITHER_CLOAK"));
        register(new SkyblockAbility("Ice Bolt", 50, -1, "FROZEN_SCYTHE"));
        register(new SkyblockAbility("Rapid-fire", 10, -1, "JERRY_STAFF"));
        register(new SkyblockAbility("Water Burst", 20, -1, "SALMON_HELMET_NEW"));
        register(new SkyblockAbility("Mist Aura", -1, -1, "SORROW_BOOTS"));
        register(new SkyblockAbility("Deploy", -1, -1, "RADIANT_POWER_ORB"));
        register(new SkyblockAbility("Ice Spray", 50, 5, "ICE_SPRAY_WAND"));
        register(new SkyblockAbility("Grand... Zapper?", -1, -1, "BLOCK_ZAPPER"));
        register(new SkyblockAbility("Seek the King", -1, 5, "ROYAL_PIGEON"));
        register(new SkyblockAbility("Mist Aura", -1, -1, "SORROW_CHESTPLATE"));
        register(new SkyblockAbility("Healing Boost", -1, -1, "REVIVED_HEART"));
        register(new SkyblockAbility("Deploy", -1, -1, "OVERFLUX_POWER_ORB"));
        register(new SkyblockAbility("Swing", -1, -1, "BONE_BOOMERANG"));
        register(new SkyblockAbility("Growth", -1, 4, "GROWTH_CHESTPLATE"));
        register(new SkyblockAbility("Squash 'em", -1, -1, "RECLUSE_FANG"));
        register(new SkyblockAbility("Roll em'", -1, -1, "PUMPKIN_DICER"));
        register(new SkyblockAbility("Cleave", -1, -1, "SUPER_CLEAVER"));
        register(new SkyblockAbility("Water Burst", 20, -1, "SALMON_BOOTS_NEW"));
        register(new SkyblockAbility("Farmer's Delight", -1, -1, "BASKET_OF_SEEDS"));
        register(new SkyblockAbility("Block Damage", -1, 60, "GUARDIAN_CHESTPLATE"));
        register(new SkyblockAbility("Water Burst", 20, -1, "SALMON_LEGGINGS"));
        register(new SkyblockAbility("Bone Shield", -1, -1, "SKELETON_HELMET"));
        register(new SkyblockAbility("Iron Punch", 70, 3, "GOLEM_SWORD"));
        register(new SkyblockAbility("Built-in Storage", -1, -1, "BUILDERS_WAND"));
        register(new SkyblockAbility("Nasty Bite", -1, -1, "MOSQUITO_BOW"));
        register(new SkyblockAbility("Ender Warp", 50, 45, "ENDER_BOW"));
        register(new SkyblockAbility("Cleave", -1, -1, "CLEAVER"));
        register(new SkyblockAbility("Party Time!", -1, -1, "PARTY_HAT_CRAB"));
        register(new SkyblockAbility("Giant's Slam", 100, 30, "GIANTS_SWORD"));
        register(new SkyblockAbility("Snow Placer", -1, -1, "SNOW_SHOVEL"));
        register(new SkyblockAbility("Greed", -1, -1, "MIDAS_SWORD"));
        register(new SkyblockAbility("Clownin' Around", -1, 316, "STARRED_BONZO_MASK"));
        register(new SkyblockAbility("Weather", -1, 5, "WEATHER_STICK"));
        register(new SkyblockAbility("ME SMASH HEAD", 100, -1, "EDIBLE_MACE"));
        register(new SkyblockAbility("Splash", 10, 1, "FISH_HAT"));
        register(new SkyblockAbility("Deploy", -1, -1, "PLASMAFLUX_POWER_ORB"));
        register(new SkyblockAbility("Dragon Rage", 100, -1, "ASPECT_OF_THE_DRAGON"));
        register(new SkyblockAbility("Burning Souls", 400, -1, "PIGMAN_SWORD"));
        register(new SkyblockAbility("Water Burst", 20, -1, "SALMON_CHESTPLATE_NEW"));
        register(new SkyblockAbility("Fire Blast", 150, 30, "EMBER_ROD"));
        register(new SkyblockAbility("Commander Whip", -1, -1, "ZOMBIE_COMMANDER_WHIP"));
        register(new SkyblockAbility("Spooktacular", -1, -1, "GHOUL_BUSTER"));
        register(new SkyblockAbility("Cleave", -1, -1, "HYPER_CLEAVER"));
        register(new SkyblockAbility("Leap", 50, 1, "SILK_EDGE_SWORD"));
        register(new SkyblockAbility("Throw", 150, 5, "LIVID_DAGGER"));
        register(new SkyblockAbility("Raise Souls", -1, 1, "NECROMANCER_SWORD"));
        register(new SkyblockAbility("Double Jump", 50, -1, "SPIDER_BOOTS"));
        register(new SkyblockAbility("Speed Boost", 50, -1, "ROGUE_SWORD"));
        register(new SkyblockAbility("Spirit Glide", 250, 60, "THORNS_BOOTS"));
        register(new SkyblockAbility("Sting", 100, -1, "STINGER_BOW"));
        register(new SkyblockAbility("Roll em'", -1, -1, "MELON_DICER"));
        register(new SkyblockAbility("Explosive Shot", -1, -1, "EXPLOSIVE_BOW"));
        register(new SkyblockAbility("Heat-Seeking Rose", 35, 1, "FLOWER_OF_TRUTH"));
        register(new SkyblockAbility("Small Heal", 60, 1, "WAND_OF_HEALING"));
        register(new SkyblockAbility("Dreadlord", 40, -1, "CRYPT_DREADLORD_SWORD"));
        register(new SkyblockAbility("Shadow Fury", -1, 15, "STARRED_SHADOW_FURY"));
        register(new SkyblockAbility("Double Jump", 40, -1, "TARANTULA_BOOTS"));
        register(new SkyblockAbility("Acupuncture", 200, 5, "VOODOO_DOLL"));
        register(new SkyblockAbility("Showtime", 100, -1, "STARRED_BONZO_STAFF"));
        register(new SkyblockAbility("Heartstopper", -1, -1, "SCORPION_FOIL"));
        register(new SkyblockAbility("Rapid Fire", -1, 100, "MACHINE_GUN_BOW"));
        register(new SkyblockAbility("Water Burst", 20, -1, "SALMON_HELMET"));
        register(new SkyblockAbility("Alchemist's Bliss", -1, -1, "NETHER_WART_POUCH"));
        register(new SkyblockAbility("Water Burst", 20, -1, "SALMON_CHESTPLATE"));
        register(new SkyblockAbility("Instant Heal", 70, -1, "ORNATE_ZOMBIE_SWORD"));
        register(new SkyblockAbility("Shadow Fury", -1, 15, "SHADOW_FURY"));
        register(new SkyblockAbility("Healing Boost", -1, -1, "ZOMBIE_HEART"));
        register(new SkyblockAbility("Witherlord", 40, 3, "CRYPT_WITHERLORD_SWORD"));
        register(new SkyblockAbility("Revive", -1, -1, "REVIVE_STONE"));
        register(new SkyblockAbility("Raise Souls", -1, 1, "REAPER_SCYTHE"));
        register(new SkyblockAbility("Rejuvenate", -1, -1, "VAMPIRE_MASK"));
        register(new SkyblockAbility("Mist Aura", -1, -1, "SORROW_HELMET"));
        register(new SkyblockAbility("Place Dirt", -1, -1, "INFINIDIRT_WAND"));
        register(new SkyblockAbility("Clownin' Around", -1, 360, "BONZO_MASK"));
        register(new SkyblockAbility("Shadow Warp", 300, 10, "SHADOW_WARP_SCROLL"));
        register(new SkyblockAbility("Molten Wave", 500, 1, "MIDAS_STAFF"));
        register(new SkyblockAbility("Growth", -1, 4, "GROWTH_HELMET"));
        register(new SkyblockAbility("Howl", 150, 20, "WEIRD_TUBA"));
        register(new SkyblockAbility("Medium Heal", 80, 1, "WAND_OF_MENDING"));
        register(new SkyblockAbility("Throw", 20, -1, "AXE_OF_THE_SHREDDED"));
        register(new SkyblockAbility("Ink Bomb", 60, 30, "INK_WAND"));
        register(new SkyblockAbility("Whassup?", -1, -1, "AATROX_BATPHONE"));
        register(new SkyblockAbility("Deploy", -1, -1, "MANA_FLUX_POWER_ORB"));
        register(new SkyblockAbility("Extreme Focus", -1, -1, "END_STONE_BOW"));
        register(new SkyblockAbility("Healing Boost", -1, -1, "CRYSTALLIZED_HEART"));
        register(new SkyblockAbility("Mist Aura", -1, -1, "SORROW_LEGGINGS"));
        register(new SkyblockAbility("Showtime", 100, -1, "BONZO_STAFF"));
        register(new SkyblockAbility("Triple Shot", -1, -1, "RUNAANS_BOW"));
        register(new SkyblockAbility("Water Burst", 20, -1, "SALMON_LEGGINGS_NEW"));
        register(new SkyblockAbility("Rejuvenate", -1, -1, "VAMPIRE_WITCH_MASK"));
        register(new SkyblockAbility("Terrain Toss", 250, 1, "YETI_SWORD"));
        register(new SkyblockAbility("Instant Transmission", 50, -1, "ASPECT_OF_THE_END"));
        register(new SkyblockAbility("Ether Transmission", 180, -1, "ASPECT_OF_THE_END"));
        register(new SkyblockAbility("Ether Transmission", 180, -1, "ASPECT_OF_THE_VOID"));
        register(new SkyblockAbility("Detonate", -1, 60, "CREEPER_LEGGINGS"));
        register(new SkyblockAbility("Extreme Focus", -1, -1, "END_STONE_SWORD"));
        register(new SkyblockAbility("Leap", 50, 1, "LEAPING_SWORD"));
        register(new SkyblockAbility("Fun Guy Bonus", -1, -1, "FUNGI_CUTTER"));
        register(new SkyblockAbility("Cleave", -1, -1, "GIANT_CLEAVER"));
        register(new SkyblockAbility("Tempest", -1, -1, "HURRICANE_BOW"));
        register(new SkyblockAbility("Big Heal", 100, 1, "WAND_OF_RESTORATION"));
        register(new SkyblockAbility("Growth", -1, 4, "GROWTH_BOOTS"));
        register(new SkyblockAbility("Stinger", 150, -1, "SCORPION_BOW"));
        register(new SkyblockAbility("Eye Beam", -1, -1, "PRECURSOR_EYE"));
        register(new SkyblockAbility("Water Burst", 20, -1, "SALMON_BOOTS"));
        register(new SkyblockAbility("Mining Speed Boost", -1, 120, null));
        register(new SkyblockAbility("Pikobulus", -1, 110, null));
        // abilities

        register(new SkyblockAbility("Healing Circle", -1, 2, "DUNGEON_STONE"));
        register(new SkyblockAbility("Wish", -1, 120, "DUNGEON_STONE"));
        register(new SkyblockAbility("Guided Sheep", -1, 30, "DUNGEON_STONE"));
        register(new SkyblockAbility("Thunderstorm", -1, 500, "DUNGEON_STONE"));
        register(new SkyblockAbility("Throwing Axe", -1, 10, "DUNGEON_STONE"));
        register(new SkyblockAbility("Ragnarok", -1, 60, "DUNGEON_STONE"));
        register(new SkyblockAbility("Explosive Shot", -1, 40, "DUNGEON_STONE"));
        register(new SkyblockAbility("Rapid Fire", -1, 100, "DUNGEON_STONE"));
        register(new SkyblockAbility("Seismic Wave", -1, 15, "DUNGEON_STONE"));
        register(new SkyblockAbility("Castle of Stone", -1, 150, "DUNGEON_STONE"));
    }

    static void register(SkyblockAbility skyblockAbility) {
        if (!skyblockAbilities.containsKey(skyblockAbility.getName()))
            skyblockAbilities.put(skyblockAbility.getName(), skyblockAbility);
        if (skyblockAbility.getItemId() != null && skyblockAbility.getCooldown() != -1) {
            List<SkyblockAbility> skyblockAbility1 = skyblockAbilitiesByItemID.computeIfAbsent(skyblockAbility.getItemId(), (a) -> new ArrayList<>());
            skyblockAbility1.add(skyblockAbilities.get(skyblockAbility.getName()));
            skyblockAbilitiesByItemID.put(skyblockAbility.getItemId(), skyblockAbility1);
        }
    }

    private final TreeSet<UsedAbility> usedAbilities = new TreeSet<UsedAbility>((c1, c2) -> {
        int a = Comparator.comparingLong(UsedAbility::getCooldownEnd).compare(c1,c2);
        return c1.getAbility().getName().equals(c2.getAbility().getName()) ? 0 : a;
    });

    @Override
    public List<StyledText> getText() {
        List<StyledText> cooldowns = new ArrayList<>();

        for (UsedAbility usedAbility : usedAbilities) {
            long end = usedAbility.getCooldownEnd();
            if (System.currentTimeMillis() >= end) {
                if (System.currentTimeMillis() <= end + 20000) {
                    cooldowns.add(new StyledText(usedAbility.getAbility().getName(), "abilityname"));
                    cooldowns.add(new StyledText(": ", "separator"));
                    cooldowns.add(new StyledText("READY\n", "ready"));
                }
            } else {
                cooldowns.add(new StyledText(usedAbility.getAbility().getName(), "abilityname"));
                cooldowns.add(new StyledText(": ", "separator"));
                long millies = end-System.currentTimeMillis();
                double decimalPlaces = (double ) Math.pow(10, 3- this.<Integer>getParameter("decimal").getValue());
                if (decimalPlaces == 0) {
                    cooldowns.add(new StyledText( this.<Integer>getParameter("decimal").getValue()+" decimal places? You'd be joking\n", "unit"));
                    continue;
                }
                millies = (long) (((millies-1) / decimalPlaces + 1) * decimalPlaces);
                long hr = (long) (millies / (1000 * 60 * 60));
                long min  = (long) (( millies / (1000*60)) % 60);
                double seconds = (millies/1000.0 ) % 60;
                String secondStr = String.format("%."+(this.<Integer>getParameter("decimal").getValue())+"f", seconds);

                if (hr > 0) {
                    cooldowns.add(new StyledText(String.valueOf(hr), "number"));
                    cooldowns.add(new StyledText("h ", "unit"));
                }
                if (hr > 0 || min > 0) {
                    cooldowns.add(new StyledText(String.valueOf(min), "number"));
                    cooldowns.add(new StyledText("m ", "unit"));
                }
                if (hr > 0 || min > 0 || seconds > 0) {
                    cooldowns.add(new StyledText(secondStr, "number"));
                    cooldowns.add(new StyledText("s ", "unit"));
                }
                cooldowns.add(new StyledText("\n", "unit"));
            }
        }
        return cooldowns;
    }

    Pattern thePattern = Pattern.compile("§b-(\\d+) Mana \\(§6(.+)§b\\)");
    Pattern thePattern2 = Pattern.compile("§r§aUsed (.+)§r§a! §r§b\\((1194) Mana\\)§r");
    Pattern thePattern3 = Pattern.compile("§r§aUsed (.+)§r§a!§r");

    private String lastActionbarAbility;

    @DGEventHandler()
    public void onChat(ClientChatReceivedEvent clientChatReceivedEvent) {
        if (clientChatReceivedEvent.type == 2) {
            Matcher m = thePattern.matcher(clientChatReceivedEvent.message.getFormattedText());
            if (m.find()) {
                String name = m.group(2);
                if (!name.equalsIgnoreCase(lastActionbarAbility)) {
                    used(name);
                }
                lastActionbarAbility = name;
            } else {
                lastActionbarAbility = null;
            }
        } else {
            String message = clientChatReceivedEvent.message.getFormattedText();
            if (message.equals("§r§aYour §r§9Bonzo's Mask §r§asaved your life!§r")) {
                used("Clownin' Around");
            } else {
                Matcher m = thePattern2.matcher(message);
                if (m.matches()) {
                    String abilityName = TextUtils.stripColor(m.group(1));
                    used(abilityName);
                } else {
                    Matcher m2 = thePattern3.matcher(message);
                    if (m2.matches()) {
                        String abilityName = TextUtils.stripColor(m2.group(1));
                        used(abilityName);
                    } else if (message.startsWith("§r§aYou used your ") || message.endsWith("§r§aPickaxe Ability!§r")) {
                        String nocolor = TextUtils.stripColor(message);
                        String abilityName = nocolor.substring(nocolor.indexOf("your") + 5, nocolor.indexOf("Pickaxe") - 1);
                        used(abilityName);
                    }
                }
            }
        }
    }

    private void used(String ability) {
        if (skyblockAbilities.containsKey(ability)) {
            SkyblockAbility skyblockAbility = skyblockAbilities.get(ability);
            if (skyblockAbility.getCooldown() > 0) {
                UsedAbility usedAbility = new UsedAbility(skyblockAbility, System.currentTimeMillis() + skyblockAbility.getCooldown() * 1000);
                for (int i = 0; i < 3; i++) usedAbilities.remove(usedAbility);
                usedAbilities.add(usedAbility);
            }
        } else {
            System.out.println("Unknown ability: "+ability);
        }
    }

    public void checkForCooldown(ItemStack itemStack) {
        if (itemStack == null) return;
        NBTTagCompound nbt = itemStack.getTagCompound();
        if (nbt == null) return;
        NBTTagCompound extra = nbt.getCompoundTag("ExtraAttributes");
        if (extra == null) return;
        String id = extra.getString("id");
        if (!skyblockAbilitiesByItemID.containsKey(id)) return;
        List<SkyblockAbility> skyblockAbility = skyblockAbilitiesByItemID.get(id);

        NBTTagCompound display = nbt.getCompoundTag("display");
        if (display == null) return;
        NBTTagList lore = display.getTagList("Lore", 8);
        int thecd = -1;
        SkyblockAbility currentAbility = null;
        for (int i = 0; i < lore.tagCount(); i++) {
            String specific = lore.getStringTagAt(i);
            if (specific.startsWith("§8Cooldown: §a") && currentAbility != null) {
                String thecdstr = TextUtils.stripColor(specific).substring(10).trim();
                thecdstr = thecdstr.substring(0, thecdstr.length() - 1);
                thecd = Integer.parseInt(thecdstr);
                currentAbility.setCooldown(thecd);
                currentAbility = null;
            } else if (specific.startsWith("§6Item Ability: ")) {
                String ability = TextUtils.stripColor(specific).substring(14).trim();

                for (SkyblockAbility skyblockAbility1 : skyblockAbility) {
                    if (skyblockAbility1.getName().equals(ability)) {
                        currentAbility = skyblockAbility1;
                        break;
                    }
                }
            }
        }
    }

    @DGEventHandler
    public void onTick(DGTickEvent event) {
        EntityPlayerSP sp = Minecraft.getMinecraft().thePlayer;
        if (sp == null) return;
        if (sp.inventory == null || sp.inventory.armorInventory == null) return;
        for (ItemStack itemStack : sp.inventory.armorInventory) {
            checkForCooldown(itemStack);
        }
        for (ItemStack itemStack : sp.inventory.mainInventory) {
            checkForCooldown(itemStack);
        }
    }
}
