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

package kr.syeyoung.dungeonsguide.mod.features.impl.party;


import kr.syeyoung.dungeonsguide.mod.config.types.AColor;
import kr.syeyoung.dungeonsguide.mod.features.FeatureRegistry;
import kr.syeyoung.dungeonsguide.mod.features.text.DefaultTextHUDFeatureStyleFeature;
import kr.syeyoung.dungeonsguide.mod.features.text.DefaultingDelegatingTextStyle;
import kr.syeyoung.dungeonsguide.mod.features.text.NullTextStyle;
import kr.syeyoung.dungeonsguide.mod.features.text.TextHUDFeature;
import kr.syeyoung.dungeonsguide.mod.guiv2.elements.richtext.TextSpan;
import kr.syeyoung.dungeonsguide.mod.party.PartyContext;
import kr.syeyoung.dungeonsguide.mod.party.PartyManager;

public class FeaturePartyList extends TextHUDFeature {
    public FeaturePartyList() {
        super("Party","Party List", "Party List as GUI", "party.list");
        registerDefaultStyle("name", DefaultingDelegatingTextStyle.derive(() -> FeatureRegistry.DEFAULT_STYLE.getStyle(DefaultTextHUDFeatureStyleFeature.Styles.NAME)));
        registerDefaultStyle("separator", DefaultingDelegatingTextStyle.derive(() -> FeatureRegistry.DEFAULT_STYLE.getStyle(DefaultTextHUDFeatureStyleFeature.Styles.SEPARATOR)));
        registerDefaultStyle("player", DefaultingDelegatingTextStyle.derive(() -> FeatureRegistry.DEFAULT_STYLE.getStyle(DefaultTextHUDFeatureStyleFeature.Styles.VALUE)));
        registerDefaultStyle("allinvite", DefaultingDelegatingTextStyle.ofDefault().setTextShader(new AColor(0xAA,0xAA,0xAA,255)).setBackgroundShader(new AColor(0, 0,0,0)));
        setEnabled(false);
    }

    @Override
    public boolean isHUDViewable() {
        return  PartyManager.INSTANCE.getPartyContext() != null;
    }
    

    @Override
    public TextSpan getDummyText() {
        TextSpan dummyText = new TextSpan(new NullTextStyle(), "");
        dummyText.addChild(new TextSpan(getStyle("name"), "Leader"));
        dummyText.addChild(new TextSpan(getStyle("separator"), ": "));
        dummyText.addChild(new TextSpan(getStyle("player"), "syeyoung"));
        dummyText.addChild(new TextSpan(getStyle("name"), "\nModerator"));
        dummyText.addChild(new TextSpan(getStyle("separator"), ": "));
        dummyText.addChild(new TextSpan(getStyle("player"), "rioho, RaidShadowLegends, Tricked"));
        dummyText.addChild(new TextSpan(getStyle("name"), "\nMember"));
        dummyText.addChild(new TextSpan(getStyle("separator"), ": "));
        dummyText.addChild(new TextSpan(getStyle("player"), "Everyone"));
        dummyText.addChild(new TextSpan(getStyle("allinvite"), "\nAll invite Off"));
        return dummyText;
    }

    @Override
    public TextSpan getText() {
        PartyContext pc = PartyManager.INSTANCE.getPartyContext();
        TextSpan text = new TextSpan(new NullTextStyle(), "");
            text.addChild(new TextSpan(getStyle("name"), "Leader"));
            text.addChild(new TextSpan(getStyle("separator"), ": "));
            text.addChild(new TextSpan(getStyle("player"), pc.getPartyOwner()+""));
            text.addChild(new TextSpan(getStyle("name"), "\nModerator"));
            text.addChild(new TextSpan(getStyle("separator"), ": "));
            text.addChild(new TextSpan(getStyle("player"), pc.getPartyModerator() == null ? "????" : String.join(", ", pc.getPartyModerator()) + (pc.isModeratorComplete() ? "" : " ?")));
            text.addChild(new TextSpan(getStyle("name"), "\nMember"));
            text.addChild(new TextSpan(getStyle("separator"), ": "));
            text.addChild(new TextSpan(getStyle("player"), pc.getPartyMember() == null ? "????" : String.join(", ", pc.getPartyMember()) + (pc.isMemberComplete() ? "" : " ?")));
            if (pc.getAllInvite() != null && !pc.getAllInvite())
                text.addChild(new TextSpan(getStyle("allinvite"), "\nAll invite Off"));
            else if (pc.getAllInvite() != null)
                text.addChild(new TextSpan(getStyle("allinvite"), "\nAll invite On"));
            else
                text.addChild(new TextSpan(getStyle("allinvite"), "\nAll invite Unknown"));
        return text;
    }
}
