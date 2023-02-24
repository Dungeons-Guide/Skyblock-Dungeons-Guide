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

public class WidgetPlayerModel extends AnnotatedImportOnlyWidget {
    @Bind(variableName = "models")
    public final BindableAttribute models = new BindableAttribute<>(WidgetList.class);

    @Bind(variableName = "disabled")
    public final BindableAttribute<Boolean> disabled = new BindableAttribute<>(Boolean.class, false);

    private CosmeticData previouslySelected;
    private CosmeticData currentSelected;

    public WidgetPlayerModel() {
        super(new ResourceLocation("dungeonsguide:gui/config/cosmetics/playerModel.gui"));


        ArrayList<Widget> list = new ArrayList<>();

        CosmeticsManager cosmeticsManager = DungeonsGuide.getDungeonsGuide().getCosmeticsManager();
        for (CosmeticData value : cosmeticsManager.getCosmeticDataMap().values()) {
            if (value.getCosmeticType().equals("model")) {
                list.add(new WidgetButton2(cosmeticsManager.getPerms().contains(value.getReqPerm()), value.getData(), () -> {
                    Minecraft.getMinecraft().getSoundHandler().playSound(PositionedSoundRecord.create(new ResourceLocation("gui.button.press"), 1.0F));
                    currentSelected = value;
                    update();
                }));
            }
        }
        List<ActiveCosmetic> activeCosmeticList =  cosmeticsManager.getActiveCosmeticByPlayer().computeIfAbsent(Minecraft.getMinecraft().thePlayer.getGameProfile().getId(), (a) -> new ArrayList<>());
        for (ActiveCosmetic activeCosmetic : activeCosmeticList) {
            CosmeticData cosmeticData =  cosmeticsManager.getCosmeticDataMap().get(activeCosmetic.getCosmeticData());
            if (cosmeticData != null && cosmeticData.getCosmeticType().equals("model")) {
                currentSelected = cosmeticData;
                previouslySelected = cosmeticData;
            }
        }
        update();

        models.setValue(list);
    }

    private void update() {
        CosmeticsManager cosmeticsManager = DungeonsGuide.getDungeonsGuide().getCosmeticsManager();

        boolean disable = currentSelected == previouslySelected
                || (currentSelected != null && !cosmeticsManager.getPerms().contains(currentSelected.getReqPerm()));
        disabled.setValue(disable);
    }

    @On(functionName = "apply")
    public void onApply() {
        CosmeticsManager cosmeticsManager = DungeonsGuide.getDungeonsGuide().getCosmeticsManager();
        boolean disable = currentSelected == previouslySelected
                || (currentSelected != null && !cosmeticsManager.getPerms().contains(currentSelected.getReqPerm()));
        if (disable) return;

        Minecraft.getMinecraft().getSoundHandler().playSound(PositionedSoundRecord.create(new ResourceLocation("gui.button.press"), 1.0F));

        if (previouslySelected != null) {
            List<ActiveCosmetic> activeCosmeticList = cosmeticsManager.getActiveCosmeticByPlayer().computeIfAbsent(Minecraft.getMinecraft().thePlayer.getGameProfile().getId(), (a) -> new ArrayList<>());
            for (ActiveCosmetic activeCosmetic : activeCosmeticList) {
                if (activeCosmetic.getCosmeticData().equals(previouslySelected.getId())) {
                    cosmeticsManager.removeCosmetic(activeCosmetic);
                }
            }
        }
        if (currentSelected != null)
            cosmeticsManager.setCosmetic(currentSelected);

        previouslySelected = currentSelected;

        update();
    }

    @On(functionName = "clear")
    public void onClear() {
        Minecraft.getMinecraft().getSoundHandler().playSound(PositionedSoundRecord.create(new ResourceLocation("gui.button.press"), 1.0F));
        currentSelected = null;
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
