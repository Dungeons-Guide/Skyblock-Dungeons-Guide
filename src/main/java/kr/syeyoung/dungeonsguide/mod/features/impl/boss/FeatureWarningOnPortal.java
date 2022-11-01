/*
 *     Dungeons Guide - The most intelligent Hypixel Skyblock Dungeons Mod
 *     Copyright (C) 2021  cyoung06
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Affero General Public License as published
 *     by the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Affero General Public License for more details.
 *
 *     You should have received a copy of the GNU Affero General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package kr.syeyoung.dungeonsguide.mod.features.impl.boss;

import com.google.common.base.Supplier;
import kr.syeyoung.dungeonsguide.mod.DungeonsGuide;
import kr.syeyoung.dungeonsguide.mod.SkyblockStatus;
import kr.syeyoung.dungeonsguide.mod.config.guiconfig.ConfigPanelCreator;
import kr.syeyoung.dungeonsguide.mod.config.guiconfig.MFeatureEdit;
import kr.syeyoung.dungeonsguide.mod.config.guiconfig.MParameterEdit;
import kr.syeyoung.dungeonsguide.mod.config.guiconfig.RootConfigPanel;
import kr.syeyoung.dungeonsguide.mod.config.types.AColor;
import kr.syeyoung.dungeonsguide.mod.dungeon.DungeonContext;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomfinder.DungeonRoom;
import kr.syeyoung.dungeonsguide.mod.features.FeatureParameter;
import kr.syeyoung.dungeonsguide.mod.features.FeatureRegistry;
import kr.syeyoung.dungeonsguide.mod.features.SimpleFeature;
import kr.syeyoung.dungeonsguide.mod.features.impl.dungeon.FeatureDungeonScore;
import kr.syeyoung.dungeonsguide.mod.features.text.*;
import kr.syeyoung.dungeonsguide.mod.gui.MPanel;
import kr.syeyoung.dungeonsguide.mod.utils.TextUtils;

import java.util.*;

public class FeatureWarningOnPortal extends SimpleFeature implements StyledTextProvider {
    public FeatureWarningOnPortal() {
        super("Dungeon.Blood Room", "Score Warning on Watcher portal", "Display warnings such as\n- 'NOT ALL ROOMS DISCOVERED'\n- 'NOT ALL ROOMS COMPLETED'\n- 'Expected Score: 304'\n- 'MISSING 3 CRYPTS'\non portal", "bossfight.warningonportal");
        addParameter("textStyles", new FeatureParameter<List<TextStyle>>("textStyles", "", "", new ArrayList<TextStyle>(), "list_textStyle"));
        getStyles().add(new TextStyle("warning", new AColor(255, 0,0,255), new AColor(255, 255,255,255), false));
        getStyles().add(new TextStyle("field_name", new AColor(255, 72,255,255), new AColor(0, 0,0,0), false));
        getStyles().add(new TextStyle("field_separator", new AColor(204, 204,204,255), new AColor(0, 0,0,0), false));
        getStyles().add(new TextStyle("field_value", new AColor(255, 255,0,255), new AColor(0, 0,0,0), false));
        getStyles().add(new TextStyle("field_etc", new AColor(204, 204,204,255), new AColor(0, 0,0,0), false));
    }


    private static final List<StyledText> dummyText = new ArrayList<StyledText>();
    static {
        dummyText.add(new StyledText("!!!WARNING!!! <- text changes in boss-room\n", "warning"));

        dummyText.add(new StyledText("Total Secrets","field_name"));
        dummyText.add(new StyledText(": ","field_separator"));
        dummyText.add(new StyledText("103/100 of 50","field_value"));
        dummyText.add(new StyledText("(103% 41.2 Explorer)","field_etc"));


        dummyText.add(new StyledText("Crypts","field_name"));
        dummyText.add(new StyledText(": ","field_separator"));
        dummyText.add(new StyledText("5/5\n","field_value"));


        dummyText.add(new StyledText("Deaths","field_name"));
        dummyText.add(new StyledText(": ","field_separator"));
        dummyText.add(new StyledText("0\n","field_value"));

        dummyText.add(new StyledText("Score Estimate","field_name"));
        dummyText.add(new StyledText(": ","field_separator"));
        dummyText.add(new StyledText("1000 ","field_value"));
        dummyText.add(new StyledText("(S++++)\n","field_etc"));


        dummyText.add(new StyledText("Skill","field_name"));
        dummyText.add(new StyledText(": ","field_separator"));
        dummyText.add(new StyledText("100 ","field_value"));
        dummyText.add(new StyledText("(0 Deaths: 0 pts)\n","field_etc"));

        dummyText.add(new StyledText("Explorer","field_name"));
        dummyText.add(new StyledText(": ","field_separator"));
        dummyText.add(new StyledText("100 ","field_value"));
        dummyText.add(new StyledText("(100% + secrets)\n","field_etc"));

        dummyText.add(new StyledText("Time","field_name"));
        dummyText.add(new StyledText(": ","field_separator"));
        dummyText.add(new StyledText("100 ","field_value"));
        dummyText.add(new StyledText("(-30m 29s)\n","field_etc"));

        dummyText.add(new StyledText("Bonus","field_name"));
        dummyText.add(new StyledText(": ","field_separator"));
        dummyText.add(new StyledText("5\n","field_value"));
    }
    @Override
    public List<StyledText> getDummyText() {
        return dummyText;
    }

    SkyblockStatus skyblockStatus = DungeonsGuide.getDungeonsGuide().getSkyblockStatus();
    @Override
    public List<StyledText> getText() {
        ArrayList<StyledText> texts = new ArrayList<StyledText>();
        DungeonContext context = DungeonsGuide.getDungeonsGuide().getDungeonFacade().getContext();
        FeatureDungeonScore.ScoreCalculation scoreCalculation = FeatureRegistry.DUNGEON_SCORE.calculateScore();

        boolean failed = context.getDungeonRoomList().stream().anyMatch(a -> a.getCurrentState() == DungeonRoom.RoomState.FAILED);
        if (context.getMapProcessor().getUndiscoveredRoom() > 0) {
            texts.add(new StyledText("There are at least "+context.getMapProcessor().getUndiscoveredRoom()+" undiscovered rooms!\n", "warning"));
        } else if (failed) {
            texts.add(new StyledText("There is a failed puzzle room! Yikes!\n", "warning"));
        } else if (!scoreCalculation.isFullyCleared()) {
            texts.add(new StyledText("Some rooms are not fully cleared!\n", "warning"));
        } else if (scoreCalculation.getTombs() < 5) {
            texts.add(new StyledText("Only less than 5 crypts are blown up!\n", "warning"));
        } else {
            texts.add(new StyledText("\n", "warning"));
        }

        texts.add(new StyledText("Total Secrets","field_name"));
        texts.add(new StyledText(": ","field_separator"));
        texts.add(new StyledText(scoreCalculation.getSecrets() +"/" + scoreCalculation.getEffectiveTotalSecrets()+" of "+scoreCalculation.getTotalSecrets(),"field_value"));
        texts.add(new StyledText(" ("+(int)(scoreCalculation.getSecrets() / (float)scoreCalculation.getEffectiveTotalSecrets() * 100.0f)+"% "+(int)Math.ceil(scoreCalculation.getSecrets() / (float)scoreCalculation.getEffectiveTotalSecrets() * 40.0f)+" Explorer)\n","field_etc"));


        texts.add(new StyledText("Crypts","field_name"));
        texts.add(new StyledText(": ","field_separator"));
        texts.add(new StyledText(scoreCalculation.getTombs() +"/5\n","field_value"));


        texts.add(new StyledText("Deaths","field_name"));
        texts.add(new StyledText(": ","field_separator"));
        texts.add(new StyledText(scoreCalculation.getDeaths() + "\n","field_value"));

        int sum = scoreCalculation.getTime() + scoreCalculation.getExplorer() + scoreCalculation.getSkill() + scoreCalculation.getBonus();
        texts.add(new StyledText("Score Estimate","field_name"));
        texts.add(new StyledText(": ","field_separator"));
        texts.add(new StyledText(sum+" ","field_value"));
        texts.add(new StyledText("("+FeatureRegistry.DUNGEON_SCORE.getLetter(sum)+")\n","field_etc"));


        texts.add(new StyledText("Skill","field_name"));
        texts.add(new StyledText(": ","field_separator"));
        texts.add(new StyledText(scoreCalculation.getSkill()+" ","field_value"));
        texts.add(new StyledText("("+scoreCalculation.getDeaths()+" Deaths: "+(scoreCalculation.getDeaths() * -2)+" pts)\n","field_etc"));

        texts.add(new StyledText("Explorer","field_name"));
        texts.add(new StyledText(": ","field_separator"));
        texts.add(new StyledText(scoreCalculation.getExplorer()+" ","field_value"));
        texts.add(new StyledText("("+ DungeonsGuide.getDungeonsGuide().getDungeonFacade().getContext().getPercentage() +"% + secrets)\n","field_etc"));

        texts.add(new StyledText("Time","field_name"));
        texts.add(new StyledText(": ","field_separator"));
        texts.add(new StyledText(scoreCalculation.getTime()+" ","field_value"));
        texts.add(new StyledText("("+ TextUtils.formatTime(FeatureRegistry.DUNGEON_SBTIME.getTimeElapsed())+")\n","field_etc"));

        texts.add(new StyledText("Bonus","field_name"));
        texts.add(new StyledText(": ","field_separator"));
        texts.add(new StyledText(scoreCalculation.getBonus()+"\n","field_value"));


        return texts;
    }

    public List<TextStyle> getStyles() {
        return this.<List<TextStyle>>getParameter("textStyles").getValue();
    }
    private Map<String, TextStyle> stylesMap;
    public Map<String, TextStyle> getStylesMap() {
        if (stylesMap == null) {
            List<TextStyle> styles = getStyles();
            Map<String, TextStyle> res = new HashMap<String, TextStyle>();
            for (TextStyle ts : styles) {
                res.put(ts.getGroupName(), ts);
            }
            stylesMap = res;
        }
        return stylesMap;
    }


    @Override
    public String getEditRoute(RootConfigPanel rootConfigPanel) {
        ConfigPanelCreator.map.put("base." + getKey() , new Supplier<MPanel>() {
            @Override
            public MPanel get() {
                MFeatureEdit featureEdit = new MFeatureEdit(FeatureWarningOnPortal.this, rootConfigPanel);
                featureEdit.addParameterEdit("textStyles", new PanelTextParameterConfig(FeatureWarningOnPortal.this));
                for (FeatureParameter parameter: getParameters()) {
                    if (parameter.getKey().equals("textStyles")) continue;
                    featureEdit.addParameterEdit(parameter.getKey(), new MParameterEdit(FeatureWarningOnPortal.this, parameter, rootConfigPanel));
                }
                return featureEdit;
            }
        });
        return "base." + getKey() ;
    }
}
