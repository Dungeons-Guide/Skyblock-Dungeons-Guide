package kr.syeyoung.dungeonsguide.features.text;

import java.util.List;
import java.util.Map;

public interface StyledTextProvider {
    List<StyledText> getDummyText();
    List<StyledText> getText();

    List<TextStyle> getStyles();
    Map<String, TextStyle> getStylesMap();
}
