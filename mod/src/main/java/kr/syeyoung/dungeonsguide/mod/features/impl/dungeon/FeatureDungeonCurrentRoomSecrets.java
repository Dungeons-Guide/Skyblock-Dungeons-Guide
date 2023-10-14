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
import kr.syeyoung.dungeonsguide.mod.events.annotations.DGEventHandler;
import kr.syeyoung.dungeonsguide.mod.features.FeatureRegistry;
import kr.syeyoung.dungeonsguide.mod.features.richtext.DefaultTextHUDFeatureStyleFeature;
import kr.syeyoung.dungeonsguide.mod.features.richtext.DefaultingDelegatingTextStyle;
import kr.syeyoung.dungeonsguide.mod.features.richtext.NullTextStyle;
import kr.syeyoung.dungeonsguide.mod.features.richtext.TextHUDFeature;
import kr.syeyoung.dungeonsguide.mod.guiv2.elements.richtext.TextSpan;
import net.minecraftforge.client.event.ClientChatReceivedEvent;

public class FeatureDungeonCurrentRoomSecrets extends TextHUDFeature {
    public FeatureDungeonCurrentRoomSecrets() {
        super("Dungeon HUD.In Dungeon HUD", "Display # Secrets in current room", "Display what your actionbar says", "dungeon.stats.secretsroom");
        this.setEnabled(false);
        registerDefaultStyle("title", DefaultingDelegatingTextStyle.derive("Feature Default - Title", () -> FeatureRegistry.DEFAULT_STYLE.getStyle(DefaultTextHUDFeatureStyleFeature.Styles.NAME)));
        registerDefaultStyle("separator", DefaultingDelegatingTextStyle.derive("Feature Default - Separator", () -> FeatureRegistry.DEFAULT_STYLE.getStyle(DefaultTextHUDFeatureStyleFeature.Styles.SEPARATOR)));
        registerDefaultStyle("currentSecrets", DefaultingDelegatingTextStyle.derive("Feature Default - CurrentSecrets", () -> FeatureRegistry.DEFAULT_STYLE.getStyle(DefaultTextHUDFeatureStyleFeature.Styles.VALUE)));
        registerDefaultStyle("separator2", DefaultingDelegatingTextStyle.derive("Feature Default - Separator2", () -> FeatureRegistry.DEFAULT_STYLE.getStyle(DefaultTextHUDFeatureStyleFeature.Styles.FRACTION)));
        registerDefaultStyle("totalSecrets", DefaultingDelegatingTextStyle.derive("Feature Default - TotalSecrets", () -> FeatureRegistry.DEFAULT_STYLE.getStyle(DefaultTextHUDFeatureStyleFeature.Styles.TOTAL)));
    }


    @Override
    public boolean isHUDViewable() {
        return SkyblockStatus.isOnDungeon();
    }

    @Override
    public TextSpan getDummyText() {
        TextSpan actualBit = new TextSpan(new NullTextStyle(), "");
        actualBit.addChild(new TextSpan(getStyle("title"), "Secrets In Room"));
        actualBit.addChild(new TextSpan(getStyle("separator"), ": "));
        actualBit.addChild(new TextSpan(getStyle("currentSecrets"), "5"));
        actualBit.addChild(new TextSpan(getStyle("separator2"), "/"));
        actualBit.addChild(new TextSpan(getStyle("totalSecrets"), "8"));
        return actualBit;
    }

    private int latestCurrSecrets = 0;
    private int latestTotalSecrets = 0;


    @Override
    public TextSpan getText() {
        if (DungeonsGuide.getDungeonsGuide().getDungeonFacade().getContext()
                != null && DungeonsGuide.getDungeonsGuide().getDungeonFacade().getContext().getBossfightProcessor() != null) return new TextSpan(new NullTextStyle(), "");
        TextSpan actualBit = new TextSpan(new NullTextStyle(), "");
        actualBit.addChild(new TextSpan(getStyle("title"), "Secrets In Room"));
        actualBit.addChild(new TextSpan(getStyle("separator"), ": "));
        actualBit.addChild(new TextSpan(getStyle("currentSecrets"), latestCurrSecrets +""));
        actualBit.addChild(new TextSpan(getStyle("separator2"), "/"));
        actualBit.addChild(new TextSpan(getStyle("totalSecrets"), latestTotalSecrets +""));
        return actualBit;
    }

    @DGEventHandler
    public void onChat(ClientChatReceivedEvent chat) {
        if (chat.type != 2) return;
        String text = chat.message.getFormattedText();
        if (!text.contains("/")) return;

        int secretsIndex = text.indexOf("Secrets");
        if (secretsIndex != -1) {
            int theIndex = 0;
            for (int i = secretsIndex; i >= 0; i--) {
                if (text.startsWith("§7", i)) {
                    theIndex = i;
                }
            }
            String it = text.substring(theIndex + 2, secretsIndex - 1);

            latestCurrSecrets = Integer.parseInt(it.split("/")[0]);
            latestTotalSecrets = Integer.parseInt(it.split("/")[1]);
        }
    }
}
