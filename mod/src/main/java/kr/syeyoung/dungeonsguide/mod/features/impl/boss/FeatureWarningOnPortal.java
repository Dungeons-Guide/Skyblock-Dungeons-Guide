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

package kr.syeyoung.dungeonsguide.mod.features.impl.boss;

import kr.syeyoung.dungeonsguide.mod.DungeonsGuide;
import kr.syeyoung.dungeonsguide.mod.config.types.TCRTextStyleMap;
import kr.syeyoung.dungeonsguide.mod.dungeon.DungeonContext;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomfinder.DungeonRoom;
import kr.syeyoung.dungeonsguide.mod.features.FeatureParameter;
import kr.syeyoung.dungeonsguide.mod.features.FeatureRegistry;
import kr.syeyoung.dungeonsguide.mod.features.SimpleFeature;
import kr.syeyoung.dungeonsguide.mod.features.impl.dungeon.FeatureDungeonScore;
import kr.syeyoung.dungeonsguide.mod.features.richtext.DefaultTextHUDFeatureStyleFeature;
import kr.syeyoung.dungeonsguide.mod.features.richtext.DefaultingDelegatingTextStyle;
import kr.syeyoung.dungeonsguide.mod.features.richtext.NullTextStyle;
import kr.syeyoung.dungeonsguide.mod.features.richtext.config.WidgetTextStyleConfig;
import kr.syeyoung.dungeonsguide.mod.guiv2.elements.richtext.TextSpan;
import kr.syeyoung.dungeonsguide.mod.utils.TextUtils;

import java.util.*;

public class FeatureWarningOnPortal extends SimpleFeature {
    public FeatureWarningOnPortal() {
        super("Dungeon HUD", "Score Warning on Watcher portal", "Display warnings such as\n- 'NOT ALL ROOMS DISCOVERED'\n- 'NOT ALL ROOMS COMPLETED'\n- 'Expected Score: 304'\n- 'MISSING 3 CRYPTS'\non portal", "bossfight.warningonportal");
        addParameter("newstyle", new FeatureParameter<>("newstyle", "TextStyle", "", styleMap, new TCRTextStyleMap(), this::updateStyle)
                .setWidgetGenerator((param) -> new WidgetTextStyleConfig(getDummyText(), styleMap)));

        registerDefaultStyle("warning", DefaultingDelegatingTextStyle.derive("Feature Default - Warning", () -> FeatureRegistry.DEFAULT_STYLE.getStyle(DefaultTextHUDFeatureStyleFeature.Styles.WARNING)));
        registerDefaultStyle("field_name", DefaultingDelegatingTextStyle.derive("Feature Default - Name", () -> FeatureRegistry.DEFAULT_STYLE.getStyle(DefaultTextHUDFeatureStyleFeature.Styles.NAME)));
        registerDefaultStyle("field_separator", DefaultingDelegatingTextStyle.derive("Feature Default - Separator", () -> FeatureRegistry.DEFAULT_STYLE.getStyle(DefaultTextHUDFeatureStyleFeature.Styles.SEPARATOR)));
        registerDefaultStyle("field_value", DefaultingDelegatingTextStyle.derive("Feature Default - Value", () -> FeatureRegistry.DEFAULT_STYLE.getStyle(DefaultTextHUDFeatureStyleFeature.Styles.VALUE)));
        registerDefaultStyle("field_etc", DefaultingDelegatingTextStyle.derive("Feature Default - ETC", () -> FeatureRegistry.DEFAULT_STYLE.getStyle(DefaultTextHUDFeatureStyleFeature.Styles.EXTRA_INFO)));

    }

