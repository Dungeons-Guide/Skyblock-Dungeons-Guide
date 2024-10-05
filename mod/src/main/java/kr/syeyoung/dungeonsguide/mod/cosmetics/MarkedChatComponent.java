package kr.syeyoung.dungeonsguide.mod.cosmetics;

import com.google.common.collect.Iterators;
import lombok.Setter;
import net.minecraft.util.ChatComponentStyle;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.IChatComponent;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Spliterator;
import java.util.function.Consumer;

public class MarkedChatComponent implements IChatComponent {
    @Setter
    private String unformatted;
    @Setter
    private String formatted;

    public MarkedChatComponent(String unformatted, String formatted) {
        this.unformatted = unformatted;
        this.formatted = formatted;
    }


    @Override
    public IChatComponent setChatStyle(ChatStyle style) {
        return this;
    }

    @Override
    public ChatStyle getChatStyle() {
        return new ChatStyle();
    }

    @Override
    public IChatComponent appendText(String text) {
        return this;
    }

    @Override
    public IChatComponent appendSibling(IChatComponent component) {
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
    public List<IChatComponent> getSiblings() {
        return Collections.emptyList();
    }

    @Override
    public IChatComponent createCopy() {
        return new MarkedChatComponent(formatted, unformatted);
    }

    @Override
    public @NotNull Iterator<IChatComponent> iterator() {
        return Iterators.forArray(this);
    }
}
