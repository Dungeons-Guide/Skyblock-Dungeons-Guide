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
import kr.syeyoung.dungeonsguide.mod.chat.ChatProcessResult;
import kr.syeyoung.dungeonsguide.mod.chat.ChatProcessor;
import kr.syeyoung.dungeonsguide.mod.chat.ChatTransmitter;
import kr.syeyoung.dungeonsguide.mod.config.types.TCStringList;
import kr.syeyoung.dungeonsguide.mod.events.annotations.DGEventHandler;
import kr.syeyoung.dungeonsguide.mod.features.FeatureParameter;
import kr.syeyoung.dungeonsguide.mod.features.SimpleFeature;
import kr.syeyoung.dungeonsguide.mod.features.impl.etc.FeatureCollectDiagnostics;
import kr.syeyoung.dungeonsguide.mod.features.impl.party.playerpreview.api.ApiFetcher;
import kr.syeyoung.dungeonsguide.mod.features.impl.party.playerpreview.datarenders.DataRendererEditor;
import kr.syeyoung.dungeonsguide.mod.features.impl.party.playerpreview.widget.WidgetProfileViewer;
import kr.syeyoung.dungeonsguide.mod.gui.elements.CompatLayer;
import kr.syeyoung.dungeonsguide.mod.overlay.AbsPosPositioner;
import kr.syeyoung.dungeonsguide.mod.overlay.OverlayManager;
import kr.syeyoung.dungeonsguide.mod.overlay.OverlayType;
import kr.syeyoung.dungeonsguide.mod.overlay.OverlayWidget;
import kr.syeyoung.dungeonsguide.mod.party.PartyContext;
import kr.syeyoung.dungeonsguide.mod.party.PartyManager;
import kr.syeyoung.dungeonsguide.mod.utils.TextUtils;
import kr.syeyoung.modapi.ModAPI;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.kyori.adventure.nbt.TagStringIO;
import net.kyori.adventure.nbt.api.BinaryTagHolder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.DataComponentValue;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.client.event.GuiScreenEvent;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.input.Mouse;

import java.io.IOException;
import java.util.*;

// TODO: do not limit element positioning
// maybe a cool editor?
public class FeatureViewPlayerStatsOnJoin extends SimpleFeature {

    static Minecraft mc = Minecraft.getMinecraft();

    public static UUID fromString(String input) {
        return UUID.fromString(input.replaceFirst("(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w{12})", "$1-$2-$3-$4-$5"));
    }

    public FeatureViewPlayerStatsOnJoin() {
        super("Dungeon Party", "View player stats when join", "view player rendering when joining/someone joins the party", "partykicker.viewstats", true);
        addParameter("datarenderers", new FeatureParameter<List<String>>("datarenderers", "DataRenderers", "Datarenderssdasd", new ArrayList<>(Arrays.asList(
                "catalv", "selected_class_lv", "dungeon_catacombs_higheststat", "dungeon_master_catacombs_higheststat", "skill_combat_lv", "skill_foraging_lv", "skill_mining_lv", "fairysouls", "dummy"
        )), TCStringList.INSTANCE)
                .setWidgetGenerator(param -> new CompatLayer(new DataRendererEditor(FeatureViewPlayerStatsOnJoin.this))));


        ChatProcessor.INSTANCE.subscribe(((txt, messageContext) -> {
            if (isEnabled() && txt.contains("§r§ejoined the dungeon group! (§r§b")) {
                String username = TextUtils.stripColor(txt).split(" ")[3];
                if (username.equalsIgnoreCase(mc.getSession().getUsername())) {
                    PartyManager.INSTANCE.requestPartyList(context -> {
                        if (context == null) {
                            ChatTransmitter.addToQueue("§eDungeons Guide §7:: §cBugged Dungeon Party ");
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
                        ChatTransmitter.addToQueue("§eDungeons Guide §7:: §e" + username + "§f's Profile §cCouldn't fetch uuid");
                        return;
                    }


                    ApiFetcher.fetchMostRecentProfileAsync(a.get());
                    try {

                        BinaryTagHolder holder = BinaryTagHolder.binaryTagHolder(TagStringIO.tagStringIO().asString(CompoundBinaryTag.builder()
                                .putString("uuid", a.orElse(null))
                                .putString("name", username).build()));
                        ChatTransmitter.addToQueue(
                                Component.text("Dungeons Guide").color(NamedTextColor.YELLOW)
                                        .append(Component.text(" :: ").color(NamedTextColor.GRAY))
                                        .append(Component.text(username).color(NamedTextColor.YELLOW))
                                        .append(Component.text("'s Profile ").color(NamedTextColor.WHITE))
                                        .append(Component.text("view").color(NamedTextColor.GRAY).hoverEvent(
                                                        ModAPI.getAPI().getPlatform().isOldChat() ?
                                                                HoverEvent.showItem(Key.key("dungeonsguide", "profileviewer"), 1, holder) :
                                                                HoverEvent.showItem(Key.key("dungeonsguide", "profileviewer"), 1, Collections.singletonMap(Key.key("dungeonsguide", "profileviewer"), holder))
                                                )
                                        ));
                    } catch (IOException e) {
                        FeatureCollectDiagnostics.queueSendLogAsync(e);
                    }
                });
    }

    private OverlayWidget widget;

    @DGEventHandler(triggerOutOfSkyblock = true)
    public void onGuiPostRender(GuiScreenEvent.MouseInputEvent.Pre mouseInputEvent) {
        if (!(mc.currentScreen instanceof GuiChat)) {
            return;
        }
        if (widget != null) return;

        Component ichatcomponent = ModAPI.getAPI().getHoveredComponent();
        GameProfile gameProfile = null;
        if (ichatcomponent ==  null || ichatcomponent.hoverEvent() == null)  return;
        HoverEvent event = ichatcomponent.hoverEvent();
        if (event == null || event.action() != HoverEvent.Action.SHOW_ITEM) return;
        HoverEvent.ShowItem showItem = (HoverEvent.ShowItem) event.value();
        if (!showItem.dataComponents().isEmpty()) {
            DataComponentValue value = showItem.dataComponents().get(Key.key("dungeonsguide", "profileviewer"));
            if (!(value instanceof BinaryTagHolder)) return;
            try {
                CompoundBinaryTag tag = TagStringIO.tagStringIO().asCompound(((BinaryTagHolder) value).string());

                gameProfile = new GameProfile(
                        UUID.fromString(tag.getString("uuid")),
                        tag.getString("username")
                );
            } catch (IOException e) {
                return;
            }
        } else {
            BinaryTagHolder tagHolder = showItem.nbt();
            if (tagHolder == null ) return;
            try {
                CompoundBinaryTag tag = TagStringIO.tagStringIO().asCompound(tagHolder.string());

                gameProfile = new GameProfile(
                        UUID.fromString(tag.getString("uuid")),
                        tag.getString("username")
                );
            } catch (IOException e) {
                return;
            }
        }

        if (widget == null) {
            ScaledResolution scaledResolution = new ScaledResolution(mc);

            int mouseX = Mouse.getX();
            int mouseY = (ModAPI.getAPI().getDisplayHeight() - Mouse.getY());

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
    public void onGuiClose(GuiOpenEvent event) {
        if (!(event.gui instanceof GuiChat) && widget != null) {
            OverlayManager.getInstance().removeOverlay(widget);
            widget = null;
        }
    }



}
