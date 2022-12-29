package kr.syeyoung.dungeonsguide.mod.guiv2;

import lombok.AllArgsConstructor;
import lombok.Getter;


// Idea heavily taken from flutter.
@AllArgsConstructor @Getter
public class ConstraintBox {
    private int minWidth;
    private int maxWidth;
    private int minHeight;
    private int maxHeight;
}
