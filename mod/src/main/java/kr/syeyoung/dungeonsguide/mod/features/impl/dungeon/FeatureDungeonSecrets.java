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

package kr.syeyoung.dungeonsguide.mod.features.impl.dungeon;


import kr.syeyoung.dungeonsguide.mod.DungeonsGuide;
import kr.syeyoung.dungeonsguide.mod.SkyblockStatus;
import kr.syeyoung.dungeonsguide.mod.config.types.AColor;
import kr.syeyoung.dungeonsguide.mod.dungeon.DungeonContext;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomfinder.DungeonRoom;
import kr.syeyoung.dungeonsguide.mod.features.FeatureRegistry;
import kr.syeyoung.dungeonsguide.mod.features.richtext.DefaultTextHUDFeatureStyleFeature;
import kr.syeyoung.dungeonsguide.mod.features.richtext.DefaultingDelegatingTextStyle;
import kr.syeyoung.dungeonsguide.mod.features.richtext.NullTextStyle;
import kr.syeyoung.dungeonsguide.mod.features.richtext.TextHUDFeature;
import kr.syeyoung.dungeonsguide.mod.guiv2.elements.richtext.TextSpan;
import kr.syeyoung.dungeonsguide.mod.parallelUniverse.tab.TabList;
import kr.syeyoung.dungeonsguide.mod.parallelUniverse.tab.TabListEntry;
import kr.syeyoung.dungeonsguide.mod.utils.TextUtils;

public class FeatureDungeonSecrets extends TextHUDFeature {
    public FeatureDungeonSecrets() {
        super("Dungeon HUD.In Dungeon HUD", "Display Total # of Secrets", "Display how much total secrets have been found in a dungeon run.\n+ sign means DG does not know the correct number, but it's somewhere above that number.", "dungeon.stats.secrets");
        this.setEnabled(false);
        registerDefaultStyle("title", DefaultingDelegatingTextStyle.derive("Feature Default - Title", () -> FeatureRegistry.DEFAULT_STYLE.getStyle(DefaultTextHUDFeatureStyleFeature.Styles.NAME)));
        registerDefaultStyle("separator", DefaultingDelegatingTextStyle.derive("Feature Default - Separator", () -> FeatureRegistry.DEFAULT_STYLE.getStyle(DefaultTextHUDFeatureStyleFeature.Styles.SEPARATOR)));
        registerDefaultStyle("currentSecrets", DefaultingDelegatingTextStyle.derive("Feature Default - CurrentSecrets", () -> FeatureRegistry.DEFAULT_STYLE.getStyle(DefaultTextHUDFeatureStyleFeature.Styles.VALUE)));
        registerDefaultStyle("separator2", DefaultingDelegatingTextStyle.derive("Feature Default - Separator2", () -> FeatureRegistry.DEFAULT_STYLE.getStyle(DefaultTextHUDFeatureStyleFeature.Styles.FRACTION)));
        registerDefaultStyle("totalSecrets", DefaultingDelegatingTextStyle.derive("Feature Default - TotalSecrets", () -> FeatureRegistry.DEFAULT_STYLE.getStyle(DefaultTextHUDFeatureStyleFeature.Styles.TOTAL)));
        registerDefaultStyle("unknown", DefaultingDelegatingTextStyle.derive("Feature Default - Unknown", () -> FeatureRegistry.DEFAULT_STYLE.getStyle(DefaultTextHUDFeatureStyleFeature.Styles.DEFAULT))
                .setTextShader(new AColor(0xFF, 0xFF,0x55,255)));
    }

    SkyblockStatus skyblockStatus = DungeonsGuide.getDungeonsGuide().getSkyblockStatus();
    public int getSecretsFound() {
        for (TabListEntry tabListEntry : TabList.INSTANCE.getTabListEntries()) {
            String name = tabListEntry.getEffectiveName();
            if (name.startsWith("§r Secrets Found: §r§b") && !name.contains("%")) {
                String noColor = TextUtils.stripColor(name);
                return Integer.parseInt(noColor.substring(16));
            }
        }
        return 0;
    }
    public double getSecretPercentage() {
        for (TabListEntry tabListEntry : TabList.INSTANCE.getTabListEntries()) {
            String name = tabListEntry.getEffectiveName();
            if (name.startsWith("§r Secrets Found: §r") && name.contains("%")) {
                String noColor = TextUtils.stripColor(name);
                return Double.parseDouble(noColor.substring(16).replace("%", ""));
            }
        }
        return 0;
    }

