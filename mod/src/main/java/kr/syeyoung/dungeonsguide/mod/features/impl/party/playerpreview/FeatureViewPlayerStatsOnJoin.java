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

import kr.syeyoung.dungeonsguide.mod.events.impl.ScreenOpenEvent;
import kr.syeyoung.dungeonsguide.mod.events.impl.ScreenMouseEvent;
import com.mojang.authlib.GameProfile;
import kr.syeyoung.dungeonsguide.mod.chat.ChatProcessResult;
import kr.syeyoung.dungeonsguide.mod.chat.ChatProcessor;
import kr.syeyoung.dungeonsguide.mod.chat.ChatTransmitter;
import kr.syeyoung.dungeonsguide.mod.config.types.TCStringList;
import kr.syeyoung.dungeonsguide.mod.events.annotations.DGEventHandler;
import kr.syeyoung.dungeonsguide.mod.features.FeatureParameter;
import kr.syeyoung.dungeonsguide.mod.features.SimpleFeature;
import kr.syeyoung.dungeonsguide.mod.features.impl.party.playerpreview.api.ApiFetcher;
// import kr.syeyoung.dungeonsguide.mod.features.impl.party.playerpreview.datarenders.DataRendererEditor;
import kr.syeyoung.dungeonsguide.mod.features.impl.party.playerpreview.widget.WidgetProfileViewer;
// import kr.syeyoung.dungeonsguide.mod.gui.elements.CompatLayer;
import kr.syeyoung.dungeonsguide.mod.overlay.AbsPosPositioner;
import kr.syeyoung.dungeonsguide.mod.overlay.OverlayManager;
import kr.syeyoung.dungeonsguide.mod.overlay.OverlayType;
import kr.syeyoung.dungeonsguide.mod.overlay.OverlayWidget;
import kr.syeyoung.dungeonsguide.mod.party.PartyContext;
import kr.syeyoung.dungeonsguide.mod.party.PartyManager;
import kr.syeyoung.dungeonsguide.mod.utils.TextUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.input.Mouse;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

// TODO: do not limit element positioning
// maybe a cool editor?
public class FeatureViewPlayerStatsOnJoin extends SimpleFeature {

    static Minecraft mc = Minecraft.getInstance();

    public static UUID fromString(String input) {
        return UUID.fromString(input.replaceFirst("(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w{12})", "$1-$2-$3-$4-$5"));
    }

    public FeatureViewPlayerStatsOnJoin() {
        super("Dungeon Party", "View player stats when join", "view player rendering when joining/someone joins the party", "partykicker.viewstats", true);
        addParameter("datarenderers", new FeatureParameter<List<String>>("datarenderers", "DataRenderers", "Datarenderssdasd", new ArrayList<>(Arrays.asList(
                "catalv", "selected_class_lv", "dungeon_catacombs_higheststat", "dungeon_master_catacombs_higheststat", "skill_combat_lv", "skill_foraging_lv", "skill_mining_lv", "fairysouls", "dummy"
        )), TCStringList.INSTANCE));
                // TODO: the data renderer editor is built on the room editor's MPanel, not in the first 26.1 build
//                .setWidgetGenerator(param -> new CompatLayer(new DataRendererEditor(FeatureViewPlayerStatsOnJoin.this))));


        ChatProcessor.INSTANCE.subscribe(((txt, messageContext) -> {
            if (isEnabled() && txt.contains("§r§ejoined the dungeon group! (§r§b")) {
                String username = TextUtils.stripColor(txt).split(" ")[3];
                if (username.equalsIgnoreCase(mc.getSession().getUsername())) {
                    PartyManager.INSTANCE.requestPartyList(context -> {
                        if (context == null) {
                            ChatTransmitter.addToQueue(Component.literal("§eDungeons Guide §7:: §cBugged Dungeon Party "));
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
                        ChatTransmitter.addToQueue(Component.literal("§eDungeons Guide §7:: §e" + username + "§f's Profile §cCouldn't fetch uuid"));
                        return;
                    }


                    ApiFetcher.fetchMostRecentProfileAsync(a.get());

                    Component comp = Component.literal("§eDungeons Guide §7:: §e" + username + "§f's Profile ")
                            .append(Component.literal("§7view").setStyle(Style.EMPTY.withHoverEvent(new HoverEventRenderPlayer(
                                    new GameProfile(fromString(a.get()), username)))));

                    ChatTransmitter.addToQueue((MutableComponent) comp);
                });
    }

    private OverlayWidget widget;

    @DGEventHandler(triggerOutOfSkyblock = true)
    public void onGuiPostRender(ScreenMouseEvent mouseInputEvent) {
        if (!(mc.screen instanceof GuiChat)) {
            return;
        }
        ScaledResolution scaledResolution = new ScaledResolution(mc);

        Component ichatcomponent = getHoveredComponent(scaledResolution);
        GameProfile gameProfile = null;
        if (ichatcomponent !=  null && ichatcomponent.getStyle().getHoverEvent() instanceof HoverEventRenderPlayer) {
            gameProfile = ((HoverEventRenderPlayer) ichatcomponent.getStyle().getHoverEvent()).getGameProfile();
        }
        if (gameProfile != null && widget == null) {
            int mouseX = Mouse.getX();
            int mouseY = (Minecraft.getInstance().getWindow().getHeight() - Mouse.getY());

            double width = 220 * scaledResolution.getScaleFactor();
            double height = 220 * scaledResolution.getScaleFactor();
            widget = new OverlayWidget(
                    new WidgetProfileViewer(gameProfile, () -> {
                        if (widget != null) {
                            OverlayManager.getInstance().removeOverlay(widget);
                            widget = null;
                        }
                    }),
                    OverlayType.OVER_CHAT,
                    new AbsPosPositioner(mouseX, mouseY-height),
                    getClass().getSimpleName()
            );
            OverlayManager.getInstance().addOverlay(widget);
        }
    }
    @DGEventHandler(triggerOutOfSkyblock = true)
    public void onGuiClose(ScreenOpenEvent event) {
        if (!(event.gui instanceof GuiChat) && widget != null) {
            OverlayManager.getInstance().removeOverlay(widget);
            widget = null;
        }
    }


    public Component getHoveredComponent(ScaledResolution scaledResolution) {
        Component ichatcomponent = null;
//        if (Loader.isModLoaded("hychat")) {
//            try {
//                ChatManager chatManager = HyChat.getInstance().getChatManager();
//                GuiChatBox guiChatBox = chatManager.getFocusedChat();
//
//                int x = guiChatBox.getX(scaledResolution);
//                int y = guiChatBox.getY(scaledResolution);
//                ichatcomponent = guiChatBox.chatArray.getHoveredComponent(guiChatBox.getSelectedTab().getChatLines(), Mouse.getX(), Mouse.getY(), x, y);
//            } catch (Exception t) {
//            }
//        }
//        if (ichatcomponent == null) {
            ichatcomponent = Minecraft.getInstance().gui.getChatGUI().getChatComponent(Mouse.getX(), Mouse.getY());
//        }
        return ichatcomponent;
    }

}
