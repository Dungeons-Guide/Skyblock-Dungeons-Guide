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

package kr.syeyoung.dungeonsguide.features.impl.party;

import kr.syeyoung.dungeonsguide.chat.PartyContext;
import kr.syeyoung.dungeonsguide.chat.PartyManager;
import kr.syeyoung.dungeonsguide.config.types.AColor;
import kr.syeyoung.dungeonsguide.features.text.StyledText;
import kr.syeyoung.dungeonsguide.features.text.TextHUDFeature;
import kr.syeyoung.dungeonsguide.features.text.TextStyle;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FeaturePartyList extends TextHUDFeature {
    public FeaturePartyList() {
        super("Party","Party List", "Party List as GUI", "party.list", false, getFontRenderer().getStringWidth("Watcher finished spawning all mobs!"), getFontRenderer().FONT_HEIGHT*4);
        getStyles().add(new TextStyle("name", new AColor(0x00, 0xAA,0xAA,255), new AColor(0, 0,0,0), false));
        getStyles().add(new TextStyle("separator", new AColor(0x55, 0x55,0x55,255), new AColor(0, 0,0,0), false));
        getStyles().add(new TextStyle("player", new AColor(0x55, 0xFF,0xFF,255), new AColor(0, 0,0,0), false));
        getStyles().add(new TextStyle("allinvite", new AColor(0xAA,0xAA,0xAA,255), new AColor(0, 0,0,0), false));
        setEnabled(true);
    }

    @Override
    public boolean isHUDViewable() {
        return  PartyManager.INSTANCE.getPartyContext() != null;
    }

    @Override
    public java.util.List<String> getUsedTextStyle() {
        return Arrays.asList("name" ,"separator", "player", "allinvite");
    }

    private static final List<StyledText> dummyText = new ArrayList<StyledText>();
    static {
        dummyText.add(new StyledText("Leader","name"));
        dummyText.add(new StyledText(": ","separator"));
        dummyText.add(new StyledText("syeyoung","player"));
        dummyText.add(new StyledText("\nModerator","name"));
        dummyText.add(new StyledText(": ","separator"));
        dummyText.add(new StyledText("rioho, RaidShadowLegends, Tricked","player"));
        dummyText.add(new StyledText("\nMember","name"));
        dummyText.add(new StyledText(": ","separator"));
        dummyText.add(new StyledText("Everyone","player"));
        dummyText.add(new StyledText("\nAll invite Off","allinvite"));
    }

    @Override
    public List<StyledText> getDummyText() {
        return dummyText;
    }

    @Override
    public boolean doesScaleWithHeight() {
        return false;
    }

    @Override
    public List<StyledText> getText() {
        PartyContext pc = PartyManager.INSTANCE.getPartyContext();
        List<StyledText> text= new ArrayList<>();
            text.add(new StyledText("Leader","name"));
            text.add(new StyledText(": ","separator"));
            text.add(new StyledText(pc.getPartyOwner()+"","player"));
            text.add(new StyledText("\nModerator","name"));
            text.add(new StyledText(": ","separator"));
            text.add(new StyledText(pc.getPartyModerator() == null ? "????" : String.join(", ", pc.getPartyModerator()) + (pc.isModeratorComplete() ? "" : " ?"),"player"));
            text.add(new StyledText("\nMember","name"));
            text.add(new StyledText(": ","separator"));
            text.add(new StyledText(pc.getPartyMember() == null ? "????" : String.join(", ", pc.getPartyMember()) + (pc.isMemberComplete() ? "" : " ?"),"player"));
            if (pc.getAllInvite() != null && !pc.getAllInvite())
                text.add(new StyledText("\nAll invite Off","allinvite"));
            else if (pc.getAllInvite() != null)
                text.add(new StyledText("\nAll invite On","allinvite"));
            else
                text.add(new StyledText("\nAll invite Unknown","allinvite"));
        return text;
    }
}
