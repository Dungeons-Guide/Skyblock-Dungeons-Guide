package kr.syeyoung.modapi.v1_21_9.adventure;

import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.kyori.adventure.text.serializer.json.JSONOptions;
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
                .editOptions((b) -> {
                    b.values(JSONOptions.byDataVersion().at(4423));
                })
                .build();
    }

    @Override
    public @NotNull Consumer<GsonComponentSerializer.Builder> builder() {
        return (r) -> {};
    }
}