    public TextSpan getDummyText() {
        TextSpan root = new TextSpan(new NullTextStyle(), "");
        root.addChild(new TextSpan(getStyle( "warning"),"!!!WARNING!!! <- text changes in boss-room\n"));

        root.addChild(new TextSpan(getStyle("field_name"),"Total Secrets"));
        root.addChild(new TextSpan(getStyle("field_separator"),": "));
        root.addChild(new TextSpan(getStyle("field_value"),"103/100 of 50 "));
        root.addChild(new TextSpan(getStyle("field_etc"),"(103% 41.2 Explorer)"));


        root.addChild(new TextSpan(getStyle("field_name"),"Crypts"));
        root.addChild(new TextSpan(getStyle("field_separator"),": "));
        root.addChild(new TextSpan(getStyle("field_value"),"5/5\n"));


        root.addChild(new TextSpan(getStyle("field_name"),"Deaths"));
        root.addChild(new TextSpan(getStyle("field_separator"),": "));
        root.addChild(new TextSpan(getStyle("field_value"),"0\n"));

        root.addChild(new TextSpan(getStyle("field_name"),"Score Estimate"));
        root.addChild(new TextSpan(getStyle("field_separator"),": "));
        root.addChild(new TextSpan(getStyle("field_value"),"1000 "));
        root.addChild(new TextSpan(getStyle("field_etc"),"(S++++)\n"));


        root.addChild(new TextSpan(getStyle("field_name"),"Skill"));
        root.addChild(new TextSpan(getStyle("field_separator"),": "));
        root.addChild(new TextSpan(getStyle("field_value"),"100 "));
        root.addChild(new TextSpan(getStyle("field_etc"),"(0 Deaths: 0 pts)\n"));

        root.addChild(new TextSpan(getStyle("field_name"),"Explorer"));
        root.addChild(new TextSpan(getStyle("field_separator"),": "));
        root.addChild(new TextSpan(getStyle("field_value"),"100 "));
        root.addChild(new TextSpan(getStyle("field_etc"),"(100% + secrets)\n"));

        root.addChild(new TextSpan(getStyle("field_name"),"Time"));
        root.addChild(new TextSpan(getStyle("field_separator"),": "));
        root.addChild(new TextSpan(getStyle("field_value"),"100 "));
        root.addChild(new TextSpan(getStyle("field_etc"),"(-30m 29s)\n"));

        root.addChild(new TextSpan(getStyle("field_name"),"Bonus"));
        root.addChild(new TextSpan(getStyle("field_separator"),": "));
        root.addChild(new TextSpan(getStyle("field_value"),"5\n"));
        return root;
    }

    public TextSpan getText() {
        TextSpan root = new TextSpan(new NullTextStyle(), "");

        DungeonContext context = DungeonsGuide.getDungeonsGuide().getDungeonFacade().getContext();
        FeatureDungeonScore.ScoreCalculation scoreCalculation = FeatureRegistry.DUNGEON_SCORE.calculateScore();

        boolean failed = context.getScaffoldParser().getDungeonRoomList().stream().anyMatch(a -> a.getCurrentState() == DungeonRoom.RoomState.FAILED);
        if (context.getScaffoldParser().getUndiscoveredRoom() > 0) {
            root.addChild(new TextSpan(getStyle( "warning"),"There are at least "+context.getScaffoldParser().getUndiscoveredRoom()+" undiscovered rooms!\n"));
        } else if (failed) {
            root.addChild(new TextSpan(getStyle( "warning"),"There is a failed puzzle room! Yikes!\n"));
        } else if (!scoreCalculation.isFullyCleared()) {
            root.addChild(new TextSpan(getStyle( "warning"),"Some rooms are not fully cleared!\n"));
        } else if (scoreCalculation.getTombs() < 5) {
            root.addChild(new TextSpan(getStyle( "warning"),"Only less than 5 crypts are blown up!\n"));
        } else {
            root.addChild(new TextSpan(getStyle( "warning"),"\n"));
        }

        root.addChild(new TextSpan(getStyle("field_name"),"Total Secrets"));
        root.addChild(new TextSpan(getStyle("field_separator"),": "));
        root.addChild(new TextSpan(getStyle("field_value"),scoreCalculation.getSecrets() +"/" + scoreCalculation.getEffectiveTotalSecrets()+" of "+scoreCalculation.getTotalSecrets()));
        root.addChild(new TextSpan(getStyle("field_etc")," ("+(int)(scoreCalculation.getSecrets() / (float)scoreCalculation.getEffectiveTotalSecrets() * 100.0f)+"% "+(int)Math.ceil(scoreCalculation.getSecrets() / (float)scoreCalculation.getEffectiveTotalSecrets() * 40.0f)+" Explorer)\n"));


        root.addChild(new TextSpan(getStyle("field_name"),"Crypts"));
        root.addChild(new TextSpan(getStyle("field_separator"),": "));
        root.addChild(new TextSpan(getStyle("field_value"),scoreCalculation.getTombs() +"/5\n"));


        root.addChild(new TextSpan(getStyle("field_name"),"Deaths"));
        root.addChild(new TextSpan(getStyle("field_separator"),": "));
        root.addChild(new TextSpan(getStyle("field_value"),scoreCalculation.getDeaths() + "\n"));

        int sum = scoreCalculation.getTime() + scoreCalculation.getExplorer() + scoreCalculation.getSkill() + scoreCalculation.getBonus();
        root.addChild(new TextSpan(getStyle("field_name"),"Score Estimate"));
        root.addChild(new TextSpan(getStyle("field_separator"),": "));
        root.addChild(new TextSpan(getStyle("field_value"),sum+" "));
        root.addChild(new TextSpan(getStyle("field_etc"),"("+FeatureRegistry.DUNGEON_SCORE.getLetter(sum)+")\n"));


        root.addChild(new TextSpan(getStyle("field_name"),"Skill"));
        root.addChild(new TextSpan(getStyle("field_separator"),": "));
        root.addChild(new TextSpan(getStyle("field_value"),scoreCalculation.getSkill()+" "));
        root.addChild(new TextSpan(getStyle("field_etc"),"("+scoreCalculation.getDeaths()+" Deaths: "+(scoreCalculation.getDeaths() * -2)+" pts)\n"));

        root.addChild(new TextSpan(getStyle("field_name"),"Explorer"));
        root.addChild(new TextSpan(getStyle("field_separator"),": "));
        root.addChild(new TextSpan(getStyle("field_value"),scoreCalculation.getExplorer()+" "));
        root.addChild(new TextSpan(getStyle("field_etc"),"("+ DungeonsGuide.getDungeonsGuide().getDungeonFacade().getContext().getPercentage() +"% + secrets)\n"));

        root.addChild(new TextSpan(getStyle("field_name"),"Time"));
        root.addChild(new TextSpan(getStyle("field_separator"),": "));
        root.addChild(new TextSpan(getStyle("field_value"),scoreCalculation.getTime()+" "));
        root.addChild(new TextSpan(getStyle("field_etc"),"("+ TextUtils.formatTime(FeatureRegistry.DUNGEON_SBTIME.getTimeElapsed())+")\n"));

        root.addChild(new TextSpan(getStyle("field_name"),"Bonus"));
        root.addChild(new TextSpan(getStyle("field_separator"),": "));
        root.addChild(new TextSpan(getStyle("field_value"),scoreCalculation.getBonus()+"\n"));

        return root;
    }


