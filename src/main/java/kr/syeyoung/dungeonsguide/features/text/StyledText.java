package kr.syeyoung.dungeonsguide.features.text;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class StyledText {
    private String text;
    private String group;
}