    public int getTotalSecretsInt() {
        if (getSecretsFound() != 0) return (int) Math.ceil (getSecretsFound() / getSecretPercentage() * 100);
        DungeonContext context = DungeonsGuide.getDungeonsGuide().getDungeonFacade().getContext();
        if (context == null) return 0;
        int totalSecrets = 0;
        if (context.getScaffoldParser() == null) return 0;
        for (DungeonRoom dungeonRoom : context.getScaffoldParser().getDungeonRoomList()) {
            if (dungeonRoom.getTotalSecrets() != -1)
                totalSecrets += dungeonRoom.getTotalSecrets();
        }
        return totalSecrets;
    }
    public boolean sureOfTotalSecrets() {
        if (getSecretsFound() != 0) return true;
        DungeonContext context = DungeonsGuide.getDungeonsGuide().getDungeonFacade().getContext();
        if (context.getScaffoldParser() == null) return false;
        if (context.getScaffoldParser().getUndiscoveredRoom() > 0) return false;
        boolean allKnown = true;
        for (DungeonRoom dungeonRoom : context.getScaffoldParser().getDungeonRoomList()) {
            if (dungeonRoom.getTotalSecrets() == -1) allKnown = false;
        }
        return allKnown;
    }

    public String getTotalSecrets() {
        DungeonContext context = DungeonsGuide.getDungeonsGuide().getDungeonFacade().getContext();
        if (context == null) return "?";
        if (context.getScaffoldParser() == null) return "?";
        int totalSecrets = 0;
        boolean allknown = true;
        for (DungeonRoom dungeonRoom : context.getScaffoldParser().getDungeonRoomList()) {
            if (dungeonRoom.getTotalSecrets() != -1)
                totalSecrets += dungeonRoom.getTotalSecrets();
            else allknown = false;
        }
        return totalSecrets + (allknown ? "":"+");
    }



    @Override
    public boolean isHUDViewable() {
        return DungeonsGuide.getDungeonsGuide().getDungeonFacade().getContext() != null;
    }

    @Override
    public TextSpan getDummyText() {
        TextSpan dummyText = new TextSpan(new NullTextStyle(), "");
        dummyText.addChild(new TextSpan(getStyle("title"), "Secrets"));
        dummyText.addChild(new TextSpan(getStyle("separator"), ": "));
        dummyText.addChild(new TextSpan(getStyle("currentSecrets"), "999"));
        dummyText.addChild(new TextSpan(getStyle("separator2"), "/"));
        dummyText.addChild(new TextSpan(getStyle("totalSecrets"), "2"));
        dummyText.addChild(new TextSpan(getStyle("unknown"), "+"));
        return dummyText;
    }

    @Override
    public TextSpan getText() {
        TextSpan actualBit = new TextSpan(new NullTextStyle(), "");
        actualBit.addChild(new TextSpan(getStyle("title"), "Secrets"));
        actualBit.addChild(new TextSpan(getStyle("separator"), ": "));
        actualBit.addChild(new TextSpan(getStyle("currentSecrets"), getSecretsFound() +""));
        actualBit.addChild(new TextSpan(getStyle("separator2"), "/"));

        actualBit.addChild(new TextSpan(getStyle("totalSecrets"), (int)Math.ceil(getTotalSecretsInt() * DungeonsGuide.getDungeonsGuide().getDungeonFacade().getContext().getSecretPercentage())+" of "+getTotalSecretsInt()));
        actualBit.addChild(new TextSpan(getStyle("unknown"), getTotalSecrets().contains("+") ? "+" : ""));
        return actualBit;
    }

}
