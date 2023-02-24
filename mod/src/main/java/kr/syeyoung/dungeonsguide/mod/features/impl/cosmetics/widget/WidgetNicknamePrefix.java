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

package kr.syeyoung.dungeonsguide.mod.features.impl.cosmetics.widget;

import kr.syeyoung.dungeonsguide.mod.DungeonsGuide;
import kr.syeyoung.dungeonsguide.mod.cosmetics.ActiveCosmetic;
import kr.syeyoung.dungeonsguide.mod.cosmetics.CosmeticData;
import kr.syeyoung.dungeonsguide.mod.cosmetics.CosmeticsManager;
import kr.syeyoung.dungeonsguide.mod.guiv2.BindableAttribute;
import kr.syeyoung.dungeonsguide.mod.guiv2.Widget;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.AnnotatedImportOnlyWidget;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.annotations.Bind;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.annotations.On;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.data.WidgetList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.util.ResourceLocation;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

public class WidgetNicknamePrefix extends AnnotatedImportOnlyWidget {
    @Bind(variableName = "preview")
    public final BindableAttribute<String> preview = new BindableAttribute<>(String.class);
    @Bind(variableName = "prefixes")
    public final BindableAttribute prefixes = new BindableAttribute<>(WidgetList.class);
    @Bind(variableName = "colors")
    public final BindableAttribute colors = new BindableAttribute<>(WidgetList.class);

    @Bind(variableName = "disabled")
    public final BindableAttribute<Boolean> disabled = new BindableAttribute<>(Boolean.class, false);

    private CosmeticData previouslySelectedColor;
    private CosmeticData currentSelectedColor;


    private CosmeticData previouslySelectedPrefix;
    private CosmeticData currentSelectedPrefix;

    public WidgetNicknamePrefix() {
        super(new ResourceLocation("dungeonsguide:gui/config/cosmetics/nicknamePrefix.gui"));


        ArrayList<Widget> list = new ArrayList<>();
        ArrayList<Widget> list2 = new ArrayList<>();

        CosmeticsManager cosmeticsManager = DungeonsGuide.getDungeonsGuide().getCosmeticsManager();
        for (CosmeticData value : cosmeticsManager.getCosmeticDataMap().values()) {
            if (value.getCosmeticType().equals("bracket_color")) {
                list.add(new WidgetColorButton(cosmeticsManager.getPerms().contains(value.getReqPerm()), value.getData().substring(1), () -> {
                    Minecraft.getMinecraft().getSoundHandler().playSound(PositionedSoundRecord.create(new ResourceLocation("gui.button.press"), 1.0F));
                    currentSelectedColor = value;
                    update();
                }));
            } else if (value.getCosmeticType().equals("nprefix")) {
                char control = value.getData().charAt(0);

                if ((control == 'Y' || control == 'N') && !cosmeticsManager.getPerms().contains(value.getReqPerm())) continue;
                list2.add(new WidgetButton(cosmeticsManager.getPerms().contains(value.getReqPerm()), value.getData().substring(1), () -> {
                    Minecraft.getMinecraft().getSoundHandler().playSound(PositionedSoundRecord.create(new ResourceLocation("gui.button.press"), 1.0F));
                    currentSelectedPrefix = value;
                    update();
                }));
            }
        }
        List<ActiveCosmetic> activeCosmeticList =  cosmeticsManager.getActiveCosmeticByPlayer().computeIfAbsent(Minecraft.getMinecraft().thePlayer.getGameProfile().getId(), (a) -> new ArrayList<>());
        for (ActiveCosmetic activeCosmetic : activeCosmeticList) {
            CosmeticData cosmeticData =  cosmeticsManager.getCosmeticDataMap().get(activeCosmetic.getCosmeticData());
            if (cosmeticData != null && cosmeticData.getCosmeticType().equals("bracket_color")) {
                currentSelectedColor = cosmeticData;
                previouslySelectedColor = cosmeticData;
            } else if (cosmeticData != null && cosmeticData.getCosmeticType().equals("nprefix")) {
                currentSelectedPrefix = cosmeticData;
                previouslySelectedPrefix = cosmeticData;
            }
        }

        if (currentSelectedColor == null) {
            for (CosmeticData value : cosmeticsManager.getCosmeticDataMap().values()) {
                if (value.getCosmeticType().equals("bracket_color") && cosmeticsManager.getPerms().contains(value.getReqPerm())) {
                    currentSelectedColor = value;
                    break;
                }
            }
        }
        update();

        colors.setValue(list);
        prefixes.setValue(list2);
    }

