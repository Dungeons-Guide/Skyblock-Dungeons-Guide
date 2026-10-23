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
package kr.syeyoung.dungeonsguide.mod.features.impl.advanced;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import kr.syeyoung.dungeonsguide.launcher.Main;
import kr.syeyoung.dungeonsguide.mod.features.SimpleFeature;
import net.minecraft.client.Minecraft;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;

/**
 * Writes the held item and the open container's slots as their component json, to measure Hypixel's 26.1 item
 * layout (custom_data, profile, lore) for D125 (dgdebug itemcapture).
 */
public class FeatureItemCapture extends SimpleFeature {
    private static final Gson GSON = new Gson();

    public FeatureItemCapture() {
        super("Debug", "Item Capture", "Writes the held item and the open container's slots as component json on /dgdebug itemcapture", "debug.itemcapture", false);
    }

    public File capture() throws IOException {
        Minecraft mc = Minecraft.getInstance();
        DynamicOps<JsonElement> ops = mc.level.registryAccess().createSerializationContext(JsonOps.INSTANCE);

        JsonObject capture = new JsonObject();
        capture.addProperty("time", System.currentTimeMillis());
        capture.add("held", encode(ops, mc.player.getMainHandItem()));

        AbstractContainerMenu menu = mc.player.containerMenu;
        if (mc.screen != null) capture.addProperty("screen", mc.screen.getTitle().getString());
        JsonArray slots = new JsonArray();
        for (Slot slot : menu.slots) {
            if (!slot.hasItem()) continue;
            JsonObject entry = new JsonObject();
            entry.addProperty("slot", slot.index);
            entry.add("item", encode(ops, slot.getItem()));
            slots.add(entry);
        }
        capture.add("slots", slots);

        File dir = new File(Main.getConfigDir(), "itemcapture");
        dir.mkdirs();
        File file = new File(dir, System.currentTimeMillis() + ".json");
        try (Writer writer = new FileWriter(file, StandardCharsets.UTF_8)) {
            writer.write(GSON.toJson(capture));
        }
        return file;
    }

    private static JsonElement encode(DynamicOps<JsonElement> ops, ItemStack stack) {
        if (stack.isEmpty()) return null;
        return ItemStack.CODEC.encodeStart(ops, stack).getOrThrow();
    }
}
