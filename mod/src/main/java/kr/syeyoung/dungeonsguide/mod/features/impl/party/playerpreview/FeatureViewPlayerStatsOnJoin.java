/*
 *     Dungeons Guide - The most intelligent Hypixel Skyblock Dungeons Mod
 *     Copyright (C) 2021  cyoung06
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Affero General Public License as published
 *     by the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Affero General Public License for more details.
 *
 *     You should have received a copy of the GNU Affero General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package kr.syeyoung.dungeonsguide.mod.features.impl.party.playerpreview;

import com.mojang.authlib.GameProfile;
import io.github.moulberry.hychat.HyChat;
import io.github.moulberry.hychat.chat.ChatManager;
import io.github.moulberry.hychat.gui.GuiChatBox;
import kr.syeyoung.dungeonsguide.mod.DungeonsGuide;
import kr.syeyoung.dungeonsguide.mod.chat.ChatProcessResult;
import kr.syeyoung.dungeonsguide.mod.chat.ChatProcessor;
import kr.syeyoung.dungeonsguide.mod.chat.ChatTransmitter;
import kr.syeyoung.dungeonsguide.mod.config.guiconfig.ConfigPanelCreator;
import kr.syeyoung.dungeonsguide.mod.config.guiconfig.MFeatureEdit;
import kr.syeyoung.dungeonsguide.mod.config.guiconfig.MParameterEdit;
import kr.syeyoung.dungeonsguide.mod.config.guiconfig.RootConfigPanel;
import kr.syeyoung.dungeonsguide.mod.cosmetics.ActiveCosmetic;
import kr.syeyoung.dungeonsguide.mod.cosmetics.CosmeticData;
import kr.syeyoung.dungeonsguide.mod.features.FeatureParameter;
import kr.syeyoung.dungeonsguide.mod.features.FeatureRegistry;
import kr.syeyoung.dungeonsguide.mod.features.SimpleFeature;
import kr.syeyoung.dungeonsguide.mod.features.impl.party.playerpreview.api.ApiFetcher;
import kr.syeyoung.dungeonsguide.mod.features.impl.party.playerpreview.api.PlayerSkyblockData;
import kr.syeyoung.dungeonsguide.mod.features.impl.party.playerpreview.api.SkinFetcher;
import kr.syeyoung.dungeonsguide.mod.features.impl.party.playerpreview.api.playerprofile.PlayerProfile;
import kr.syeyoung.dungeonsguide.mod.features.impl.party.playerpreview.datarenders.DataRendererEditor;
import kr.syeyoung.dungeonsguide.mod.features.impl.party.playerpreview.datarenders.DataRendererRegistry;
import kr.syeyoung.dungeonsguide.mod.features.impl.party.playerpreview.datarenders.IDataRenderer;
import kr.syeyoung.dungeonsguide.mod.features.listener.GuiClickListener;
import kr.syeyoung.dungeonsguide.mod.features.listener.GuiPostRenderListener;
import kr.syeyoung.dungeonsguide.mod.party.PartyContext;
import kr.syeyoung.dungeonsguide.mod.party.PartyManager;
import kr.syeyoung.dungeonsguide.mod.utils.TextUtils;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.event.HoverEvent;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.fml.client.config.GuiUtils;
import net.minecraftforge.fml.common.Loader;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;

import java.awt.*;
import java.util.List;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class FeatureViewPlayerStatsOnJoin extends SimpleFeature implements GuiPostRenderListener, GuiClickListener {

    static Minecraft mc = Minecraft.getMinecraft();
    protected Rectangle popupRect;
    ChangeProfileWidget profileButtonWidget = new ChangeProfileWidget();
    private String lastuid; // actually current uid
    private CompletableFuture<Optional<PlayerSkyblockData>> profileFuture;
    private Future<Optional<GameProfile>> gameProfileFuture;
    private Future<SkinFetcher.SkinSet> skinFuture;
    private FakePlayer fakePlayer;
    private boolean shouldDraw = false;
    public FeatureViewPlayerStatsOnJoin() {
        super("Party", "View player stats when join", "view player rendering when joining/someone joins the party", "partykicker.viewstats", true);


        addParameter("datarenderers", new FeatureParameter<List<String>>("datarenderers", "DataRenderers", "Datarenderssdasd", new ArrayList<>(Arrays.asList(
                "catalv", "selected_class_lv", "dungeon_catacombs_higheststat", "dungeon_master_catacombs_higheststat", "skill_combat_lv", "skill_foraging_lv", "skill_mining_lv", "fairysouls", "dummy"
        )), "stringlist"));


        ChatProcessor.INSTANCE.subscribe(((txt, messageContext) -> {

            if (isEnabled() && txt.contains("§r§ejoined the dungeon group! (§r§b")) {
                String username = TextUtils.stripColor(txt).split(" ")[3];
                if (username.equalsIgnoreCase(mc.getSession().getUsername())) {
                    PartyManager.INSTANCE.requestPartyList(context -> {
                        if (context == null) {
                            ChatTransmitter.addToQueue(new ChatComponentText("§eDungeons Guide §7:: §cBugged Dungeon Party "));
                        } else {
                            processPartyMembers(context);
                        }
                    });
                } else {
                    processMemberJoin(username);
                }


            }


            return ChatProcessResult.NONE;
        }));

    }

    public static void clip(ScaledResolution resolution, int x, int y, int width, int height) {
        if (width < 0 || height < 0) return;

        int scale = resolution.getScaleFactor();
        GL11.glScissor((x) * scale, mc.displayHeight - (y + height) * scale, (width) * scale, height * scale);
    }

    public static void processPartyMembers(PartyContext context) {
        for (String member : context.getPartyRawMembers()) {
            processMemberJoin(member);
        }
    }

    private static void processMemberJoin(@NotNull String username) {
        ApiFetcher.fetchUUIDAsync(username)
                .thenAccept(a -> {
                    if (a == null) {
                        ChatTransmitter.addToQueue(new ChatComponentText("§eDungeons Guide §7:: §e" + username + "§f's Profile §cCouldn't fetch uuid"));
                        return;
                    }


                    ApiFetcher.fetchMostRecentProfileAsync(a.get(), FeatureRegistry.PARTYKICKER_APIKEY.getAPIKey());

                    IChatComponent comp = new ChatComponentText("§eDungeons Guide §7:: §e" + username + "§f's Profile ")
                            .appendSibling(new ChatComponentText("§7view").setChatStyle(new ChatStyle().setChatHoverEvent(new HoverEventRenderPlayer(a.orElse(null)))));

                    ChatTransmitter.addToQueue((ChatComponentText) comp);


                });
    }

    @Override
    public void onGuiPostRender(GuiScreenEvent.DrawScreenEvent.Post rendered) {
        if (!(mc.currentScreen instanceof GuiChat)) {
            cancelRender();
            return;
        }

        ScaledResolution scaledResolution = new ScaledResolution(mc);

        IChatComponent ichatcomponent = getHoveredComponent(scaledResolution);
        String uid = null;
        if (ichatcomponent != null && ichatcomponent.getChatStyle().getChatHoverEvent() instanceof HoverEventRenderPlayer) {
            uid = ((HoverEventRenderPlayer) ichatcomponent.getChatStyle().getChatHoverEvent()).getUuid();
        }

        int width = scaledResolution.getScaledWidth();
        int height = scaledResolution.getScaledHeight();
        int mouseX = Mouse.getX() * width / mc.displayWidth;
        int mouseY = height - Mouse.getY() * height / mc.displayHeight - 1;

        shouldCancelRendering(uid, mouseX, mouseY);

        if (lastuid == null) return;


        if (popupRect == null) {
            popupRect = new Rectangle(mouseX, mouseY, 220, 220);
            if (popupRect.y + popupRect.height > scaledResolution.getScaledHeight()) {
                popupRect.y -= popupRect.y + popupRect.height - scaledResolution.getScaledHeight();
            }
        }

        if (profileFuture == null) {
            profileFuture = ApiFetcher.fetchMostRecentProfileAsync(lastuid, FeatureRegistry.PARTYKICKER_APIKEY.getAPIKey());
        }

        if (gameProfileFuture == null) {
            gameProfileFuture = ApiFetcher.getSkinGameProfileByUUIDAsync(lastuid);
        }
        boolean plsSetAPIKEY = false;
        if (skinFuture == null && gameProfileFuture.isDone()) {
            try {
                skinFuture = SkinFetcher.getSkinSet(gameProfileFuture.get().orElse(null));
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }

        try {
            if (fakePlayer == null && skinFuture != null && profileFuture != null && skinFuture.isDone() && profileFuture.isDone() && profileFuture.get().isPresent()) {
                if (profileButtonWidget.getCurrentrySelectedProfile(profileFuture.get().get()) != null) {
                    if (skinFuture.get() != null) {
                        profileButtonWidget.setCurrentyselectedprofile(profileFuture.get().get().getLastestprofileArrayIndex());
                        fakePlayer = new FakePlayer(gameProfileFuture.get().orElse(null), skinFuture.get(), profileButtonWidget.getCurrentrySelectedProfile(profileFuture.get().get()), profileButtonWidget.getCurrentyselectedprofile());
                    }
                }
            } else if (fakePlayer != null) {
                if (fakePlayer.getProfileNumber() != profileButtonWidget.getCurrentyselectedprofile()) {
                    fakePlayer = new FakePlayer(gameProfileFuture.get().orElse(null), skinFuture.get(), profileButtonWidget.getCurrentrySelectedProfile(profileFuture.get().get()), profileButtonWidget.getCurrentyselectedprofile());
                }
            }
        } catch (InterruptedException | ExecutionException e) {
            plsSetAPIKEY = true;
        }


        Optional<PlayerProfile> playerProfile;
        if (plsSetAPIKEY || !profileFuture.isDone()) {
            playerProfile = null;
        } else {
            PlayerSkyblockData data = null;
            try {
                data = profileFuture.get().get();
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException(e);
            }
            PlayerProfile currentlySelectedProfile = profileButtonWidget.getCurrentrySelectedProfile(data);
            playerProfile = Optional.ofNullable(currentlySelectedProfile);
        }


        draw(scaledResolution, mouseX, mouseY, plsSetAPIKEY, playerProfile);
    }

    private void draw(ScaledResolution scaledResolution, int mouseX, int mouseY, boolean plsSetAPIKEY, Optional<PlayerProfile> playerProfile) {
        GlStateManager.pushMatrix();
        GlStateManager.translate(popupRect.x, popupRect.y, 0);
        int backroundGuiColor = 0xFF23272a;
        Gui.drawRect(0, 0, popupRect.width, popupRect.height, backroundGuiColor);
        Gui.drawRect(2, 2, popupRect.width - 2, popupRect.height - 2, 0XFF2c2f33);

        GlStateManager.enableBlend();
        GL14.glBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ONE_MINUS_SRC_ALPHA);
        if (plsSetAPIKEY) {
            mc.fontRendererObj.drawString("Please set API KEY on /dg -> Party Kicker", 5, 5, 0xFFFFFFFF);
            GlStateManager.popMatrix();
        } else if (playerProfile == null) {
            mc.fontRendererObj.drawString("Fetching data...", 5, 5, 0xFFFFFFFF);
            GlStateManager.popMatrix();
        } else if (!playerProfile.isPresent()) {
            mc.fontRendererObj.drawString("User could not be found", 5, 5, 0xFFFFFFFF);
            GlStateManager.popMatrix();
        } else {
            int relX = mouseX - popupRect.x;
            int relY = mouseY - popupRect.y;
            FontRenderer fr = mc.fontRendererObj;
            GL11.glEnable(GL11.GL_SCISSOR_TEST);
            clip(scaledResolution, popupRect.x, popupRect.y, popupRect.width, popupRect.height);

            Gui.drawRect(0, 168, 90, 195, backroundGuiColor);
            Gui.drawRect(2, 170, 88, 193, new Rectangle(2, 170, 86, 23).contains(relX, relY) ? 0xFFff7777 : 0xFFFF3333);

            Gui.drawRect(0, 193, 90, 220, backroundGuiColor);
            Gui.drawRect(2, 195, 88, 218, new Rectangle(2, 195, 86, 23).contains(relX, relY) ? 0xFF859DF0 : 0xFF7289da);


            GlStateManager.enableBlend();
            GL14.glBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ONE_MINUS_SRC_ALPHA);
            GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ONE_MINUS_SRC_ALPHA);
            fr.drawString("Kick", (90 - fr.getStringWidth("Kick")) / 2, (364 - fr.FONT_HEIGHT) / 2, 0xFFFFFFFF);
            fr.drawString("Invite", (90 - fr.getStringWidth("Invite")) / 2, (414 - fr.FONT_HEIGHT) / 2, 0xFFFFFFFF);

            GlStateManager.pushMatrix();
            GlStateManager.translate(95, 5, 0);
            int culmutativeY = 5;
            IDataRenderer dataRendererToHover = null;
            for (String datarenderers : this.<List<String>>getParameter("datarenderers").getValue()) {
                GlStateManager.enableBlend();
                GL14.glBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ONE_MINUS_SRC_ALPHA);
                GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ONE_MINUS_SRC_ALPHA);
                IDataRenderer dataRenderer = DataRendererRegistry.getDataRenderer(datarenderers);
                Dimension dim;
                if (dataRenderer == null) {
                    fr.drawString("Couldn't find Datarenderer", 0, 0, 0xFFFF0000);
                    fr.drawString(datarenderers, 0, fr.FONT_HEIGHT, 0xFFFF0000);
                    dim = new Dimension(0, fr.FONT_HEIGHT * 2);
                } else {
                    GlStateManager.pushMatrix();
                    dim = dataRenderer.renderData(playerProfile.get());
                    GlStateManager.popMatrix();
                }
                if (relX >= 95 && relX <= popupRect.width && relY >= culmutativeY && relY < culmutativeY + dim.height && dataRenderer != null) {
                    dataRendererToHover = dataRenderer;
                }
                culmutativeY += dim.height;
                GlStateManager.translate(0, dim.height, 0);
            }
            GlStateManager.popMatrix();
            GlStateManager.enableBlend();
            GL14.glBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ONE_MINUS_SRC_ALPHA);
            Gui.drawRect(0, 0, 90, 170, backroundGuiColor);
            Gui.drawRect(2, 2, 88, 168, 0xFF444444);

            GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ONE_MINUS_SRC_ALPHA);

            profileButtonWidget.drawChangeProfileButton(relX, relY);


            Gui.drawRect(78, 156, 90, 170, backroundGuiColor);
            fr.drawString("§eI", 82, 159, -1);


            GlStateManager.color(1, 1, 1, 1.0F);
            if (fakePlayer != null) {
                drawFakePlayer(scaledResolution, mouseX, mouseY, playerProfile, relX, relY, fr);
            } else {
                GlStateManager.enableBlend();
                GL14.glBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ONE_MINUS_SRC_ALPHA);
                GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ONE_MINUS_SRC_ALPHA);
                fr.drawString("Loading", 5, 35, 0xFFEFFF00);
            }
            GlStateManager.popMatrix();
            GL11.glDisable(GL11.GL_SCISSOR_TEST);
            if (dataRendererToHover != null && !shouldDraw) {
                dataRendererToHover.onHover(playerProfile.get(), mouseX, mouseY);
            }
            GL11.glEnable(GL11.GL_SCISSOR_TEST);
            GlStateManager.pushMatrix();
            GlStateManager.translate(popupRect.x, popupRect.y, 0);
            if (shouldDraw) {
                int startX = 81;
                int startY = 86;
                clip(scaledResolution, popupRect.x + startX - 1, popupRect.y + startY - 1, 164, 74);
                GlStateManager.translate(startX, startY, 1);
                Gui.drawRect(-1, -1, 163, 73, 0xFF000000);
                GlStateManager.disableLighting();
                ItemStack toHover = null;
                int rx = relX - startX;
                int ry = relY - startY;

                if (playerProfile.get().getInventory() != null) {
                    GlStateManager.disableRescaleNormal();
                    RenderHelper.enableGUIStandardItemLighting();
                    GlStateManager.disableLighting();
                    for (int i = 0; i < playerProfile.get().getInventory().length; i++) {
                        int x = (i % 9) * 18;
                        int y = (i / 9) * 18;
                        if (x <= rx && rx < x + 18 && y <= ry && ry < y + 18) {
                            toHover = playerProfile.get().getInventory()[(i + 9) % 36];
                        }
                        Gui.drawRect(x, y, x + 18, y + 18, 0xFF000000);
                        Gui.drawRect(x + 1, y + 1, x + 17, y + 17, 0xFF666666);
                        GlStateManager.color(1, 1, 1, 1.0F);

                        mc.getRenderItem().renderItemAndEffectIntoGUI(playerProfile.get().getInventory()[(i + 9) % 36], (i % 9) * 18 + 1, (i / 9) * 18 + 1);
                    }

                    if (toHover != null) {
                        drawItemStackToolTip(scaledResolution, mouseX, mouseY, fr, toHover);
                    }
                } else {
                    Gui.drawRect(0, 0, 162, 72, 0xFF666666);
                    fr.drawSplitString("Player has disabled Inventory API", 5, 5, 142, -1);
                }

            }
            GL11.glDisable(GL11.GL_SCISSOR_TEST);
            GlStateManager.popMatrix(); // 33 66 108 130 154 // 5 75
        }


    }

    private void drawFakePlayer(ScaledResolution scaledResolution, int mouseX, int mouseY, Optional<PlayerProfile> playerProfile, int relX, int relY, FontRenderer fr) {
        clip(scaledResolution, popupRect.x + 2, popupRect.y + 2, 86, 166);
        GuiInventory.drawEntityOnScreen(45, 150, 60, -(mouseX - popupRect.x - 75), 0, fakePlayer);

        String toDraw = fakePlayer.getName();
        List<ActiveCosmetic> activeCosmetics = DungeonsGuide.getDungeonsGuide().getCosmeticsManager().getActiveCosmeticByPlayer().get(UUID.fromString(TextUtils.insertDashUUID(playerProfile.get().getMemberUID())));
        CosmeticData prefix = null;
        CosmeticData color = null;
        if (activeCosmetics != null) {
            for (ActiveCosmetic activeCosmetic : activeCosmetics) {
                CosmeticData cosmeticData = DungeonsGuide.getDungeonsGuide().getCosmeticsManager().getCosmeticDataMap().get(activeCosmetic.getCosmeticData());
                if (cosmeticData != null) {
                    if (cosmeticData.getCosmeticType().equals("prefix")) prefix = cosmeticData;
                    if (cosmeticData.getCosmeticType().equals("color")) color = cosmeticData;
                }
            }
        }
        toDraw = (color == null ? "§e" : color.getData().replace("&", "§")) + toDraw;
        if (prefix != null) toDraw = prefix.getData().replace("&", "§") + " " + toDraw;

        GlStateManager.enableBlend();
        GL14.glBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ONE_MINUS_SRC_ALPHA);


        String profileName = "on §6" + playerProfile.get().getProfileName();
        fr.drawString(profileName, (90 - fr.getStringWidth(profileName)) / 2, 15, -1);


        fr.drawString(toDraw, (90 - fr.getStringWidth(toDraw)) / 2, 10 - (fr.FONT_HEIGHT / 2), -1);

        ItemStack toHover = null;
        if (relX > 20 && relX < 70) {
            if (33 <= relY && relY <= 66) {
                toHover = fakePlayer.getInventory()[3];
            } else if (66 <= relY && relY <= 108) {
                toHover = fakePlayer.getInventory()[2];
            } else if (108 <= relY && relY <= 130) {
                toHover = fakePlayer.getInventory()[1];
            } else if (130 <= relY && relY <= 154) {
                toHover = fakePlayer.getInventory()[0];
            }
        } else if (relX > 0 && relX <= 20) {
            if (80 <= relY && relY <= 120) {
                toHover = fakePlayer.inventory.mainInventory[fakePlayer.inventory.currentItem];
            }
        }

        if (toHover != null) {
            drawItemStackToolTip(scaledResolution, mouseX, mouseY, fr, toHover);
            GlStateManager.translate(popupRect.x, popupRect.y, 0);
        }
        clip(scaledResolution, popupRect.x, popupRect.y, popupRect.width, popupRect.height);
    }

    public void drawItemStackToolTip(ScaledResolution scaledResolution, int mouseX, int mouseY, FontRenderer fr, ItemStack toHover) {
        List<String> list = toHover.getTooltip(mc.thePlayer, mc.gameSettings.advancedItemTooltips);
        for (int i = 0; i < list.size(); ++i) {
            if (i == 0) {
                list.set(i, toHover.getRarity().rarityColor + list.get(i));
            } else {
                list.set(i, EnumChatFormatting.GRAY + list.get(i));
            }
        }
        FontRenderer font = toHover.getItem().getFontRenderer(toHover);
        GlStateManager.popMatrix();
        GL11.glDisable(GL11.GL_SCISSOR_TEST);
        FontRenderer theRenderer = (font == null ? fr : font);
        GuiUtils.drawHoveringText(list, mouseX, mouseY, scaledResolution.getScaledWidth(), scaledResolution.getScaledHeight(), -1, theRenderer);
        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        GlStateManager.pushMatrix();
    }

    private void shouldCancelRendering(String uid, int mouseX, int mouseY) {
        if (!((popupRect != null && (popupRect.contains(mouseX, mouseY) || shouldDraw)) || uid != null && uid.equals(lastuid))) {
            cancelRender();
        }

        if (uid != null && !uid.equals(lastuid) && (popupRect == null || (!popupRect.contains(mouseX, mouseY) && !shouldDraw))) {
            cancelRender();
            lastuid = uid;
        }
    }

    public void cancelRender() {
        popupRect = null;
        profileFuture = null;
        lastuid = null;
        gameProfileFuture = null;
        skinFuture = null;
        fakePlayer = null;
        shouldDraw = false;
    }

    @Override
    public void onMouseInput(GuiScreenEvent.MouseInputEvent.Pre mouseInputEvent) {
        ScaledResolution scaledResolution = new ScaledResolution(mc);
        int width = scaledResolution.getScaledWidth();
        int height = scaledResolution.getScaledHeight();
        int mouseX = Mouse.getX() * width / mc.displayWidth;
        int mouseY = height - Mouse.getY() * height / mc.displayHeight - 1;

        if (Mouse.getEventButton() != -1 && Mouse.isButtonDown(Mouse.getEventButton()) && shouldDraw)
            shouldDraw = false;
        if (popupRect == null || !popupRect.contains(mouseX, mouseY)) return;

        mouseInputEvent.setCanceled(true);

        int relX = mouseX - popupRect.x;
        int relY = mouseY - popupRect.y;

        try {
            PlayerSkyblockData playerData;

            if (profileFuture.isDone()) {
                playerData = profileFuture.get().orElse(null);
            } else {
                return;
            }

            if (playerData == null) {
                return;
            }

            if (Mouse.getEventButton() == -1 && !Mouse.isButtonDown(Mouse.getEventButton())) return;

            if (new Rectangle(2, 195, 86, 23).contains(relX, relY)) {
                // invite
                ChatProcessor.INSTANCE.addToChatQueue("/p invite " + ApiFetcher.fetchNicknameAsync(profileButtonWidget.getCurrentrySelectedProfile(playerData).getMemberUID()).get().orElse("-"), () -> {
                }, true);
            } else if (new Rectangle(2, 170, 86, 23).contains(relX, relY)) {
                // kick
                ChatProcessor.INSTANCE.addToChatQueue("/p kick " + ApiFetcher.fetchNicknameAsync(profileButtonWidget.getCurrentrySelectedProfile(playerData).getMemberUID()).get().orElse("-"), () -> {
                }, true);
            } else if (new Rectangle(80, 159, 10, 11).contains(relX, relY)) {
                shouldDraw = true;
            }


            this.profileButtonWidget.handleClickProfileButton(playerData);


        } catch (InterruptedException | ExecutionException e) {
        }


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
            } catch (Throwable t) {
            }
        }
        if (ichatcomponent == null) {
            ichatcomponent = Minecraft.getMinecraft().ingameGUI.getChatGUI().getChatComponent(Mouse.getX(), Mouse.getY());
        }
        return ichatcomponent;
    }

    @Override
    public String getEditRoute(RootConfigPanel rootConfigPanel) {
        ConfigPanelCreator.map.put("base." + getKey(), () -> {

            MFeatureEdit featureEdit = new MFeatureEdit(FeatureViewPlayerStatsOnJoin.this, rootConfigPanel);
            featureEdit.addParameterEdit("datarenderers", new DataRendererEditor(FeatureViewPlayerStatsOnJoin.this));
            for (FeatureParameter parameter : getParameters()) {
                if (parameter.getKey().equals("datarenderers")) continue;
                featureEdit.addParameterEdit(parameter.getKey(), new MParameterEdit(FeatureViewPlayerStatsOnJoin.this, parameter, rootConfigPanel));
            }
            return featureEdit;
        });
        return "base." + getKey();
    }

    public static class HoverEventRenderPlayer extends HoverEvent {
        @Getter
        private final String uuid;
        private IChatComponent cached;

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

        @Override
        public IChatComponent getValue() {
            if (cached == null) {
                cached = new ChatComponentText("")
                        .setChatStyle(
                                new ChatStyle()
                                        .setChatHoverEvent(
                                                new HoverEvent(
                                                        Action.SHOW_TEXT,
                                                        new ChatComponentText(uuid)
                                                )
                                        )
                        );
                return cached;
            }
            return cached;
        }
    }

    class ChangeProfileWidget {
        FontRenderer fr;
        @Getter
        @Setter
        int currentyselectedprofile = 0;
        String buttonText = "Switch Profile";
        int stringWidth;
        int textx;
        int texty;
        int blockWidth;
        int blockHeight;
        long clickDeBounce = 0;

        public ChangeProfileWidget() {
            fr = Minecraft.getMinecraft().fontRendererObj;
            stringWidth = fr.getStringWidth(buttonText);
            textx = ((83 - stringWidth) / 2);
            texty = (324 - fr.FONT_HEIGHT) / 2;
            blockWidth = stringWidth + 3;
            blockHeight = fr.FONT_HEIGHT + 2;
        }

        void drawChangeProfileButton(float relX, float relY) {

            boolean contains = isWithinButtonRec(relX, relY);

            Gui.drawRect(textx - 5, texty - 1, textx + blockWidth, texty + blockHeight, contains ? 0xFFFFFFFF : 0xFF30afd3);

            fr.drawString(buttonText, textx, texty + 2, contains ? 0x30afd3 : 0xFFFFFF);
        }

        Rectangle getButtonRec() {
            return new Rectangle(textx - 5, texty - 1, blockWidth, blockHeight);
        }

        boolean isWithinButtonRec(float relX, float relY) {
            return getButtonRec().contains(relX, relY);
        }

        void handleClickProfileButton(PlayerSkyblockData playerData) {

            if (System.currentTimeMillis() <= clickDeBounce) {
                return;
            } else {
                clickDeBounce = System.currentTimeMillis() + 200;
            }

            ScaledResolution scaledResolution = new ScaledResolution(mc);
            int width = scaledResolution.getScaledWidth();
            int height = scaledResolution.getScaledHeight();
            int mouseX = Mouse.getX() * width / mc.displayWidth;
            int mouseY = height - Mouse.getY() * height / mc.displayHeight - 1;

            int relX = mouseX - popupRect.x;
            int relY = mouseY - popupRect.y;

            if (isWithinButtonRec(relX, relY)) {
                if (profileButtonWidget.currentyselectedprofile + 1 >= playerData.getPlayerProfiles().length) {
                    profileButtonWidget.currentyselectedprofile = 0;
                } else {
                    profileButtonWidget.currentyselectedprofile++;
                }
            }
        }

        PlayerProfile getCurrentrySelectedProfile(PlayerSkyblockData data) {
            if (data == null) return null;
            if (data.getPlayerProfiles() == null) return null;
            if (data.getPlayerProfiles().length == 0) return null;
            if (data.getPlayerProfiles().length < currentyselectedprofile) return null;
            return data.getPlayerProfiles()[currentyselectedprofile];
        }
    }
}
