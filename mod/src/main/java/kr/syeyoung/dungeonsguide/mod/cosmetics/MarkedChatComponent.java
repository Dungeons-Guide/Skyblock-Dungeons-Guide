package kr.syeyoung.dungeonsguide.mod.cosmetics;

import com.google.common.collect.Iterators;
import lombok.Setter;
import net.minecraft.util.ChatComponentStyle;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Spliterator;
import java.util.function.Consumer;

public class MarkedChatComponent implements Component {
    @Setter
    private String unformatted;
    @Setter
    private String formatted;

    public MarkedChatComponent(String unformatted, String formatted) {
        this.unformatted = unformatted;
        this.formatted = formatted;
    }


    @Override
    public Component setChatStyle(Style style) {
        return this;
    }

    @Override
    public Style getChatStyle() {
        return Style.EMPTY;
    }

    @Override
    public Component appendText(String text) {
        return this;
    }

    @Override
    public Component appendSibling(Component component) {
        return this;
    }

    @Override
    public String getUnformattedTextForChat() {
        return unformatted;
    }

    @Override
    public String getUnformattedText() {
        return unformatted;
    }

    @Override
    public String getFormattedText() {
        return formatted;
    }

    @Override
    public List<Component> getSiblings() {
        return Collections.emptyList();
    }

    @Override
    public Component createCopy() {
        return new MarkedChatComponent(formatted, unformatted);
    }

    @Override
    public @NotNull Iterator<Component> iterator() {
        return Iterators.forArray(this);
    }
}
