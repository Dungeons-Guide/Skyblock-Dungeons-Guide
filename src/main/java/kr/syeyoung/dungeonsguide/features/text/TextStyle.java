package kr.syeyoung.dungeonsguide.features.text;

import kr.syeyoung.dungeonsguide.config.types.AColor;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.awt.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TextStyle {
    private String groupName;
    private AColor color;
    private AColor background;
}
