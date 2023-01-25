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
import kr.syeyoung.dungeonsguide.mod.chat.ChatProcessResult;
import kr.syeyoung.dungeonsguide.mod.chat.ChatProcessor;
import kr.syeyoung.dungeonsguide.mod.chat.ChatTransmitter;
import kr.syeyoung.dungeonsguide.mod.config.guiconfig.ConfigPanelCreator;
import kr.syeyoung.dungeonsguide.mod.config.guiconfig.MFeatureEdit;
import kr.syeyoung.dungeonsguide.mod.config.guiconfig.MParameterEdit;
import kr.syeyoung.dungeonsguide.mod.config.guiconfig.RootConfigPanel;
import kr.syeyoung.dungeonsguide.mod.events.annotations.DGEventHandler;
import kr.syeyoung.dungeonsguide.mod.features.FeatureParameter;
import kr.syeyoung.dungeonsguide.mod.features.FeatureRegistry;
import kr.syeyoung.dungeonsguide.mod.features.SimpleFeature;
import kr.syeyoung.dungeonsguide.mod.features.impl.party.playerpreview.api.ApiFetcher;
import kr.syeyoung.dungeonsguide.mod.features.impl.party.playerpreview.datarenders.DataRendererEditor;
import kr.syeyoung.dungeonsguide.mod.features.impl.party.playerpreview.widget.WidgetProfileViewer;
import kr.syeyoung.dungeonsguide.mod.guiv2.primitive.Rect;
import kr.syeyoung.dungeonsguide.mod.overlay.*;
import kr.syeyoung.dungeonsguide.mod.party.PartyContext;
import kr.syeyoung.dungeonsguide.mod.party.PartyManager;
import kr.syeyoung.dungeonsguide.mod.utils.TextUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.IChatComponent;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.fml.common.Loader;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.input.Mouse;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

// TODO: do not limit element positioning
// maybe a cool editor?
public class FeatureViewPlayerStatsOnJoin extends SimpleFeature {

    static Minecraft mc = Minecraft.getMinecraft();

    public static UUID fromString(String input) {
        return UUID.fromString(input.replaceFirst("(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w{12})", "$1-$2-$3-$4-$5"));
    }

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
                            .appendSibling(new ChatComponentText("§7view").setChatStyle(new ChatStyle().setChatHoverEvent(new HoverEventRenderPlayer(
                                    new GameProfile(fromString(a.get()), username)))));

                    ChatTransmitter.addToQueue((ChatComponentText) comp);
                });
    }

    private OverlayWidget widget;

    @DGEventHandler(triggerOutOfSkyblock = true)
    public void onGuiPostRender(GuiScreenEvent.MouseInputEvent.Pre mouseInputEvent) {
        if (!(mc.currentScreen instanceof GuiChat)) {
            return;
        }
        ScaledResolution scaledResolution = new ScaledResolution(mc);

        IChatComponent ichatcomponent = getHoveredComponent(scaledResolution);
        GameProfile gameProfile = null;
        if (ichatcomponent !=  null && ichatcomponent.getChatStyle().getChatHoverEvent() instanceof HoverEventRenderPlayer) {
            gameProfile = ((HoverEventRenderPlayer) ichatcomponent.getChatStyle().getChatHoverEvent()).getGameProfile();
        }
        if (gameProfile != null && widget == null) {
            int mouseX = Mouse.getX();
            int mouseY = (Minecraft.getMinecraft().displayHeight - Mouse.getY());

            double width = 220 * scaledResolution.getScaleFactor();
            double height = 220 * scaledResolution.getScaleFactor();
            widget = new OverlayWidget(
                    new WidgetProfileViewer(gameProfile, FeatureRegistry.PARTYKICKER_APIKEY.getAPIKey(),
                            scaledResolution.getScaleFactor(), () -> {
                        if (widget != null) {
                            OverlayManager.getInstance().removeOverlay(widget);
                            widget = null;
                        }
                    }),
                    OverlayType.OVER_CHAT,
                    new AbsPosPositioner(mouseX, mouseY-height)
            );
            OverlayManager.getInstance().addOverlay(widget);
        }
    }
    @DGEventHandler(triggerOutOfSkyblock = true)
    public void onGuiClose(GuiOpenEvent event) {
        if (!(event.gui instanceof GuiChat) && widget != null) {
            OverlayManager.getInstance().removeOverlay(widget);
            widget = null;
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

}
