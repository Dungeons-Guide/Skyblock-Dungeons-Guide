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

package kr.syeyoung.dungeonsguide.mod.chat;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import kr.syeyoung.dungeonsguide.launcher.Main;
import kr.syeyoung.dungeonsguide.mod.events.ListenerPriority;
import kr.syeyoung.dungeonsguide.mod.events.annotations.SubscribeEvent;
import kr.syeyoung.dungeonsguide.mod.events.impl.ChatReceivedEvent;
import kr.syeyoung.dungeonsguide.mod.events.impl.TitleEvent;
import kr.syeyoung.dungeonsguide.mod.utils.TextUtils;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;

/**
 * Writes what the server sends as chat / action bar / titles, to measure for D104 (dgdebug chatcapture).
 * One json object per line: time, type, the component as json, and the 1.8.9 style legacy string.
 */
public class ChatCapture {
    public static final ChatCapture INSTANCE = new ChatCapture();

    private final Gson gson = new Gson();
    private Writer writer;
    @Getter
    private File file;

    public boolean isCapturing() {
        return writer != null;
    }

    public void start() throws IOException {
        if (writer != null) return;
        File dir = new File(Main.getConfigDir(), "chatcapture");
        dir.mkdirs();
        file = new File(dir, System.currentTimeMillis() + ".jsonl");
        writer = new FileWriter(file, StandardCharsets.UTF_8);
    }

    public void stop() throws IOException {
        if (writer == null) return;
        writer.close();
        writer = null;
    }

    @SubscribeEvent(priority = ListenerPriority.FIRST, receiveCanceled = true)
    public void onChat(ChatReceivedEvent event) {
        write(event.type == 0 ? "chat" : event.type == 1 ? "system" : "actionbar", event.message);
    }

    @SubscribeEvent(priority = ListenerPriority.FIRST)
    public void onTitle(TitleEvent event) {
        if (event.getPacketTitle() instanceof ClientboundSetTitleTextPacket title)
            write("title", title.text());
        else if (event.getPacketTitle() instanceof ClientboundSetSubtitleTextPacket subtitle)
            write("subtitle", subtitle.text());
    }

    private void write(String type, Component component) {
        if (writer == null) return;
        try {
            DynamicOps<JsonElement> ops = Minecraft.getInstance().level == null ? JsonOps.INSTANCE
                    : Minecraft.getInstance().level.registryAccess().createSerializationContext(JsonOps.INSTANCE);
            JsonObject line = new JsonObject();
            line.addProperty("time", System.currentTimeMillis());
            line.addProperty("type", type);
            line.add("json", ComponentSerialization.CODEC.encodeStart(ops, component).getOrThrow());
            line.addProperty("legacy", TextUtils.getFormattedText(component));
            writer.write(gson.toJson(line));
            writer.write('\n');
            writer.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
