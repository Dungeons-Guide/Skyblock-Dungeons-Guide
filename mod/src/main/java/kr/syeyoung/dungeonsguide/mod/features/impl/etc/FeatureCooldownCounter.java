/*
 * Dungeons Guide - The most intelligent Hypixel Skyblock Dungeons Mod
 * Copyright (C) 2021  cyoung06
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

package kr.syeyoung.dungeonsguide.mod.features.impl.etc;

<<<<<<<< HEAD:mod/src/main/java/kr/syeyoung/dungeonsguide/features/impl/etc/FeatureCooldownCounter.java
import kr.syeyoung.dungeonsguide.config.types.AColor;
import kr.syeyoung.dungeonsguide.features.listener.DungeonQuitListener;
import kr.syeyoung.dungeonsguide.features.listener.GuiOpenListener;
import kr.syeyoung.dungeonsguide.features.text.StyledText;
import kr.syeyoung.dungeonsguide.features.text.TextHUDFeature;
import kr.syeyoung.dungeonsguide.features.text.TextStyle;
========
import kr.syeyoung.dungeonsguide.mod.config.types.AColor;
import kr.syeyoung.dungeonsguide.mod.features.listener.DungeonQuitListener;
import kr.syeyoung.dungeonsguide.mod.features.listener.GuiOpenListener;
import kr.syeyoung.dungeonsguide.mod.features.text.StyledText;
import kr.syeyoung.dungeonsguide.mod.features.text.TextHUDFeature;
import kr.syeyoung.dungeonsguide.mod.features.text.TextStyle;
>>>>>>>> origin/breaking-changes-just-working-im-not-putting-all-of-these-into-3.0-but-for-the-sake-of-beta-release-this-thing-exists:mod/src/main/java/kr/syeyoung/dungeonsguide/mod/features/impl/etc/FeatureCooldownCounter.java
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.inventory.ContainerChest;
import net.minecraftforge.client.event.GuiOpenEvent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FeatureCooldownCounter extends TextHUDFeature implements DungeonQuitListener, GuiOpenListener {
    public FeatureCooldownCounter() {
        super("Dungeon", "Dungeon Cooldown Counter", "Counts 10 seconds after leaving dungeon", "qol.cooldown", true, getFontRenderer().getStringWidth("Cooldown: 10s "), getFontRenderer().FONT_HEIGHT);
        getStyles().add(new TextStyle("title", new AColor(0x00, 0xAA,0xAA,255), new AColor(0, 0,0,0), false));
        getStyles().add(new TextStyle("separator", new AColor(0x55, 0x55,0x55,255), new AColor(0, 0,0,0), false));
        getStyles().add(new TextStyle("number", new AColor(0x55, 0xFF,0xFF,255), new AColor(0, 0,0,0), false));
    }

    private long leftDungeonTime = 0L;

    private static final java.util.List<StyledText> dummyText=  new ArrayList<StyledText>();
    static {
        dummyText.add(new StyledText("Cooldown","title"));
        dummyText.add(new StyledText(": ","separator"));
        dummyText.add(new StyledText("20s","number"));
    }

    @Override
    public List<StyledText> getDummyText() {
        return dummyText;
    }

    @Override
    public boolean isHUDViewable() {
        return System.currentTimeMillis() - leftDungeonTime < 20000;
    }

    @Override
    public java.util.List<String> getUsedTextStyle() {
        return Arrays.asList("title", "separator", "number");
    }

    @Override
    public List<StyledText> getText() {
        List<StyledText> actualBit = new ArrayList<StyledText>();
        actualBit.add(new StyledText("Cooldown","title"));
        actualBit.add(new StyledText(": ","separator"));
        actualBit.add(new StyledText((20 - (System.currentTimeMillis() - leftDungeonTime) / 1000)+"s","number"));
        return actualBit;
    }

    @Override
    public boolean doesScaleWithHeight() {
        return true;
    }

    @Override
    public void onDungeonQuit() {
        leftDungeonTime = System.currentTimeMillis();
    }

    @Override
    public void onGuiOpen(GuiOpenEvent rendered) {
        if (!(rendered.gui instanceof GuiChest)) return;
        ContainerChest chest = (ContainerChest) ((GuiChest) rendered.gui).inventorySlots;
        if (chest.getLowerChestInventory().getName().contains("On cooldown!")) {
            leftDungeonTime = System.currentTimeMillis();
        } else if (chest.getLowerChestInventory().getName().contains("Error")) {
            leftDungeonTime = System.currentTimeMillis();
        }
    }
}
