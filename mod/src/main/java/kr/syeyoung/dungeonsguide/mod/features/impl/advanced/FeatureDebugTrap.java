/*
 * Dungeons Guide - The most intelligent Hypixel Skyblock Dungeons Mod
 * Copyright (C) 2022  cyoung06 (syeyoung)
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

package kr.syeyoung.dungeonsguide.mod.features.impl.advanced;

import kr.syeyoung.dungeonsguide.mod.DungeonsGuide;
import kr.syeyoung.dungeonsguide.mod.SkyblockStatus;
import kr.syeyoung.dungeonsguide.mod.features.FeatureRegistry;
import kr.syeyoung.dungeonsguide.mod.features.richtext.DefaultTextHUDFeatureStyleFeature;
import kr.syeyoung.dungeonsguide.mod.features.richtext.DefaultingDelegatingTextStyle;
import kr.syeyoung.dungeonsguide.mod.features.richtext.NullTextStyle;
import kr.syeyoung.dungeonsguide.mod.features.richtext.TextHUDFeature;
import kr.syeyoung.dungeonsguide.mod.guiv2.elements.richtext.TextSpan;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.EntityBat;

import java.util.ArrayList;
import java.util.List;

public class FeatureDebugTrap extends TextHUDFeature {
    public FeatureDebugTrap() {
        super("Debug", "Display the current amount of bat entities", "", "advanced.bat");
        registerDefaultStyle("base", DefaultingDelegatingTextStyle.derive("Feature Default - Base", () -> FeatureRegistry.DEFAULT_STYLE.getStyle(DefaultTextHUDFeatureStyleFeature.Styles.NAME)));
        registerDefaultStyle("batsamm", DefaultingDelegatingTextStyle.derive("Feature Default - Batsamm", () -> FeatureRegistry.DEFAULT_STYLE.getStyle(DefaultTextHUDFeatureStyleFeature.Styles.VALUE)));
    }

    static List<Long> avgStorage = new ArrayList<>();

    public static void updateVal(long timeItTookForThePacketToProcess){
        System.out.println(timeItTookForThePacketToProcess);
        if(avgStorage.size() > 5){
            avgStorage.remove( 0);
        }

        avgStorage.add(timeItTookForThePacketToProcess);
    }


    static long getAvgTimeItTookToPacket(){
        return (long) avgStorage.stream().mapToLong(val -> val).average().orElse(0.0);
    }

    @Override
    public boolean isHUDViewable() {
        return SkyblockStatus.isOnDungeon() && DungeonsGuide.getDungeonsGuide().getDungeonFacade().getContext() != null;
    }

    @Override
    public TextSpan getDummyText() {
        TextSpan textSpan = new TextSpan(new NullTextStyle(), "");
        textSpan.addChild(new TextSpan(getStyle("base"), "Bats: "));
        textSpan.addChild(new TextSpan(getStyle("batsamm"), "9999"));
        return textSpan;
    }

    @Override
    public TextSpan getText() {

        List<Entity> bats = Minecraft.getMinecraft().theWorld.getEntities(EntityBat.class, e -> true);


        TextSpan actualBit = new TextSpan(new NullTextStyle(), "");
        actualBit.addChild(new TextSpan(getStyle("base"), "Bats: "));


        actualBit.addChild(new TextSpan(getStyle("batsamm"), String.valueOf(getAvgTimeItTookToPacket())));

        return actualBit;
    }
}
