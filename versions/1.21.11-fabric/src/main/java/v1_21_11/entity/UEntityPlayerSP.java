package v1_21_11.entity;

import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import kr.syeyoung.modapi.entity.UPlayerSelf;
import kr.syeyoung.modapi.gui.UContainer;
import kr.syeyoung.modapi.item.UInventoryPlayer;
import kr.syeyoung.modapi.util.GameMode;
import kr.syeyoung.modapi.v1_21_5.entity.UEntityPlayerImpl;
import kr.syeyoung.modapi.v1_21_5.gui.UContainerChestImpl;
import kr.syeyoung.modapi.v1_21_5.gui.UContainerImpl;
import kr.syeyoung.modapi.v1_21_5.item.UInventoryPlayerImpl;
import lombok.Getter;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.audience.MessageType;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.text.Text;
import net.minecraft.text.TextCodecs;
import org.jetbrains.annotations.NotNull;

public class UEntityPlayerSP extends UEntityPlayerImpl implements UPlayerSelf, Audience {
    @Getter
    protected ClientPlayerEntity delegate;

    public UEntityPlayerSP(ClientPlayerEntity delegate) {
        super(delegate);
        this.delegate = delegate;
    }

    // TODO: is this the best place to be??
    public String getClientBrand() {
        return MinecraftClient.getInstance().getNetworkHandler().getBrand();
    }

    public UInventoryPlayer getInventory() {
        return delegate.getInventory() == null ? null : new UInventoryPlayerImpl(delegate.getInventory());
    }

    public UContainer getOpenContainer() {
        ScreenHandler c = delegate.currentScreenHandler;
        if (c == null || c == delegate.playerScreenHandler) return null;
        if (c instanceof GenericContainerScreenHandler) return new UContainerChestImpl((GenericContainerScreenHandler) c, MinecraftClient.getInstance().currentScreen.getTitle());
        return new UContainerImpl(c);
    }

    @Override
    public void setOpenContainer(UContainer guiChest) {
        delegate.currentScreenHandler = ((UContainerImpl)guiChest).getDelegate();
    }

    @Override
    public void sendMessageToServer(String message) {
        if (message.startsWith("/")) {
            delegate.networkHandler.sendChatCommand(message.substring(1));
        } else {
            delegate.networkHandler.sendChatMessage(message);
        }
    }


    @SuppressWarnings("UnstableApiUsage")
    @Override
    public void sendMessage(@NotNull Identity source, @NotNull Component message, @NotNull MessageType type) {
        JsonElement element = GsonComponentSerializer.gson().serializeToTree(message);
        Text component1 = TextCodecs.CODEC.decode(delegate.getRegistryManager().getOps(JsonOps.INSTANCE), element).getOrThrow().getFirst();

        delegate.sendMessage(component1, false); // apparently fabric takes care of most stuff.

    }

    @Override
    public GameMode getGameMode() {
        switch (delegate.getGameMode()) {
            case CREATIVE:
                return GameMode.CREATIVE;
            case SPECTATOR:
                return GameMode.SPECTATOR;
            case SURVIVAL:
                return GameMode.SURVIVAL;
            case ADVENTURE:
                return GameMode.ADVENTURE;
            case null:
                break;
            default:
                return null;
        }
        return null;
    }
}
