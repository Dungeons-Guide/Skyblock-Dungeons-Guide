package kr.syeyoung.modapi.v1_21_11.util;

import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.text.TextCodecs;

public class TextUtils {
    public static Component fromText(Text text) {
        JsonElement elem = TextCodecs.CODEC.encodeStart(MinecraftClient.getInstance().player.getRegistryManager().getOps(JsonOps.INSTANCE), text).getOrThrow();
        return GsonComponentSerializer.gson().deserializeFromTree(elem);
    }

    public static Text fromComponent(Component component) {
        JsonElement elem = GsonComponentSerializer.gson().serializeToTree(component);
        return TextCodecs.CODEC.decode(MinecraftClient.getInstance().player.getRegistryManager().getOps(JsonOps.INSTANCE), elem).getOrThrow().getFirst();
    }
}
