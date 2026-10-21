/*
 * Dungeons Guide - The most intelligent Hypixel Skyblock Dungeons Mod
 * Copyright (C) 2026  cyoung06 (syeyoung)
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

package kr.syeyoung.dungeonsguide.mod.dungeon.data;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.commands.arguments.blocks.BlockStateParser;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * 1.8.9's blocks as groups of 26.1 block states (D110 / D112). In 1.8.9 `block == Blocks.stone` also matched granite,
 * so code checks keep that meaning through these instead of comparing 26.1 blocks.
 * The table is /legacy_blocks.json, written by dgdebug convertroomdata from vanilla's flattening data.
 */
@AllArgsConstructor
public enum LegacyBlock {
    AIR(0), STONE(1), PLANKS(5), BEDROCK(7), SPONGE(19), DISPENSER(23), WOOL(35), DOUBLE_STONE_SLAB(43), STONE_SLAB(44),
    TNT(46), BOOKSHELF(47), CHEST(54), STANDING_SIGN(63), WALL_SIGN(68), LEVER(69), WOODEN_PRESSURE_PLATE(72),
    STONE_BUTTON(77), SNOW(80), CLAY(82), MONSTER_EGG(97), STONEBRICK(98), IRON_BARS(101), STONE_BRICK_STAIRS(109),
    END_PORTAL_FRAME(120), COMMAND_BLOCK(137), SKULL(144), TRAPPED_CHEST(146), LIGHT_WEIGHTED_PRESSURE_PLATE(147),
    QUARTZ_ORE(153), DROPPER(158), STAINED_HARDENED_CLAY(159), STAINED_GLASS_PANE(160), BARRIER(166), PRISMARINE(168),
    SEA_LANTERN(169), CARPET(171), HARDENED_CLAY(172), COAL_BLOCK(173), STONE_SLAB2(182);

    @Getter
    private final int legacyId;

    private static final Map<Integer, LegacyBlock> BY_ID = new HashMap<>();
    static {
        for (LegacyBlock value : values()) BY_ID.put(value.legacyId, value);
    }

    public static final String TABLE = "/legacy_blocks.json";

    // legacy id << 4 | meta for each 26.1 state, and the state for each legacy id << 4 | meta
    private static Map<BlockState, Integer> legacyByState;
    private static Map<Block, Integer> legacyByBlock;
    private static BlockState[] stateByLegacy;

    private static synchronized void load() {
        if (stateByLegacy != null) return;
        Map<BlockState, Integer> byState = new HashMap<>();
        Map<Block, Integer> byBlock = new HashMap<>();
        BlockState[] byLegacy = new BlockState[4096];
        try (InputStream is = LegacyBlock.class.getResourceAsStream(TABLE)) {
            if (is == null) throw new IllegalStateException(TABLE + " is missing, run dgdebug convertroomdata in a dev client");
            JsonArray entries = new Gson().fromJson(new InputStreamReader(is, StandardCharsets.UTF_8), JsonArray.class);
            for (JsonElement element : entries) {
                JsonObject entry = element.getAsJsonObject();
                int legacy = entry.get("id").getAsInt() << 4 | entry.get("meta").getAsInt();
                BlockState state = parse(entry.get("state").getAsString());
                byLegacy[legacy] = state;
                // ids / metas 1.8.9 didn't have came out as air, they don't map back
                if (state.isAir() && legacy >> 4 != 0) continue;
                byState.putIfAbsent(state, legacy);
                byBlock.putIfAbsent(state.getBlock(), legacy);
            }
        } catch (Exception e) {
            throw new IllegalStateException("Couldn't load " + TABLE, e);
        }
        legacyByState = byState;
        legacyByBlock = byBlock;
        stateByLegacy = byLegacy;
    }

    public static BlockState parse(String state) {
        try {
            return BlockStateParser.parseForBlock(BuiltInRegistries.BLOCK, state, false).blockState();
        } catch (Exception e) {
            throw new IllegalArgumentException("Bad block state " + state, e);
        }
    }

    // legacy id << 4 | meta, -1 for blocks 1.8.9 didn't have
    public static int legacy(BlockState state) {
        load();
        Integer legacy = legacyByState.get(state);
        if (legacy == null) legacy = legacyByBlock.get(state.getBlock());
        return legacy == null ? -1 : legacy;
    }

    public static int legacyId(BlockState state) {
        int legacy = legacy(state);
        return legacy == -1 ? -1 : legacy >> 4;
    }

    public static int meta(BlockState state) {
        int legacy = legacy(state);
        return legacy == -1 ? 0 : legacy & 15;
    }

    public static LegacyBlock of(BlockState state) {
        return BY_ID.get(legacyId(state));
    }

    public static BlockState state(int legacyId, int meta) {
        load();
        BlockState state = stateByLegacy[(legacyId << 4 | meta) & 4095];
        return state == null ? Blocks.AIR.defaultBlockState() : state;
    }

    public BlockState state(int meta) {
        return state(legacyId, meta);
    }

    // null is no block, like 1.8.9's null == Blocks.x
    public boolean is(BlockState state) {
        return state != null && legacyId(state) == legacyId;
    }
}
