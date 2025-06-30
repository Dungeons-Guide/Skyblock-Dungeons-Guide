package kr.syeyoung.modapi.v1_8_9.entity;

import kr.syeyoung.modapi.entity.UPlayerSelf;
import kr.syeyoung.modapi.gui.UContainer;
import kr.syeyoung.modapi.item.UInventoryPlayer;
import kr.syeyoung.modapi.v1_8_9.gui.UContainerChestImpl;
import kr.syeyoung.modapi.v1_8_9.gui.UContainerImpl;
import kr.syeyoung.modapi.v1_8_9.item.UInventoryPlayerImpl;
import lombok.Getter;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.audience.MessageType;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.util.IChatComponent;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.common.MinecraftForge;
import org.jetbrains.annotations.NotNull;

public class UEntityPlayerSP extends UEntityPlayerImpl implements UPlayerSelf, Audience {
    @Getter
    protected EntityPlayerSP delegate;

    public UEntityPlayerSP(EntityPlayerSP delegate) {
        super(delegate);
        this.delegate = delegate;
    }

    // TODO: is this the best place to be??
    public String getClientBrand() {
        return delegate.getClientBrand();
    }

    public UInventoryPlayer getInventory() {
        return delegate.inventory == null ? null : new UInventoryPlayerImpl(delegate.inventory);
    }

    public UContainer getOpenContainer() {
        Container c = delegate.openContainer;
        if (c == null) return null;
        if (c instanceof ContainerChest) return new UContainerChestImpl((ContainerChest) c);
        return new UContainerImpl(delegate.openContainer);
    }

    @Override
    public void setOpenContainer(UContainer guiChest) {
        delegate.openContainer = ((UContainerImpl)guiChest).getDelegate();
    }

    @Override
    public void sendMessageToServer(String message) {
        delegate.sendChatMessage(message);
    }


    @SuppressWarnings("UnstableApiUsage")
    @Override
    public void sendMessage(@NotNull Identity source, @NotNull Component message, @NotNull MessageType type) {
        IChatComponent component = IChatComponent.Serializer.jsonToComponent(
                GsonComponentSerializer.colorDownsamplingGson().serialize(message)
        );

        ClientChatReceivedEvent event = new ClientChatReceivedEvent((byte) 1, component);
        MinecraftForge.EVENT_BUS.post(event);
        if (!event.isCanceled()) {
            Minecraft.getMinecraft().thePlayer.addChatMessage(event.message);
        }
    }
}
