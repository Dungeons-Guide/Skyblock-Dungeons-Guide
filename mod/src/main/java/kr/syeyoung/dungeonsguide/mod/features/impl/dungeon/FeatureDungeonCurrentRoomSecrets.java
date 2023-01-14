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
import kr.syeyoung.dungeonsguide.mod.events.annotations.DGEventHandler;
import kr.syeyoung.dungeonsguide.mod.features.text.StyledText;
import kr.syeyoung.dungeonsguide.mod.features.text.TextHUDFeature;
import kr.syeyoung.dungeonsguide.mod.features.text.TextStyle;
import net.minecraftforge.client.event.ClientChatReceivedEvent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FeatureDungeonCurrentRoomSecrets extends TextHUDFeature {
    public FeatureDungeonCurrentRoomSecrets() {
        super("Dungeon.HUDs", "Display # Secrets in current room", "Display what your actionbar says", "dungeon.stats.secretsroom", true, getFontRenderer().getStringWidth("Secrets In Room: 8/8"), getFontRenderer().FONT_HEIGHT);
        this.setEnabled(false);
        getStyles().add(new TextStyle("title", new AColor(0x00, 0xAA,0xAA,255), new AColor(0, 0,0,0), false));
        getStyles().add(new TextStyle("separator", new AColor(0x55, 0x55,0x55,255), new AColor(0, 0,0,0), false));
        getStyles().add(new TextStyle("currentSecrets", new AColor(0x55, 0xFF,0xFF,255), new AColor(0, 0,0,0), false));
        getStyles().add(new TextStyle("separator2", new AColor(0x55, 0x55,0x55,255), new AColor(0, 0,0,0), false));
        getStyles().add(new TextStyle("totalSecrets",  new AColor(0x55, 0xFF,0xFF,255), new AColor(0, 0,0,0), false));
    }

    SkyblockStatus skyblockStatus = DungeonsGuide.getDungeonsGuide().getSkyblockStatus();


    private static final List<StyledText> dummyText=  new ArrayList<StyledText>();
    static {
        dummyText.add(new StyledText("Secrets In Room","title"));
        dummyText.add(new StyledText(": ","separator"));
        dummyText.add(new StyledText("5","currentSecrets"));
        dummyText.add(new StyledText("/","separator2"));
        dummyText.add(new StyledText("8","totalSecrets"));
    }

    @Override
    public boolean isHUDViewable() {
        return skyblockStatus.isOnDungeon();
    }

    @Override
    public List<String> getUsedTextStyle() {
        return Arrays.asList("title", "separator", "currentSecrets", "separator2", "totalSecrets");
    }

    @Override
    public List<StyledText> getDummyText() {
        return dummyText;
    }

    private int latestCurrSecrets = 0;
    private int latestTotalSecrets = 0;


    @Override
    public List<StyledText> getText() {
        if (DungeonsGuide.getDungeonsGuide().getDungeonFacade().getContext().getBossfightProcessor() != null) return new ArrayList<StyledText>();
        List<StyledText> actualBit = new ArrayList<StyledText>();
        actualBit.add(new StyledText("Secrets In Room","title"));
        actualBit.add(new StyledText(": ","separator"));
        actualBit.add(new StyledText(latestCurrSecrets +"","currentSecrets"));
        actualBit.add(new StyledText("/","separator2"));
        actualBit.add(new StyledText(latestTotalSecrets +"","totalSecrets"));
        return actualBit;
    }

    @DGEventHandler
    public void onChat(ClientChatReceivedEvent chat) {
        if (chat.type != 2) return;
        String text = chat.message.getFormattedText();
        if (!text.contains("/")) return;

        int secretsIndex = text.indexOf("Secrets");
        if (secretsIndex != -1) {
            int theindex = 0;
            for (int i = secretsIndex; i >= 0; i--) {
                if (text.startsWith("§7", i)) {
                    theindex = i;
                }
            }
            String it = text.substring(theindex + 2, secretsIndex - 1);

            latestCurrSecrets = Integer.parseInt(it.split("/")[0]);
            latestTotalSecrets = Integer.parseInt(it.split("/")[1]);
        }
    }
}