    private Map<String, DefaultingDelegatingTextStyle> defaultStyleMap = new HashMap<>();
    private Map<String, DefaultingDelegatingTextStyle> styleMap = new HashMap<>();
    public void registerDefaultStyle(String name, DefaultingDelegatingTextStyle style) {
        defaultStyleMap.put(name, style);
        styleMap.put(name, DefaultingDelegatingTextStyle.derive("User Setting of "+name, () -> defaultStyleMap.get(name)));
    }
    public DefaultingDelegatingTextStyle getStyle(String name) {
        return styleMap.get(name);
    }

    public void updateStyle(Map<String, DefaultingDelegatingTextStyle> map) {
        styleMap.clear();
        Set<String> wasIn = new HashSet<>(map.keySet());
        Set<String> needsToBeIn = new HashSet<>(defaultStyleMap.keySet());
        needsToBeIn.removeAll(wasIn);
        for (Map.Entry<String, DefaultingDelegatingTextStyle> stringDefaultingDelegatingTextStyleEntry : map.entrySet()) {
            if (!defaultStyleMap.containsKey(stringDefaultingDelegatingTextStyleEntry.getKey())) continue;
            DefaultingDelegatingTextStyle newStyle = stringDefaultingDelegatingTextStyleEntry.getValue();
            newStyle.setName("User Setting of "+defaultStyleMap.get(stringDefaultingDelegatingTextStyleEntry.getKey()).name);
            newStyle.setParent(() -> defaultStyleMap.get(stringDefaultingDelegatingTextStyleEntry.getKey()));
            styleMap.put(stringDefaultingDelegatingTextStyleEntry.getKey(), newStyle);
        }
        for (String s : needsToBeIn) {
            styleMap.put(s, DefaultingDelegatingTextStyle.derive("User Setting of "+defaultStyleMap.get(s).name, () -> defaultStyleMap.get(s)));
            map.put(s, styleMap.get(s));
        }
    }
}
