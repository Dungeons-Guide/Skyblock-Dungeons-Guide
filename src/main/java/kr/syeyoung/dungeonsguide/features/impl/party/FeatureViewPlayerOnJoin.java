package kr.syeyoung.dungeonsguide.features.impl.party;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import io.github.moulberry.hychat.HyChat;
import io.github.moulberry.hychat.chat.ChatManager;
import io.github.moulberry.hychat.gui.GuiChatBox;
import kr.syeyoung.dungeonsguide.config.guiconfig.FeatureEditPane;
import kr.syeyoung.dungeonsguide.features.FeatureRegistry;
import kr.syeyoung.dungeonsguide.features.SimpleFeature;
import kr.syeyoung.dungeonsguide.features.impl.party.api.ApiFetchur;
import kr.syeyoung.dungeonsguide.features.impl.party.api.PlayerProfile;
import kr.syeyoung.dungeonsguide.features.impl.party.api.SkinFetchur;
import kr.syeyoung.dungeonsguide.features.listener.ChatListener;
import kr.syeyoung.dungeonsguide.features.listener.GuiPostRenderListener;
import kr.syeyoung.dungeonsguide.gui.MPanel;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.client.gui.*;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.client.resources.SkinManager;
import net.minecraft.event.HoverEvent;
import net.minecraft.item.ItemStack;
import net.minecraft.util.*;
import net.minecraft.world.World;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.fml.common.Loader;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.List;
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
    private Future<Optional<GameProfile>> gfFuture;
    private Future<SkinFetchur.SkinSet> skinFuture;
    private FakePlayer fakePlayer;
    @SneakyThrows
    @Override
    public void onGuiPostRender(GuiScreenEvent.DrawScreenEvent.Post rendered) {
        if (!(Minecraft.getMinecraft().currentScreen instanceof GuiChat)) {
            popupRect = null;
            profileFuture = null;
            gfFuture = null;
            skinFuture=  null;
            fakePlayer= null;
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
            gfFuture = null;
            skinFuture=  null;
            fakePlayer= null;
        }

        if (uid != null && !uid.equals(lastuid) && (popupRect==null || !popupRect.contains(mouseX, mouseY))) {
            popupRect = null;
            profileFuture = null;
            gfFuture = null;
            skinFuture=  null;
            fakePlayer= null;
            lastuid = uid;
        }
        if (lastuid == null) return;


        if (popupRect == null) {
            popupRect = new Rectangle(mouseX, mouseY, 150, 200);
            if (popupRect.y + popupRect.height > scaledResolution.getScaledHeight()) {
                popupRect.y -= popupRect.y + popupRect.height - scaledResolution.getScaledHeight();
            }
        }

        if (profileFuture == null) {
            profileFuture = ApiFetchur.fetchMostRecentProfileAsync(lastuid, FeatureRegistry.PARTYKICKER_APIKEY.getAPIKey());
        }

        if (gfFuture == null) {
            gfFuture = ApiFetchur.getSkinGameProfileByUUIDAsync(lastuid);
        }
        if (skinFuture == null && gfFuture.isDone()) {
            skinFuture = SkinFetchur.getSkinSet(gfFuture.get().orElse(null));
        }

        if (fakePlayer == null && skinFuture != null && profileFuture != null && skinFuture.isDone() && profileFuture.isDone()) {
            fakePlayer = new FakePlayer(gfFuture.get().orElse(null), skinFuture.get(), profileFuture.get().orElse(null));
        }


        render(popupRect, scaledResolution, mouseX, mouseY, profileFuture.isDone() ? profileFuture.get() : null);
    }

    public void render(Rectangle popupRect, ScaledResolution scaledResolution, int mouseX, int mouseY, Optional<PlayerProfile> playerProfile) {

        GlStateManager.pushMatrix();
        GlStateManager.translate(popupRect.x, popupRect.y, 0);
        Gui.drawRect(0,0, popupRect.width, popupRect.height, 0xFF000000);
        Gui.drawRect(1,1, popupRect.width-1, popupRect.height-1, 0xFFAAAAAA);
        if (playerProfile == null) {
            Minecraft.getMinecraft().fontRendererObj.drawString("Fetching data...", 5,5, 0xFFFFFFFF);
            GlStateManager.popMatrix();
            return;
        }
        if (!playerProfile.isPresent()) {
            Minecraft.getMinecraft().fontRendererObj.drawString("User could not be found", 5,5, 0xFFFFFFFF);
            GlStateManager.popMatrix();
            return;
        }


        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        MPanel.clip(scaledResolution, popupRect.x, popupRect.y, popupRect.width, popupRect.height);
        Gui.drawRect(0,0, 80, popupRect.height-40, 0xFF000000);
        Gui.drawRect(1,1, 79, popupRect.height-41, 0xFF444444);
        GlStateManager.color(1, 1, 1, 1.0F);

        FontRenderer fr = Minecraft.getMinecraft().fontRendererObj;
        if (fakePlayer != null) {
            GuiInventory.drawEntityOnScreen(40, 150, 60, -(mouseX - popupRect.x - 75), 0, fakePlayer);
            fr.drawString(fakePlayer.getName(), (80 - fr.getStringWidth(fakePlayer.getName())) / 2, 15, 0xFFEFFF00);

            int relX = mouseX - popupRect.x;
            int relY = mouseY - popupRect.y;
            ItemStack toHover = null;
            System.out.println(relX + " , "+relY);
            if (relX > 5 && relX < 75) {
                if (33<=relY && relY <= 66) {
                    toHover = fakePlayer.getInventory()[3];
                } else if (66 <= relY && relY <= 108) {
                    toHover = fakePlayer.getInventory()[2];
                } else if (108 <= relY && relY <= 130) {
                    toHover = fakePlayer.getInventory()[1];
                } else if (130 <= relY && relY <= 154) {
                    toHover = fakePlayer.getInventory()[0];
                }
            }

            if (toHover != null) {
                List<String> list = toHover.getTooltip(Minecraft.getMinecraft().thePlayer, Minecraft.getMinecraft().gameSettings.advancedItemTooltips);

                for (int i = 0; i < list.size(); ++i)
                {
                    if (i == 0)
                    {
                        list.set(i, toHover.getRarity().rarityColor + (String)list.get(i));
                    }
                    else
                    {
                        list.set(i, EnumChatFormatting.GRAY + (String)list.get(i));
                    }
                }

                FontRenderer font = toHover.getItem().getFontRenderer(toHover);
                System.out.println(list);
                GL11.glDisable(GL11.GL_SCISSOR_TEST);
                FontRenderer theRenderer = (font == null ? fr : font);
                int minY = scaledResolution.getScaledHeight() - (list.size()+4) * theRenderer.FONT_HEIGHT - popupRect.y;

                FeatureEditPane.drawHoveringText(list,relX, Math.min(minY, relY), theRenderer);
                GL11.glEnable(GL11.GL_SCISSOR_TEST);
            }
        } else {
            fr.drawString("Loading", 5,35, 0xFFEFFF00);
        }

        GL11.glDisable(GL11.GL_SCISSOR_TEST);
        GlStateManager.popMatrix(); // 33 66 108 130 154 // 5 75
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

    public static class FakePlayer extends EntityOtherPlayerMP {
        @Setter
        @Getter
        private PlayerProfile skyblockProfile;
        private SkinFetchur.SkinSet skinSet;
        private PlayerProfile.Armor armor;
        private FakePlayer(World w) {
            super(w, null);
            throw new UnsupportedOperationException("what");
        }
        public FakePlayer(GameProfile playerProfile, SkinFetchur.SkinSet skinSet, PlayerProfile skyblockProfile) {
            super(Minecraft.getMinecraft().theWorld, playerProfile);
            this.skyblockProfile = skyblockProfile;
            this.skinSet = skinSet;
            armor=  skyblockProfile.getCurrentArmor();
            this.inventory.armorInventory = skyblockProfile.getCurrentArmor().getArmorSlots();
        }

        public String getSkinType() {
            return this.skinSet == null ? DefaultPlayerSkin.getSkinType(getGameProfile().getId()) : this.skinSet.getSkinType();
        }

        public ResourceLocation getLocationSkin() {
            return com.google.common.base.Objects.firstNonNull(skinSet.getSkinLoc(), DefaultPlayerSkin.getDefaultSkin(getGameProfile().getId()));
        }

        public ResourceLocation getLocationCape() {
            return skinSet.getCapeLoc();
        }

        @Override
        public ItemStack[] getInventory() {
            return this.inventory.armorInventory;
        }
    }
}
