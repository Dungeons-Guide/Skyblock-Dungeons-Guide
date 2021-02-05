package kr.syeyoung.dungeonsguide.config.types;

import lombok.Getter;
import lombok.Setter;

import java.awt.*;

@Getter
@Setter
public class AColor extends Color {
    private boolean chroma;
    private float chromaSpeed;

    public AColor(int r, int g, int b, int a) {
        super(r, g, b, a);
    }

    public AColor(int rgba, boolean hasalpha) {
        super(rgba, hasalpha);
    }
}