    private void update() {
        CosmeticsManager cosmeticsManager = DungeonsGuide.getDungeonsGuide().getCosmeticsManager();
        String prefix = currentSelectedPrefix == null ? "" : currentSelectedPrefix.getData().substring(1);
        if (currentSelectedColor != null && currentSelectedPrefix != null) {
            char control = currentSelectedPrefix.getData().charAt(0);
            if (!(control == 'Y' || control == 'T')) {
                prefix = currentSelectedColor.getData() + "[" + prefix + "§r" + currentSelectedColor.getData() + "]";
            }
        }
        if (!prefix.isEmpty()) prefix += " ";

        preview.setValue(String.join("\n",new String[] {
                        "§9Party §8> §r§a[RANK§6+§a] %prefix%§a%name%§f: TEST",
                        "§2Guild > §r§a[RANK§6+§a] %prefix%§a%name% §3[Vet]§f: TEST",
                        "§dTo §r§r§a[RANK§r§6+§r§a] %prefix%§a%name%§r§7: §r§7TEST§r",
                        "§dFrom §r§r§a[RANK§r§6+§r§a] %prefix%§a%name%§r§7: §r§7TEST§r",
                        "§r§b[RANK§c+§b] %prefix%§a%name%§f: TEST",
                        "§r§bCo-op > §r§a[RANK§6+§a] %prefix%§a%name%§f: §rTEST§r"
                }).replace("%name%", Minecraft.getMinecraft().getSession().getUsername())
                .replace("%prefix%", prefix));

        boolean disable = (currentSelectedColor == previouslySelectedColor && currentSelectedPrefix == previouslySelectedPrefix)
                || (currentSelectedColor != null && !cosmeticsManager.getPerms().contains(currentSelectedColor.getReqPerm()))
                || (currentSelectedPrefix != null && !cosmeticsManager.getPerms().contains(currentSelectedPrefix.getReqPerm()));
        disabled.setValue(disable);
    }

    @On(functionName = "apply")
    public void onApply() {
        CosmeticsManager cosmeticsManager = DungeonsGuide.getDungeonsGuide().getCosmeticsManager();
        boolean disable = (currentSelectedColor == previouslySelectedColor && currentSelectedPrefix == previouslySelectedPrefix)
                || (currentSelectedColor != null && !cosmeticsManager.getPerms().contains(currentSelectedColor.getReqPerm()))
                || (currentSelectedPrefix != null && !cosmeticsManager.getPerms().contains(currentSelectedPrefix.getReqPerm()));
        if (disable) return;

        Minecraft.getMinecraft().getSoundHandler().playSound(PositionedSoundRecord.create(new ResourceLocation("gui.button.press"), 1.0F));

        if (previouslySelectedColor != null) {
            List<ActiveCosmetic> activeCosmeticList = cosmeticsManager.getActiveCosmeticByPlayer().computeIfAbsent(Minecraft.getMinecraft().thePlayer.getGameProfile().getId(), (a) -> new ArrayList<>());
            for (ActiveCosmetic activeCosmetic : activeCosmeticList) {
                if (activeCosmetic.getCosmeticData().equals(previouslySelectedColor.getId())) {
                    cosmeticsManager.removeCosmetic(activeCosmetic);
                }
            }
        }
        if (previouslySelectedPrefix != null) {
            List<ActiveCosmetic> activeCosmeticList = cosmeticsManager.getActiveCosmeticByPlayer().computeIfAbsent(Minecraft.getMinecraft().thePlayer.getGameProfile().getId(), (a) -> new ArrayList<>());
            for (ActiveCosmetic activeCosmetic : activeCosmeticList) {
                if (activeCosmetic.getCosmeticData().equals(previouslySelectedPrefix.getId())) {
                    cosmeticsManager.removeCosmetic(activeCosmetic);
                }
            }
        }
        if (currentSelectedColor != null)
            cosmeticsManager.setCosmetic(currentSelectedColor);
        if (currentSelectedPrefix != null)
            cosmeticsManager.setCosmetic(currentSelectedPrefix);

        previouslySelectedColor = currentSelectedColor;
        previouslySelectedPrefix = currentSelectedPrefix;

        update();
    }

    @On(functionName = "clear")
    public void onClear() {
        Minecraft.getMinecraft().getSoundHandler().playSound(PositionedSoundRecord.create(new ResourceLocation("gui.button.press"), 1.0F));
        currentSelectedColor = null;
        currentSelectedPrefix = null;

        CosmeticsManager cosmeticsManager = DungeonsGuide.getDungeonsGuide().getCosmeticsManager();
        for (CosmeticData value : cosmeticsManager.getCosmeticDataMap().values()) {
            if (value.getCosmeticType().equals("bracket_color") && cosmeticsManager.getPerms().contains(value.getReqPerm())) {
                currentSelectedColor = value;
                break;
            }
        }

        update();
    }

    @On(functionName = "shop")
    public void openShop() {
        Minecraft.getMinecraft().getSoundHandler().playSound(PositionedSoundRecord.create(new ResourceLocation("gui.button.press"), 1.0F));
        try {
            Desktop.getDesktop().browse(new URI("https://store.dungeons.guide/"));
        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
        }
    }
}
