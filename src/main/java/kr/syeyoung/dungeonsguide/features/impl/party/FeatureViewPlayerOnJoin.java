package kr.syeyoung.dungeonsguide.features.impl.party;

import io.github.moulberry.hychat.HyChat;
import io.github.moulberry.hychat.chat.ChatManager;
import io.github.moulberry.hychat.gui.GuiChatBox;
import kr.syeyoung.dungeonsguide.features.FeatureRegistry;
import kr.syeyoung.dungeonsguide.features.SimpleFeature;
import kr.syeyoung.dungeonsguide.features.impl.party.api.ApiFetchur;
import kr.syeyoung.dungeonsguide.features.impl.party.api.PlayerProfile;
import kr.syeyoung.dungeonsguide.features.listener.ChatListener;
import kr.syeyoung.dungeonsguide.features.listener.GuiPostRenderListener;
import kr.syeyoung.dungeonsguide.gui.MPanel;
import lombok.Getter;
import lombok.SneakyThrows;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.*;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.event.HoverEvent;
import net.minecraft.util.ChatComponentStyle;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.IChatComponent;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.fml.common.Loader;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class FeatureViewPlayerOnJoin extends SimpleFeature implements GuiPostRenderListener, ChatListener {

    public FeatureViewPlayerOnJoin() {
        super("Party Kicker", "View player stats when join", "view player rendering when joining/someone joins the party", "partykicker.viewstats", true);
    }

    private Rectangle popupRect;
    private String lastuid; // actually current uid
    private Future<Optional<PlayerProfile>> profileFuture;
    @SneakyThrows
    @Override
    public void onGuiPostRender(GuiScreenEvent.DrawScreenEvent.Post rendered) {
        if (!(Minecraft.getMinecraft().currentScreen instanceof GuiChat)) {
            popupRect = null;
            profileFuture = null;
            return;
        }
        ScaledResolution scaledResolution = new ScaledResolution(Minecraft.getMinecraft());
        int width = scaledResolution.getScaledWidth();
        int height = scaledResolution.getScaledHeight();
        int mouseX = Mouse.getX() * width / Minecraft.getMinecraft().displayWidth;
        int mouseY = height - Mouse.getY() * height / Minecraft.getMinecraft().displayHeight - 1;

        IChatComponent ichatcomponent = getHoveredComponent(scaledResolution);
        String uid = null;
        if (ichatcomponent != null && ichatcomponent.getChatStyle().getChatHoverEvent() instanceof HoverEventRenderPlayer) {
            uid = ((HoverEventRenderPlayer) ichatcomponent.getChatStyle().getChatHoverEvent()).getUuid();
        }

        if (!((popupRect != null && popupRect.contains(mouseX, mouseY)) || uid != null && uid.equals(lastuid))) {
            popupRect = null;
            profileFuture = null;
            lastuid = null;
        }

        if (uid != null && !uid.equals(lastuid) && (popupRect==null || !popupRect.contains(mouseX, mouseY))) {
            popupRect = null;
            profileFuture = null;
            lastuid = uid;
        }
        if (lastuid == null) return;


        if (popupRect == null) {
            popupRect = new Rectangle(mouseX, mouseY, 100, 200);
            if (popupRect.y + popupRect.height > scaledResolution.getScaledHeight()) {
                popupRect.y -= popupRect.y + popupRect.height - scaledResolution.getScaledHeight();
            }
        }

        if (profileFuture == null) {
            profileFuture = ApiFetchur.fetchMostRecentProfileAsync(lastuid, FeatureRegistry.PARTYKICKER_APIKEY.getAPIKey());
        }

        MPanel.clip(scaledResolution, popupRect.x, popupRect.y, popupRect.width, popupRect.height);

        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        GlStateManager.pushMatrix();
        GlStateManager.translate(popupRect.x, popupRect.y, 0);
        Gui.drawRect(0,0, popupRect.width, popupRect.height, 0xFF000000);
        System.out.println(lastuid + " - "+uid);
        if (!profileFuture.isDone()) {
            Minecraft.getMinecraft().fontRendererObj.drawString("Fetching data...", 5,5, 0xFFFFFFFF);
        } else {
            Optional<PlayerProfile> playerProfile = profileFuture.get();
            if (playerProfile.isPresent()) {
                Minecraft.getMinecraft().fontRendererObj.drawString(playerProfile.get().getMemberUID(), 5,5, 0xFFFFFFFF);
            } else {
                Minecraft.getMinecraft().fontRendererObj.drawString("User could not be found", 5,5, 0xFFFFFFFF);
            }
        }
        GlStateManager.popMatrix();
        GL11.glDisable(GL11.GL_SCISSOR_TEST);
    }

    public IChatComponent getHoveredComponent(ScaledResolution scaledResolution) {
        IChatComponent ichatcomponent = null;
        if (Loader.isModLoaded("hychat")) {
            try {
                ChatManager chatManager = HyChat.getInstance().getChatManager();
                GuiChatBox guiChatBox = chatManager.getFocusedChat();

                int x = guiChatBox.getX(scaledResolution);
                int y = guiChatBox.getY(scaledResolution);
                ichatcomponent = guiChatBox.chatArray.getHoveredComponent(guiChatBox.getSelectedTab().getChatLines(), Mouse.getX(), Mouse.getY(), x, y);
            } catch (Throwable t) {}
        }
        if (ichatcomponent == null) {
            ichatcomponent = Minecraft.getMinecraft().ingameGUI.getChatGUI().getChatComponent(Mouse.getX(), Mouse.getY());
        }
        return ichatcomponent;
    }

    @Override
    public void onChat(ClientChatReceivedEvent clientChatReceivedEvent) {
    }

    public static class HoverEventRenderPlayer extends HoverEvent {
        @Getter
        private String uuid;
        public HoverEventRenderPlayer(String uuid) {
            super(Action.SHOW_TEXT, new ChatComponentText(""));
            this.uuid = uuid;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            if (!super.equals(o)) return false;
            HoverEventRenderPlayer that = (HoverEventRenderPlayer) o;
            return Objects.equals(uuid, that.uuid);
        }

        @Override
        public int hashCode() {
            return Objects.hash(super.hashCode(), uuid);
        }

        private IChatComponent cached;

        @Override
        public IChatComponent getValue() {
            if (cached == null)
            return cached = new ChatComponentText("").setChatStyle(new ChatStyle().setChatHoverEvent(new HoverEvent(Action.SHOW_TEXT, new ChatComponentText(uuid))));
            return cached;
        }
    }
}
