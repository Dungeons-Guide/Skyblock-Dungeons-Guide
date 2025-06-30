package kr.syeyoung.modapi.v1_8_9.adventure;

import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.kyori.adventure.text.serializer.json.JSONOptions;
import net.kyori.adventure.text.serializer.json.legacyimpl.NBTLegacyHoverEventSerializer;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public class GsonProvider implements GsonComponentSerializer.Provider {
    @Override
    public @NotNull GsonComponentSerializer gson() {
        return gsonLegacy();
    }

    @Override
    public @NotNull GsonComponentSerializer gsonLegacy() {
        return GsonComponentSerializer.builder()
                .downsampleColors()
                .legacyHoverEventSerializer(NBTLegacyHoverEventSerializer.get())
                .editOptions((b) -> {
                    b.values(JSONOptions.byDataVersion().at(2525));
                })
                .build();
    }

    @Override
    public @NotNull Consumer<GsonComponentSerializer.Builder> builder() {
        return (builder) -> { builder.legacyHoverEventSerializer(NBTLegacyHoverEventSerializer.get()); };
    }
}
