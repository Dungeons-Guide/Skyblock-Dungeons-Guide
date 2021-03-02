package kr.syeyoung.dungeonsguide.features.impl.boss;

import com.google.common.base.Supplier;
import kr.syeyoung.dungeonsguide.SkyblockStatus;
import kr.syeyoung.dungeonsguide.config.guiconfig.ConfigPanelCreator;
import kr.syeyoung.dungeonsguide.config.guiconfig.GuiConfig;
import kr.syeyoung.dungeonsguide.config.guiconfig.PanelDefaultParameterConfig;
import kr.syeyoung.dungeonsguide.config.types.AColor;
import kr.syeyoung.dungeonsguide.dungeon.DungeonContext;
import kr.syeyoung.dungeonsguide.e;
import kr.syeyoung.dungeonsguide.features.FeatureParameter;
import kr.syeyoung.dungeonsguide.features.FeatureRegistry;
import kr.syeyoung.dungeonsguide.features.SimpleFeature;
import kr.syeyoung.dungeonsguide.features.impl.dungeon.FeatureDungeonScore;
import kr.syeyoung.dungeonsguide.features.text.*;
import kr.syeyoung.dungeonsguide.gui.MPanel;
import kr.syeyoung.dungeonsguide.utils.TextUtils;

import java.util.*;

public class FeatureWarningOnPortal extends SimpleFeature implements StyledTextProvider {
    public FeatureWarningOnPortal() {
        super("Bossfight", "Show warnings on red portal", "Display warnings such as\n- 'NOT ALL ROOMS DISCOVERED'\n- 'NOT ALL ROOMS COMPLETED'\n- 'Expected Score: 304'\n- 'MISSING 3 CRYPTS'\non portal", "bossfight.warningonportal");
        this.parameters.put("textStyles", new FeatureParameter<List<TextStyle>>("textStyles", "", "", new ArrayList<TextStyle>(), "list_textStyle"));
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
        dummyText.add(new StyledText("103/100 ","field_value"));
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

    SkyblockStatus skyblockStatus = e.getDungeonsGuide().getSkyblockStatus();
    @Override
    public List<StyledText> getText() {
        ArrayList<StyledText> texts = new ArrayList<StyledText>();
        DungeonContext context = skyblockStatus.getContext();
        FeatureDungeonScore.ScoreCalculation scoreCalculation = FeatureRegistry.DUNGEON_SCORE.calculateScore();

        if (context.getMapProcessor().getUndiscoveredRoom() > 0) {
            texts.add(new StyledText("There are at least "+context.getMapProcessor().getUndiscoveredRoom()+" undiscovered rooms!\n", "warning"));
        } else if (!scoreCalculation.isFullyCleared()) {
            texts.add(new StyledText("Some rooms are not fully cleared!\n", "warning"));
        } else if (scoreCalculation.getTombs() < 5) {
            texts.add(new StyledText("Only less than 5 crypts are blown up!\n", "warning"));
        } else {
            texts.add(new StyledText("\n", "warning"));
        }

        texts.add(new StyledText("Total Secrets","field_name"));
        texts.add(new StyledText(": ","field_separator"));
        texts.add(new StyledText(scoreCalculation.getSecrets() +"/" + scoreCalculation.getTotalSecrets(),"field_value"));
        texts.add(new StyledText(" ("+(int)(scoreCalculation.getSecrets() / (float)scoreCalculation.getTotalSecrets() * 100.0f)+"% "+(int)(scoreCalculation.getSecrets() / (float)scoreCalculation.getTotalSecrets() * 40.0f)+" Explorer)\n","field_etc"));


        texts.add(new StyledText("Crypts","field_name"));
        texts.add(new StyledText(": ","field_separator"));
        texts.add(new StyledText(scoreCalculation.getTombs() +"/5\n","field_value"));


        texts.add(new StyledText("Deaths","field_name"));
        texts.add(new StyledText(": ","field_separator"));
        texts.add(new StyledText(scoreCalculation.getDeaths() + "\n","field_value"));

        int sum = scoreCalculation.getTime() + scoreCalculation.getExplorer() + scoreCalculation.getExplorer() + scoreCalculation.getTime() + scoreCalculation.getBonus();
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
        texts.add(new StyledText("("+FeatureRegistry.DUNGEON_SCORE.getPercentage()+"% + secrets)\n","field_etc"));

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
    public String getEditRoute(final GuiConfig config) {
        ConfigPanelCreator.map.put("base." + getKey() , new Supplier<MPanel>() {
            @Override
            public MPanel get() {
                return new PanelDefaultParameterConfig(config, FeatureWarningOnPortal.this,
                        Arrays.asList(new MPanel[] {
                                new PanelTextParameterConfig(config, FeatureWarningOnPortal.this)
                        }), Collections.singleton("textStyles"));
            }
        });
        return "base." + getKey() ;
    }
}
