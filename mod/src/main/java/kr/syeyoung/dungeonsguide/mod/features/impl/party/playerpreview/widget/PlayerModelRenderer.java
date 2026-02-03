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

import kr.syeyoung.dungeonsguide.mod.DungeonsGuide;
import kr.syeyoung.dungeonsguide.mod.cosmetics.ActiveCosmetic;
import kr.syeyoung.dungeonsguide.mod.cosmetics.CosmeticData;
import kr.syeyoung.dungeonsguide.mod.features.FeatureRegistry;
import kr.syeyoung.dungeonsguide.mod.features.impl.party.playerpreview.api.playerprofile.PlayerProfile;
import kr.syeyoung.dungeonsguide.mod.gui.DomElement;
import kr.syeyoung.dungeonsguide.mod.gui.Widget;
import kr.syeyoung.dungeonsguide.mod.gui.elements.popups.PopupMgr;
import kr.syeyoung.dungeonsguide.mod.gui.elements.popups.RawMinecraftTooltip;
import kr.syeyoung.dungeonsguide.mod.gui.layouter.Layouter;
import kr.syeyoung.dungeonsguide.mod.gui.primitive.ConstraintBox;
import kr.syeyoung.dungeonsguide.mod.gui.primitive.Size;
import kr.syeyoung.dungeonsguide.mod.gui.renderer.Renderer;
import kr.syeyoung.dungeonsguide.mod.gui.renderer.RenderingContext;
import kr.syeyoung.dungeonsguide.mod.gui.xml.AnnotatedExportOnlyWidget;
import kr.syeyoung.dungeonsguide.mod.player.PlayerManager;
import kr.syeyoung.dungeonsguide.mod.utils.TextUtils;
import kr.syeyoung.modapi.ModAPI;
import kr.syeyoung.modapi.entity.UEntityPlayerFake;
import kr.syeyoung.modapi.item.UItemStack;
import kr.syeyoung.modapi.rendering.UFontCalculator;
import lombok.Setter;

import java.util.Collections;
import java.util.List;

public class PlayerModelRenderer extends AnnotatedExportOnlyWidget implements Layouter, Renderer {
    @Setter
    private UEntityPlayerFake fakePlayer;
    private PlayerProfile skyblockProfile;

    public PlayerModelRenderer(UEntityPlayerFake fakePlayer,  PlayerProfile skyblockProfile) {
        this.fakePlayer = fakePlayer;
        this.skyblockProfile = skyblockProfile;
    }

    public void setSkyblockProfile(PlayerProfile skyblockProfile) {
        this.skyblockProfile = skyblockProfile;

        if (skyblockProfile.getCurrentArmor() != null) {
            for (int i = 0; i < 4; i++)
                fakePlayer.setCurrentArmor(i, skyblockProfile.getCurrentArmor().getArmorSlots()[i]);
        } else {
            for (int i = 0; i < 4; i++)
                fakePlayer.setCurrentArmor(i, null);
        }

        int highestDungeonScore = Integer.MIN_VALUE;
        this.fakePlayer.setMainInventory(0, null);
        if (skyblockProfile.getInventory() != null) {
            UItemStack highestItem = null;
            for (UItemStack itemStack : skyblockProfile.getInventory()) {
                if (itemStack == null) continue;
                for (String str : itemStack.getLore()) {
                    if (TextUtils.stripColor(str).startsWith("Gear")) {
                        int dungeonScore = Integer.parseInt(TextUtils.keepIntegerCharactersOnly(TextUtils.stripColor(str).split(" ")[2]));
                        if (dungeonScore > highestDungeonScore) {
                            highestItem = itemStack;
                            highestDungeonScore = dungeonScore;
                        }
                    }
                }
            }

            this.fakePlayer.setMainInventory(0, highestItem);
            this.fakePlayer.setCurrentItem(0);
        }
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
        context.drawEntityOnScreen(45, 150, 60, (float) -relMouseX+75, 0, fakePlayer);

        String toDraw = fakePlayer.getName();
        List<ActiveCosmetic> activeCosmetics = DungeonsGuide.getDungeonsGuide().getCosmeticsManager().getActiveCosmeticByPlayer().get(
                fakePlayer.getUUID());



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

        if (FeatureRegistry.DG_INDICATOR.isEnabled() && PlayerManager.INSTANCE.getOnlineStatus().getOrDefault(fakePlayer.getUUID(), false)) {
            toDraw = "\ued00\ued02"+ toDraw;
        }
//        GlStateManager.enableBlend();
//        GL14.glBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ONE_MINUS_SRC_ALPHA);
//        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ONE_MINUS_SRC_ALPHA);


        UFontCalculator fr = ModAPI.getAPI().getFontCalculator();
        if (this.skyblockProfile != null) {
            String profileName = "on §6" + this.skyblockProfile.getProfileName();
            context.drawString(profileName, (90 - fr.getStringWidth(profileName)) / 2, 15, -1);
        }
        context.drawString(toDraw, (90 - fr.getStringWidth(toDraw)) / 2, 10 - (fr.getFontHeight() / 2), -1);
    }

    private RawMinecraftTooltip actualTooltip = new RawMinecraftTooltip(0, 0);
    private boolean tooltipShow = false;
    private double relMouseX;
    @Override
    public boolean mouseMoved(int absMouseX, int absMouseY, double relMouseX0, double relMouseY0, boolean childHandled) {
        // yes, we don't care if child handled
        UItemStack toHover = null;
        this.relMouseX = relMouseX0;
        if (relMouseX0 > 20 && relMouseX0 < 70) {
            if (33 <= relMouseY0 && relMouseY0 <= 66) {
                toHover = fakePlayer.getCurrentArmor(3);
            } else if (66 <= relMouseY0 && relMouseY0 <= 108) {
                toHover = fakePlayer.getCurrentArmor(2);
            } else if (108 <= relMouseY0 && relMouseY0 <= 130) {
                toHover = fakePlayer.getCurrentArmor(1);
            } else if (130 <= relMouseY0 && relMouseY0 <= 154) {
                toHover = fakePlayer.getCurrentArmor(0);
            }
        } else if (relMouseX0 > 0 && relMouseX0 <= 20) {
            if (80 <= relMouseY0 && relMouseY0 <= 120) {
                toHover = fakePlayer.getHeldItem();
            }
        }
        if (toHover != null) {
            List<String> list = toHover.getNormalTooltip();
            actualTooltip.setTooltip(list);
        }
        if (toHover == null && tooltipShow) {
            PopupMgr.getPopupMgr(getDomElement())
                    .closePopup(this.actualTooltip, null);
        } else if (toHover != null && !tooltipShow) {
            tooltipShow = true;
            actualTooltip.setMousePos(absMouseX, absMouseY);
            PopupMgr.getPopupMgr(getDomElement())
                    .openPopup(this.actualTooltip, (a) -> {
                        tooltipShow = false;
                    });
        }
        return false;
    }


    @Override
    public void mouseExited(int absMouseX, int absMouseY, double relMouseX, double relMouseY) {
        if (tooltipShow) {
            PopupMgr.getPopupMgr(getDomElement())
                    .closePopup(this.actualTooltip, null);
            tooltipShow = false;
        }
    }


    @Override
    public void onUnmount() {
        if (tooltipShow) {
            PopupMgr.getPopupMgr(getDomElement())
                    .closePopup(this.actualTooltip, null);
            tooltipShow = false;
        }
        super.onUnmount();
    }
}
