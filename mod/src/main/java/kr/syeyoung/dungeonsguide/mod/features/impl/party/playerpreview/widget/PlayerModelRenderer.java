/*
 * Dungeons Guide - The most intelligent Hypixel Skyblock Dungeons Mod
 * Copyright (C) 2023  cyoung06 (syeyoung)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package kr.syeyoung.dungeonsguide.mod.features.impl.party.playerpreview.widget;

import java.util.stream.Collectors;
import kr.syeyoung.dungeonsguide.mod.utils.TextUtils;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Item;
import kr.syeyoung.dungeonsguide.mod.DungeonsGuide;
import kr.syeyoung.dungeonsguide.mod.cosmetics.ActiveCosmetic;
import kr.syeyoung.dungeonsguide.mod.cosmetics.CosmeticData;
import kr.syeyoung.dungeonsguide.mod.features.FeatureRegistry;
import kr.syeyoung.dungeonsguide.mod.features.impl.party.playerpreview.FakePlayer;
import kr.syeyoung.dungeonsguide.mod.gui.DomElement;
import kr.syeyoung.dungeonsguide.mod.gui.Widget;
import kr.syeyoung.dungeonsguide.mod.gui.elements.popups.MinecraftTooltip;
import kr.syeyoung.dungeonsguide.mod.gui.elements.popups.MouseTooltip;
import kr.syeyoung.dungeonsguide.mod.gui.elements.popups.PopupMgr;
import kr.syeyoung.dungeonsguide.mod.gui.layouter.Layouter;
import kr.syeyoung.dungeonsguide.mod.gui.primitive.ConstraintBox;
import kr.syeyoung.dungeonsguide.mod.gui.primitive.Size;
import kr.syeyoung.dungeonsguide.mod.gui.renderer.Renderer;
import kr.syeyoung.dungeonsguide.mod.gui.renderer.RenderingContext;
import kr.syeyoung.dungeonsguide.mod.gui.xml.AnnotatedExportOnlyWidget;
import kr.syeyoung.dungeonsguide.mod.player.PlayerManager;
import lombok.Setter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.world.item.ItemStack;
import net.minecraft.ChatFormatting;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;

import java.util.Collections;
import java.util.List;

public class PlayerModelRenderer extends AnnotatedExportOnlyWidget implements Layouter, Renderer {
    @Setter
    private FakePlayer fakePlayer;
    public PlayerModelRenderer(FakePlayer fakePlayer) {
        this.fakePlayer = fakePlayer;
    }
    // let me do the skin fetching myself.
    @Override
    public List<Widget> build(DomElement buildContext) {
        return Collections.emptyList();
    }

    @Override
    public Size layout(DomElement buildContext, ConstraintBox constraintBox) {
        return new Size(constraintBox.getMaxWidth(), constraintBox.getMaxHeight());
    }

    @Override
    public double getMaxIntrinsicWidth(DomElement buildContext, double height) {
        return 0;
    }

    @Override
    public double getMaxIntrinsicHeight(DomElement buildContext, double width) {
        return 0;
    }

    @Override
    public void doRender(float partialTicks, RenderingContext context, DomElement buildContext) {
        // TODO: 3D previews are built later (D124), 1.8.9 drew the player model here:
//        GlStateManager.enableDepth();
//        GlStateManager.color(1, 1, 1, 1.0F);
//        GuiInventory.drawEntityOnScreen(45, 150, 60, (float) -relMouseX+75, 0, fakePlayer);
//        GlStateManager.disableDepth();

        String toDraw = fakePlayer.getGameProfile().name();
        List<ActiveCosmetic> activeCosmetics = DungeonsGuide.getDungeonsGuide().getCosmeticsManager().getActiveCosmeticByPlayer().get(
                fakePlayer.getGameProfile().id());



        String color=null, rawPrefix=null, rawPrefixColor=null;
        if (activeCosmetics != null) {
            for (ActiveCosmetic activeCosmetic : activeCosmetics) {
                CosmeticData cosmeticData = DungeonsGuide.getDungeonsGuide().getCosmeticsManager().getCosmeticDataMap().get(activeCosmetic.getCosmeticData());
                if (cosmeticData != null && cosmeticData.getCosmeticType().equals("ncolor")) {
                    color = cosmeticData.getData().replace("&", "§");
                } else if (cosmeticData != null && cosmeticData.getCosmeticType().equals("nprefix")) {
                    rawPrefix = cosmeticData.getData().replace("&", "§");
                } else if (cosmeticData != null && cosmeticData.getCosmeticType().equals("bracket_color")) {
                    rawPrefixColor = cosmeticData.getData().replace("&", "§");
                }
            }
        }

        String prefix = null;
        if (rawPrefix != null) {
            prefix = rawPrefix.substring(1);
            char control = rawPrefix.charAt(0);
            if (control != 'T' && control != 'Y') {
                if (rawPrefixColor != null)
                    prefix = rawPrefixColor+"["+prefix+"§r"+rawPrefixColor+"]";
            }
        }



        toDraw = (color == null ? "§e" : color) + toDraw;
        if (prefix != null) toDraw = prefix + " " + toDraw;

        if (FeatureRegistry.DG_INDICATOR.isEnabled() && PlayerManager.INSTANCE.getOnlineStatus().getOrDefault(fakePlayer.getGameProfile().id(), false)) {
            toDraw = "\ued00\ued02"+ toDraw;
        }


        String profileName = "on §6" + this.fakePlayer.getSkyblockProfile().getProfileName();
        Font fr = Minecraft.getInstance().font;
        context.drawString(profileName, (90 - fr.width(profileName)) / 2, 15, -1, false);
        context.drawString(toDraw, (90 - fr.width(toDraw)) / 2, 10 - (fr.lineHeight / 2), -1, false);
    }

    private MinecraftTooltip actualTooltip = new MinecraftTooltip();
    private MouseTooltip tooltip = null;
    private double relMouseX;
    @Override
    public boolean mouseMoved(int absMouseX, int absMouseY, double relMouseX0, double relMouseY0, boolean childHandled) {
        // yes, we don't care if child handled
        ItemStack toHover = null;
        this.relMouseX = relMouseX0;
        // TODO: D124, FakePlayer's armor and held item are stubbed with the preview, 1.8.9 hovered them here:
//        if (relMouseX0 > 20 && relMouseX0 < 70) {
//            if (33 <= relMouseY0 && relMouseY0 <= 66) {
//                toHover = fakePlayer.getInventory()[3];
//            } else if (66 <= relMouseY0 && relMouseY0 <= 108) {
//                toHover = fakePlayer.getInventory()[2];
//            } else if (108 <= relMouseY0 && relMouseY0 <= 130) {
//                toHover = fakePlayer.getInventory()[1];
//            } else if (130 <= relMouseY0 && relMouseY0 <= 154) {
//                toHover = fakePlayer.getInventory()[0];
//            }
//        } else if (relMouseX0 > 0 && relMouseX0 <= 20) {
//            if (80 <= relMouseY0 && relMouseY0 <= 120) {
//                toHover = fakePlayer.inventory.mainInventory[fakePlayer.inventory.currentItem];
//            }
//        }
        if (toHover != null) {
            List<String> list = toHover.getTooltipLines(Item.TooltipContext.of(Minecraft.getInstance().level), Minecraft.getInstance().player,
                Minecraft.getInstance().options.advancedItemTooltips ? TooltipFlag.ADVANCED : TooltipFlag.NORMAL)
                // TODO: D5 stopgap (D125), 1.8.9 tooltips were legacy strings
                .stream().map(TextUtils::getFormattedText).collect(Collectors.toList());
            for (int i = 0; i < list.size(); ++i) {
                if (i == 0) {
                    list.set(i, toHover.getRarity().color() + list.get(i));
                } else {
                    list.set(i, ChatFormatting.GRAY + list.get(i));
                }
            }
            actualTooltip.setTooltip(list);
        }
        if (toHover == null && this.tooltip != null) {
            PopupMgr.getPopupMgr(getDomElement())
                    .closePopup(this.tooltip, null);
            this.tooltip = null;
        } else if (toHover != null && this.tooltip == null)
            PopupMgr.getPopupMgr(getDomElement())
                    .openPopup(this.tooltip = new MouseTooltip(actualTooltip),(a) -> {
                        this.tooltip = null;
                    });
        return false;
    }


    @Override
    public void mouseExited(int absMouseX, int absMouseY, double relMouseX, double relMouseY) {
        if (this.tooltip != null) {
            PopupMgr.getPopupMgr(getDomElement())
                    .closePopup(this.tooltip, null);
            this.tooltip = null;
        }
    }


    @Override
    public void onUnmount() {
        if (this.tooltip != null) {
            PopupMgr.getPopupMgr(getDomElement())
                    .closePopup(this.tooltip, null);
            this.tooltip = null;
        }
        super.onUnmount();
    }
}
